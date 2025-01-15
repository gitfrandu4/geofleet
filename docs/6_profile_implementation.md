# Implementación del Perfil de Usuario

## Descripción General
El módulo de perfil de usuario permite a los usuarios gestionar su información personal, incluyendo la subida de fotos de perfil. La información se sincroniza con Firebase Firestore y las imágenes se almacenan en Firebase Storage.

## Características Principales

### 1. Gestión de Datos Personales
- Nombre y apellidos
- Cargo/Posición
- Correo electrónico (no editable, sincronizado con Firebase Auth)
- Género (opciones predefinidas en español)
- Fecha de nacimiento (con selector de fecha en español)
- Foto de perfil

### 2. Almacenamiento de Datos
- **Firestore**: Almacena la información del perfil en la colección `users`
- **Firebase Storage**: Almacena las fotos de perfil en `profile_images/{userId}`

### 3. Interfaz de Usuario
- Imagen de perfil circular con opción de cambio
- Campos de texto con Material Design
- Selector de género con opciones en español
- Selector de fecha localizado
- Indicadores de progreso durante operaciones
- Mensajes de feedback en español

## Estructura de Datos
```kotlin
data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val position: String = "",
    val email: String = "",
    val gender: String = "",
    val birthdate: String = "",
    val photoUrl: String = ""
)
```

## Opciones de Género
- Masculino
- Femenino
- Otro
- Prefiero no decirlo

## Mensajes de Feedback
### Éxito
- "Perfil guardado correctamente"
- "Imagen actualizada correctamente"

### Error
- "Error al cargar el perfil"
- "Error al guardar el perfil"
- "Error al subir la imagen"

### Progreso
- "Cargando perfil..."
- "Guardando perfil..."
- "Subiendo imagen..."

## Componentes Localizados
1. **Selector de Fecha**
   - Formato: dd/MM/yyyy
   - Locale: es-ES
   - Botones traducidos:
     - "Aceptar"
     - "Cancelar"

2. **Género**
   - Mapeo interno:
     ```kotlin
     "Masculino" <-> "Male"
     "Femenino" <-> "Female"
     "Otro" <-> "Other"
     "Prefiero no decirlo" <-> "Prefer not to say"
     ```

## Dependencias Principales
```gradle
implementation 'com.github.bumptech.glide:glide:4.16.0'
implementation 'com.google.firebase:firebase-storage-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'
```

## Permisos Requeridos
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Notas de Implementación
1. La imagen de perfil se almacena con el ID del usuario como nombre
2. Se utiliza Glide para la carga eficiente de imágenes
3. Los campos del formulario utilizan Material Design TextInputLayout
4. Se implementa manejo de estados de carga y errores
5. La interfaz está completamente traducida al español 
