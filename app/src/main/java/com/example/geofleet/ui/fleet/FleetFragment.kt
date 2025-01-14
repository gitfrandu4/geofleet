package com.example.geofleet.ui.fleet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.geofleet.databinding.FragmentFleetBinding

class FleetFragment : Fragment() {
    private var _binding: FragmentFleetBinding? = null
    private val binding get() = _binding!!

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
        setupViews()
    }

    private fun setupViews() {
        // TODO: Implement fleet management functionality
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
