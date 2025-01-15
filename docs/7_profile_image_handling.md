# Profile Image Handling

## Overview
GeoFleet implements a robust profile image handling system that allows users to upload and update their profile pictures. The system uses a custom `ProfileImageView` component that automatically handles loading and displaying profile images from Firebase Storage.

## Features
- 🔄 Automatic profile image loading
- 🖼️ Circular image display
- 🔒 Firebase integration
- 📱 Reusable component
- 🎨 Default placeholder support
- 👤 Real-time updates

## Implementation Details

### Storage Structure
```
firebase-storage/
└── users/
    └── {userId}/
        └── profile.jpg
```

### Component Structure
The `ProfileImageView` is a custom view that extends `AppCompatImageView` and handles all profile image loading logic:

```kotlin
class ProfileImageView : AppCompatImageView {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var snapshotListener: ListenerRegistration? = null
}
```

### Key Features

1. **Automatic Loading**
   - Loads profile image immediately upon initialization
   - Listens for real-time updates to profile changes
   - Falls back to default placeholder if no image is available

2. **Firebase Integration**
   - Uses Firestore to track profile updates
   - Integrates with Firebase Storage for image loading
   - Maintains real-time snapshot listeners

3. **Error Handling**
   - Graceful fallback to placeholder image
   - Comprehensive error logging
   - Proper cleanup of listeners

### Usage

1. **In Layout Files**
```xml
<com.example.geofleet.ui.components.ProfileImageView
    android:id="@+id/nav_header_image"
    android:layout_width="64dp"
    android:layout_height="64dp" />
```

2. **In Navigation Header**
```kotlin
headerView.findViewById<ProfileImageView>(R.id.nav_header_image)?.let { profileImageView ->
    profileImageView.startListeningToProfileChanges()
}
```

### Implementation Details

1. **Initialization**
```kotlin
init {
    setImageResource(R.drawable.ic_person)
    loadExistingProfileImage()
}
```

2. **Loading Profile Image**
```kotlin
private fun loadExistingProfileImage() {
    auth.currentUser?.let { user ->
        db.collection(UserProfile.COLLECTION_NAME)
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                if (!profile?.photoUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(profile?.photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(this)
                }
            }
    }
}
```

3. **Real-time Updates**
```kotlin
fun startListeningToProfileChanges() {
    snapshotListener?.remove()
    auth.currentUser?.let { user ->
        snapshotListener = db.collection(UserProfile.COLLECTION_NAME)
            .document(user.uid)
            .addSnapshotListener { snapshot, e ->
                // Handle profile updates
            }
    }
}
```

### Best Practices
1. Always use `circleCrop()` for consistent circular display
2. Set placeholder and error images
3. Clean up listeners in `onDetachedFromWindow()`
4. Handle all possible error cases
5. Provide detailed logging for debugging

### Dependencies
```gradle
implementation 'com.github.bumptech.glide:glide:4.16.0'
implementation 'com.google.firebase:firebase-storage-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'
```

### Notes
- The component automatically handles lifecycle events
- Uses Glide for efficient image loading and caching
- Maintains consistency across the app
- Provides real-time updates when profile changes 
