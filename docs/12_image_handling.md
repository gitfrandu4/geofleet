# Manejo de Imágenes

## Carga de Imágenes con Glide

### Configuración

En `app/build.gradle`:

```groovy
dependencies {
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    kapt 'com.github.bumptech.glide:compiler:4.16.0'
}
```

### Uso Básico

```kotlin
Glide.with(imageView)
    .load(imageUrl)
    .placeholder(R.drawable.vehicle_profile_placeholder)
    .error(R.drawable.vehicle_profile_placeholder)
    .transition(DrawableTransitionOptions.withCrossFade())
    .into(imageView)
```

## Galería de Imágenes

### Adapter para RecyclerView

```kotlin
class GalleryAdapter(
    private val onImageClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<String, GalleryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGalleryImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemGalleryImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(imageUrl: String) {
            Glide.with(binding.image)
                .load(imageUrl)
                .placeholder(R.drawable.vehicle_list_placeholder)
                .error(R.drawable.vehicle_list_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.image)
                
            binding.image.setOnClickListener {
                onImageClick(imageUrl)
            }
            
            binding.deleteButton.setOnClickListener {
                onDeleteClick(imageUrl)
            }
        }
    }
    
    private class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = 
            oldItem == newItem
            
        override fun areContentsTheSame(oldItem: String, newItem: String) = 
            oldItem == newItem
    }
}
```

### Layout del Item

```xml
<!-- item_gallery_image.xml -->
<com.google.android.material.card.MaterialCardView
    android:layout_width="120dp"
    android:layout_height="120dp"
    android:layout_margin="4dp"
    app:cardCornerRadius="8dp">

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ImageView
            android:id="@+id/image"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/deleteButton"
            style="@style/Widget.Material3.Button.IconButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="top|end"
            app:icon="@drawable/ic_delete" />
    </FrameLayout>
</com.google.android.material.card.MaterialCardView>
```

## Captura de Imágenes

### Permisos

En `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />

<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

En `res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path
        name="images"
        path="/" />
</paths>
```

### Solicitud de Permisos

```kotlin
private fun checkCameraPermission() {
    when {
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED -> {
            showImagePickerDialog()
        }
        shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
            showPermissionRationaleDialog()
        }
        else -> {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}

private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        showImagePickerDialog()
    } else {
        showPermissionDeniedMessage()
    }
}
```

### Diálogo de Selección

```kotlin
private fun showImagePickerDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.select_image)
        .setItems(
            arrayOf(
                getString(R.string.take_photo),
                getString(R.string.choose_gallery)
            )
        ) { _, which ->
            when (which) {
                0 -> takePhoto()
                1 -> pickFromGallery()
            }
        }
        .show()
}
```

### Captura con Cámara

```kotlin
private fun takePhoto() {
    val photoFile = createImageFile()
    photoUri = FileProvider.getUriForFile(
        requireContext(),
        "${requireContext().packageName}.fileprovider",
        photoFile
    )
    
    takePictureLauncher.launch(photoUri)
}

private val takePictureLauncher = registerForActivityResult(
    ActivityResultContracts.TakePicture()
) { success ->
    if (success) {
        photoUri?.let { uri ->
            viewModel.uploadImage(uri)
        }
    }
}

private fun createImageFile(): File {
    val timestamp = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.getDefault()
    ).format(Date())
    
    val storageDir = requireContext()
        .getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        
    return File.createTempFile(
        "JPEG_${timestamp}_",
        ".jpg",
        storageDir
    )
}
```

### Selección de Galería

```kotlin
private fun pickFromGallery() {
    pickImageLauncher.launch("image/*")
}

private val pickImageLauncher = registerForActivityResult(
    ActivityResultContracts.GetContent()
) { uri ->
    uri?.let { viewModel.uploadImage(it) }
}
```

## Subida a Firebase Storage

```kotlin
class VehicleProfileViewModel : ViewModel() {
    private val storage = Firebase.storage
    
    fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val url = uploadImageToStorage(uri)
                addImageToVehicle(url)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message)
            }
        }
    }
    
    private suspend fun uploadImageToStorage(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val filename = UUID.randomUUID().toString()
            val ref = storage.reference
                .child("vehicles")
                .child(filename)
                
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        }
    }
    
    private suspend fun addImageToVehicle(url: String) {
        val vehicle = (_uiState.value as? UiState.Success)?.vehicle
            ?: return
            
        val updatedImages = vehicle.images + url
        saveVehicle(vehicle.copy(images = updatedImages))
    }
}
```

## Placeholders

### Perfil de Vehículo

```xml
<!-- vehicle_profile_placeholder.xml -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item>
        <shape android:shape="rectangle">
            <gradient
                android:angle="135"
                android:endColor="#F2632A"
                android:startColor="#FFF5F2"
                android:type="linear" />
            <corners android:radius="12dp" />
        </shape>
    </item>
</layer-list>
```

### Lista de Vehículos

```xml
<!-- vehicle_list_placeholder.xml -->
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item>
        <shape android:shape="rectangle">
            <solid android:color="#FFF5F2" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item
        android:width="48dp"
        android:height="48dp"
        android:gravity="center">
        <vector
            android:width="48dp"
            android:height="48dp"
            android:viewportWidth="24"
            android:viewportHeight="24">
            <path
                android:fillColor="#F2632A"
                android:pathData="M18.92,6.01C18.72,5.42 18.16,5 17.5,5h-11c-0.66,0 -1.21,0.42 -1.42,1.01L3,12v8c0,0.55 0.45,1 1,1h1c0.55,0 1,-0.45 1,-1v-1h12v1c0,0.55 0.45,1 1,1h1c0.55,0 1,-0.45 1,-1v-8l-2.08,-5.99zM6.5,16c-0.83,0 -1.5,-0.67 -1.5,-1.5S5.67,13 6.5,13s1.5,0.67 1.5,1.5S7.33,16 6.5,16zM17.5,16c-0.83,0 -1.5,-0.67 -1.5,-1.5s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5zM5,11l1.5,-4.5h11L19,11L5,11z"/>
        </vector>
    </item>
</layer-list>
```

## Recursos Útiles

- [Glide Documentation](https://github.com/bumptech/glide)
- [Firebase Storage](https://firebase.google.com/docs/storage)
- [Android Camera2 API](https://developer.android.com/training/camera2)
- [FileProvider](https://developer.android.com/reference/androidx/core/content/FileProvider) 
