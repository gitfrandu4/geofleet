# Technical Documentation

## Real-time Vehicle Position Updates

### Overview
The application implements real-time vehicle position tracking using Kotlin Coroutines for efficient background processing. The update interval is configurable through the BuildConfig system. Vehicle positions are now enhanced with geocoding support to display human-readable addresses.

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
- **Geocoding Integration**: Converts coordinates to addresses for better user experience

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

#### Position Data Flow
1. Positions are fetched from the API
2. Stored in Room database for offline access
3. Updated in Firestore for real-time sync
4. Coordinates are geocoded to addresses
5. UI is updated with the latest information

### UI Components
- Vehicle markers on map with distinct states (selected/unselected)
- Real-time position updates
- Geocoded addresses in vehicle list
- Loading states during address resolution
- Error handling with user feedback via Snackbar

### Configuration
The refresh interval can be configured in `local.properties`:
```properties
REFRESH_INTERVAL_MILLIS=60000
```

### Data Persistence
- Vehicle positions stored in Room database
- Geocoded addresses cached for 7 days
- Automatic cleanup of expired cache entries
- Firestore synchronization for real-time updates

## Best Practices
1. Coroutine scope management
2. Proper error handling
3. Configurable parameters
4. Memory leak prevention
5. User feedback for errors
6. Efficient caching strategies
7. Graceful fallback mechanisms 
