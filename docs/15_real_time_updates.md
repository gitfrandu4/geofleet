# Technical Documentation

## Real-time Vehicle Position Updates

### Overview
The application implements real-time vehicle position tracking using Kotlin Coroutines for efficient background processing. The update interval is configurable through the BuildConfig system.

### Key Components

#### 1. Build Configuration
In `app/build.gradle`, a custom BuildConfig field is defined:
```groovy
buildConfigField "long", "REFRESH_INTERVAL_MILLIS", "${properties.getProperty('REFRESH_INTERVAL_MILLIS', '60000')}L"
```
This allows for configurable refresh intervals that can be modified without code changes.

#### 2. MapActivity Implementation
The `MapActivity` class handles periodic position updates using the following components:

- **refreshJob**: A coroutine Job that manages the lifecycle of periodic updates
- **startPeriodicRefresh()**: Initiates periodic updates using coroutines
- **Lifecycle Management**: Proper cleanup in `onDestroy()` to prevent memory leaks

### Technical Implementation Details

#### Coroutine Usage
```kotlin
private fun startPeriodicRefresh() {
    refreshJob?.cancel()
    refreshJob = lifecycleScope.launch {
        while (isActive) {
            try {
                refreshVehiclePositions()
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing vehicle positions", e)
            }
            delay(BuildConfig.REFRESH_INTERVAL_MILLIS)
        }
    }
}
```

Key aspects:
- Uses `lifecycleScope` to tie the coroutine lifecycle to the activity
- Implements error handling for network failures
- Configurable refresh interval via BuildConfig
- Proper cancellation handling

#### Memory Management
The implementation includes proper cleanup:
```kotlin
override fun onDestroy() {
    super.onDestroy()
    refreshJob?.cancel()
}
```

### UI Components
- Vehicle markers on map with distinct states (selected/unselected)
- Real-time position updates
- Error handling with user feedback via Snackbar

### Configuration
The refresh interval can be configured in `local.properties`:
```properties
REFRESH_INTERVAL_MILLIS=60000
```

## Best Practices
1. Coroutine scope management
2. Proper error handling
3. Configurable parameters
4. Memory leak prevention
5. User feedback for errors 
