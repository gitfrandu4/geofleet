# Implementación de Geocodificación

## Descripción General
La aplicación GeoFleet ahora incluye funcionalidad de geocodificación para convertir coordenadas de vehículos en direcciones legibles por humanos. Esta característica mejora la experiencia del usuario al mostrar la información de ubicación en un formato más comprensible.

## Componentes Principales

### Entidad GeocodedAddress
```kotlin
@Entity(tableName = "geocoded_addresses")
data class GeocodedAddress(
    val coordinates: String,  // Formato: "lat,lng"
    val address: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### GeocodingRepository
La clase `GeocodingRepository` maneja el proceso de geocodificación con las siguientes características:
- Utiliza la API Geocoder de Android para la conversión de coordenadas a direcciones
- Implementa caché local para minimizar llamadas a la API
- Período de validez de caché de 7 días
- Retroceso a coordenadas crudas si la geocodificación falla

### Estrategia de Caché
- Las direcciones se almacenan en caché en una base de datos Room local
- Cada entrada en caché incluye:
  - Cadena de coordenadas
  - Dirección geocodificada
  - Marca de tiempo para invalidación de caché
- Limpieza automática de entradas de caché expiradas

### Manejo de Errores
- Retroceso elegante a coordenadas crudas si:
  - El servicio de geocodificación no está disponible
  - No se encuentra dirección para las coordenadas
  - Ocurren errores de red
- Registro completo para depuración

## Detalles de Implementación

### Integración con Base de Datos
- Entidad `GeocodedAddress` añadida a la base de datos Room principal
- Versión de base de datos actualizada para manejar la nueva entidad
- Implementación de `GeocodedAddressDao` para operaciones de base de datos

### Visualización en Lista de Vehículos
- `VehicleAdapter` actualizado para mostrar:
  - Estado de carga mientras se obtiene la dirección
  - Dirección geocodificada cuando está disponible
  - Retroceso a coordenadas si es necesario
- Icono de ubicación añadido para mejor retroalimentación visual

### Consideraciones de Rendimiento
- Geocodificación asíncrona usando corrutinas de Kotlin
- Caché local para reducir llamadas a la API
- Limpieza por lotes de entradas de caché expiradas

## Configuración
- No se requieren claves de API adicionales (usa el Geocoder integrado de Android)
- Duración de caché configurable en `GeocodingRepository`
- Registro habilitado para propósitos de depuración

## Mejoras Futuras
- Implementar geocodificación por lotes para múltiples ubicaciones
- Agregar soporte para geocodificación inversa
- Mejorar la personalización del formato de direcciones
- Considerar implementar geocodificación sin conexión 
