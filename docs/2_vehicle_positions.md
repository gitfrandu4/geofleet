# Documentación de la Funcionalidad de Posiciones de Vehículos

## Descripción General
La funcionalidad de posiciones de vehículos muestra datos de ubicación en tiempo real en dos formatos:
1. Una vista de mapa que muestra las posiciones actuales de todos los vehículos
2. Una vista de lista que muestra información detallada de cada vehículo con capacidades de búsqueda y filtrado

## Configuración
La funcionalidad utiliza un archivo de configuración externo (`assets/config.properties`) para gestionar los IDs de vehículos:

```properties
# Configuración de Vehículos
vehicle.ids=1509,1511,1512,1528,1793
API_TOKEN=your_api_token_here
BASE_URL=https://api.example.com/
```
La configuración se gestiona a través de la utilidad `ConfigurationReader`:
```kotlin
object ConfigurationReader {
    fun init(context: Context)
    fun getVehicleIds(): List<String>
}
```

## Componentes

### Base de Datos
- **Entidad**: `VehiclePositionEntity`
  ```kotlin
  @Entity(tableName = "vehicle_positions")
  data class VehiclePositionEntity(
      @PrimaryKey val vehicleId: String,
      val latitude: Double,
      val longitude: Double,
      val timestamp: Long = System.currentTimeMillis()
  )
  ```

### API
- **Servicio**: `VehicleService`
  ```kotlin
  interface VehicleService {
      @GET("vehicle/{id}")
      suspend fun getVehiclePosition(
          @Path("id") vehicleId: String,
          @Header("Authorization") token: String
      ): VehiclePosition?
  }
  ```

### Vistas

#### Vista de Mapa (`VehiclePositionsFragment`)
- Muestra vehículos en un mapa de Google
- Actualiza automáticamente las posiciones cuando:
  - Se crea el fragmento
  - El mapa está listo
  - El usuario regresa al fragmento
  - El usuario actualiza manualmente
- Utiliza marcadores personalizados para vehículos:
  - Vehículos regulares: Color de marcador predeterminado
  - Vehículo seleccionado: Marcador verde (cuando se abre desde la vista de flota)
- Centra y hace zoom en el vehículo seleccionado cuando se abre desde la lista
- Muestra todos los vehículos en vista cuando se abre normalmente

#### Vista de Flota (`FleetFragment`)
- Muestra una lista de todos los vehículos con:
  - Imagen/icono del vehículo
  - ID del vehículo (formato localizado: "Vehículo X")
  - Última posición conocida
  - Acciones rápidas:
    - Botón de perfil (para futuros detalles/configuración del vehículo)
    - Botón de mapa (abre el mapa centrado en el vehículo con marcador resaltado)
- Características:
  - Funcionalidad de búsqueda para filtrar vehículos
  - Contador total de vehículos en un círculo flotante
  - Funcionalidad de actualización por deslizamiento
  - Vista de estado vacío cuando no hay vehículos disponibles
  - Manejo de errores con opciones de reintento

### Flujo de Datos
1. **Carga Inicial**:
   - Cargar IDs de vehículos desde la configuración
   - Obtener posiciones desde la API
   - Almacenar en base de datos Room
   - Actualizar Firestore con posición actual e historial
   - Actualizar UI

2. **Flujo de Actualización**:
   - Cancelar cualquier trabajo de actualización en curso
   - Obtener nuevas posiciones para todos los vehículos
   - Actualizar almacenamiento local y en la nube
   - Actualizar UI
   - Actualizar contador total de vehículos

3. **Flujo de Búsqueda**:
   - Usuario ingresa texto de búsqueda
   - La lista se filtra en tiempo real
   - El contador total se actualiza para reflejar los resultados filtrados
   - La lista original se conserva para reinicio

4. **Manejo de Errores**:
   - Los errores de red muestran opción de reintento
   - Los vehículos faltantes se registran en el log
   - Los errores de API se manejan correctamente
   - Las cancelaciones de trabajos se gestionan adecuadamente

### Integración con Firebase
- Cada vehículo tiene:
  - Documento de posición actual
  - Colección de historial de posiciones
  ```json
  vehicles/
    ├── {vehicle_id}/
    │   ├── current_position/
    │   │   ├── latitude: Double
    │   │   ├── longitude: Double
    │   │   └── timestamp: Long
    │   └── coordinates_history/
    │       └── {position_id}/
    │           ├── coordinates/
    │           │   ├── latitude: Double
    │           │   └── longitude: Double
    │           ├── timestamp: Long
    │           └── created_at: Long
  ```

## Uso
1. **Ver Todos los Vehículos**:
   - Abrir la sección de Flota desde el menú de navegación
   - Todos los vehículos se muestran en una lista desplazable
   - Usar la barra de búsqueda para filtrar vehículos
   - Ver el contador total de vehículos en el círculo flotante
   - Deslizar hacia abajo para actualizar la lista

2. **Ver Vehículo en el Mapa**:
   - Hacer clic en el botón de mapa en cualquier tarjeta de vehículo
   - El mapa se abrirá centrado en ese vehículo
   - El marcador del vehículo seleccionado se resaltará en verde
   - Otros vehículos serán visibles con marcadores predeterminados
   - Usar el FAB para actualizar todas las posiciones

3. **Actualizar Posiciones**:
   - Deslizar para actualizar en la vista de flota
   - Hacer clic en el FAB en la vista de mapa
   - Las posiciones se actualizan automáticamente al regresar a cualquier vista

## Dependencias
```gradle
// Room
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// SwipeRefreshLayout
implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"

// Lifecycle
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'

// Material Design
implementation 'com.google.android.material:material:1.11.0'
```

## Manejo de Errores
- Los errores de red son capturados y registrados
- La UI permanece receptiva durante la actualización
- El caché local asegura la disponibilidad de datos
- SwipeRefreshLayout indica el estado de actualización
- Los trabajos de corrutinas se gestionan adecuadamente para prevenir fugas de memoria
- Registro detallado para depuración y monitoreo


## Error Handling
- Network errors are caught and logged
- UI remains responsive during refresh
- Local cache ensures data availability
- SwipeRefreshLayout indicates refresh state 
