package com.example.geofleet.ui.components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.example.geofleet.R
import com.example.geofleet.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProfileImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var snapshotListener: ListenerRegistration? = null

    init {
        // Set default image
        setImageResource(R.drawable.ic_person)
        // Load existing profile image if available
        loadExistingProfileImage()
    }

    private fun loadExistingProfileImage() {
        auth.currentUser?.let { user ->
            Log.d(TAG, "Loading profile for user: ${user.uid}")
            db.collection(UserProfile.COLLECTION_NAME)
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profile = document.toObject(UserProfile::class.java)
                        Log.d(TAG, "Profile loaded: $profile")
                        if (!profile?.photoUrl.isNullOrEmpty()) {
                            Log.d(TAG, "Loading existing profile image: ${profile?.photoUrl}")
                            Glide.with(this)
                                .load(profile?.photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(this)
                        } else {
                            Log.d(TAG, "No photo URL in profile")
                        }
                    } else {
                        Log.d(TAG, "No profile document exists")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading existing profile image", e)
                }
        } ?: Log.d(TAG, "No authenticated user")
    }

    fun startListeningToProfileChanges() {
        // Remove existing listener if any
        snapshotListener?.remove()
        
        auth.currentUser?.let { user ->
            Log.d(TAG, "Starting to listen for profile changes for user: ${user.uid}")
            
            snapshotListener = db.collection(UserProfile.COLLECTION_NAME)
                .document(user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Error listening for profile changes", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject(UserProfile::class.java)
                        Log.d(TAG, "Profile updated: $profile")
                        
                        if (!profile?.photoUrl.isNullOrEmpty()) {
                            Log.d(TAG, "Loading updated profile image: ${profile?.photoUrl}")
                            Glide.with(this)
                                .load(profile?.photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .into(this)
                        } else {
                            Log.d(TAG, "No photo URL in updated profile")
                            setImageResource(R.drawable.ic_person)
                        }
                    } else {
                        Log.d(TAG, "Profile document does not exist")
                        setImageResource(R.drawable.ic_person)
                    }
                }
        } ?: run {
            Log.d(TAG, "No authenticated user found")
            setImageResource(R.drawable.ic_person)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        snapshotListener?.remove()
    }

    companion object {
        private const val TAG = "ProfileImageView"
    }
} 
