# Arquitectura MVVM en Android

## Introducción

La arquitectura MVVM (Model-View-ViewModel) es un patrón de diseño que separa la lógica de negocio de la interfaz de usuario. En nuestra aplicación, utilizamos MVVM junto con Kotlin Flows para crear una arquitectura robusta y mantenible.

## Componentes Principales

### Model (Modelo)

Representa los datos y la lógica de negocio. En nuestra app:

```kotlin
data class Vehicle(
    val id: String = "",
    val plate: String = "",
    val brand: String = "",
    val model: String = "",
    val vehicleType: String = "",
    val state: VehicleState = VehicleState.ACTIVE,
    // ...otros campos
)

enum class VehicleState {
    ACTIVE, DOWN, MAINTENANCE, // ...otros estados
}
```

### View (Vista)

Representa la interfaz de usuario. En Android, son los Fragments y Activities:

```kotlin
class VehicleProfileFragment : Fragment() {
    private val viewModel: VehicleProfileViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observar cambios en el ViewModel
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
        
        // Manejar eventos de UI
        binding.saveFab.setOnClickListener {
            viewModel.saveVehicle()
        }
    }
}
```

### ViewModel

Actúa como intermediario entre el Model y la View:

```kotlin
class VehicleProfileViewModel : ViewModel() {
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
}
```

## Estados de UI

Utilizamos sealed classes para representar los diferentes estados de la UI:

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val vehicle: Vehicle) : UiState()
    data class Error(val message: String?) : UiState()
    object Saved : UiState()
}
```

## Repository Pattern

Abstrae el origen de los datos:

```kotlin
class VehicleRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getVehicle(id: String): Vehicle {
        return withContext(Dispatchers.IO) {
            val doc = firestore.collection("vehicles")
                .document(id)
                .get()
                .await()
                
            doc.toObject(Vehicle::class.java)
                ?.copy(id = doc.id)
                ?: throw Exception("Vehicle not found")
        }
    }
    
    suspend fun saveVehicle(vehicle: Vehicle) {
        withContext(Dispatchers.IO) {
            firestore.collection("vehicles")
                .document(vehicle.id)
                .set(vehicle)
                .await()
        }
    }
}
```

## Inyección de Dependencias

Utilizamos Hilt para la inyección de dependencias:

```kotlin
@HiltAndroidApp
class GeoFleetApplication : Application()

@AndroidEntryPoint
class VehicleProfileFragment : Fragment()

@HiltViewModel
class VehicleProfileViewModel @Inject constructor(
    private val repository: VehicleRepository
) : ViewModel()
```

## Mejores Prácticas

### 1. Manejo de Estados

- Usar sealed classes para estados de UI
- Mantener estados inmutables
- Usar StateFlow para estados y SharedFlow para eventos

### 2. Coroutines y Flows

- Usar viewModelScope para operaciones asíncronas
- Cancelar coroutines automáticamente al destruir el ViewModel
- Usar Flow para streams de datos reactivos

### 3. Testing

```kotlin
@Test
fun `when loading vehicle succeeds, state should be Success`() = runTest {
    // Given
    val vehicle = Vehicle(id = "1", plate = "ABC123")
    coEvery { repository.getVehicle("1") } returns vehicle
    
    // When
    viewModel.loadVehicle("1")
    
    // Then
    assert(viewModel.uiState.value is UiState.Success)
}
```

## Ventajas de MVVM

1. **Separación de Responsabilidades**
   - La View solo maneja la UI
   - El ViewModel contiene la lógica de presentación
   - El Model maneja los datos y reglas de negocio

2. **Testabilidad**
   - ViewModels son fáciles de testear
   - No hay dependencias directas de Android
   - Podemos mockear el repository

3. **Mantenibilidad**
   - Código más organizado
   - Cambios localizados
   - Fácil de extender

## Recursos Útiles

- [Android Architecture Components](https://developer.android.com/topic/libraries/architecture)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Hilt Dependency Injection](https://dagger.dev/hilt/) 
