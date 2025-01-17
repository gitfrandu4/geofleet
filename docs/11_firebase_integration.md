# Integración con Firebase

## Configuración Inicial

### Dependencias

En `app/build.gradle`:

```groovy
dependencies {
    // Firebase BoM
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    
    // Firebase Auth y Firestore
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-storage-ktx'
}
```

### Inicialización

En la clase `Application`:

```kotlin
class GeoFleetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
```

## Autenticación

### Login con Email y Contraseña

```kotlin
class LoginActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    
    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                // Usuario autenticado correctamente
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // Error de autenticación
                showError(e.message)
            }
    }
}
```

### Estado de Autenticación

```kotlin
class SplashActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (auth.currentUser != null) {
            // Usuario ya autenticado
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // Usuario no autenticado
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
```

## Cloud Firestore

### Estructura de Datos

```
vehicles/
    ├── vehicleId1/
    │   ├── plate: "ABC123"
    │   ├── brand: "Toyota"
    │   ├── model: "Hilux"
    │   ├── vehicleType: "Camión"
    │   ├── state: "ACTIVE"
    │   └── images: ["url1", "url2"]
    │
    └── vehicleId2/
        ├── plate: "XYZ789"
        └── ...
```

### Operaciones CRUD

#### Crear/Actualizar Vehículo

```kotlin
suspend fun saveVehicle(vehicle: Vehicle) {
    withContext(Dispatchers.IO) {
        firestore.collection("vehicles")
            .document(vehicle.id)
            .set(vehicle)
            .await()
    }
}
```

#### Leer Vehículo

```kotlin
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
```

#### Listar Vehículos

```kotlin
suspend fun getVehicles(): List<Vehicle> {
    return withContext(Dispatchers.IO) {
        firestore.collection("vehicles")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Vehicle::class.java)
                    ?.copy(id = doc.id)
            }
    }
}
```

#### Eliminar Vehículo

```kotlin
suspend fun deleteVehicle(id: String) {
    withContext(Dispatchers.IO) {
        firestore.collection("vehicles")
            .document(id)
            .delete()
            .await()
    }
}
```

## Cloud Storage

### Subir Imágenes

```kotlin
suspend fun uploadImage(uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference
            .child("vehicles")
            .child(filename)
            
        ref.putFile(uri).await()
        ref.downloadUrl.await().toString()
    }
}
```

### Eliminar Imágenes

```kotlin
suspend fun deleteImage(url: String) {
    withContext(Dispatchers.IO) {
        storage.getReferenceFromUrl(url)
            .delete()
            .await()
    }
}
```

## Seguridad

### Reglas de Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /vehicles/{vehicleId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

### Reglas de Storage

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /vehicles/{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
                   && request.resource.size < 5 * 1024 * 1024
                   && request.resource.contentType.matches('image/.*');
    }
  }
}
```

## Mejores Prácticas

### 1. Manejo de Errores

```kotlin
suspend fun safeFirestoreCall<T>(
    call: suspend () -> T
): Result<T> = try {
    Result.success(call())
} catch (e: FirebaseFirestoreException) {
    Result.failure(e)
}
```

### 2. Caché Offline

```kotlin
firestore.firestoreSettings = firestoreSettings {
    isPersistenceEnabled = true
    cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
}
```

### 3. Batch Operations

```kotlin
suspend fun updateVehicleImages(
    vehicleId: String,
    images: List<String>
) {
    withContext(Dispatchers.IO) {
        firestore.runBatch { batch ->
            val ref = firestore.collection("vehicles")
                .document(vehicleId)
            
            batch.update(ref, "images", images)
            batch.update(ref, "updatedAt", FieldValue.serverTimestamp())
        }.await()
    }
}
```

## Recursos Útiles

- [Firebase Console](https://console.firebase.google.com)
- [Documentación de Firebase para Android](https://firebase.google.com/docs/android/setup)
- [Guía de Cloud Firestore](https://firebase.google.com/docs/firestore)
- [Guía de Cloud Storage](https://firebase.google.com/docs/storage) 
