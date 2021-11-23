package com.thequietz.travelog.schedule.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.gms.maps.model.LatLng
import com.thequietz.travelog.R
import com.thequietz.travelog.databinding.FragmentScheduleDetailBinding
import com.thequietz.travelog.map.GoogleMapFragment
import com.thequietz.travelog.place.model.PlaceDetailModel
import com.thequietz.travelog.schedule.adapter.ScheduleDetailAdapter
import com.thequietz.travelog.schedule.adapter.ScheduleTouchHelperCallback
import com.thequietz.travelog.schedule.viewmodel.ScheduleDetailViewModel
import com.thequietz.travelog.util.ScheduleControlType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScheduleDetailFragment :
    GoogleMapFragment<FragmentScheduleDetailBinding, ScheduleDetailViewModel>() {
    override val layoutId = R.layout.fragment_schedule_detail
    override val viewModel by viewModels<ScheduleDetailViewModel>()
    lateinit var adapter: ScheduleDetailAdapter
    private val args: ScheduleDetailFragmentArgs by navArgs()

    override var drawMarker = true
    override var isMarkerNumbered = true
    override var drawOrderedPolyline = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.type == ScheduleControlType.TYPE_CREATE)
            args.schedule.run { viewModel.createSchedule(name, schedulePlace, date) }
        else
            viewModel.loadSchedule(args.schedule)

        binding.btnNext.setOnClickListener {
            viewModel.saveSchedule()
            val action =
                ScheduleDetailFragmentDirections.actionScheduleDetailFragmentToScheduleFragment()
            findNavController().navigate(action)
        }

        val stateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        viewModel.detailList.observe(viewLifecycleOwner, { list ->
            targetList.value = list.map {
                LatLng(
                    it.destination.geometry.location.latitude,
                    it.destination.geometry.location.longitude
                )
            }.toMutableList()
        })

        stateHandle?.getLiveData<PlaceDetailModel>("result")?.observe(
            viewLifecycleOwner,
            {
                Log.d("Cal:GetStateIndex", viewModel.selectedIndex.toString())
                viewModel.addScheduleDetail(it)

                stateHandle.remove<PlaceDetailModel>("result")
            }
        )
    }

    override fun initViewModel() {
        binding.viewModel = viewModel
        initRecycler()
        initItemObserver()
    }

    private fun initRecycler() {
        val startDate = args.schedule.date.split("~")[0]
        val endDate = args.schedule.date.split("~")[1]
        viewModel.initItemList(startDate, endDate)
        adapter = ScheduleDetailAdapter(viewModel) {
            val action =
                ScheduleDetailFragmentDirections.actionScheduleDetailFragmentToPlaceRecommendFragment()
            this.findNavController().navigate(action)
        }
        val callback = ScheduleTouchHelperCallback(adapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvSchedule)
        binding.rvSchedule.adapter = adapter
    }

    private fun initItemObserver() {
        viewModel.itemList.observe(viewLifecycleOwner, {
            adapter.submitList(it.toMutableList())
        })

        viewModel.placeDetailList.observe(viewLifecycleOwner, {
            viewModel.initDetailList(
                args.schedule.date.split("~")[0],
                args.schedule.date.split("~")[1],
                it
            )
            viewModel.placeDetailList.removeObservers(viewLifecycleOwner)
        })

        viewModel.colorList.observe(viewLifecycleOwner, {
            markerColorList = it
        })
    }

    override fun initTargetList() {
        if (isInitial)
            baseTargetList =
                args.schedule.schedulePlace.map { LatLng(it.mapY.toDouble(), it.mapX.toDouble()) }
                    .toMutableList()
    }
}
