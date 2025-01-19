# Documentación Técnica

## Actualizaciones en Tiempo Real de Posición de Vehículos

### Descripción General
La aplicación implementa seguimiento de posición de vehículos en tiempo real utilizando Corrutinas de Kotlin para un procesamiento eficiente en segundo plano. El intervalo de actualización es configurable a través del sistema BuildConfig. Las posiciones de los vehículos ahora están mejoradas con soporte de geocodificación para mostrar direcciones legibles por humanos.

### Componentes Principales

#### 1. Configuración de Build
En `app/build.gradle`, se define un campo BuildConfig personalizado:
```groovy
buildConfigField "long", "REFRESH_INTERVAL_MILLIS", "${properties.getProperty('REFRESH_INTERVAL_MILLIS', '60000')}L"
```
Esto permite intervalos de actualización configurables que se pueden modificar sin cambios en el código.

#### 2. Implementación de MapActivity
La clase `MapActivity` maneja actualizaciones periódicas de posición utilizando los siguientes componentes:

- **refreshJob**: Un Job de corrutina que gestiona el ciclo de vida de las actualizaciones periódicas
- **startPeriodicRefresh()**: Inicia actualizaciones periódicas usando corrutinas
- **Gestión del Ciclo de Vida**: Limpieza adecuada en `onDestroy()` para prevenir fugas de memoria
- **Integración de Geocodificación**: Convierte coordenadas a direcciones para una mejor experiencia de usuario

### Detalles Técnicos de Implementación

#### Uso de Corrutinas
```kotlin
private fun startPeriodicRefresh() {
    refreshJob?.cancel()
    refreshJob = lifecycleScope.launch {
        while (isActive) {
            try {
                refreshVehiclePositions()
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar posiciones de vehículos", e)
            }
            delay(BuildConfig.REFRESH_INTERVAL_MILLIS)
        }
    }
}
```

Aspectos clave:
- Utiliza `lifecycleScope` para vincular el ciclo de vida de la corrutina a la actividad
- Implementa manejo de errores para fallos de red
- Intervalo de actualización configurable vía BuildConfig
- Manejo adecuado de cancelación

#### Gestión de Memoria
La implementación incluye limpieza adecuada:
```kotlin
override fun onDestroy() {
    super.onDestroy()
    refreshJob?.cancel()
}
```

#### Flujo de Datos de Posición
1. Las posiciones se obtienen de la API
2. Se almacenan en la base de datos Room para acceso sin conexión
3. Se actualizan en Firestore para sincronización en tiempo real
4. Las coordenadas se geocodifican a direcciones
5. La UI se actualiza con la información más reciente

### Componentes de UI
- Marcadores de vehículos en el mapa con estados distintos (seleccionado/no seleccionado)
- Actualizaciones de posición en tiempo real
- Direcciones geocodificadas en la lista de vehículos
- Estados de carga durante la resolución de direcciones
- Manejo de errores con retroalimentación al usuario vía Snackbar

### Configuración
El intervalo de actualización se puede configurar en `local.properties`:
```properties
REFRESH_INTERVAL_MILLIS=60000
```

### Persistencia de Datos
- Posiciones de vehículos almacenadas en base de datos Room
- Direcciones geocodificadas en caché por 7 días
- Limpieza automática de entradas de caché expiradas
- Sincronización con Firestore para actualizaciones en tiempo real

## Mejores Prácticas
1. Gestión de scope de corrutinas
2. Manejo adecuado de errores
3. Parámetros configurables
4. Prevención de fugas de memoria
5. Retroalimentación al usuario para errores
6. Estrategias eficientes de caché
7. Mecanismos de respaldo elegantes 
