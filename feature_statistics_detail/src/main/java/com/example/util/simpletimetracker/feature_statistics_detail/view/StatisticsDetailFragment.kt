package com.example.util.simpletimetracker.feature_statistics_detail.view

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.view.TransitionNames
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.di.StatisticsDetailComponentProvider
import com.example.util.simpletimetracker.feature_statistics_detail.extra.StatisticsDetailExtra
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewModel.StatisticsDetailViewModel
import com.example.util.simpletimetracker.navigation.params.StatisticsDetailParams
import kotlinx.android.synthetic.main.statistics_detail_fragment.buttonsStatisticsDetailGrouping
import kotlinx.android.synthetic.main.statistics_detail_fragment.buttonsStatisticsDetailLength
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailAverage
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailDates
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailRecords
import kotlinx.android.synthetic.main.statistics_detail_fragment.cardStatisticsDetailTotal
import kotlinx.android.synthetic.main.statistics_detail_fragment.chartStatisticsDetail
import kotlinx.android.synthetic.main.statistics_detail_fragment.chartStatisticsDetailDaily
import kotlinx.android.synthetic.main.statistics_detail_fragment.ivStatisticsDetailItemIcon
import kotlinx.android.synthetic.main.statistics_detail_fragment.layoutStatisticsDetailItem
import kotlinx.android.synthetic.main.statistics_detail_fragment.tvStatisticsDetailItemName
import javax.inject.Inject

class StatisticsDetailFragment : BaseFragment(R.layout.statistics_detail_fragment),
    StandardDialogListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<StatisticsDetailViewModel>

    private val viewModel: StatisticsDetailViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val typeId: Long by lazy { arguments?.getLong(ARGS_ID).orZero() }

    override fun initDi() {
        (activity?.application as StatisticsDetailComponentProvider)
            .statisticsDetailComponent
            ?.inject(this)
    }

    override fun initUi() {
        if (BuildVersions.isLollipopOrHigher()) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        ViewCompat.setTransitionName(
            layoutStatisticsDetailItem,
            TransitionNames.STATISTICS_DETAIL + typeId
        )
    }

    override fun initUx() {
        buttonsStatisticsDetailGrouping.listener = viewModel::onChartGroupingClick
        buttonsStatisticsDetailLength.listener = viewModel::onChartLengthClick
        cardStatisticsDetailRecords.listener = viewModel::onRecordsClick
    }

    override fun initViewModel(): Unit = with(viewModel) {
        val filterType = arguments?.getSerializable(ARGS_FILTER_TYPE) as? ChartFilterType
        extra = StatisticsDetailExtra(typeId, filterType ?: ChartFilterType.ACTIVITY)
        previewViewData.observe(viewLifecycleOwner, ::setPreviewViewData)
        viewData.observe(viewLifecycleOwner, ::setViewData)
        chartViewData.observe(viewLifecycleOwner, ::updateChartViewData)
        dailyChartViewData.observe(viewLifecycleOwner, ::updateDailyChartViewData)
        chartGroupingViewData.observe(viewLifecycleOwner, buttonsStatisticsDetailGrouping.adapter::replace)
        chartLengthViewData.observe(viewLifecycleOwner, buttonsStatisticsDetailLength.adapter::replace)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    private fun setPreviewViewData(viewData: StatisticsDetailPreviewViewData) {
        tvStatisticsDetailItemName.text = viewData.name
        layoutStatisticsDetailItem.setCardBackgroundColor(viewData.color)
        chartStatisticsDetail.setBarColor(viewData.color)
        chartStatisticsDetailDaily.setBarColor(viewData.color)
        if (viewData.iconId != null) {
            ivStatisticsDetailItemIcon.visible = true
            ivStatisticsDetailItemIcon.setBackgroundResource(viewData.iconId)
            ivStatisticsDetailItemIcon.tag = viewData.iconId
        } else {
            ivStatisticsDetailItemIcon.visible = false
        }
    }

    private fun setViewData(viewData: StatisticsDetailViewData) {
        cardStatisticsDetailTotal.items = viewData.totalDuration
        cardStatisticsDetailRecords.items = viewData.timesTracked
        cardStatisticsDetailAverage.items = viewData.averageRecord
        cardStatisticsDetailDates.items = viewData.datesTracked
    }

    private fun updateChartViewData(viewData: StatisticsDetailChartViewData) {
        chartStatisticsDetail.setBars(viewData.data)
        chartStatisticsDetail.setLegendTextSuffix(viewData.legendSuffix)
    }

    private fun updateDailyChartViewData(viewData: StatisticsDetailChartViewData) {
        chartStatisticsDetailDaily.setBars(viewData.data)
        chartStatisticsDetailDaily.setLegendTextSuffix(viewData.legendSuffix)
    }

    companion object {
        private const val ARGS_ID = "args_id"
        private const val ARGS_FILTER_TYPE = "args_filter_type"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is StatisticsDetailParams -> {
                    putLong(ARGS_ID, data.id)
                    putSerializable(ARGS_FILTER_TYPE, data.filterType)
                }
            }
        }
    }
}
