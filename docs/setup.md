# GeoFleet - Configuración del Proyecto

Este documento describe los pasos necesarios para configurar el proyecto GeoFleet, una aplicación Android desarrollada en Kotlin que utiliza Firebase y Google Maps.

## 1. Configuración Inicial

### 1.1 Configuración del build.gradle (Proyecto)

```
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.2'
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22'
        classpath 'com.google.gms:google-services:4.4.1'
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

### 1.2 Configuración del settings.gradle

```
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GeoFleet"
include ':app'
```

### 1.3 Configuración del build.gradle (App)

```
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.google.gms.google-services'
}

android {
    namespace 'com.example.geofleet'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.geofleet"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    // ... otras configuraciones ...
}

dependencies {
    // Kotlin
    implementation 'androidx.core:core-ktx:1.12.0'
    
    // UI Components
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.2')
    implementation 'com.google.firebase:firebase-analytics-ktx'
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-messaging-ktx'
    
    // Google Maps
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

## 2. Configuración de Firebase

### 2.1 Crear Proyecto en Firebase
1. Ir a [Firebase Console](https://console.firebase.google.com)
2. Crear un nuevo proyecto o seleccionar uno existente
3. Agregar una aplicación Android:
   - Package name: `com.example.geofleet`
   - Descargar el archivo `google-services.json`
   - Colocar el archivo en la carpeta `app/` del proyecto

### 2.2 Estructura del google-services.json
El archivo debe contener la siguiente estructura básica:

```
{
  "project_info": {
    "project_number": "123456789000",
    "project_id": "tu-proyecto-12345",
    "storage_bucket": "tu-proyecto-12345.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789000:android:abc123def456",
        "android_client_info": {
          "package_name": "com.example.geofleet"
        }
      }
      // ... resto de la configuración ...
    }
  ]
}
```

## 3. Configuración del AndroidManifest.xml

El archivo AndroidManifest.xml debe incluir los permisos necesarios y las configuraciones para Google Maps y Firebase:

```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.example.geofleet">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <application>
        <!-- Configuración de Google Maps API Key -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="TU_API_KEY_AQUI" />

        <!-- Configuración de Firebase Cloud Messaging -->
        <service
            android:name=".service.MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
</manifest>
```

## 4. Pasos Pendientes

1. Obtener y configurar la API Key de Google Maps
2. Implementar la autenticación de usuarios en Firebase
3. Configurar Firebase Cloud Messaging para notificaciones
4. Implementar la integración con Google Maps
5. Configurar Firestore para el almacenamiento de datos

## 5. Notas Importantes

- No compartir el archivo `google-services.json` en repositorios públicos
- Mantener las API keys seguras y no exponerlas en el código
- Asegurarse de habilitar los servicios necesarios en la consola de Firebase
- Verificar la compatibilidad de versiones entre las diferentes dependencias

## 6. Configuración de Jetpack Compose y View Binding

Se han agregado las siguientes configuraciones al archivo `app/build.gradle`:

```
android {
    // ... configuración existente ...

    buildFeatures {
        compose true
        viewBinding true
    }

    composeOptions {
        kotlinCompilerExtensionVersion '1.5.8'
    }
}

dependencies {
    // Compose
    implementation platform('androidx.compose:compose-bom:2024.02.00')
    implementation 'androidx.activity:activity-compose:1.8.2'
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    debugImplementation 'androidx.compose.ui:ui-tooling'
    debugImplementation 'androidx.compose.ui:ui-test-manifest'
}

Esta configuración permite:
- Usar Jetpack Compose para la UI moderna
- Usar View Binding para las vistas XML tradicionales
- Tener interoperabilidad entre ambos sistemas de UI
