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
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class VehiclePositionsFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentVehiclePositionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VehiclePositionsViewModel by viewModels()
    private var googleMap: GoogleMap? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    private var customMarkerBitmap: BitmapDescriptor? = null

    companion object {
        private const val TAG = "VehiclePositionsFragment"
    }

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

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.fab.setOnClickListener {
            Log.d(TAG, "FAB clicked, refreshing positions")
            viewModel.refreshVehiclePositions()
        }

        // Create custom marker bitmap
        createCustomMarkerBitmap()

        observeViewModel()
    }

    private fun createCustomMarkerBitmap() {
        val markerView = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_marker, null)

        // Set the marker icon
        val markerIcon = markerView.findViewById<ImageView>(R.id.marker_icon)
        markerIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_vehicle_marker))

        customMarkerBitmap = createCustomMarker(markerView)
    }

    private fun createCustomMarker(view: View): BitmapDescriptor {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.vehiclePositions.collectLatest { positions ->
                Log.d(TAG, "Received ${positions.size} vehicle positions")
                updateMapMarkers(positions)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "Map is ready")
        googleMap = map
        
        try {
            map.uiSettings.apply {
                isZoomControlsEnabled = true
                isCompassEnabled = true
                isMapToolbarEnabled = true
            }
            
            // Set initial camera position to Spain
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(40.4168, -3.7038), // Madrid coordinates
                6f // Zoom level
            ))
            
            Log.d(TAG, "Map initial setup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up map: ${e.message}", e)
        }
        
        viewModel.refreshVehiclePositions()
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
            Snackbar.make(binding.root, R.string.no_vehicles, Snackbar.LENGTH_SHORT).show()
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
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(lat, lng),
                            12f
                        )
                    )
                    Log.d(TAG, "Camera centered on first position")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
