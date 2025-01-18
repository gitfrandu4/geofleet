package com.example.geofleet.ui.vehicles

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.geofleet.R
import com.example.geofleet.data.model.Vehicle
import com.example.geofleet.databinding.FragmentVehicleProfileBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class VehicleProfileFragment : Fragment() {
    private var _binding: FragmentVehicleProfileBinding? = null
    private val binding
        get() = _binding!!
    private val viewModel: VehicleProfileViewModel by viewModels()
    private val args: VehicleProfileFragmentArgs by navArgs()
    private lateinit var galleryAdapter: GalleryAdapter
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var currentPhotoPath: String? = null

    private val takePicture =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    currentPhotoPath?.let { path ->
                        viewModel.uploadImage(Uri.fromFile(File(path)))
                    }
                }
            }

    private val pickImage =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri -> viewModel.uploadImage(uri) }
                }
            }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGalleryRecyclerView()
        setupImageButtons()
        setupDatePicker()
        setupDropdowns()
        setupSaveButton()

        // Set initial placeholder
        binding.vehicleImage.setImageResource(R.drawable.vehicle_profile_placeholder)

        observeViewModel()
        viewModel.loadVehicle(args.vehicleId)
    }

    private fun setupGalleryRecyclerView() {
        galleryAdapter =
                GalleryAdapter(
                        onDeleteClick = { imageUrl ->
                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.delete_image)
                                    .setMessage(R.string.delete_image_confirmation)
                                    .setPositiveButton(R.string.delete) { _, _ ->
                                        viewModel.deleteImage(imageUrl)
                                    }
                                    .setNegativeButton(R.string.cancel, null)
                                    .show()
                        }
                )
        binding.galleryRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = galleryAdapter
        }
    }

    private fun setupImageButtons() {
        binding.apply {
            addImageButton.setOnClickListener { showImagePickerDialog(false) }
        }
    }

    private fun setupDatePicker() {
        binding.serviceDateEditText.setOnClickListener {
            val picker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText(getString(R.string.service_date))
                            .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                binding.serviceDateEditText.setText(dateFormat.format(date))
            }

            picker.show(childFragmentManager, "date_picker")
        }
    }

    private fun setupDropdowns() {
        binding.apply {
            // Vehicle Type Dropdown - Using Spanish types from resources
            val vehicleTypes = requireContext().resources.getStringArray(R.array.vehicle_types)
            val vehicleTypeAdapter =
                    ArrayAdapter(requireContext(), R.layout.list_item, vehicleTypes)
            (vehicleTypeEditText as? AutoCompleteTextView)?.setAdapter(vehicleTypeAdapter)

            // Vehicle State Dropdown - Using Spanish states from resources
            val states = requireContext().resources.getStringArray(R.array.vehicle_states)
            val stateAdapter = ArrayAdapter(requireContext(), R.layout.list_item, states)
            (stateEditText as? AutoCompleteTextView)?.setAdapter(stateAdapter)
        }
    }

    private fun setupSaveButton() {
        binding.apply {
            saveFab.setOnClickListener {
                val vehicle =
                        Vehicle(
                                id = args.vehicleId,
                                plate = plateEditText.text.toString(),
                                alias = aliasEditText.text.toString(),
                                brand = brandModelEditText.text.toString().split(" ").firstOrNull(),
                                model =
                                        brandModelEditText
                                                .text
                                                .toString()
                                                .split(" ")
                                                .drop(1)
                                                .joinToString(" "),
                                vehicleType = vehicleTypeEditText.text.toString(),
                                chassisNumber = chassisNumberEditText.text.toString(),
                                kilometers = kilometersEditText.text.toString().toIntOrNull(),
                                maxPassengers = 0, // Not used for work vehicles
                                wheelchair = false, // Not used for work vehicles
                                inServiceFrom =
                                        serviceDateEditText.text.toString().let { dateStr ->
                                            if (dateStr.isNotEmpty()) {
                                                dateFormat.parse(dateStr)
                                            } else {
                                                null
                                            }
                                        },
                                state =
                                        Vehicle.VehicleState.fromString(
                                                stateEditText.text.toString()
                                        ),
                                images = viewModel.vehicle.value?.images ?: emptyList()
                        )
                viewModel.saveVehicle(vehicle)
            }
        }
    }

    private fun showImagePickerDialog(isProfileImage: Boolean) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.select_image)
                .setItems(
                        arrayOf(
                                getString(R.string.take_photo),
                                getString(R.string.choose_from_gallery)
                        )
                ) { _, which ->
                    when (which) {
                        0 -> checkCameraPermissionAndTakePhoto(isProfileImage)
                        1 -> chooseFromGallery(isProfileImage)
                    }
                }
                .show()
    }

    private fun checkCameraPermissionAndTakePhoto(isProfileImage: Boolean) {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                takePhoto(isProfileImage)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showCameraPermissionRationale(isProfileImage)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showCameraPermissionRationale(isProfileImage: Boolean) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.camera_permission_needed)
                .setMessage(R.string.camera_permission_explanation)
                .setPositiveButton(R.string.continue_text) { dialog: DialogInterface, _: Int ->
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .show()
    }

    private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isGranted: Boolean ->
                if (isGranted) {
                    takePhoto(true)
                } else {
                    Snackbar.make(
                                    binding.root,
                                    R.string.camera_permission_denied,
                                    Snackbar.LENGTH_LONG
                            )
                            .show()
                }
            }

    private fun takePhoto(isProfileImage: Boolean) {
        val photoFile = createImageFile()
        photoFile.also { file ->
            val photoURI =
                    FileProvider.getUriForFile(
                            requireContext(),
                            "com.example.geofleet.fileprovider",
                            file
                    )
            currentPhotoPath = file.absolutePath
            takePicture.launch(photoURI)
        }
    }

    private fun chooseFromGallery(isProfileImage: Boolean) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.vehicle.collect { vehicle -> vehicle?.let { updateUI(it) } } }

                launch {
                    viewModel.error.collect { error ->
                        error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
                    }
                }

                launch {
                    viewModel.isSaving.collect { isSaving ->
                        binding.apply {
                            saveFab.isEnabled = !isSaving
                            if (isSaving) {
                                Snackbar.make(
                                                root,
                                                R.string.uploading_image,
                                                Snackbar.LENGTH_INDEFINITE
                                        )
                                        .show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.saveComplete.collect { success ->
                        success?.let {
                            val messageResId =
                                    if (it) R.string.changes_saved else R.string.error_saving
                            Snackbar.make(binding.root, messageResId, Snackbar.LENGTH_LONG).show()
                            viewModel.resetSaveComplete()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(vehicle: Vehicle) {
        binding.apply {
            vehicleIdText.text = getString(R.string.vehicle_id_format, vehicle.id)

            // Load profile image if available
            if (vehicle.images.isNotEmpty()) {
                Glide.with(requireContext())
                        .load(vehicle.images.first())
                        .placeholder(R.drawable.vehicle_profile_placeholder)
                        .error(R.drawable.vehicle_profile_placeholder)
                        .centerCrop()
                        .into(vehicleImage)
            } else {
                vehicleImage.setImageResource(R.drawable.vehicle_profile_placeholder)
            }

            plateEditText.setText(vehicle.plate)
            aliasEditText.setText(vehicle.alias)
            brandModelEditText.setText(
                    listOfNotNull(vehicle.brand, vehicle.model).joinToString(" ")
            )
            vehicleTypeEditText.setText(vehicle.vehicleType)
            chassisNumberEditText.setText(vehicle.chassisNumber)
            kilometersEditText.setText(vehicle.kilometers?.toString())
            serviceDateEditText.setText(
                    vehicle.inServiceFrom?.let { date -> dateFormat.format(date) }
            )
            stateEditText.setText(Vehicle.VehicleState.toSpanishString(vehicle.state))
            galleryAdapter.submitList(vehicle.images)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "VehicleProfileFragment"
    }
}
