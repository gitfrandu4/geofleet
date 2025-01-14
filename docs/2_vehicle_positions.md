# Vehicle Positions Feature Documentation

## Overview
The vehicle positions feature displays real-time location data for vehicles in a scrollable list format. It implements a local caching mechanism using Room database and provides pull-to-refresh functionality for updating the data.

## Configuration
The feature uses an external configuration file (`assets/config.properties`) to manage vehicle IDs:

```properties
# Vehicle Configuration
vehicle.ids=vehicle1,vehicle2,vehicle3
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
      val latitude: String,
      val longitude: String,
      val timestamp: Long = System.currentTimeMillis()
  )
  ```

- **DAO**: `VehiclePositionDao`
  ```kotlin
  @Dao
  interface VehiclePositionDao {
      @Insert(onConflict = OnConflictStrategy.REPLACE)
      suspend fun insertAll(positions: List<VehiclePositionEntity>)

      @Query("SELECT * FROM vehicle_positions ORDER BY timestamp DESC")
      fun getAllPositions(): Flow<List<VehiclePositionEntity>>
  }
  ```

### UI Components

#### Layout Files
1. **Activity Layout** (`activity_vehicle_positions.xml`):
   - CoordinatorLayout with AppBarLayout
   - MaterialToolbar with refresh action
   - SwipeRefreshLayout containing RecyclerView

2. **List Item Layout** (`item_vehicle_position.xml`):
   - MaterialCardView containing:
     - Vehicle ID
     - Position coordinates
     - Timestamp

### Activity Implementation
`VehiclePositionsActivity` manages the UI and data flow:

```kotlin
class VehiclePositionsActivity : AppCompatActivity() {
    // Key components
    private lateinit var binding: ActivityVehiclePositionsBinding
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var database: AppDatabase
    private val adapter = VehiclePositionAdapter()

    // Features
    - RecyclerView with LinearLayoutManager
    - SwipeRefreshLayout for pull-to-refresh
    - Toolbar with refresh action
    - Real-time position updates using Flow
    - Local caching in Room database
}
```

### Adapter Implementation
`VehiclePositionAdapter` handles the display of individual vehicle positions:
- Extends ListAdapter for efficient list updates
- Uses DiffUtil for optimized item updates
- Displays vehicle ID, coordinates, and formatted timestamp

## Data Flow
1. Initial Load:
   - Activity observes database for positions
   - Triggers initial refresh to fetch latest data

2. Refresh Operation:
   - User triggers refresh (pull-to-refresh or button)
   - Fetches new positions from repository
   - Updates local database
   - UI automatically updates through Flow observation

3. Data Persistence:
   - All positions are cached in Room database
   - UI always shows data from local database
   - Ensures offline availability

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
