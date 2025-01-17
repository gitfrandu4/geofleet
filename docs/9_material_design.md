# Material Design 3 en Android

## Componentes Principales

### Cards (MaterialCardView)

Las tarjetas son contenedores que agrupan contenido relacionado:

```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    style="@style/Widget.Material3.CardView.Elevated">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <!-- Contenido de la tarjeta -->
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

### Botones

#### Extended FAB (Floating Action Button)

Botón flotante con texto e icono:

```xml
<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/save_changes"
    app:icon="@drawable/ic_save" />
```

#### FAB Regular

Botón flotante circular:

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:srcCompat="@drawable/ic_camera" />
```

### Campos de Texto

#### TextInputLayout con Estilo Outlined

```xml
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/plate_number">

    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
</com.google.android.material.textfield.TextInputLayout>
```

### Diálogos

#### AlertDialog con Material Design

```kotlin
MaterialAlertDialogBuilder(requireContext())
    .setTitle(R.string.select_image)
    .setItems(arrayOf("Opción 1", "Opción 2")) { _, which ->
        when (which) {
            0 -> // Acción para opción 1
            1 -> // Acción para opción 2
        }
    }
    .show()
```

### Snackbars

Mensajes informativos en la parte inferior:

```kotlin
Snackbar.make(
    binding.root,
    R.string.changes_saved,
    Snackbar.LENGTH_LONG
).show()
```

## Temas y Estilos

### Colores del Sistema

En `colors.xml`:
```xml
<resources>
    <color name="primary">#F2632A</color>
    <color name="on_primary">#FFFFFF</color>
    <color name="secondary">#795548</color>
    <color name="on_secondary">#FFFFFF</color>
    <color name="surface">#FFFFFF</color>
    <color name="on_surface">#000000</color>
</resources>
```

### Tema de la Aplicación

En `themes.xml`:
```xml
<style name="Theme.GeoFleet" parent="Theme.Material3.DayNight.NoActionBar">
    <item name="colorPrimary">@color/primary</item>
    <item name="colorOnPrimary">@color/on_primary</item>
    <item name="colorSecondary">@color/secondary</item>
    <item name="colorOnSecondary">@color/on_secondary</item>
    <item name="android:colorBackground">@color/surface</item>
    <item name="colorOnBackground">@color/on_surface</item>
</style>
```

## Mejores Prácticas

### Espaciado Consistente

- Usar múltiplos de 8dp para márgenes y padding principales
- Usar múltiplos de 4dp para elementos más pequeños

```xml
android:padding="16dp"
android:layout_margin="8dp"
android:layout_marginTop="4dp"
```

### Tipografía

Usar los estilos de texto predefinidos:

```xml
android:textAppearance="?attr/textAppearanceTitleLarge"
android:textAppearance="?attr/textAppearanceBodyMedium"
```

### Elevación y Sombras

Material Design 3 maneja automáticamente las sombras basándose en la elevación:

```xml
android:elevation="2dp"
```

### Animaciones y Transiciones

Material Design incluye animaciones predefinidas:

```kotlin
// Animación de FAB al mostrar/ocultar
binding.fab.show() // o hide()

// Transición entre estados
TransitionManager.beginDelayedTransition(container)
```

## Recursos Útiles

- [Material Design 3 para Android](https://m3.material.io/develop/android)
- [Componentes Material](https://github.com/material-components/material-components-android)
- [Guía de Implementación](https://material.io/develop/android/docs/getting-started) 
