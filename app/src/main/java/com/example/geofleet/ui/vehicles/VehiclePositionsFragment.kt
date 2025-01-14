package com.example.geofleet.ui.vehicles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.data.local.VehiclePositionEntity
import com.example.geofleet.data.repository.VehicleRepository
import com.example.geofleet.databinding.FragmentVehiclePositionsBinding
import com.example.geofleet.util.ConfigurationReader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VehiclePositionsFragment : Fragment() {
    private var _binding: FragmentVehiclePositionsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var database: AppDatabase
    private val adapter = VehiclePositionAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehiclePositionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vehicleRepository = VehicleRepository(requireContext())
        database = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupSwipeRefresh()
        observePositions()
        refreshPositions()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            adapter = this@VehiclePositionsFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            refreshPositions()
        }
    }

    private fun observePositions() {
        viewLifecycleOwner.lifecycleScope.launch {
            database.vehiclePositionDao().getAllPositions().collectLatest { positions ->
                android.util.Log.d("VehiclePositions", "Received ${positions.size} positions from database")
                adapter.submitList(positions)
            }
        }
    }

    private fun refreshPositions() {
        binding.swipeRefresh.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val positions = vehicleRepository.getVehiclePositions(ConfigurationReader.getVehicleIds())
                android.util.Log.d("VehiclePositions", "Fetched positions from repository: $positions")
                val entities = positions.mapNotNull { (id, position) ->
                    position?.let {
                        VehiclePositionEntity(
                            vehicleId = id,
                            latitude = it.latitude,
                            longitude = it.longitude
                        )
                    }
                }
                android.util.Log.d("VehiclePositions", "Saving ${entities.size} positions to database")
                database.vehiclePositionDao().insertAll(entities)
            } catch (e: Exception) {
                android.util.Log.e("VehiclePositions", "Error refreshing positions", e)
                e.printStackTrace()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class VehiclePositionAdapter : ListAdapter<VehiclePositionEntity, VehiclePositionAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<VehiclePositionEntity>() {
        override fun areItemsTheSame(oldItem: VehiclePositionEntity, newItem: VehiclePositionEntity): Boolean {
            return oldItem.vehicleId == newItem.vehicleId
        }

        override fun areContentsTheSame(oldItem: VehiclePositionEntity, newItem: VehiclePositionEntity): Boolean {
            return oldItem == newItem
        }
    }
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            com.example.geofleet.databinding.ItemVehiclePositionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            vehicleIdText.text = "Vehicle ID: ${item.vehicleId}"
            positionText.text = "Position: ${item.latitude}, ${item.longitude}"
            timestampText.text = "Last Update: ${dateFormat.format(Date(item.timestamp))}"
        }
    }

    class ViewHolder(val binding: com.example.geofleet.databinding.ItemVehiclePositionBinding) : 
        RecyclerView.ViewHolder(binding.root)
} 
