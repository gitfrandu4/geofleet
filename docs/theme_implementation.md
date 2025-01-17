# Theme Implementation Documentation

## Overview

This document details the implementation of Material Design 3 theming in GeoFleet, including color resources management and dark theme support.

## Color System Implementation

### Base Theme Colors

The application implements a comprehensive Material Design 3 color system with both light and dark theme variants. The color system is structured in two main files:

- `values/colors.xml`: Default (light) theme colors
- `values-night/colors.xml`: Dark theme specific colors

### Color Tokens

Key semantic color tokens used throughout the application:

```xml
Primary Colors:
- md_theme_dark_primary
- md_theme_dark_onPrimary
- md_theme_dark_primaryContainer
- md_theme_dark_onPrimaryContainer

Secondary Colors:
- md_theme_dark_secondary
- md_theme_dark_onSecondary
- md_theme_dark_secondaryContainer
- md_theme_dark_onSecondaryContainer

Tertiary Colors:
- md_theme_dark_tertiary
- md_theme_dark_onTertiary
- md_theme_dark_tertiaryContainer
- md_theme_dark_onTertiaryContainer
```

## Resource Organization

### 1. Base Colors (`values/colors.xml`)
- Contains default light theme colors
- Includes brand-specific colors
- Provides fallback values for all color resources

### 2. Night Colors (`values-night/colors.xml`)
- Contains dark theme specific colors
- Automatically applied when dark theme is active
- Maintains consistent naming with base colors

## Theme Integration

### Layout Implementation

Example usage in layouts:

```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    style="@style/Widget.Material3.CardView.Elevated">

    <!-- Content using theme colors -->
    <TextView
        android:textColor="?attr/colorOnSurface"
        android:background="?attr/colorSurface"
        ... />
</com.google.android.material.card.MaterialCardView>
```

### Best Practices

1. **Color References**
   - Always use theme attributes (`?attr/colorPrimary`) instead of direct color references
   - Maintain consistency across light and dark themes

2. **Resource Naming**
   - Follow Material Design 3 naming conventions
   - Use descriptive prefixes for custom colors

3. **Accessibility**
   - Ensure sufficient contrast ratios
   - Test color combinations in both themes

## Common Issues and Solutions

### 1. Missing Default Resources
Problem: Colors defined in `values-night` but missing in base `values`.
Solution: Always define colors in both locations.

```xml
// In values/colors.xml
<color name="md_theme_dark_primary">#006C4C</color>

// In values-night/colors.xml
<color name="md_theme_dark_primary">#6CDBAC</color>
```

### 2. Theme Attribute Resolution
Problem: Direct color references don't adapt to theme changes.
Solution: Use theme attributes.

```xml
<!-- Incorrect -->
android:textColor="@color/md_theme_dark_primary"

<!-- Correct -->
android:textColor="?attr/colorPrimary"
```

## Testing Theme Implementation

1. **Runtime Testing**
   ```kotlin
   // Force dark theme
   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

   // Force light theme
   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
   ```

2. **Visual Testing**
   - Test layouts in both themes
   - Verify color contrast meets accessibility standards
   - Check dynamic color adaptation

## Additional Resources

- [Material Design Color System](https://m3.material.io/styles/color/overview)
- [Android Theme Attributes](https://developer.android.com/develop/ui/views/theming/themes)
- [Dark Theme Implementation](https://developer.android.com/develop/ui/views/theming/darktheme)

## Maintenance Notes

1. **Adding New Colors**
   - Add to both `values/colors.xml` and `values-night/colors.xml`
   - Document usage in this file
   - Update theme attributes if necessary

2. **Color Updates**
   - Test changes in both themes
   - Verify accessibility compliance
   - Update documentation accordingly
