# Implementación de Retrofit y OkHttp

## Descripción General

Este documento detalla la implementación y configuración de Retrofit junto con OkHttp para el manejo de peticiones HTTP en la aplicación GeoFleet.

## Configuración Base

### Dependencias Gradle

```gradle
dependencies {
    // Retrofit
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // OkHttp
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
}
```

## Implementación

### 1. Configuración del Cliente OkHttp

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) 
            HttpLoggingInterceptor.Level.BODY 
        else 
            HttpLoggingInterceptor.Level.NONE
    })
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

### 2. Configuración de Retrofit

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

### 3. Definición de Servicios API

```kotlin
interface ApiService {
    @GET("vehicles")
    suspend fun getVehicles(): Response<List<VehicleInfo>>

    @GET("vehicles/{id}")
    suspend fun getVehicleById(@Path("id") id: String): Response<VehicleInfo>
}
```

## Interceptores Personalizados

### Interceptor de Autenticación

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${getAuthToken()}")
            .build()
        return chain.proceed(request)
    }
}
```

### Interceptor de Conectividad

```kotlin
class ConnectivityInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isOnline(context)) {
            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }
}
```

## Manejo de Errores

```kotlin
sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val message: String) : NetworkResult<T>()
    class Loading<T> : NetworkResult<T>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            NetworkResult.Success(response.body()!!)
        } else {
            NetworkResult.Error(response.message())
        }
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "Error desconocido")
    }
}
```

## Uso en Repositorios

```kotlin
class VehicleRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getVehicles(): NetworkResult<List<VehicleInfo>> {
        return safeApiCall { apiService.getVehicles() }
    }

    suspend fun getVehicleById(id: String): NetworkResult<VehicleInfo> {
        return safeApiCall { apiService.getVehicleById(id) }
    }
}
```

## Mejores Prácticas

1. **Manejo de Caché**
   - Implementar estrategias de caché con OkHttp
   - Utilizar interceptores para control de caché

2. **Seguridad**
   - Usar HTTPS
   - Implementar certificados SSL pinning
   - Manejar tokens de forma segura

3. **Rendimiento**
   - Configurar timeouts apropiados
   - Implementar reintentos para peticiones fallidas
   - Monitorear el uso de memoria

## Solución de Problemas Comunes

### 1. Errores de Conexión
```kotlin
try {
    // Llamada a la API
} catch (e: SocketTimeoutException) {
    // Manejar timeout
} catch (e: UnknownHostException) {
    // Manejar error de DNS
}
```

### 2. Errores de Serialización
```kotlin
try {
    // Parseo de JSON
} catch (e: JsonSyntaxException) {
    // Manejar error de formato JSON
}
```

## Recursos Adicionales

- [Documentación oficial de Retrofit](https://square.github.io/retrofit/)
- [Documentación oficial de OkHttp](https://square.github.io/okhttp/)
- [Guía de mejores prácticas para APIs en Android](https://developer.android.com/guide/topics/network/performance) 
