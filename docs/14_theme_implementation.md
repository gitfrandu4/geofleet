# Documentación de Implementación de Temas

## Descripción General

Este documento detalla la implementación de temas de Material Design 3 en GeoFleet, incluyendo la gestión de recursos de color y soporte para tema oscuro.

## Implementación del Sistema de Colores

### Colores Base del Tema

La aplicación implementa un sistema completo de colores de Material Design 3 con variantes tanto para tema claro como oscuro. El sistema de colores está estructurado en dos archivos principales:

- `values/colors.xml`: Colores del tema predeterminado (claro)
- `values-night/colors.xml`: Colores específicos del tema oscuro

### Tokens de Color

Tokens de color semánticos clave utilizados en toda la aplicación:

```xml
Colores Primarios:
- md_theme_dark_primary
- md_theme_dark_onPrimary
- md_theme_dark_primaryContainer
- md_theme_dark_onPrimaryContainer

Colores Secundarios:
- md_theme_dark_secondary
- md_theme_dark_onSecondary
- md_theme_dark_secondaryContainer
- md_theme_dark_onSecondaryContainer

Colores Terciarios:
- md_theme_dark_tertiary
- md_theme_dark_onTertiary
- md_theme_dark_tertiaryContainer
- md_theme_dark_onTertiaryContainer
```

## Organización de Recursos

### 1. Colores Base (`values/colors.xml`)
- Contiene colores predeterminados del tema claro
- Incluye colores específicos de la marca
- Proporciona valores de respaldo para todos los recursos de color

### 2. Colores Nocturnos (`values-night/colors.xml`)
- Contiene colores específicos del tema oscuro
- Se aplica automáticamente cuando el tema oscuro está activo
- Mantiene nombres consistentes con los colores base

## Integración del Tema

### Implementación en Layouts

Ejemplo de uso en layouts:

```xml
<com.google.android.material.card.MaterialCardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    style="@style/Widget.Material3.CardView.Elevated">

    <!-- Contenido usando colores del tema -->
    <TextView
        android:textColor="?attr/colorOnSurface"
        android:background="?attr/colorSurface"
        ... />
</com.google.android.material.card.MaterialCardView>
```

### Mejores Prácticas

1. **Referencias de Color**
   - Siempre usar atributos del tema (`?attr/colorPrimary`) en lugar de referencias directas a colores
   - Mantener consistencia entre temas claro y oscuro

2. **Nomenclatura de Recursos**
   - Seguir las convenciones de nombres de Material Design 3
   - Usar prefijos descriptivos para colores personalizados

3. **Accesibilidad**
   - Asegurar ratios de contraste suficientes
   - Probar combinaciones de colores en ambos temas

## Problemas Comunes y Soluciones

### 1. Recursos Predeterminados Faltantes
Problema: Colores definidos en `values-night` pero faltantes en `values` base.
Solución: Siempre definir colores en ambas ubicaciones.

```xml
// En values/colors.xml
<color name="md_theme_dark_primary">#006C4C</color>

// En values-night/colors.xml
<color name="md_theme_dark_primary">#6CDBAC</color>
```

### 2. Resolución de Atributos del Tema
Problema: Las referencias directas a colores no se adaptan a los cambios de tema.
Solución: Usar atributos del tema.

```xml
<!-- Incorrecto -->
android:textColor="@color/md_theme_dark_primary"

<!-- Correcto -->
android:textColor="?attr/colorPrimary"
```

## Pruebas de Implementación del Tema

1. **Pruebas en Tiempo de Ejecución**
   ```kotlin
   // Forzar tema oscuro
   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

   // Forzar tema claro
   AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
   ```

2. **Pruebas Visuales**
   - Probar layouts en ambos temas
   - Verificar que el contraste de color cumpla con los estándares de accesibilidad
   - Comprobar la adaptación dinámica del color

## Recursos Adicionales

- [Sistema de Color de Material Design](https://m3.material.io/styles/color/overview)
- [Atributos de Tema en Android](https://developer.android.com/develop/ui/views/theming/themes)
- [Implementación del Tema Oscuro](https://developer.android.com/develop/ui/views/theming/darktheme)

## Notas de Mantenimiento

1. **Agregar Nuevos Colores**
   - Agregar tanto en `values/colors.xml` como en `values-night/colors.xml`
   - Documentar el uso en este archivo
   - Actualizar atributos del tema si es necesario

2. **Actualizaciones de Color**
   - Probar cambios en ambos temas
   - Verificar cumplimiento de accesibilidad
   - Actualizar documentación según corresponda
