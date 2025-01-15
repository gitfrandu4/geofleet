# Profile Image Handling

## Overview
GeoFleet implements a robust profile image handling system that allows users to upload and update their profile pictures. The system supports both camera capture and gallery selection, with proper permission handling and Firebase Storage integration.

## Features
- 📸 Camera capture support
- 🖼️ Gallery image selection
- 🔒 Runtime permission handling
- 💾 Firebase Storage integration
- 🔄 Progress tracking
- ⚡ Efficient image management

## Implementation Details

### Storage Structure
```
firebase-storage/
└── users/
    └── {userId}/
        └── profile.jpg
```

### Permission Handling
The app handles the following permissions:
- `CAMERA` - For capturing new photos
- `READ_MEDIA_IMAGES` (Android 13+) - For gallery access
- `READ_EXTERNAL_STORAGE` (Android 12 and below) - For gallery access

### Image Upload Process
1. **Image Selection**
   - User can choose between camera or gallery
   - Appropriate permissions are checked and requested if needed
   - Selected image is displayed in the UI immediately

2. **Upload Preparation**
   - Image is prepared for upload to Firebase Storage
   - A unique path is generated based on user ID
   - Previous profile image is automatically overwritten

3. **Upload Process**
   - Progress is tracked and displayed to user
   - Upload state is managed (button disabled during upload)
   - Success/failure feedback is provided via Snackbar

4. **Profile Update**
   - Upon successful upload, download URL is obtained
   - Profile document in Firestore is updated with new image URL
   - UI is updated to reflect changes

## Code Examples

### Image Selection
```kotlin
private fun openImagePicker() {
    val options = arrayOf("Tomar foto", "Elegir de la galería")
    AlertDialog.Builder(requireContext())
        .setTitle("Seleccionar imagen")
        .setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> checkGalleryPermission()
            }
        }
        .show()
}
```

### Upload Process
```kotlin
private fun uploadImage(imageRef: StorageReference, uri: Uri) {
    val uploadTask = imageRef.putFile(uri)
    
    uploadTask.addOnProgressListener { taskSnapshot ->
        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
        showProgress("Subiendo imagen: ${progress.toInt()}%")
    }.addOnSuccessListener {
        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            saveUserProfile(downloadUri.toString())
            showSuccess("Imagen actualizada correctamente")
        }
    }
}
```

## Error Handling
- Permission denials
- Upload failures
- Invalid image files
- Network issues
- Storage quota exceeded

## Best Practices
1. Always check permissions before accessing camera or gallery
2. Show upload progress to users
3. Provide clear feedback for success/failure
4. Clean up temporary files after upload
5. Handle configuration changes properly
6. Implement proper error handling
7. Use appropriate image compression when needed

## Firebase Storage Rules
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
``` 
