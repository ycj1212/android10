package com.thequietz.travelog.record.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thequietz.travelog.data.RecordRepository
import com.thequietz.travelog.data.db.dao.JoinRecord
import com.thequietz.travelog.record.view.RecordViewOneFragmentArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecordViewOneViewModel @Inject constructor(
    val repository: RecordRepository
) : ViewModel() {

    companion object {
        private val _currentPosition = MutableLiveData<Int>()
        val currentPosition: LiveData<Int> = _currentPosition

        var _currentJoinRecord = MutableLiveData<JoinRecord>()
        val currentJoinRecord: LiveData<JoinRecord> = _currentJoinRecord

        var currentTravleId: Int = 0
    }

    private val _dataList = MutableLiveData<List<JoinRecord>>()
    val dataList: LiveData<List<JoinRecord>> = _dataList

    private val _currentImage = MutableLiveData<JoinRecord>()
    val currentImage: LiveData<JoinRecord> = _currentImage

    private val _islistUpdate = MutableLiveData<Boolean>()
    val islistUpdate: LiveData<Boolean> = _islistUpdate

    var day: String = ""
    var place: String = ""
    var startInd = 0

    init {
        // createRecord()
        // loadRecord()
        // _currentPosition.value = 0
        startInd = 0
    }
    fun initVariable(args: RecordViewOneFragmentArgs) {
        day = args.day
        place = args.place
        currentTravleId = args.travelId

    }
    fun loadRecord() {
        val res = mutableListOf<JoinRecord>()
        viewModelScope.launch {
            val placeList = withContext(Dispatchers.IO) {
                repository.loadRecordImagesByTravelId(currentTravleId)
            }
            placeList.forEachIndexed { ind, it ->
                if (it.place == place) {
                    startInd = res.size
                }
                val temp = withContext(Dispatchers.IO) {
                    repository.loadJoinedRecordByTravelIdAndPlace(currentTravleId, it.place)
                }
                if (temp.size == 0) {
                    res.add( withContext(Dispatchers.IO) {
                        repository.loadDefaultJoinedRecordByTravelId(currentTravleId, it.place)
                    })
                } else {
                    temp.forEach {
                        res.add(it)
                    }
                }
            }
            _dataList.value = res
            // setCurrentImage(currentInd)
        }
    }

    fun setCurrentImage(position: Int) {
        _currentImage.value = dataList.value?.get(position)
    }

//    fun isCommentChanged(str: String): Boolean {
//        if (currentImage.value?.comment != str) {
//            return true
//        }
//        return false
//    }

    fun setCurrentPosition(position: Int) {
        if (position <0) {
            return
        } else {
            _currentPosition.value = position
            dataList.value?.let {
                _currentJoinRecord.value = it.get(position)
            }
        }
    }

    fun resetIsListUpdate() {
        _islistUpdate.value = false
    }

    fun updateComment(comment: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                viewModelScope.launch {
                    _islistUpdate.value = true
                }
            }
            withContext(Dispatchers.IO) {
                loadRecord()
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                viewModelScope.launch {
                    _islistUpdate.value = true
                }
                currentImage.value?.newRecordImage?.let {
                    if (it.isDefault == false) {
                        repository.deleteNewRecordImage(it.newRecordImageId)
                        currentPosition.value?.let { setCurrentPosition(it - 1) }
                    }
                }
            }
            withContext(Dispatchers.IO) {
                loadRecord()
            }
        }
    }
}
