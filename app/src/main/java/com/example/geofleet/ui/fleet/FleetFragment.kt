package com.example.geofleet.ui.fleet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geofleet.R
import com.example.geofleet.databinding.FragmentFleetBinding
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
        adapter =
                VehicleAdapter(
                        onProfileClick = { vehicleId ->
                            // TODO: Navigate to vehicle profile
                        },
                        onMapClick = { vehicleId ->
                            // TODO: Navigate to map centered on vehicle
                        }
                )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FleetFragment.adapter
        }
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
}
