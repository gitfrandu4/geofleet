# GeoFleet - Sistema de Monitoreo de Flotas

## Índice

- [GeoFleet - Sistema de Monitoreo de Flotas](#geofleet---sistema-de-monitoreo-de-flotas)
  - [Índice](#índice)
  - [Descripción General](#descripción-general)
  - [Objetivos del Proyecto](#objetivos-del-proyecto)
  - [Arquitectura y Patrones de Diseño](#arquitectura-y-patrones-de-diseño)
    - [Patrones y Principios Clave](#patrones-y-principios-clave)
    - [Manejo de Imágenes de Perfil](#manejo-de-imágenes-de-perfil)
  - [Funcionalidades Clave](#funcionalidades-clave)
  - [Tecnologías Utilizadas](#tecnologías-utilizadas)
  - [Estructura del Proyecto](#estructura-del-proyecto)
  - [Requisitos Previos](#requisitos-previos)
  - [Configuración Técnica](#configuración-técnica)
    - [1. Firebase](#1-firebase)
    - [2. Google Maps](#2-google-maps)
    - [3. Gradle](#3-gradle)
  - [Base de Datos Local y Sincronización](#base-de-datos-local-y-sincronización)
    - [Estructura de Datos](#estructura-de-datos)
    - [Flujo de Datos](#flujo-de-datos)
  - [Detalles Técnicos Destacados](#detalles-técnicos-destacados)
    - [Integración de Mapas](#integración-de-mapas)
    - [Gestión de Perfiles](#gestión-de-perfiles)
  - [Funcionalidades Futuras](#funcionalidades-futuras)
  - [Instrucciones para Ejecutar](#instrucciones-para-ejecutar)
  - [Configuración](#configuración)
    - [Archivo config.properties](#archivo-configproperties)
    - [Autenticación](#autenticación)
    - [Funcionalidades Principales](#funcionalidades-principales)
      - [Monitoreo de Vehículos](#monitoreo-de-vehículos)
      - [Interfaz de Usuario](#interfaz-de-usuario)
  - [CI/CD y Automatización (🤖)](#cicd-y-automatización-)
    - [Integración con GitHub AI](#integración-con-github-ai)
    - [Flujo de Trabajo de CI](#flujo-de-trabajo-de-ci)
    - [Comandos de AI en Pull Requests](#comandos-de-ai-en-pull-requests)
    - [Beneficios de la Integración](#beneficios-de-la-integración)
  - [Conclusiones](#conclusiones)

---

## Descripción General

**GeoFleet** es una aplicación **Android nativa** desarrollada en **Kotlin** que permite **monitorear y gestionar flotas de vehículos** en tiempo real. Combina:
- **Firebase** (Authentication, Firestore, Storage)
- **Google Maps**
- **Room Database** (para soporte offline)

Su finalidad es brindar una **vista centralizada** de la ubicación de cada vehículo, con actualizaciones en tiempo real y funciones complementarias como **gestión de perfiles**, **persistencia local**, y **sincronización de datos**.

---

## Objetivos del Proyecto

1. **Monitoreo en Tiempo Real**  
   - Actualizar la posición de la flota automáticamente con **Google Maps** y **Firestore**.

2. **Gestión Eficiente de Datos**  
   - Implementar **Room** para trabajar offline y sincronizar con Firebase Firestore cuando haya conexión.

3. **Escalabilidad y Extensibilidad**  
   - Usar arquitectura **Clean** y el patrón **MVVM**, de forma modular, para facilitar la adición de nuevas funciones.

4. **Seguridad y Privacidad**  
   - Integrar **Firebase Authentication** para el control de acceso de usuarios y proteger datos sensibles.

5. **Experiencia de Usuario Óptima**  
   - Implementar **Material Design 3** ofreciendo una interfaz amigable, limpia y adaptable a distintos dispositivos.

---

## Arquitectura y Patrones de Diseño

El proyecto se ha diseñado siguiendo **MVVM** y elementos de **Clean Architecture**:

- **Model (Dominio & Datos)**  
  Representa la capa de datos (POJOs, repositorios, uso de Room, etc.).

- **View (UI)**  
  Actividades y Fragments que interactúan directamente con el usuario y muestran la información.

- **ViewModel (Lógica de Presentación)**  
  Gestiona la comunicación entre la capa de datos y la vista, manejando estados y eventos.

- **Repositories**  
  Se encargan de orquestar la obtención y el envío de datos a fuentes como **Room**, **APIs** y **Firebase**.

### Patrones y Principios Clave

- **Repository Pattern**: Abstrae la fuente de datos real ante la UI.  
- **Observer Pattern**: Uso de **LiveData** y **Flow** para actualizar la UI al cambiar datos.  
- **Dependency Injection** (opcional): Factible con **Hilt** o **Koin**.  
- **SOLID**: Se promueve responsabilidad única y separación de intereses.

### Manejo de Imágenes de Perfil
El proyecto implementa un sistema robusto para el manejo de imágenes de perfil usando un componente personalizado `ProfileImageView` que:
- Gestiona automáticamente la carga de imágenes desde Firebase Storage
- Proporciona visualización circular de imágenes
- Maneja actualizaciones en tiempo real
- Implementa fallbacks y placeholders
- Mantiene consistencia en toda la aplicación

---

## Funcionalidades Clave

- **🗺️ Mapa en Tiempo Real**  
  Muestra en Google Maps la posición de los vehículos.

- **💾 Base de Datos Local**  
  Uso de **Room Database** para acceso sin conexión.

- **🔄 Sincronización en Tiempo Real**  
  Integración con **Firebase Firestore** para actualizaciones instantáneas.

- **📱 Gestión de Perfiles**  
  Subida y manejo de imágenes en **Firebase Storage** y login con **Firebase Authentication**.

- **🌐 Interfaz Moderna**  
  Basada en **Material Design 3**, con navegación limpia y soporte para gestos de Android.

- **👤 Gestión Avanzada de Perfiles**
  - Edición de datos personales (nombre, cargo, género)
  - Selector de fecha de nacimiento localizado
  - Sistema robusto de manejo de imágenes de perfil
  - Sincronización en tiempo real con Firebase

---

## Tecnologías Utilizadas

- **Kotlin**  
- **Firebase** (Authentication, Firestore, Storage)  
- **Google Maps SDK**  
- **Jetpack Components** (Room, Navigation, ViewModel, LiveData, ViewBinding)  
- **Coroutines & Flow**  
- **Material Design 3**  
- **Retrofit & OkHttp** (para posibles integraciones con APIs externas)  
- **Glide** (carga de imágenes)

---

## Estructura del Proyecto

```
GeoFleet/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/geofleet/
│   │   │   │   ├── data/          # Modelos, DAO y repositorios
│   │   │   │   ├── ui/            # Activities y Fragments (Vistas)
│   │   │   │   ├── service/       # Servicios Firebase y lógica de negocio
│   │   │   │   └── utils/         # Utilidades y extensiones comunes
│   │   ├── res/                   # Recursos XML (layouts, drawables, strings)
│   │   ├── AndroidManifest.xml    # Configuración de permisos y actividades
├── build.gradle                   # Configuración de dependencias y plugins
├── docs/                          # Documentación técnica y archivos de soporte
└── proguard-rules.pro             # Configuración de optimización y minificación
```

---

---

## Requisitos Previos

- **Software**  
  - Android Studio (Arctic Fox o superior)  
  - JDK 8+  
  - Google Play Services  

- **Servicios**  
  - Cuenta Firebase (Authentication, Firestore y Storage activos)  
  - API Key de Google Maps  

---

## Configuración Técnica

### 1. Firebase

1. Crear un proyecto en la [Firebase Console](https://console.firebase.google.com/).  
2. Descargar `google-services.json` y colocarlo en la carpeta `app/`.  
3. Habilitar:
   - **Authentication** (para control de acceso)  
   - **Firestore** (para almacenar y sincronizar datos)  
   - **Storage** (para almacenar imágenes)  

### 2. Google Maps

1. Obtener la API Key desde [Google Cloud Console](https://console.cloud.google.com/).  
2. Agregarla a `local.properties`:
   ```
   MAPS_API_KEY=tu_api_key_aqui
   ```

### 3. Gradle

Asegúrate de incluir las siguientes dependencias en `build.gradle`:

```
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'com.google.gms.google-services'
}

android {
    // ...
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:33.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    implementation 'com.google.firebase:firebase-storage'
    implementation 'com.google.android.gms:play-services-maps:18.2.0'

    // Jetpack & UI
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'

    // Glide
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    implementation 'androidx.room:room-runtime:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'

    // (Opcional) Retrofit & OkHttp
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.10.0'
}
```

---

## Base de Datos Local y Sincronización

El proyecto implementa una robusta estrategia de sincronización:

### Estructura de Datos
```kotlin
@Entity(tableName = "vehicle_positions")
data class VehiclePositionEntity(
    @PrimaryKey val vehicleId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)
```

### Flujo de Datos
1. **Carga Inicial**:
   - Carga de IDs desde configuración
   - Obtención de posiciones desde API
   - Almacenamiento en Room
   - Actualización en Firestore
   - Actualización de UI

2. **Actualizaciones**:
   - Cancelación de trabajos en curso
   - Obtención de nuevas posiciones
   - Actualización de almacenamiento local y en la nube
   - Actualización de UI
   - Actualización de contadores

---

## Detalles Técnicos Destacados

### Integración de Mapas

- Uso de **Google Maps SDK**.  
- Marcadores personalizados usando layouts e **Inflate**:
  ```
  fun createCustomMarker(): BitmapDescriptor {
      val view = LayoutInflater.from(context).inflate(R.layout.marker_layout, null)
      val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
      view.draw(Canvas(bitmap))
      return BitmapDescriptorFactory.fromBitmap(bitmap)
  }
  ```

### Gestión de Perfiles

- Subida y carga de imágenes con **Firebase Storage**.  
- Ejemplo de carga con **Glide**:
  ```
  Glide.with(this)
      .load(photoUrl)
      .circleCrop()
      .placeholder(R.drawable.ic_person_placeholder)
      .error(R.drawable.ic_person_error)
      .into(this)
  ```

---

## Funcionalidades Futuras

1. **Clustering** de marcadores.  
2. **Filtros avanzados** (estado, ubicación).  
3. **Estados de vehículo** con distintos colores de marcadores.

---

## Instrucciones para Ejecutar

1. **Clona el repositorio**:
   ```
   git clone https://github.com/gitfrandu4/geofleet.git
   ```
2. **Abre el proyecto en Android Studio**.
3. **Configura** `google-services.json` y **MAPS_API_KEY** en `local.properties`.
4. **Compila y ejecuta** en emulador o dispositivo.

---

## Configuración

### Archivo config.properties

```
# URL base de la API
BASE_URL=https://api.example.com/

# IDs de vehículos a monitorear
vehicle.ids=1528,1793

# Token de autenticación para la API
API_TOKEN=your_api_token
```

### Autenticación

- Se requiere **Firebase Authentication** para acceso a la app.  
- Para llamadas a APIs externas, se usa un token Bearer definido en `config.properties`.

### Funcionalidades Principales

#### Monitoreo de Vehículos
- Visualiza en tiempo real las posiciones.  
- Soporta actualización manual con un FAB o menú.  
- Filtra coordenadas inválidas y persiste localmente la información.  

#### Interfaz de Usuario
- Navegación con un **Navigation Drawer** o **Bottom Navigation** (dependiendo de la configuración).  
- Alertas (Snackbars, Toasts) y reintentos en caso de errores.  
- Diseño moderno con **Material Design 3**.

---

## CI/CD y Automatización (🤖)

El proyecto implementa un sistema completo de CI/CD usando GitHub Actions en combinación con GitHub AI para mejorar continuamente la calidad del código:

### Integración con GitHub AI
![GitHub Actions con AI](docs/images/github-actions-ai.png)

### Flujo de Trabajo de CI
![Android CI Workflow](docs/images/android_ci_workflow.png)

Como se muestra en la imagen, el flujo de trabajo de CI incluye:
- ✅ **Verificación de Código**: Análisis automático del código mediante ktlint y Android Lint
- 📊 **Generación de Reportes**: Creación y almacenamiento de informes de análisis
- 🔄 **Integración Continua**: Verificación automática en cada pull request

El proyecto utiliza una innovadora combinación de GitHub Actions y AI para:
- **Revisión Automática de Código**: Cada pull request es analizado por AI para detectar posibles mejoras y problemas.
- **Sugerencias de Optimización**: La AI proporciona recomendaciones específicas para mejorar el código.
- **Detección de Errores**: Identificación temprana de problemas potenciales antes de que lleguen a producción.

### Comandos de AI en Pull Requests
Los desarrolladores pueden utilizar comandos especiales en los comentarios:
- `/review` - Solicita una revisión técnica detallada
- `/summary` - Genera un resumen técnico del cambio
- `/suggest` - Obtiene sugerencias de mejora específicas

### Beneficios de la Integración
- **Mejora Continua**: Cada PR recibe feedback automático para mejorar la calidad del código
- **Aprendizaje Activo**: Los desarrolladores reciben sugerencias educativas sobre mejores prácticas
- **Detección Temprana**: Los problemas se identifican y corrigen antes de llegar a la rama principal
- **Consistencia**: Asegura que todo el código siga los mismos estándares de calidad

---

## Conclusiones

El desarrollo de **GeoFleet** ha sido un viaje enriquecedor, especialmente al transitar desde un entorno de desarrollo web hacia el ecosistema de aplicaciones móviles nativas en Android. A continuación, se destacan algunas conclusiones clave:

1. **Desafíos de Integración**: 
   - Como desarrollador experimentado en web, uno de los principales desafíos fue adaptar ciertas características al entorno Android, como la gestión de permisos y la optimización de la interfaz de usuario para diferentes dispositivos y resoluciones.
   - La integración de servicios nativos como Firebase y Google Maps presentó una curva de aprendizaje, pero resultó en una aplicación más robusta y eficiente.

2. **Satisfacción del Resultado Final**:
   - Comparado con una web adaptada al móvil, la aplicación nativa ofrece una experiencia de usuario más fluida y rápida, aprovechando al máximo las capacidades del dispositivo.
   - La capacidad de trabajar offline y la sincronización en tiempo real son características que destacan en la aplicación nativa, proporcionando un valor añadido significativo.

3. **Uso de Inteligencia Artificial**:
   - La integración de herramientas de IA en el flujo de desarrollo ha sido fundamental. Desde el uso de **Cursor** para mejorar la eficiencia del código, hasta **OpenAI** para revisiones automáticas y **Copilot** para sugerencias de código, cada paso del desarrollo se ha beneficiado de estas tecnologías.
   - El sistema de CI/CD automatizado con GitHub Actions y AI ha permitido mantener altos estándares de calidad, detectando y corrigiendo problemas antes de que lleguen a producción.

4. **Enfoque Nativo Android**:  
   - Al basarse en Kotlin y el ecosistema oficial de Android (Jetpack, Google Maps SDK), se aprovechan las optimizaciones y buenas prácticas de la plataforma.  
   - Permite acceso directo a APIS específicas de Android y una personalización completa de la UI según *Material Design*.

5. **Clean Architecture y MVVM**:  
   - Facilitan la separación de responsabilidades y el escalado de la app, garantizando que nuevas funcionalidades (p. ej. notificaciones push, nuevos módulos de datos) puedan integrarse sin romper la estructura existente.  
   - La capa de datos se abstrae mediante repositorios, lo que permite intercambiar Firebase por cualquier otro backend sin grandes cambios en la UI.

6. **Offline First**:  
   - La inclusión de **Room Database** asegura que la aplicación siga funcionando en entornos con poca o nula conectividad, lo que es esencial en contextos industriales o de logística donde la cobertura de red puede ser limitada.

7. **Sincronización en Tiempo Real**:  
   - El uso de **Firebase Firestore** proporciona actualizaciones inmediatas a todos los clientes conectados, mejorando la coordinación de la flota y la toma de decisiones en tiempo real.

8. **Mejoras de Rendimiento**:  
   - Se utilizan **Coroutines** de Kotlin para un manejo eficiente de hilos y evitar bloqueos en la UI.  

9. **Futuras Expansiones**:  
   - El proyecto está preparado para integrar otras APIs (p. ej. de tráfico, clima, rutas) usando **Retrofit & OkHttp**; esto abre posibilidades para planificar y optimizar rutas, obtener datos externos, etc.  
   - El soporte a **Clustering de Marcadores** mejoraría la visualización de grandes flotas en el mapa.  

En resumen, el desarrollo de **GeoFleet** no solo ha resultado en una aplicación móvil nativa de alta calidad, sino que también ha enriquecido mi experiencia como desarrollador, ampliando mis habilidades y conocimientos en el ecosistema Android.
