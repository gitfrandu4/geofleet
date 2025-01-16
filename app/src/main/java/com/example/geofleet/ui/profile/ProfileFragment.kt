package com.example.geofleet.ui.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.geofleet.LoginActivity
import com.example.geofleet.R
import com.example.geofleet.data.model.UserProfile
import com.example.geofleet.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                selectedImageUri = Uri.fromFile(File(path))
                binding.profileImage.setImageURI(selectedImageUri)
            }
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.profileImage.setImageURI(uri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            when (lastPermissionRequested) {
                Manifest.permission.CAMERA -> takePictureAfterPermission()
                else -> openGallery()
            }
        } else {
            showError("Se necesitan permisos para ${if (lastPermissionRequested == Manifest.permission.CAMERA) "usar la cámara" else "acceder a las imágenes"}")
        }
    }

    private var lastPermissionRequested = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = Firebase.storage("gs://geofleet-8f1ca.firebasestorage.app")

        setupViews()
        loadUserProfile()
    }

    private fun setupViews() {
        // Setup gender dropdown
        val genders = arrayOf("Masculino", "Femenino", "Otro", "Prefiero no decirlo")
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.list_item, genders)
        binding.genderAutoComplete.setAdapter(genderAdapter)

        // Setup date picker
        binding.birthdateEditText.setOnClickListener {
            showDatePicker()
        }

        // Setup profile image
        binding.profileImage.setOnClickListener {
            openImagePicker()
        }

        // Setup save button
        binding.saveButton.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageAndSaveProfile()
            } else {
                saveUserProfile()
            }
        }

        // Setup logout button
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }

        // Set email from Firebase Auth
        auth.currentUser?.email?.let { email ->
            binding.emailEditText.setText(email)
        }
    }

    private fun openImagePicker() {
        val options = arrayOf("Tomar foto", "Elegir de la galería")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar imagen")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> checkGalleryPermission()
                }
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePictureAfterPermission()
            }
            else -> {
                lastPermissionRequested = Manifest.permission.CAMERA
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkGalleryPermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openGallery()
                    }
                    else -> {
                        lastPermissionRequested = Manifest.permission.READ_MEDIA_IMAGES
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openGallery()
                    }
                    else -> {
                        lastPermissionRequested = Manifest.permission.READ_EXTERNAL_STORAGE
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    private fun takePictureAfterPermission() {
        try {
            val photoFile = createImageFile()
            photoFile.also { file ->
                val photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )
                takePicture.launch(photoURI)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error creando el archivo de imagen", e)
            showError("Error al preparar la cámara")
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                binding.birthdateEditText.setText(dateFormatter.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Traducir los botones del DatePickerDialog
        datePickerDialog.setButton(DatePickerDialog.BUTTON_POSITIVE, "Aceptar", datePickerDialog)
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Cancelar", datePickerDialog)

        datePickerDialog.show()
    }

    private fun uploadImageAndSaveProfile() {
        selectedImageUri?.let { uri ->
            try {
                val userId = auth.currentUser?.uid ?: run {
                    showError("Error: Usuario no autenticado")
                    return
                }

                val storageRef = storage.reference
                // Simplify the storage path and ensure it starts at root
                val imageRef = storageRef.child("users").child(userId).child("profile.jpg")

                binding.saveButton.isEnabled = false
                showProgress("Subiendo imagen...")

                uploadImage(imageRef, uri)
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al subir imagen: ${e.message}", e)
                showError("Error inesperado al subir la imagen")
                binding.saveButton.isEnabled = true
                hideProgress()
            }
        } ?: run {
            Log.e(TAG, "No hay imagen seleccionada")
            showError("No se ha seleccionado ninguna imagen")
        }
    }

    private fun uploadImage(imageRef: StorageReference, uri: Uri) {
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            Log.d(TAG, "Progreso de subida: $progress%")
            showProgress("Subiendo imagen: ${progress.toInt()}%")
        }.addOnSuccessListener {
            Log.d(TAG, "Imagen subida correctamente")
            imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                Log.d(TAG, "URL de descarga obtenida: $downloadUri")
                saveUserProfile(downloadUri.toString())
                showSuccess("Imagen actualizada correctamente")
                binding.saveButton.isEnabled = true
                hideProgress()
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error al subir imagen: ${e.message}", e)
            showError("Error al subir la imagen: ${e.localizedMessage ?: "Error desconocido"}")
            binding.saveButton.isEnabled = true
            hideProgress()
        }
    }

    private fun loadUserProfile() {
        showProgress("Cargando perfil...")
        auth.currentUser?.uid?.let { userId ->
            db.collection(UserProfile.COLLECTION_NAME)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profile = document.toObject(UserProfile::class.java)
                        profile?.let { updateUI(it) }
                    }
                    hideProgress()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al cargar perfil", e)
                    showError("Error al cargar el perfil")
                    hideProgress()
                }
        }
    }

    private fun updateUI(profile: UserProfile) {
        binding.apply {
            firstNameEditText.setText(profile.firstName)
            lastNameEditText.setText(profile.lastName)
            positionEditText.setText(profile.position)
            genderAutoComplete.setText(mapGenderToSpanish(profile.gender), false)
            birthdateEditText.setText(profile.birthdate)

            if (profile.photoUrl.isNotEmpty()) {
                Glide.with(this@ProfileFragment)
                    .load(profile.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(profileImage)
            }
        }
    }

    private fun mapGenderToSpanish(gender: String): String {
        return when (gender) {
            "Male" -> "Masculino"
            "Female" -> "Femenino"
            "Other" -> "Otro"
            "Prefer not to say" -> "Prefiero no decirlo"
            else -> gender
        }
    }

    private fun mapGenderToEnglish(gender: String): String {
        return when (gender) {
            "Masculino" -> "Male"
            "Femenino" -> "Female"
            "Otro" -> "Other"
            "Prefiero no decirlo" -> "Prefer not to say"
            else -> gender
        }
    }

    private fun saveUserProfile(photoUrl: String = "") {
        val profile = UserProfile(
            firstName = binding.firstNameEditText.text.toString(),
            lastName = binding.lastNameEditText.text.toString(),
            position = binding.positionEditText.text.toString(),
            email = binding.emailEditText.text.toString(),
            gender = mapGenderToEnglish(binding.genderAutoComplete.text.toString()),
            birthdate = binding.birthdateEditText.text.toString(),
            photoUrl = if (photoUrl.isEmpty()) binding.profileImage.tag as? String ?: "" else photoUrl
        )

        showProgress("Guardando perfil...")
        auth.currentUser?.uid?.let { userId ->
            db.collection(UserProfile.COLLECTION_NAME)
                .document(userId)
                .set(profile)
                .addOnSuccessListener {
                    showSuccess("Perfil guardado correctamente")
                    binding.saveButton.isEnabled = true
                    hideProgress()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al guardar perfil", e)
                    showError("Error al guardar el perfil")
                    binding.saveButton.isEnabled = true
                    hideProgress()
                }
        }
    }

    private fun showProgress(message: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.progressText.text = message
        binding.progressText.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.progressText.visibility = View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.error_color, null))
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.success_color, null))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}