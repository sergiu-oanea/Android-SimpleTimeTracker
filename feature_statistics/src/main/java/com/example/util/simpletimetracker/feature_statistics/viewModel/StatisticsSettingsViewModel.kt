package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.feature_statistics.viewData.RangeLength
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsRangeViewData
import javax.inject.Inject

class StatisticsSettingsViewModel @Inject constructor() : ViewModel() {

    val rangeLength: LiveData<RangeLength> by lazy {
        return@lazy MutableLiveData(RangeLength.DAY)
    }

    fun onRangeClick(rangeData: StatisticsRangeViewData) {
        (rangeLength as MutableLiveData).value = rangeData.rangeLength
    }
}
