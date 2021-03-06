package com.example.util.simpletimetracker.feature_dialogs.chartFilter.view

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.ChartFilterDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.adapter.ChartFilterAdapter
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.di.ChartFilterComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewModel.ChartFilterViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.chart_filter_dialog_fragment.*
import javax.inject.Inject

class ChartFilterDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChartFilterViewModel>

    private val viewModel: ChartFilterViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val recordTypesAdapter: ChartFilterAdapter by lazy {
        ChartFilterAdapter(
            viewModel::onRecordTypeClick,
            viewModel::onCategoryClick
        )
    }

    private var behavior: BottomSheetBehavior<View>? = null
    private var chartFilterDialogListener: ChartFilterDialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.chart_filter_dialog_fragment,
            container,
            false
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initDialog()
        initDi()
        initUi()
        initUx()
        initViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is ChartFilterDialogListener -> {
                chartFilterDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is ChartFilterDialogListener && it.isResumed }
                    ?.let { chartFilterDialogListener = it as? ChartFilterDialogListener }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        chartFilterDialogListener?.onChartFilterDialogDismissed()
    }

    private fun initDialog() {
        dialog?.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.let { bottomSheet ->
            behavior = BottomSheetBehavior.from<View>(bottomSheet)
        }
        behavior?.apply {
            peekHeight = 0
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initDi() {
        (activity?.application as ChartFilterComponentProvider)
            .chartFilterComponent
            ?.inject(this)
    }

    private fun initUi() {
        rvChartFilterContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    private fun initUx() {
        buttonsChartFilterType.listener = viewModel::onFilterTypeClick
    }

    private fun initViewModel(): Unit = with(viewModel) {
        filterTypeViewData.observe(viewLifecycleOwner, buttonsChartFilterType.adapter::replace)
        types.observe(viewLifecycleOwner, recordTypesAdapter::replace)
    }
}