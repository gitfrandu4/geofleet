# Perfil de Vehículo

## Estructura del Modelo

```kotlin
data class Vehicle(
    @get:Exclude val id: String = "",
    val plate: String = "",
    val alias: String = "",
    val brand: String = "",
    val model: String = "",
    val vehicleType: String = "",
    val chassisNumber: String = "",
    val kilometers: Int = 0,
    val maxPassengers: Int = 0,
    val wheelchair: Boolean = false,
    val inServiceFrom: Date? = null,
    val state: VehicleState = VehicleState.ACTIVE,
    val images: List<String> = emptyList()
)

enum class VehicleState {
    ACTIVE,      // Activo
    DOWN,        // Averiado
    MAINTENANCE, // En Mantenimiento
    REPAIR_SHOP, // En Taller
    RESERVED,    // Reservado
    TRANSFER,    // En Traslado
    INSPECTION,  // En Revisión
    PENDING_PAPERS, // Pendiente de Papeles
    RENTED,      // Alquilado
    SOLD;        // Vendido

    companion object {
        fun fromString(value: String): VehicleState {
            return when (value.uppercase()) {
                "ACTIVO" -> ACTIVE
                "AVERIADO" -> DOWN
                "EN MANTENIMIENTO" -> MAINTENANCE
                "EN TALLER" -> REPAIR_SHOP
                "RESERVADO" -> RESERVED
                "EN TRASLADO" -> TRANSFER
                "EN REVISIÓN" -> INSPECTION
                "PENDIENTE DE PAPELES" -> PENDING_PAPERS
                "ALQUILADO" -> RENTED
                "VENDIDO" -> SOLD
                else -> valueOf(value)
            }
        }

        fun toSpanishString(state: VehicleState): String {
            return when (state) {
                ACTIVE -> "Activo"
                DOWN -> "Averiado"
                MAINTENANCE -> "En Mantenimiento"
                REPAIR_SHOP -> "En Taller"
                RESERVED -> "Reservado"
                TRANSFER -> "En Traslado"
                INSPECTION -> "En Revisión"
                PENDING_PAPERS -> "Pendiente de Papeles"
                RENTED -> "Alquilado"
                SOLD -> "Vendido"
            }
        }
    }
}
```

## Interfaz de Usuario

### Layout Principal

```xml
<!-- fragment_vehicle_profile.xml -->
<androidx.coordinatorlayout.widget.CoordinatorLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:fitsSystemWindows="true">

        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:layout_width="match_parent"
            android:layout_height="200dp"
            app:layout_scrollFlags="scroll|exitUntilCollapsed">

            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <ImageView
                    android:id="@+id/profileImage"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:scaleType="centerCrop" />

                <com.google.android.material.floatingactionbutton.FloatingActionButton
                    android:id="@+id/changeProfileImageButton"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_gravity="bottom|end"
                    android:layout_margin="16dp"
                    app:srcCompat="@drawable/ic_camera" />
            </FrameLayout>

            <TextView
                android:id="@+id/vehicleIdText"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="bottom|start"
                android:layout_margin="16dp"
                android:textColor="@android:color/white"
                android:textSize="14sp" />
        </com.google.android.material.appbar.CollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>

    <androidx.core.widget.NestedScrollView
        android:id="@+id/nestedScrollView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layout_behavior="@string/appbar_scrolling_view_behavior">

        <!-- Contenido del perfil -->
        
    </androidx.core.widget.NestedScrollView>

    <com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
        android:id="@+id/saveFab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|center"
        android:layout_marginBottom="16dp"
        android:text="@string/save_changes"
        app:icon="@drawable/ic_save" />
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### Campos de Información

```xml
<!-- Dentro del NestedScrollView -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <com.google.android.material.card.MaterialCardView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/basic_info"
                android:textAppearance="?attr/textAppearanceTitleMedium" />

            <com.google.android.material.textfield.TextInputLayout
                style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:hint="@string/plate_number">

                <com.google.android.material.textfield.TextInputEditText
                    android:id="@+id/plateEditText"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content" />
            </com.google.android.material.textfield.TextInputLayout>

            <!-- Más campos... -->
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <!-- Galería de imágenes -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/image_gallery"
            android:textAppearance="?attr/textAppearanceTitleMedium" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/addImageButton"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/add_image"
            app:icon="@drawable/ic_add_photo" />

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/galleryRecyclerView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
    </LinearLayout>
</LinearLayout>
```

## Lógica de Presentación

### ViewModel

```kotlin
@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val repository: VehicleRepository,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                val vehicle = repository.getVehicle(vehicleId)
                _uiState.value = UiState.Success(vehicle)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message)
            }
        }
    }

    fun saveVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            try {
                repository.saveVehicle(vehicle)
                _uiState.value = UiState.Saved
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message)
            }
        }
    }

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

sealed class UiState {
    object Loading : UiState()
    data class Success(val vehicle: Vehicle) : UiState()
    data class Error(val message: String?) : UiState()
    object Saved : UiState()
}
```

### Fragment

```kotlin
@AndroidEntryPoint
class VehicleProfileFragment : Fragment() {

    private val viewModel: VehicleProfileViewModel by viewModels()
    private var photoUri: Uri? = null
    private val galleryAdapter = GalleryAdapter(
        onImageClick = { /* Mostrar imagen en grande */ },
        onDeleteClick = { /* Eliminar imagen */ }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        observeState()
        
        arguments?.getString("vehicleId")?.let { id ->
            viewModel.loadVehicle(id)
        }
    }

    private fun setupViews() {
        binding.apply {
            galleryRecyclerView.adapter = galleryAdapter
            
            changeProfileImageButton.setOnClickListener {
                checkCameraPermission()
            }
            
            addImageButton.setOnClickListener {
                checkCameraPermission()
            }
            
            saveFab.setOnClickListener {
                saveVehicle()
            }
        }
        
        setupDropdowns()
    }

    private fun setupDropdowns() {
        val vehicleTypes = resources.getStringArray(R.array.vehicle_types)
        val vehicleStates = resources.getStringArray(R.array.vehicle_states)
        
        binding.apply {
            (vehicleTypeInput.editText as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.item_dropdown,
                    vehicleTypes
                )
            )
            
            (stateInput.editText as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.item_dropdown,
                    vehicleStates
                )
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading()
                    is UiState.Success -> updateUI(state.vehicle)
                    is UiState.Error -> showError(state.message)
                    is UiState.Saved -> showSavedMessage()
                }
            }
        }
    }

    private fun updateUI(vehicle: Vehicle) {
        binding.apply {
            vehicleIdText.text = getString(R.string.vehicle_id_format, vehicle.id)
            
            Glide.with(profileImage)
                .load(vehicle.images.firstOrNull())
                .placeholder(R.drawable.vehicle_profile_placeholder)
                .error(R.drawable.vehicle_profile_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(profileImage)
                
            plateEditText.setText(vehicle.plate)
            brandEditText.setText(vehicle.brand)
            modelEditText.setText(vehicle.model)
            
            (vehicleTypeInput.editText as? AutoCompleteTextView)?.setText(
                vehicle.vehicleType,
                false
            )
            
            (stateInput.editText as? AutoCompleteTextView)?.setText(
                VehicleState.toSpanishString(vehicle.state),
                false
            )
            
            chassisNumberEditText.setText(vehicle.chassisNumber)
            kilometersEditText.setText(vehicle.kilometers.toString())
            maxPassengersEditText.setText(vehicle.maxPassengers.toString())
            wheelchairSwitch.isChecked = vehicle.wheelchair
            
            serviceDateEditText.setText(
                vehicle.inServiceFrom?.let { date ->
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(date)
                } ?: ""
            )
            
            galleryAdapter.submitList(vehicle.images)
        }
    }

    private fun saveVehicle() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        val vehicle = Vehicle(
            id = arguments?.getString("vehicleId") ?: "",
            plate = binding.plateEditText.text.toString(),
            brand = binding.brandEditText.text.toString(),
            model = binding.modelEditText.text.toString(),
            vehicleType = binding.vehicleTypeInput.editText?.text.toString(),
            state = VehicleState.fromString(
                binding.stateInput.editText?.text.toString()
            ),
            chassisNumber = binding.chassisNumberEditText.text.toString(),
            kilometers = binding.kilometersEditText.text.toString()
                .toIntOrNull() ?: 0,
            maxPassengers = binding.maxPassengersEditText.text.toString()
                .toIntOrNull() ?: 0,
            wheelchair = binding.wheelchairSwitch.isChecked,
            inServiceFrom = binding.serviceDateEditText.text.toString()
                .let { dateStr ->
                    if (dateStr.isNotEmpty()) dateFormat.parse(dateStr)
                    else null
                }
        )
        
        viewModel.saveVehicle(vehicle)
    }
}
```

## Recursos de Strings

```xml
<!-- strings.xml -->
<resources>
    <string name="basic_info">Información Básica</string>
    <string name="plate_number">Matrícula</string>
    <string name="brand_model">Marca y Modelo</string>
    <string name="technical_details">Detalles Técnicos</string>
    <string name="vehicle_type">Tipo de Vehículo</string>
    <string name="chassis_number">Número de Bastidor</string>
    <string name="kilometers">Kilómetros</string>
    <string name="max_passengers">Capacidad de Pasajeros</string>
    <string name="wheelchair">Adaptado para Silla de Ruedas</string>
    <string name="service_date">Fecha de Alta</string>
    <string name="vehicle_state">Estado</string>
    <string name="image_gallery">Galería de Imágenes</string>
    <string name="add_image">Añadir Imagen</string>
    <string name="save_changes">Guardar Cambios</string>
    <string name="changes_saved">Cambios guardados correctamente</string>
    <string name="vehicle_id_format">ID: %s</string>
    
    <string-array name="vehicle_types">
        <item>Camión</item>
        <item>Furgoneta</item>
        <item>Excavadora</item>
        <item>Grúa</item>
        <item>Hormigonera</item>
        <item>Carretilla Elevadora</item>
        <item>Retroexcavadora</item>
        <item>Volquete</item>
        <item>Otro</item>
    </string-array>
    
    <string-array name="vehicle_states">
        <item>Activo</item>
        <item>Averiado</item>
        <item>En Mantenimiento</item>
        <item>En Taller</item>
        <item>Reservado</item>
        <item>En Traslado</item>
        <item>En Revisión</item>
        <item>Pendiente de Papeles</item>
        <item>Alquilado</item>
        <item>Vendido</item>
    </string-array>
</resources>
```

## Recursos Útiles

- [CollapsingToolbarLayout](https://material.io/components/app-bars-top#collapsing-top-app-bar)
- [TextInputLayout](https://material.io/components/text-fields)
- [ExtendedFloatingActionButton](https://material.io/components/buttons-floating-action-button#extended-fab)
- [RecyclerView](https://developer.android.com/guide/topics/ui/layout/recyclerview) 
