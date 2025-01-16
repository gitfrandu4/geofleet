# Navigation and Fragments Implementation

## Overview
The app implements a Material Design 3 navigation system using a Navigation Drawer and fragments for different sections. The navigation is handled by the Android Navigation Component, providing a consistent and predictable user experience.

## Main Components

### MainActivity
The central activity that hosts the navigation system and fragments:

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    // Navigation setup
    val navHostFragment = supportFragmentManager
        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    navController = navHostFragment.navController
    
    appBarConfiguration = AppBarConfiguration(
        setOf(R.id.nav_vehicle_positions, R.id.nav_fleet, R.id.nav_profile),
        binding.drawerLayout
    )
}
```

### Navigation Graph
Defined in `nav_graph.xml`, specifies the navigation structure:

```xml
<navigation
    android:id="@+id/nav_graph"
    app:startDestination="@id/nav_vehicle_positions">

    <fragment
        android:id="@+id/nav_vehicle_positions"
        android:name="com.example.geofleet.ui.vehicles.VehiclePositionsFragment"
        android:label="@string/vehicle_positions_map" />

    <fragment
        android:id="@+id/nav_fleet"
        android:name="com.example.geofleet.ui.fleet.FleetFragment"
        android:label="@string/fleet" />

    <fragment
        android:id="@+id/nav_profile"
        android:name="com.example.geofleet.ui.profile.ProfileFragment"
        android:label="@string/profile" />
</navigation>
```

## Fragments

### 1. Vehicle Positions Fragment
Displays real-time vehicle positions in a list format:
- SwipeRefreshLayout for pull-to-refresh
- RecyclerView for position list
- Offline support via Room database
- Real-time updates using Flow

### 2. Fleet Management Fragment
Manages the fleet of vehicles:
```kotlin
class FleetFragment : Fragment() {
    // Features:
    - Search functionality with Material TextInputLayout
    - RecyclerView for vehicle list
    - FloatingActionButton for adding new vehicles
    - Material Design 3 components throughout
}
```

Layout structure:
```xml
<CoordinatorLayout>
    <ConstraintLayout>
        <TextInputLayout/> <!-- Search -->
        <RecyclerView/>    <!-- Vehicle list -->
    </ConstraintLayout>
    <FloatingActionButton/> <!-- Add vehicle -->
</CoordinatorLayout>
```

### 3. Profile Fragment
Handles user profile management:
```kotlin
class ProfileFragment : Fragment() {
    // Features:
    - Profile image with circular shape
    - Editable user name
    - Read-only email display
    - Profile update functionality
    - Logout option
}
```

Layout structure:
```xml
<NestedScrollView>
    <ConstraintLayout>
        <ShapeableImageView/>  <!-- Profile picture -->
        <TextInputLayout/>     <!-- Name input -->
        <TextInputLayout/>     <!-- Email display -->
        <MaterialButton/>      <!-- Save changes -->
        <MaterialButton/>      <!-- Logout -->
    </ConstraintLayout>
</NestedScrollView>
```

## Material Design 3 Implementation

### Components Used
- MaterialToolbar for top app bar
- NavigationView for drawer
- MaterialButton for actions
- TextInputLayout for text inputs
- FloatingActionButton for primary actions
- MaterialCardView for list items
- ShapeableImageView for profile picture

### Styles
Custom styles for consistent appearance:
```xml
<style name="CircleImageView">
    <item name="cornerFamily">rounded</item>
    <item name="cornerSize">50%</item>
</style>
```

## Navigation Flow
1. User logs in through LoginActivity
2. MainActivity launches with navigation drawer
3. VehiclePositionsFragment shown as initial screen
4. User can navigate between sections using drawer
5. Back button closes drawer if open, otherwise follows system back behavior

## Dependencies
```gradle
implementation "androidx.navigation:navigation-fragment-ktx:2.7.7"
implementation "androidx.navigation:navigation-ui-ktx:2.7.7"
implementation 'com.google.android.material:material:1.11.0'
```

## Future Enhancements
1. Fleet Management:
   - Vehicle addition/editing
   - Vehicle details view
   - Search filters

2. Profile Management:
   - Profile picture upload
   - Additional user settings
   - Theme preferences

3. General:
   - Deep linking support
   - Transition animations
   - Tablet layout optimization 
