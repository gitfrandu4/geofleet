# Manejo de Imágenes de Perfil

## Descripción General
GeoFleet implementa un sistema robusto de manejo de imágenes de perfil que permite a los usuarios cargar y actualizar sus fotos de perfil. El sistema utiliza un componente personalizado `ProfileImageView` que maneja automáticamente la carga y visualización de imágenes de perfil desde Firebase Storage.

## Características
- 🔄 Carga automática de imagen de perfil
- 🖼️ Visualización circular de imagen
- 🔒 Integración con Firebase
- 📱 Componente reutilizable
- 🎨 Soporte de imagen predeterminada
- 👤 Actualizaciones en tiempo real

## Detalles de Implementación

### Estructura de Almacenamiento
```
firebase-storage/
└── users/
    └── {userId}/
        └── profile.jpg
```

### Estructura del Componente
El `ProfileImageView` es una vista personalizada que extiende `AppCompatImageView` y maneja toda la lógica de carga de imágenes de perfil:

```kotlin
class ProfileImageView : AppCompatImageView {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var snapshotListener: ListenerRegistration? = null
}
```

### Características Principales

1. **Carga Automática**
   - Carga la imagen de perfil inmediatamente al inicializar
   - Escucha actualizaciones en tiempo real de cambios en el perfil
   - Utiliza imagen predeterminada si no hay imagen disponible

2. **Integración con Firebase**
   - Utiliza Firestore para rastrear actualizaciones de perfil
   - Se integra con Firebase Storage para la carga de imágenes
   - Mantiene listeners de snapshots en tiempo real

3. **Manejo de Errores**
   - Retroceso elegante a imagen predeterminada
   - Registro completo de errores
   - Limpieza adecuada de listeners

### Uso

1. **En Archivos de Layout**
```xml
<com.example.geofleet.ui.components.ProfileImageView
    android:id="@+id/nav_header_image"
    android:layout_width="64dp"
    android:layout_height="64dp" />
```

2. **En el Encabezado de Navegación**
```kotlin
headerView.findViewById<ProfileImageView>(R.id.nav_header_image)?.let { profileImageView ->
    profileImageView.startListeningToProfileChanges()
}
```

### Detalles de Implementación

1. **Inicialización**
```kotlin
init {
    setImageResource(R.drawable.ic_person)
    loadExistingProfileImage()
}
```

2. **Carga de Imagen de Perfil**
```kotlin
private fun loadExistingProfileImage() {
    auth.currentUser?.let { user ->
        db.collection(UserProfile.COLLECTION_NAME)
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                if (!profile?.photoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(profile?.photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(this)
                }
            }
    }
}
```

3. **Actualizaciones en Tiempo Real**
```kotlin
fun startListeningToProfileChanges() {
    snapshotListener?.remove()
    auth.currentUser?.let { user ->
        snapshotListener = db.collection(UserProfile.COLLECTION_NAME)
            .document(user.uid)
            .addSnapshotListener { snapshot, e ->
                // Manejar actualizaciones de perfil
            }
    }
}
```

### Mejores Prácticas
1. Siempre usar `circleCrop()` para visualización circular consistente
2. Establecer imágenes de placeholder y error
3. Limpiar listeners en `onDetachedFromWindow()`
4. Manejar todos los casos posibles de error
5. Proporcionar registro detallado para depuración

### Dependencias
```gradle
implementation 'com.github.bumptech.glide:glide:4.16.0'
implementation 'com.google.firebase:firebase-storage-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'
```

### Notas
- El componente maneja automáticamente eventos del ciclo de vida
- Utiliza Glide para carga y caché eficiente de imágenes
- Mantiene consistencia en toda la aplicación
- Proporciona actualizaciones en tiempo real cuando el perfil cambia 
