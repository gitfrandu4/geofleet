# Geocoding Implementation

## Overview
The GeoFleet application now includes geocoding functionality to convert vehicle coordinates into human-readable addresses. This feature enhances user experience by displaying location information in a more understandable format.

## Key Components

### GeocodedAddress Entity
```kotlin
@Entity(tableName = "geocoded_addresses")
data class GeocodedAddress(
    val coordinates: String,  // Format: "lat,lng"
    val address: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### GeocodingRepository
The `GeocodingRepository` class handles the geocoding process with the following features:
- Uses Android's Geocoder API for coordinate-to-address conversion
- Implements local caching to minimize API calls
- Cache validity period of 7 days
- Fallback to raw coordinates if geocoding fails

### Caching Strategy
- Addresses are cached in a local Room database
- Each cached entry includes:
  - Coordinates string
  - Geocoded address
  - Timestamp for cache invalidation
- Automatic cleanup of expired cache entries

### Error Handling
- Graceful fallback to raw coordinates if:
  - Geocoding service is unavailable
  - No address found for coordinates
  - Network errors occur
- Comprehensive logging for debugging

## Implementation Details

### Database Integration
- Added `GeocodedAddress` entity to the main Room database
- Database version updated to handle the new entity
- Implemented `GeocodedAddressDao` for database operations

### Vehicle List Display
- Updated `VehicleAdapter` to show:
  - Loading state while fetching address
  - Geocoded address when available
  - Fallback to coordinates if needed
- Added location icon for better visual feedback

### Performance Considerations
- Asynchronous geocoding using Kotlin coroutines
- Local caching to reduce API calls
- Batch cleanup of expired cache entries

## Configuration
- No additional API keys required (uses Android's built-in Geocoder)
- Cache duration configurable in `GeocodingRepository`
- Logging enabled for debugging purposes

## Future Improvements
- Implement batch geocoding for multiple locations
- Add reverse geocoding support
- Enhance address format customization
- Consider implementing offline geocoding 
