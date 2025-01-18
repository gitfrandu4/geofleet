package com.example.geofleet.ui.fleet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geofleet.R
import com.example.geofleet.data.local.AppDatabase
import com.example.geofleet.data.repository.GeocodingRepository
import com.example.geofleet.databinding.FragmentFleetBinding
import com.example.geofleet.ui.MapActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class FleetFragment : Fragment() {
    private var _binding: FragmentFleetBinding? = null
    private val binding
        get() = _binding!!
    private val viewModel: FleetViewModel by viewModels()
    private lateinit var adapter: VehicleAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFleetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val database = AppDatabase.getDatabase(requireContext())
        val geocodingRepository =
                GeocodingRepository(requireContext(), database.geocodedAddressDao())

        adapter =
                VehicleAdapter(
                        onProfileClick = { vehicleId ->
                            findNavController()
                                    .navigate(
                                            FleetFragmentDirections
                                                    .actionFleetFragmentToVehicleProfileFragment(
                                                            vehicleId
                                                    )
                                    )
                        },
                        onMapClick = { vehicleId ->
                            Log.d(TAG, "Map clicked for vehicle: $vehicleId")
                            navigateToMap(vehicleId)
                        },
                        geocodingRepository = geocodingRepository
                )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FleetFragment.adapter
        }
    }

    private fun navigateToMap(vehicleId: String) {
        val intent =
                Intent(requireContext(), MapActivity::class.java).apply {
                    putExtra("selected_vehicle_id", vehicleId)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
        startActivity(intent)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadVehicles() }
    }

    private fun setupSearch() {
        binding.searchInput.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {
                        viewModel.setSearchQuery(s?.toString() ?: "")
                    }
                    override fun afterTextChanged(s: Editable?) {}
                }
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.swipeRefresh.isRefreshing = isLoading
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
                    }
                }

                launch {
                    viewModel.filteredVehicles.collect { vehicles ->
                        adapter.submitList(vehicles)
                        binding.emptyView.visibility =
                                if (vehicles.isEmpty()) View.VISIBLE else View.GONE
                        binding.totalVehicles.text =
                                getString(R.string.total_vehicles, vehicles.size)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "FleetFragment"
    }
}
