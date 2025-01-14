package com.example.geofleet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.data.local.VehiclePositionEntity
import com.example.geofleet.data.repository.VehicleRepository
import com.example.geofleet.databinding.ActivityVehiclePositionsBinding
import com.example.geofleet.databinding.ItemVehiclePositionBinding
import com.example.geofleet.util.ConfigurationReader
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VehiclePositionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVehiclePositionsBinding
    private lateinit var vehicleRepository: VehicleRepository
    private lateinit var database: AppDatabase
    private val adapter = VehiclePositionAdapter()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVehiclePositionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ConfigurationReader.init(this)

        vehicleRepository = VehicleRepository(this)
        database = AppDatabase.getDatabase(this)

        setupRecyclerView()
        setupSwipeRefresh()
        setupToolbar()
        observePositions()
        refreshPositions()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@VehiclePositionsActivity)
            adapter = this@VehiclePositionsActivity.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            refreshPositions()
        }
    }

    private fun setupToolbar() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                com.example.geofleet.R.id.action_refresh -> {
                    refreshPositions()
                    true
                }
                else -> false
            }
        }
    }

    private fun observePositions() {
        lifecycleScope.launch {
            database.vehiclePositionDao().getAllPositions().collectLatest { positions ->
                android.util.Log.d("VehiclePositions", "Received ${positions.size} positions from database")
                adapter.submitList(positions)
            }
        }
    }

    private fun refreshPositions() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
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
            ItemVehiclePositionBinding.inflate(
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

    class ViewHolder(val binding: ItemVehiclePositionBinding) : RecyclerView.ViewHolder(binding.root)
} 
