package com.example.geofleet.ui.fleet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geofleet.databinding.FragmentFleetBinding
import com.example.geofleet.ui.MapActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

private const val TAG = "FleetFragment"

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
        setupObservers()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        adapter =
                VehicleAdapter(
                        onProfileClick = { vehicleId ->
                            // TODO: Implementar navegación al perfil del vehículo
                            Log.d(TAG, "Click en perfil del vehículo: $vehicleId")
                            Snackbar.make(
                                            binding.root,
                                            "Perfil del vehículo $vehicleId",
                                            Snackbar.LENGTH_SHORT
                                    )
                                    .show()
                        },
                        onMapClick = { vehicleId ->
                            // Navegar al mapa centrado en el vehículo
                            Log.d(TAG, "Click en mapa del vehículo: $vehicleId")
                            val intent =
                                    Intent(requireContext(), MapActivity::class.java).apply {
                                        putExtra("vehicle_id", vehicleId)
                                    }
                            startActivity(intent)
                        }
                )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FleetFragment.adapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar estado de carga
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.swipeRefresh.isRefreshing = isLoading
                    }
                }

                // Observar errores
                launch {
                    viewModel.error.collect { error ->
                        error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
                    }
                }

                // Observar lista de vehículos
                launch {
                    viewModel.vehicles.collect { vehicles ->
                        Log.d(TAG, "Actualizando lista con ${vehicles.size} vehículos")
                        adapter.submitList(vehicles)

                        // Mostrar mensaje si no hay vehículos
                        binding.emptyView.visibility =
                                if (vehicles.isEmpty()) View.VISIBLE else View.GONE
                        binding.recyclerView.visibility =
                                if (vehicles.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadVehicles() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
