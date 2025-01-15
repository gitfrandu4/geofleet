# Splash Screen Implementation

## Overview
The app implements a splash screen using Android 12's SplashScreen API to provide a smooth launch experience while checking authentication status. The splash screen displays the app's icon and brand color during the initial app launch.

## Implementation Details

### 1. Theme Configuration
Located in `themes.xml`:
```xml
<style name="Theme.GeoFleet.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/brand_color</item>
    <item name="windowSplashScreenAnimatedIcon">@mipmap/ic_launcher</item>
    <item name="windowSplashScreenAnimationDuration">300</item>
    <item name="postSplashScreenTheme">@style/Theme.GeoFleet</item>
</style>
```

### 2. SplashActivity Implementation
Located in `app/src/main/java/com/example/geofleet/SplashActivity.kt`:
```kotlin
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { true }

        lifecycleScope.launch {
            delay(1000) // Show splash for 1 second
            navigateToNextScreen()
        }
    }
}
```

### 3. Navigation Flow
1. App launches → Shows splash screen with brand color and app icon
2. During splash screen:
   - Checks Firebase authentication status
   - Determines appropriate destination
3. Navigates to:
   - `MapActivity` if user is authenticated
   - `LoginActivity` if user is not authenticated

### 4. AndroidManifest Configuration
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

## Dependencies
```gradle
implementation 'androidx.core:core-splashscreen:1.0.1'
```

## Key Features
- Uses modern SplashScreen API
- Maintains brand identity with custom colors
- Handles authentication check during launch
- Provides smooth transition to appropriate screen
- No flashing or jarring transitions

## Technical Notes
1. The splash screen is kept visible using `setKeepOnScreenCondition { true }` until authentication check completes
2. A 1-second delay ensures smooth animation and prevents jarring transitions
3. Uses Kotlin coroutines for asynchronous operations
4. Follows Material Design guidelines for splash screens

## Future Enhancements
1. Add custom exit animation
2. Implement progress indicator for long-running operations
3. Add offline mode detection
4. Cache authentication state for faster startup 
