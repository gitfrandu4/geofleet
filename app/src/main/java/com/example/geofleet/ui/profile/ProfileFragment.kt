package com.example.geofleet.ui.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.Calendar
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

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.profileImage.setImageURI(uri)
            }
        }
    }

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
        storage = Firebase.storage
        
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
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
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
            val storageRef = storage.reference
            val imageRef = storageRef.child("profile_images/${auth.currentUser?.uid}")
            
            binding.saveButton.isEnabled = false
            showProgress("Subiendo imagen...")

            imageRef.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnSuccessListener { downloadUri ->
                    saveUserProfile(downloadUri.toString())
                    showSuccess("Imagen actualizada correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al subir imagen", e)
                    showError("Error al subir la imagen")
                    binding.saveButton.isEnabled = true
                }
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
