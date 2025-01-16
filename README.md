# GeoFleet - Sistema de Monitoreo de Flotas

## Índice

- [GeoFleet - Sistema de Monitoreo de Flotas](#geofleet---sistema-de-monitoreo-de-flotas)
  - [Índice](#índice)
  - [Descripción General](#descripción-general)
  - [Objetivos del Proyecto](#objetivos-del-proyecto)
  - [Arquitectura y Patrones de Diseño](#arquitectura-y-patrones-de-diseño)
    - [Patrones y Principios Clave](#patrones-y-principios-clave)
  - [Tecnologías y Herramientas](#tecnologías-y-herramientas)
  - [Estructura del Proyecto](#estructura-del-proyecto)
  - [Requisitos Previos](#requisitos-previos)
  - [Configuración Técnica](#configuración-técnica)
    - [1. Firebase](#1-firebase)
    - [2. Google Maps](#2-google-maps)
    - [3. Gradle](#3-gradle)
  - [Base de Datos Local y Sincronización](#base-de-datos-local-y-sincronización)
  - [CI/CD y Automatización](#cicd-y-automatización)

---

## Descripción General

**GeoFleet** es una aplicación Android desarrollada en **Kotlin** que permite monitorear y gestionar flotas de vehículos en tiempo real. Para ello hace uso de tecnologías modernas como **Firebase** y **Google Maps**, asegurando una experiencia ágil y confiable tanto en entornos online como offline.

Este proyecto está diseñado bajo principios de **arquitectura limpia** (Clean Architecture) y patrones **MVVM**, enfatizando la fácil mantenibilidad del código y la escalabilidad futura. Asimismo, integra buenas prácticas de sincronización en tiempo real y almacenamiento local para garantizar la disponibilidad de datos incluso sin conexión.

---

## Objetivos del Proyecto

1. **Monitoreo en Tiempo Real**  
   Proporcionar una vista actualizada de la posición de la flota sobre un mapa, con datos de ubicación que se refrescan automáticamente.

2. **Gestión Eficiente de Datos**  
   Utilizar una **base de datos local (Room)** para un acceso rápido y offline, sincronizando con **Firebase Firestore** cuando la red esté disponible.

3. **Escalabilidad y Extensibilidad**  
   Diseñar un sistema con arquitectura modular (MVVM y repositorios) que facilite la incorporación de nuevas funcionalidades.

4. **Seguridad y Privacidad**  
   Implementar **Firebase Authentication** para controlar el acceso a la aplicación y **token** para la API cuando se requiera, garantizando la protección de datos.

5. **Experiencia de Usuario Óptima**  
   Emplear principios de **Material Design 3**, proporcionando una interfaz intuitiva, adaptada a distintos dispositivos y con flujos de navegación limpios.

---

## Arquitectura y Patrones de Diseño

Para asegurar la mantenibilidad y la escalabilidad, GeoFleet está organizado siguiendo el patrón **MVVM (Model-View-ViewModel)** y algunos principios de **Clean Architecture**:

- **Model (Dominio & Datos)**  
  Representa la información de la aplicación, ya sea proveniente de la base de datos local, de la capa de red (APIs) o de los servicios de Firebase.

- **View (UI)**  
  Compuesta por Activities, Fragments y elementos de la interfaz de usuario; expone los datos al usuario y reacciona a sus interacciones.

- **ViewModel (Lógica de Presentación)**  
  Actúa como puente entre la UI y los modelos, gestionando el estado de la vista y orquestando las operaciones de negocio y datos.

- **Repositories**  
  Son responsables de orquestar la obtención de datos, ya sea desde la **Room Database**, la **API** o **Firebase**, aplicando lógica adicional cuando sea necesario (cache, validaciones, transformaciones, etc.).

### Patrones y Principios Clave

- **Repository Pattern**: Abstrae la fuente de datos para que la UI no dependa de la implementación concreta (DB, Firebase, API).  
- **Observer Pattern**: Utiliza **LiveData** y **Flow** para notificar a las vistas ante cambios en la fuente de datos.  
- **Dependency Injection** (opcional): Puede integrarse con librerías como **Hilt** o **Koin** para inyección de dependencias.  
- **SOLID**: Se promueve la responsabilidad única y la separación de intereses en todas las capas.

---

## Tecnologías y Herramientas

```kotlin
// Kotlin es el lenguaje principal de desarrollo
```

```
firebase
// Firebase ofrece autenticación, Firestore y Storage
```

```
material
// Diseño basado en Material Design 3 para UI moderna
```

```
room
// Base de datos local con soporte offline
```

---

## Estructura del Proyecto

```bash
GeoFleet/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/geofleet/
│   │   │   │   ├── data/          # Modelos, DAO y repositorios
│   │   │   │   ├── ui/            # Activities y Fragments (Vistas)
│   │   │   │   ├── service/       # Servicios Firebase y lógica adicional
│   │   │   │   └── utils/         # Utilidades comunes (helpers, extensiones)
│   │   ├── res/                   # Recursos XML (layouts, drawables, strings)
│   │   ├── AndroidManifest.xml    # Configuración de permisos y actividades
├── build.gradle                   # Configuración de dependencias y plugins
├── docs/                          # Documentación técnica y assets
└── proguard-rules.pro             # Configuración de optimización y minificación
```

---

## Requisitos Previos

```bash
# Software necesario
Android Studio (versión Arctic Fox o superior)
JDK 8+
Google Play Services

# Servicios necesarios
Cuenta Firebase habilitada con Authentication, Firestore y Storage
API Key de Google Maps
```

---

## Configuración Técnica

### 1. Firebase

```bash
# Pasos para configuración Firebase
1. Crear proyecto en Firebase Console
2. Descargar archivo google-services.json
3. Habilitar Authentication, Firestore y Storage
```

### 2. Google Maps

```bash
# Configuración de Google Maps
1. Obtener API Key en Google Cloud Console
2. Agregar API Key en local.properties

MAPS_API_KEY=tu_api_key_aqui
```

### 3. Gradle

```groovy
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.google.gms.google-services'
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:33.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    implementation 'androidx.room:room-runtime:2.6.1'
}
```

---

## Base de Datos Local y Sincronización

```bash
# Modelo Room para almacenamiento local
```
kotlin
@Entity(tableName = "vehicle_positions")
data class VehiclePositionEntity(
    @PrimaryKey val vehicleId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
```

# DAO para acceso a la base de datos
```
kotlin
@Dao
interface VehiclePositionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(positions: List<VehiclePositionEntity>)

    @Query("SELECT * FROM vehicle_positions ORDER BY timestamp DESC")
    fun getAllPositions(): Flow<List<VehiclePositionEntity>>
}
```

---

## Funcionalidades Futuras

```bash
# Funcionalidades a implementar
1. Clustering: Agrupación de marcadores
2. Filtros avanzados
3. Estado del vehículo según color
```

---

## CI/CD y Automatización

```bash
# Workflow de CI/CD con GitHub Actions
```
yml
name: Android CI/CD

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - name: Setup JDK
      uses: actions/setup-java@v3
      with:
        java-version: 17
    - name: Checkout code
      uses: actions/checkout@v3
    - name: Build with Gradle
      run: ./gradlew build
```
