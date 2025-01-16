package com.example.geofleet.ui.fleet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.geofleet.R
import com.example.geofleet.databinding.ItemVehicleBinding
import com.squareup.picasso.Picasso

class VehicleAdapter(
        private val onProfileClick: (String) -> Unit,
        private val onMapClick: (String) -> Unit
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
            binding.apply {
                vehicleId.text = vehicle.name

                // Cargar imagen del vehículo
                if (vehicle.photoUrl != null) {
                    Picasso.get()
                            .load(vehicle.photoUrl)
                            .placeholder(R.drawable.ic_vehicle_marker)
                            .error(R.drawable.ic_vehicle_marker)
                            .into(vehicleImage)
                } else {
                    vehicleImage.setImageResource(R.drawable.ic_vehicle_marker)
                }

                // Configurar botones
                profileButton.setOnClickListener { onProfileClick(vehicle.id) }
                mapButton.setOnClickListener { onMapClick(vehicle.id) }

                // Mostrar última posición si está disponible
                vehicle.lastPosition?.let { position ->
                    lastPosition.text =
                            binding.root.context.getString(
                                    R.string.last_position_format,
                                    position.latitude,
                                    position.longitude
                            )
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
