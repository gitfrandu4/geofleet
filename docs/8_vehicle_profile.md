# Perfil de Vehículo

## CollapsingToolbarLayout

El `CollapsingToolbarLayout` es un componente de Material Design que permite crear una barra de herramientas que se colapsa y expande al hacer scroll. En nuestro caso, lo usamos para mostrar la imagen del vehículo con un efecto de parallax.

```xml
<com.google.android.material.appbar.CollapsingToolbarLayout
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:fitsSystemWindows="true"
    app:contentScrim="?attr/colorPrimary"
    app:layout_scrollFlags="scroll|exitUntilCollapsed"
    app:titleEnabled="false">
```

Atributos importantes:
- `app:contentScrim`: Color que se mostrará cuando la toolbar esté colapsada
- `app:layout_scrollFlags`: Define el comportamiento del scroll
- `app:titleEnabled`: Controla si se muestra el título

## Campos Editables con Material Design

### TextInputLayout

El `TextInputLayout` es un contenedor que añade funcionalidades a los campos de texto como:
- Etiquetas flotantes
- Mensajes de error
- Contadores de caracteres

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    android:hint="@string/plate_number">

    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/plateEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</com.google.android.material.textfield.TextInputLayout>
```

### Dropdowns (Menús Desplegables)

Para campos con opciones predefinidas, usamos `TextInputLayout` con el estilo `ExposedDropdownMenu`:

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox.ExposedDropdownMenu"
    android:hint="@string/vehicle_type">

    <AutoCompleteTextView
        android:id="@+id/vehicleTypeEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="none" />
</com.google.android.material.textfield.TextInputLayout>
```

En Kotlin, configuramos el adapter:
```kotlin
val vehicleTypes = resources.getStringArray(R.array.vehicle_types)
val adapter = ArrayAdapter(context, R.layout.list_item, vehicleTypes)
(vehicleTypeEditText as? AutoCompleteTextView)?.setAdapter(adapter)
```

## Manejo de Imágenes

### Carga de Imágenes con Glide

Usamos Glide para cargar imágenes de forma eficiente:

```kotlin
Glide.with(requireContext())
    .load(imageUrl)
    .placeholder(R.drawable.vehicle_profile_placeholder)
    .error(R.drawable.vehicle_profile_placeholder)
    .centerCrop()
    .into(imageView)
```

### Galería de Imágenes con RecyclerView

Para mostrar múltiples imágenes, usamos un `RecyclerView` con scroll horizontal:

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/galleryRecyclerView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
```

## Manejo de Estados

### Enums en Kotlin

Definimos los estados del vehículo usando un enum con métodos útiles:

```kotlin
enum class VehicleState {
    ACTIVE, DOWN, MAINTENANCE;

    companion object {
        fun fromString(value: String?): VehicleState {
            return when (value?.lowercase()) {
                "active", "activo" -> ACTIVE
                "down", "averiado" -> DOWN
                "maintenance", "mantenimiento" -> MAINTENANCE
                else -> ACTIVE
            }
        }

        fun toSpanishString(state: VehicleState): String {
            return when (state) {
                ACTIVE -> "Activo"
                DOWN -> "Averiado"
                MAINTENANCE -> "En Mantenimiento"
            }
        }
    }
}
```

## Arquitectura MVVM

### ViewModel

El ViewModel maneja la lógica de negocio y mantiene el estado:

```kotlin
class VehicleProfileViewModel : ViewModel() {
    private val _vehicle = MutableStateFlow<Vehicle?>(null)
    val vehicle: StateFlow<Vehicle?> = _vehicle.asStateFlow()

    fun loadVehicle(id: String) {
        viewModelScope.launch {
            // Cargar datos de Firestore
        }
    }
}
```

### Observación de Cambios

En el Fragment, observamos los cambios usando Kotlin Flows:

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.vehicle.collect { vehicle ->
            vehicle?.let { updateUI(it) }
        }
    }
}
```

## Permisos de Cámara

Para tomar fotos, necesitamos solicitar permisos:

```kotlin
private fun checkCameraPermission() {
    when {
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> {
            takePhoto()
        }
        shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
            showPermissionRationale()
        }
        else -> {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
```

## Integración con Firestore

### Guardado de Datos

```kotlin
fun saveVehicle(vehicle: Vehicle) {
    viewModelScope.launch {
        try {
            val vehicleData = mapOf(
                "plate" to vehicle.plate,
                "state" to Vehicle.VehicleState.toSpanishString(vehicle.state)
                // ... otros campos
            )
            db.collection("vehicles")
                .document(vehicle.id)
                .set(vehicleData)
        } catch (e: Exception) {
            _error.value = "Error al guardar: ${e.message}"
        }
    }
}
``` 
