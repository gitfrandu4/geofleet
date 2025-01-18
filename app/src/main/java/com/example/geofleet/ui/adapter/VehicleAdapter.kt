package com.example.geofleet.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.geofleet.R
import com.example.geofleet.data.model.VehicleInfo
import com.example.geofleet.data.repository.GeocodingRepository
import com.example.geofleet.databinding.ItemVehicleBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

private const val TAG = "VehicleAdapter"

class VehicleAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val geocodingRepository: GeocodingRepository,
    private val onItemClick: (VehicleInfo) -> Unit
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
            binding.root.setOnClickListener {
                val position = layoutPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
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
            Log.d(TAG, "📦 Binding vehicle: ${vehicle.id}")
            binding.vehicleId.text = vehicle.id
            binding.deleteButton.isEnabled = true

            // Load vehicle image
            vehicle.images.firstOrNull()?.let { imageUrl ->
                Log.d(TAG, "🖼️ Loading vehicle image from: $imageUrl")
                // ... existing image loading code ...
            }

            // Show last position
            if (vehicle.lastPosition != null) {
                val lat = vehicle.lastPosition.latitude
                val lng = vehicle.lastPosition.longitude
                Log.d(TAG, "📍 Last position: $lat, $lng")

                binding.lastPosition.setText(R.string.loading_address)
                lifecycleOwner.lifecycleScope.launch {
                    try {
                        val address = geocodingRepository.getAddressFromCoordinates(lat, lng)
                        binding.lastPosition.text = address
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error getting address", e)
                        binding.lastPosition.text = "$lat, $lng"
                    }
                }
            } else {
                Log.d(TAG, "⚠️ No position available")
                binding.lastPosition.setText(R.string.no_position_available)
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
}
