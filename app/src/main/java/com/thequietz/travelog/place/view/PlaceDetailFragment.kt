package com.thequietz.travelog.place.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.thequietz.travelog.R
import com.thequietz.travelog.databinding.FragmentPlaceDetailBinding
import com.thequietz.travelog.place.adapter.PlaceDetailAdapter
import com.thequietz.travelog.place.model.PlaceSearchModel
import com.thequietz.travelog.place.viewmodel.PlaceDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaceDetailFragment : Fragment() {

    private var _binding: FragmentPlaceDetailBinding? = null
    private val binding get() = _binding!!
    private val navArgs: PlaceDetailFragmentArgs by navArgs()
    private val viewModel: PlaceDetailViewModel by viewModels()

    private lateinit var imageAdapter: PlaceDetailAdapter
    private lateinit var model: PlaceSearchModel
    private lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_place_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gson = Gson()
        model = gson.fromJson(navArgs.param, PlaceSearchModel::class.java)
        imageAdapter = PlaceDetailAdapter()

        binding.viewModel = viewModel
        binding.rvPlaceDetail.adapter = imageAdapter
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.detail.observe(viewLifecycleOwner, {
            (binding.rvPlaceDetail.adapter as PlaceDetailAdapter).submitList(it.photos)
        })
        viewModel.loadPlaceDetail(model.placeId)
    }
}
