package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.interactor.RecordsViewDataInteractor
import com.example.util.simpletimetracker.core.viewData.RecordViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordsViewModel @Inject constructor(
    private val router: Router,
    private val recordsViewDataInteractor: RecordsViewDataInteractor
) : ViewModel() {

    lateinit var extra: RecordsExtra

    val records: LiveData<List<ViewHolderType>> by lazy {
        updateRecords()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    fun onRecordClick(item: RecordViewData, sharedElements: Map<Any, String>) {
        val params = when (item) {
            is RecordViewData.Tracked -> ChangeRecordParams.Tracked(
                transitionName = TransitionNames.RECORD + item.id,
                id = item.id
            )
            is RecordViewData.Untracked -> ChangeRecordParams.Untracked(
                transitionName = TransitionNames.RECORD + item.getUniqueId(),
                timeStarted = item.timeStartedTimestamp,
                timeEnded = item.timeEndedTimestamp
            )
        }
        router.navigate(
            screen = Screen.CHANGE_RECORD_FROM_MAIN,
            data = params,
            sharedElements = sharedElements
        )
    }

    fun onVisible() {
        updateRecords()
    }

    fun onNeedUpdate() {
        updateRecords()
    }

    private fun updateRecords() = viewModelScope.launch {
        (records as MutableLiveData).value = loadRecordsViewData()
    }

    private suspend fun loadRecordsViewData(): List<ViewHolderType> {
        return recordsViewDataInteractor.getViewData(extra.rangeStart, extra.rangeEnd)
    }
}
