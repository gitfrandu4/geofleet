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
import kotlinx.coroutines.launch

private const val TAG = "VehicleAdapter"

class VehicleAdapter(
        private val onProfileClick: (String) -> Unit,
        private val onMapClick: (String) -> Unit,
        private val geocodingRepository: GeocodingRepository
) : ListAdapter<VehicleInfo, VehicleAdapter.VehicleViewHolder>(VehicleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VehicleViewHolder(private val binding: ItemVehicleBinding) :
            RecyclerView.ViewHolder(binding.root) {

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

                // Configure buttons
                profileButton.setOnClickListener { onProfileClick(vehicle.id) }
                mapButton.setOnClickListener { onMapClick(vehicle.id) }

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
                            val fallbackText =
                                    root.context.getString(
                                            R.string.last_position_format,
                                            vehicle.lastPosition.latitude,
                                            vehicle.lastPosition.longitude
                                    )
                            Log.d(
                                    TAG,
                                    "Using fallback coordinates for vehicle ${vehicle.id}: $fallbackText"
                            )
                            lastPosition.text = fallbackText
                        }
                    }
                            ?: run {
                                Log.e(
                                        TAG,
                                        "Context is not a LifecycleOwner for vehicle ${vehicle.id}"
                                )
                                // If context is not a LifecycleOwner, show coordinates
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
