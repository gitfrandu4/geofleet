package com.example.geofleet.ui.vehicles

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.geofleet.R
import com.example.geofleet.data.model.VehiclePosition
import com.example.geofleet.databinding.FragmentVehiclePositionsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Locale

class VehiclePositionsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentVehiclePositionsBinding? = null
    private val binding
        get() = _binding!!
    private val viewModel: VehiclePositionsViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    private var customMarkerBitmap: BitmapDescriptor? = null
    private var isLoading = false
    private val TAG = "VehiclePositionsFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing ViewModel")
        // Force ViewModel initialization
        viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView started")
        _binding = FragmentVehiclePositionsBinding.inflate(inflater, container, false)
        Log.d(TAG, "Layout inflated, FAB exists: ${_binding?.fab != null}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated started")

        setupObservers()
        setupMap()
        setupFab()
    }

    private fun setupMap() {
        Log.d(TAG, "Setting up map")
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        createCustomMarkerBitmap()
    }

    private fun setupFab() {
        binding.fab.let {
            Log.d(TAG, "Setting up FAB")
            it.visibility = View.VISIBLE
            it.setOnClickListener { _ ->
                Log.d(TAG, "🔄 FAB clicked - Starting manual refresh")
                Log.d(TAG, "Current loading state: $isLoading")
                if (!isLoading) {
                    Log.d(TAG, "✅ Initiating vehicle positions refresh")
                    refreshVehiclePositions()
                } else {
                    Log.d(TAG, "⚠️ Refresh already in progress, skipping")
                }
            }
        }
    }

    private fun setupObservers() {
        Log.d(TAG, "Setting up observers")
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d(TAG, "Launching observers coroutine")
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.d(TAG, "Observers in STARTED state")

                launch {
                    Log.d(TAG, "Collecting isLoading state")
                    viewModel.isLoading.collect { loading ->
                        Log.d(TAG, "Loading state changed: $loading")
                        isLoading = loading
                        binding.fab?.isEnabled = !loading
                        if (loading) {
                            showLoadingMessage()
                        }
                    }
                }

                launch {
                    Log.d(TAG, "Collecting error state")
                    viewModel.error.collect { error ->
                        error?.let {
                            Log.e(TAG, "Error received", it)
                            handleError(it)
                        }
                    }
                }

                launch {
                    Log.d(TAG, "Collecting vehicle positions")
                    viewModel.vehiclePositions.collect { positions ->
                        Log.d(TAG, "Received ${positions.size} positions")
                        updateMapMarkers(positions)
                    }
                }
            }
        }
    }

    private fun showLoadingMessage() {
        _binding?.let { binding ->
            Snackbar.make(binding.root, R.string.loading_positions, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun handleError(error: Throwable) {
        _binding?.let { binding ->
            val messageResId =
                when (error) {
                    is UnknownHostException -> R.string.error_no_internet
                    else -> R.string.error_loading_positions
                }
            Snackbar.make(binding.root, messageResId, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_retry) { refreshVehiclePositions() }
                .show()
        }
    }

    private fun refreshVehiclePositions() {
        Log.d(TAG, "🚗 Requesting vehicle positions refresh")
        Log.d(TAG, "Loading state before refresh: $isLoading")
        viewModel.refreshVehiclePositions()
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "onMapReady: Configurando mapa")
        googleMap = map

        try {
            map.uiSettings.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMapToolbarEnabled = true
            }

            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(28.4697, -16.2548), // Tenerife coordinates
                    9f // Zoom level
                )
            )

            // Initial load after map is ready
            Log.d(TAG, "onMapReady: Iniciando carga inicial")
            refreshVehiclePositions()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map: ${e.message}", e)
        }
    }

    private fun createCustomMarkerBitmap() {
        val markerView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker, null)

        val markerIcon = markerView.findViewById<ImageView>(R.id.marker_icon)
        markerIcon.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_vehicle_marker)
        )

        customMarkerBitmap = createCustomMarker(markerView)
    }

    private fun createCustomMarker(view: View): BitmapDescriptor {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap =
            Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun updateMapMarkers(positions: List<VehiclePosition>) {
        val map = googleMap
        if (map == null) {
            Log.e(TAG, "Map is null when trying to update markers")
            return
        }

        map.clear()

        if (positions.isEmpty()) {
            Log.d(TAG, "No positions to display")
            _binding?.let { binding ->
                Snackbar.make(binding.root, R.string.no_vehicles, Snackbar.LENGTH_SHORT).show()
            }
            return
        }

        Log.d(TAG, "Updating map with ${positions.size} markers")
        val boundsBuilder = LatLngBounds.Builder()

        positions.forEach { position ->
            val lat = position.getLatitudeAsDouble()
            val lng = position.getLongitudeAsDouble()
            Log.d(TAG, "Adding marker for vehicle ${position.vehicleId} at lat/lng: ($lat,$lng)")

            if (lat != 0.0 || lng != 0.0) {
                val latLng = LatLng(lat, lng)
                boundsBuilder.include(latLng)

                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(customMarkerBitmap)
                        .title("Vehicle ${position.vehicleId}")
                        .snippet("Last update: ${dateFormat.format(position.timestamp)}")
                )
            }
        }

        try {
            val bounds = boundsBuilder.build()
            map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    100 // padding in pixels
                )
            )
            Log.d(TAG, "Camera animated to show all markers")
        } catch (e: Exception) {
            Log.e(TAG, "Error animating camera: ${e.message}")
            // If bounds are invalid, at least center on the first position
            positions.firstOrNull()?.let { position ->
                val lat = position.getLatitudeAsDouble()
                val lng = position.getLongitudeAsDouble()
                if (lat != 0.0 || lng != 0.0) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 12f))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        if (googleMap != null && !isLoading) {
            Log.d(TAG, "onResume: Refreshing vehicle positions")
            refreshVehiclePositions()
        } else {
            Log.d(TAG, "onResume: Skipping refresh - map not ready or loading in progress")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "VehiclePositionsFragment"
    }
}
