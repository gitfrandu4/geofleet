# Integración del Mapa y Marcadores

## Cambios Realizados

### 1. Creación de MapActivity
- Nueva actividad para mostrar el mapa de Google Maps
- Implementación de `OnMapReadyCallback` para manejar la inicialización del mapa
- Sistema de actualización automática de marcadores usando Flow
- Botón de actualización (FAB) para refrescar manualmente las posiciones

### 2. Modelo de Datos
- Actualización de `VehiclePositionEntity`:
  ```kotlin
  @Entity(tableName = "vehicle_positions")
  data class VehiclePositionEntity(
      @PrimaryKey
      val vehicleId: String,
      val latitude: Double,  // Cambiado de String a Double
      val longitude: Double, // Cambiado de String a Double
      val timestamp: Long = System.currentTimeMillis()
  )
  ```

### 3. Base de Datos
- Incremento de la versión de la base de datos a 5
- Implementación de `fallbackToDestructiveMigration()` para manejar cambios de esquema

### 4. Flujo de Navegación
- Modificación del flujo de login para dirigir a `MapActivity` después del inicio de sesión exitoso
- Integración con el proceso de carga inicial de datos

### 5. Layout del Mapa
```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout>
    <fragment
        android:id="@+id/map"
        android:name="com.google.android.gms.maps.SupportMapFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <FloatingActionButton
        android:id="@+id/fab"
        android:contentDescription="@string/refresh_positions"
        app:srcCompat="@drawable/ic_refresh" />
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

### 6. Marcadores Personalizados
- Implementación de marcadores personalizados para vehículos:
  ```xml
  <!-- Layout del marcador personalizado -->
  <LinearLayout
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:padding="4dp"
      android:background="@drawable/marker_background">
      <ImageView
          android:layout_width="20dp"
          android:layout_height="20dp"
          android:src="@drawable/ic_vehicle_marker"/>
  </LinearLayout>
  ```

- Diseño del icono del vehículo:
  ```xml
  <vector
      android:width="16dp"
      android:height="16dp"
      android:viewportWidth="24"
      android:viewportHeight="24">
      <path
          android:fillColor="#f2632a"
          android:pathData="M20,8h-3L17,4L3,4c-1.1..."/>
  </vector>
  ```

- Fondo del marcador optimizado:
  ```xml
  <shape>
      <solid android:color="#FFFFFF" />
      <corners android:radius="4dp" />
      <stroke
          android:width="0.5dp"
          android:color="#80000000" />
  </shape>
  ```

## Funcionalidades Implementadas

1. **Visualización de Vehículos**
   - Marcadores personalizados con icono de camión
   - Tamaño reducido para mejor visualización del mapa
   - Color del icono personalizado (#f2632a)
   - Fondo blanco con borde sutil para mejor contraste
   - Título del marcador muestra el ID del vehículo
   - Filtrado automático de coordenadas inválidas (0,0)

2. **Actualización de Datos**
   - Actualización automática mediante Flow desde Room
   - Actualización manual mediante botón de refresh
   - Limpieza de marcadores antiguos antes de añadir nuevos

3. **Centrado del Mapa**
   - Centrado automático en la primera posición válida
   - Nivel de zoom establecido en 12f para una vista óptima

## Notas Técnicas

- Los datos de posición se almacenan localmente en Room
- Las coordenadas se convierten de String a Double durante el proceso de guardado
- Se implementó manejo de errores para coordenadas inválidas
- Se añadió logging detallado para facilitar el debugging
- Los marcadores personalizados se crean mediante un proceso de bitmap rendering
- Optimización del tamaño de los marcadores para mejor rendimiento

## Autenticación y Actualización de Datos

### Autenticación de API
- Todas las peticiones a la API requieren autenticación mediante token Bearer
- El token se configura en `config.properties`:
  ```properties
  API_TOKEN=your_api_token
  ```
- Se incluye en las cabeceras HTTP:
  ```kotlin
  @GET("vehicle/{id}")
  suspend fun getVehiclePosition(
      @Path("id") vehicleId: String,
      @Header("Authorization") token: String
  ): VehiclePosition
  ```

### Actualización de Posiciones
1. **Botón de Recarga (FAB)**
   - Ubicado en la esquina inferior derecha
   - Proporciona feedback visual durante la carga
   - Se deshabilita durante la actualización

2. **Proceso de Actualización**
   ```kotlin
   // 1. Obtener configuración
   val vehicleIds = properties.getProperty("vehicle.ids").split(",")
   val apiToken = "Bearer ${properties.getProperty("API_TOKEN")}"

   // 2. Obtener posiciones en paralelo
   val positions = vehicleIds.map { vehicleId ->
       async {
           vehicleService.getVehiclePosition(vehicleId, apiToken)
       }
   }.awaitAll()

   // 3. Guardar en base de datos
   database.vehiclePositionDao().insertAll(positions)

   // 4. Actualizar mapa
   updateMap()
   ```

3. **Manejo de Errores**
   - Errores de red muestran Snackbar con opción de reintentar
   - Errores de autenticación (401) se registran en el log
   - Posiciones inválidas (0,0) se filtran automáticamente

### Persistencia y Caché
- Las posiciones se almacenan en la base de datos local
- La vista del mapa lee desde la base de datos
- Actualización manual mediante FAB o menú
- Método `getPositionsSnapshot()` para obtener datos actuales

### Navegación
- Implementación actualizada de `onBackPressed()`
- Soporte para Android moderno
- Manejo correcto del drawer de navegación

## Próximos Pasos
1. Implementar actualización automática periódica
2. Añadir animaciones de transición para marcadores
3. Implementar clustering para múltiples vehículos
4. Añadir filtros por estado o ruta 
