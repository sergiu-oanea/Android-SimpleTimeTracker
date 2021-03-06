package com.example.util.simpletimetracker.feature_records_all.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.TypesFilterDialogListener
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.viewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.feature_records_all.R
import com.example.util.simpletimetracker.feature_records_all.adapter.RecordAllAdapter
import com.example.util.simpletimetracker.feature_records_all.di.RecordsAllComponentProvider
import com.example.util.simpletimetracker.feature_records_all.extra.RecordsAllExtra
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllSortOrderViewData
import com.example.util.simpletimetracker.feature_records_all.viewModel.RecordsAllViewModel
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.SnackBarParams
import kotlinx.android.synthetic.main.records_all_fragment.*
import javax.inject.Inject

class RecordsAllFragment : BaseFragment(R.layout.records_all_fragment),
    TypesFilterDialogListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<RecordsAllViewModel>

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: RecordsAllViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val removeRecordViewModel: RemoveRecordViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { removeRecordViewModelFactory }
    )
    private val recordsAdapter: RecordAllAdapter by lazy {
        RecordAllAdapter(viewModel::onRecordClick)
    }

    override fun initDi() {
        (activity?.application as RecordsAllComponentProvider)
            .recordsAllComponent
            ?.inject(this)
    }

    override fun initUi() {
        parentFragment?.postponeEnterTransition()

        rvRecordsAllList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter

            viewTreeObserver.addOnPreDrawListener {
                parentFragment?.startPostponedEnterTransition()
                true
            }
        }
    }

    override fun initUx() {
        spinnerRecordsAllSort.onItemSelected = viewModel::onRecordTypeOrderSelected
        cardRecordsAllFilter.setOnClick(viewModel::onFilterClick)
    }

    override fun initViewModel() {
        with(viewModel) {
            extra = RecordsAllExtra(arguments?.getLongArray(ARGS_TYPE_IDS)?.toList().orEmpty())
            records.observe(viewLifecycleOwner, recordsAdapter::replaceAsNew)
            sortOrderViewData.observe(viewLifecycleOwner, ::updateCardOrderViewData)
        }
        with(removeRecordViewModel) {
            needUpdate.observe(viewLifecycleOwner, ::onUpdateNeeded)
            message.observe(viewLifecycleOwner, ::showMessage)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
        spinnerRecordsAllSort.jumpDrawablesToCurrentState()
    }

    override fun onTypesSelected(typesSelected: List<Long>) {
        viewModel.onTypesSelected(typesSelected)
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && this.isResumed) {
            router.show(Notification.SNACK_BAR, message)
            removeRecordViewModel.onMessageShown()
        }
    }

    private fun onUpdateNeeded(isUpdateNeeded: Boolean) {
        if (isUpdateNeeded && this@RecordsAllFragment.isResumed) {
            viewModel.onNeedUpdate()
            removeRecordViewModel.onUpdated()
        }
    }

    private fun updateCardOrderViewData(viewData: RecordsAllSortOrderViewData) {
        spinnerRecordsAllSort.setData(viewData.items, viewData.selectedPosition)
    }

    companion object {
        private const val ARGS_TYPE_IDS = "args_type_ids"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is RecordsAllParams -> putLongArray(ARGS_TYPE_IDS, data.typeIds.toLongArray())
            }
        }
    }
}
