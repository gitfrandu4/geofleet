# Implementación de Navegación y Fragmentos

## Descripción General
La aplicación implementa un sistema de navegación Material Design 3 utilizando un Navigation Drawer y fragmentos para diferentes secciones. La navegación se maneja mediante el Componente de Navegación de Android, proporcionando una experiencia de usuario consistente y predecible.

## Componentes Principales

### MainActivity
La actividad central que aloja el sistema de navegación y fragmentos:

```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    // Configuración de navegación
    val navHostFragment = supportFragmentManager
        .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    navController = navHostFragment.navController
    
    appBarConfiguration = AppBarConfiguration(
        setOf(R.id.nav_vehicle_positions, R.id.nav_fleet, R.id.nav_profile),
        binding.drawerLayout
    )
}
```

### Gráfico de Navegación
Definido en `nav_graph.xml`, especifica la estructura de navegación:

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

## Fragmentos

### 1. Fragmento de Posiciones de Vehículos
Muestra las posiciones de vehículos en tiempo real en formato lista:
- SwipeRefreshLayout para actualizar al deslizar
- RecyclerView para la lista de posiciones
- Soporte sin conexión mediante base de datos Room
- Actualizaciones en tiempo real usando Flow

### 2. Fragmento de Gestión de Flota
Gestiona la flota de vehículos:
```kotlin
class FleetFragment : Fragment() {
    // Características:
    - Funcionalidad de búsqueda con Material TextInputLayout
    - RecyclerView para lista de vehículos
    - FloatingActionButton para agregar nuevos vehículos
    - Componentes Material Design 3 en toda la interfaz
}
```

Estructura del layout:
```xml
<CoordinatorLayout>
    <ConstraintLayout>
        <TextInputLayout/> <!-- Búsqueda -->
        <RecyclerView/>    <!-- Lista de vehículos -->
    </ConstraintLayout>
    <FloatingActionButton/> <!-- Agregar vehículo -->
</CoordinatorLayout>
```

### 3. Fragmento de Perfil
Maneja la gestión del perfil de usuario:
```kotlin
class ProfileFragment : Fragment() {
    // Características:
    - Imagen de perfil con forma circular
    - Nombre de usuario editable
    - Visualización de correo electrónico (solo lectura)
    - Funcionalidad de actualización de perfil
    - Opción de cierre de sesión
}
```

Estructura del layout:
```xml
<NestedScrollView>
    <ConstraintLayout>
        <ShapeableImageView/>  <!-- Foto de perfil -->
        <TextInputLayout/>     <!-- Campo de nombre -->
        <TextInputLayout/>     <!-- Visualización de correo -->
        <MaterialButton/>      <!-- Guardar cambios -->
        <MaterialButton/>      <!-- Cerrar sesión -->
    </ConstraintLayout>
</NestedScrollView>
```

## Implementación Material Design 3

### Componentes Utilizados
- MaterialToolbar para la barra superior
- NavigationView para el drawer
- MaterialButton para acciones
- TextInputLayout para entradas de texto
- FloatingActionButton para acciones principales
- MaterialCardView para elementos de lista
- ShapeableImageView para foto de perfil

### Estilos
Estilos personalizados para apariencia consistente:
```xml
<style name="CircleImageView">
    <item name="cornerFamily">rounded</item>
    <item name="cornerSize">50%</item>
</style>
```

## Flujo de Navegación
1. Usuario inicia sesión a través de LoginActivity
2. MainActivity se inicia con navigation drawer
3. VehiclePositionsFragment se muestra como pantalla inicial
4. Usuario puede navegar entre secciones usando el drawer
5. Botón atrás cierra el drawer si está abierto, de lo contrario sigue el comportamiento del sistema

## Dependencias
```gradle
implementation "androidx.navigation:navigation-fragment-ktx:2.7.7"
implementation "androidx.navigation:navigation-ui-ktx:2.7.7"
implementation 'com.google.android.material:material:1.11.0'
```

## Mejoras Futuras
1. Gestión de Flota:
   - Adición/edición de vehículos
   - Vista de detalles de vehículo
   - Filtros de búsqueda

2. Gestión de Perfil:
   - Carga de foto de perfil
   - Configuraciones adicionales de usuario
   - Preferencias de tema

3. General:
   - Soporte para deep linking
   - Animaciones de transición
   - Optimización de layout para tablets
