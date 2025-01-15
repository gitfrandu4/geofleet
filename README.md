# GeoFleet - Sistema de Monitoreo de Flotas

## Descripción
GeoFleet es una aplicación Android moderna para el monitoreo y gestión de flotas de vehículos en tiempo real. La aplicación está desarrollada en Kotlin y utiliza las últimas tecnologías y mejores prácticas de desarrollo Android.

## Características Principales
- 🗺️ Visualización en tiempo real de posiciones de vehículos
- 🔐 Sistema de autenticación seguro con Firebase
- 💾 Almacenamiento local con Room Database
- 🌐 Sincronización en tiempo real
- 📱 Interfaz moderna y responsive
- 🔄 Soporte offline-first
- �� Material Design 3
- 📸 Gestión de imágenes de perfil con Firebase Storage

## Tecnologías Utilizadas
- Kotlin
- Firebase:
  - Authentication
  - Firestore
  - Storage
- Google Maps SDK
- Jetpack Components:
  - Room Database
  - Navigation Component
  - ViewModel & LiveData
  - ViewBinding
- Coroutines & Flow
- Material Design 3
- Retrofit & OkHttp
- Glide para carga de imágenes

## Requisitos Previos
- Android Studio Arctic Fox o superior
- JDK 8 o superior
- SDK Android API 34
- Google Play Services
- Cuenta de Firebase con Storage habilitado
- API Key de Google Maps

## Configuración
1. Clonar el repositorio:
```bash
git clone https://github.com/gitfrandu4/geofleet.git
```

2. Configurar Firebase:
   - Crear proyecto en Firebase Console
   - Agregar aplicación Android
   - Habilitar Authentication, Firestore y Storage
   - Descargar `google-services.json` y colocarlo en `app/`
   - Configurar reglas de Storage para permisos de usuario

3. Configurar Google Maps:
   - Obtener API Key de Google Cloud Console
   - Agregar en `local.properties`:
```properties
MAPS_API_KEY=tu_api_key_aqui
```

## Estructura del Proyecto
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/geofleet/
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   ├── local/
│   │   │   │   └── remote/
│   │   │   ├── ui/
│   │   │   │   ├── profile/
│   │   │   │   ├── vehicles/
│   │   │   │   └── fleet/
│   │   │   ├── service/
│   │   │   └── utils/
│   │   └── res/
│   └── test/
├── docs/
│   └── profile_image_handling.md
├── build.gradle
└── proguard-rules.pro
```

## Características Detalladas

### Monitoreo de Vehículos
- Visualización en mapa de posiciones actuales
- Historial de recorridos
- Actualización en tiempo real
- Modo offline

### Gestión de Usuarios
- Registro y autenticación
- Perfiles de usuario con foto
- Gestión de roles y permisos
- Recuperación de contraseña
- Carga y actualización de imágenes de perfil
- Soporte para cámara y galería

### Base de Datos Local
- Caché de posiciones
- Sincronización bidireccional
- Manejo de conflictos
- Migraciones automáticas

## Contribución
1. Fork el proyecto
2. Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## Licencia
Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## Contacto
Enlace del proyecto: [https://github.com/gitfrandu4/geofleet](https://github.com/gitfrandu4/geofleet)
