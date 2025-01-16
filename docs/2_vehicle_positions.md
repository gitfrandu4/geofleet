# Vehicle Positions Feature Documentation

## Overview
The vehicle positions feature displays real-time location data for vehicles in two formats:
1. A map view showing all vehicles' current positions
2. A list view showing detailed information for each vehicle

## Configuration
The feature uses an external configuration file (`assets/config.properties`) to manage vehicle IDs:

```properties
# Vehicle Configuration
vehicle.ids=1509,1511,1512,1528,1793
API_TOKEN=your_api_token_here
BASE_URL=https://api.example.com/
```
Configuration is managed through the `ConfigurationReader` utility:
```kotlin
object ConfigurationReader {
    fun init(context: Context)
    fun getVehicleIds(): List<String>
}
```

## Components

### Database
- **Entity**: `VehiclePositionEntity`
  ```kotlin
  @Entity(tableName = "vehicle_positions")
  data class VehiclePositionEntity(
      @PrimaryKey val vehicleId: String,
      val latitude: Double,
      val longitude: Double,
      val timestamp: Long = System.currentTimeMillis()
  )
  ```

### API
- **Service**: `VehicleService`
  ```kotlin
  interface VehicleService {
      @GET("vehicle/{id}")
      suspend fun getVehiclePosition(
          @Path("id") vehicleId: String,
          @Header("Authorization") token: String
      ): VehiclePosition?
  }
  ```

### Views

#### Map View (`VehiclePositionsFragment`)
- Displays vehicles on a Google Map
- Auto-refreshes positions when:
  - Fragment is created
  - Map is ready
  - User returns to the fragment
  - User manually refreshes
- Uses custom markers for vehicles

#### Fleet View (`FleetFragment`)
- Displays a list of all vehicles with:
  - Vehicle image/icon
  - Vehicle ID
  - Last known position
  - Quick actions:
    - Profile button (for future vehicle details/settings)
    - Map button (opens map centered on the vehicle)
- Features:
  - Pull-to-refresh functionality
  - Empty state view when no vehicles are available
  - Error handling with retry options

### Data Flow
1. **Initial Load**:
   - Load vehicle IDs from configuration
   - Fetch positions from API
   - Store in Room database
   - Update Firestore
   - Update UI

2. **Refresh Flow**:
   - Cancel any ongoing refresh
   - Fetch new positions for all vehicles
   - Update local and cloud storage
   - Refresh UI

3. **Error Handling**:
   - Network errors show retry option
   - Missing vehicles are logged
   - API errors are handled gracefully

### Firebase Integration
- Each vehicle has:
  - Current position document
  - History collection of positions
  ```json
  vehicles/
    ├── {vehicle_id}/
    │   ├── current_position/
    │   │   ├── latitude: Double
    │   │   ├── longitude: Double
    │   │   └── timestamp: Long
    │   └── coordinates_history/
    │       └── {position_id}/
    │           ├── coordinates/
    │           │   ├── latitude: Double
    │           │   └── longitude: Double
    │           ├── timestamp: Long
    │           └── created_at: Long
  ```

## Usage
1. **View All Vehicles**:
   - Open the Fleet section from the navigation drawer
   - All vehicles are displayed in a scrollable list
   - Pull down to refresh the list

2. **View Vehicle on Map**:
   - Click the map button on any vehicle card
   - The map will open centered on that vehicle

3. **Refresh Positions**:
   - Pull to refresh in the fleet view
   - Click the FAB in the map view
   - Positions auto-refresh when returning to either view

## Dependencies
```gradle
// Room
implementation "androidx.room:room-runtime:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"

// SwipeRefreshLayout
implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"

// Lifecycle
implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
```

## Usage
1. Launch `VehiclePositionsActivity`
2. Positions are automatically loaded and displayed
3. Pull down to refresh or use toolbar refresh button
4. Positions are cached locally for offline access


## Error Handling
- Network errors are caught and logged
- UI remains responsive during refresh
- Local cache ensures data availability
- SwipeRefreshLayout indicates refresh state 
