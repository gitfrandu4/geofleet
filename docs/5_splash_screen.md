# Implementación de Pantalla de Inicio

## Descripción General
La aplicación implementa una pantalla de inicio utilizando la API SplashScreen de Android 12 para proporcionar una experiencia de inicio suave mientras verifica el estado de autenticación. La pantalla de inicio muestra el ícono de la aplicación y el color de marca durante el lanzamiento inicial de la aplicación.

## Detalles de Implementación

### 1. Configuración del Tema
Ubicado en `themes.xml`:
```xml
<style name="Theme.GeoFleet.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/brand_color</item>
    <item name="windowSplashScreenAnimatedIcon">@mipmap/ic_launcher</item>
    <item name="windowSplashScreenAnimationDuration">300</item>
    <item name="postSplashScreenTheme">@style/Theme.GeoFleet</item>
</style>
```

### 2. Implementación de SplashActivity
Ubicado en `app/src/main/java/com/example/geofleet/SplashActivity.kt`:
```kotlin
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }

        lifecycleScope.launch {
            delay(1000) // Mostrar splash por 1 segundo
            navigateToNextScreen()
        }
    }
}
```

### 3. Flujo de Navegación
1. La aplicación inicia → Muestra la pantalla de inicio con el color de marca y el ícono
2. Durante la pantalla de inicio:
   - Verifica el estado de autenticación de Firebase
   - Determina el destino apropiado
3. Navega a:
   - `MapActivity` si el usuario está autenticado
   - `LoginActivity` si el usuario no está autenticado

### 4. Configuración del AndroidManifest
```xml
<activity
    android:name=".SplashActivity"
    android:exported="true"
    android:theme="@style/Theme.GeoFleet.Splash">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

## Dependencias
```gradle
implementation 'androidx.core:core-splashscreen:1.0.1'
```

## Características Principales
- Utiliza la API moderna de SplashScreen
- Mantiene la identidad de marca con colores personalizados
- Maneja la verificación de autenticación durante el inicio
- Proporciona una transición suave a la pantalla apropiada
- Sin destellos ni transiciones bruscas

## Notas Técnicas
1. La pantalla de inicio se mantiene visible usando `setKeepOnScreenCondition { true }` hasta que se complete la verificación de autenticación
2. Un retraso de 1 segundo asegura una animación suave y evita transiciones bruscas
3. Utiliza corrutinas de Kotlin para operaciones asíncronas
4. Sigue las pautas de Material Design para pantallas de inicio

## Mejoras Futuras
1. Agregar animación de salida personalizada
2. Implementar indicador de progreso para operaciones largas
3. Agregar detección de modo sin conexión
4. Almacenar en caché el estado de autenticación para un inicio más rápido 
