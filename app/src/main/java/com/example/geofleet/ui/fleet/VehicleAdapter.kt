package com.example.geofleet.ui.fleet

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geofleet.R
import com.example.geofleet.data.repository.GeocodingRepository
import com.example.geofleet.databinding.ItemVehicleBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private const val TAG = "VehicleAdapter"

class VehicleAdapter(
        private val onProfileClick: (String) -> Unit,
        private val onMapClick: (String) -> Unit,
        private val geocodingRepository: GeocodingRepository
) : ListAdapter<VehicleInfo, VehicleAdapter.VehicleViewHolder>(VehicleDiffCallback()) {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VehicleViewHolder(private val binding: ItemVehicleBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.profileButton.setOnClickListener {
                val position = layoutPosition
                if (position != RecyclerView.NO_POSITION) {
                    onProfileClick(getItem(position).id)
                }
            }

            binding.mapButton.setOnClickListener {
                val position = layoutPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMapClick(getItem(position).id)
                }
            }

            binding.deleteButton.setOnClickListener {
                val position = layoutPosition
                if (position != RecyclerView.NO_POSITION) {
                    showDeleteConfirmation(getItem(position))
                }
            }
        }

        private fun showDeleteConfirmation(vehicle: VehicleInfo) {
            MaterialAlertDialogBuilder(binding.root.context)
                    .setTitle(R.string.delete_vehicle)
                    .setMessage(R.string.delete_vehicle_confirmation)
                    .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                    .setPositiveButton(R.string.delete) { dialog, _ ->
                        deleteVehicle(vehicle)
                        dialog.dismiss()
                    }
                    .show()
        }

        private fun deleteVehicle(vehicle: VehicleInfo) {
            Log.d(TAG, "🗑️ Deleting vehicle: ${vehicle.id}")
            binding.deleteButton.isEnabled = false

            db.collection("vehicles")
                    .document(vehicle.id)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ Vehicle deleted successfully: ${vehicle.id}")
                        val currentList = currentList.toMutableList()
                        val position = currentList.indexOfFirst { it.id == vehicle.id }
                        if (position != -1) {
                            currentList.removeAt(position)
                            submitList(currentList)
                        }
                        Snackbar.make(
                                        binding.root,
                                        binding.root.context.getString(
                                                R.string.vehicle_deleted,
                                                vehicle.id
                                        ),
                                        Snackbar.LENGTH_LONG
                                )
                                .show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Error deleting vehicle: ${vehicle.id}", e)
                        binding.deleteButton.isEnabled = true
                        Snackbar.make(
                                        binding.root,
                                        R.string.error_deleting_vehicle,
                                        Snackbar.LENGTH_LONG
                                )
                                .show()
                    }
        }

        fun bind(vehicle: VehicleInfo) {
            Log.d(TAG, "Binding vehicle: ${vehicle.id}")

            binding.apply {
                vehicleId.text = root.context.getString(R.string.vehicle_format, vehicle.name)

                // Load vehicle image
                if (vehicle.photoUrl != null) {
                    Log.d(TAG, "Loading photo for vehicle ${vehicle.id}: ${vehicle.photoUrl}")
                    Glide.with(itemView.context)
                            .load(vehicle.photoUrl)
                            .placeholder(R.drawable.vehicle_list_placeholder)
                            .error(R.drawable.vehicle_list_placeholder)
                            .centerCrop()
                            .into(binding.vehicleImage)
                } else {
                    Log.d(TAG, "No photo URL for vehicle ${vehicle.id}, using placeholder")
                    vehicleImage.setImageResource(R.drawable.vehicle_profile_placeholder)
                }

                // Show last position with geocoded address
                if (vehicle.lastPosition != null) {
                    Log.d(
                            TAG,
                            "Last position for vehicle ${vehicle.id}: lat=${vehicle.lastPosition.latitude}, lon=${vehicle.lastPosition.longitude}"
                    )

                    // Show loading state
                    lastPosition.text = root.context.getString(R.string.loading_address)

                    // Launch coroutine to get and show the address
                    (root.context as? LifecycleOwner)?.lifecycleScope?.launch {
                        try {
                            Log.d(TAG, "Fetching address for vehicle ${vehicle.id}")
                            val address =
                                    geocodingRepository.getAddressFromCoordinates(
                                            vehicle.lastPosition.latitude,
                                            vehicle.lastPosition.longitude
                                    )
                            Log.d(TAG, "Got address for vehicle ${vehicle.id}: $address")
                            lastPosition.text = address
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting address for vehicle ${vehicle.id}", e)
                            // On error, show coordinates as fallback
                            lastPosition.text =
                                    root.context.getString(
                                            R.string.last_position_format,
                                            vehicle.lastPosition.latitude,
                                            vehicle.lastPosition.longitude
                                    )
                        }
                    }
                            ?: run {
                                Log.d(TAG, "Context is not LifecycleOwner, showing coordinates")
                                lastPosition.text =
                                        root.context.getString(
                                                R.string.last_position_format,
                                                vehicle.lastPosition.latitude,
                                                vehicle.lastPosition.longitude
                                        )
                            }
                } else {
                    Log.d(TAG, "No position available for vehicle ${vehicle.id}")
                    lastPosition.text = root.context.getString(R.string.no_position_available)
                }
            }
        }
    }
}

class VehicleDiffCallback : DiffUtil.ItemCallback<VehicleInfo>() {
    override fun areItemsTheSame(oldItem: VehicleInfo, newItem: VehicleInfo): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: VehicleInfo, newItem: VehicleInfo): Boolean {
        return oldItem == newItem
    }
}
