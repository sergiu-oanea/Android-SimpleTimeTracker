package com.example.util.simpletimetracker.feature_change_record.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.rotateDown
import com.example.util.simpletimetracker.core.extension.rotateUp
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.utils.BuildVersions
import com.example.util.simpletimetracker.core.viewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordAdapter
import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponentProvider
import com.example.util.simpletimetracker.feature_change_record.extra.ChangeRecordExtra
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import com.example.util.simpletimetracker.navigation.params.ChangeRecordParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.change_record_fragment.*
import javax.inject.Inject

class ChangeRecordFragment : BaseFragment(R.layout.change_record_fragment),
    DateTimeDialogListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    private val viewModel: ChangeRecordViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val removeRecordViewModel: RemoveRecordViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { removeRecordViewModelFactory }
    )
    private val typesAdapter: ChangeRecordAdapter by lazy {
        ChangeRecordAdapter(viewModel::onTypeClick)
    }
    private val extra: ChangeRecordExtra by lazy {
        arguments?.getParcelable<ChangeRecordExtra>(ARGS_EXTRA) ?: ChangeRecordExtra.New()
    }

    override fun initDi() {
        (activity?.application as ChangeRecordComponentProvider)
            .changeRecordComponent
            ?.inject(this)
    }

    override fun initUi() {
        if (BuildVersions.isLollipopOrHigher()) {
            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(android.R.transition.move)
        }

        val transitionName: String = when (extra) {
            is ChangeRecordExtra.Tracked -> (extra as? ChangeRecordExtra.Tracked)?.transitionName.orEmpty()
            is ChangeRecordExtra.Untracked -> (extra as? ChangeRecordExtra.Untracked)?.transitionName.orEmpty()
            else -> ""
        }
        ViewCompat.setTransitionName(previewChangeRecord, transitionName)

        rvChangeRecordType.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    override fun initUx() {
        fieldChangeRecordType.setOnClick(viewModel::onTypeChooserClick)
        fieldChangeRecordTimeStarted.setOnClick(viewModel::onTimeStartedClick)
        fieldChangeRecordTimeEnded.setOnClick(viewModel::onTimeEndedClick)
        btnChangeRecordSave.setOnClick(viewModel::onSaveClick)
        btnChangeRecordDelete.setOnClick {
            viewModel.onDeleteClick()
            removeRecordViewModel.onDeleteClick()
        }
    }

    override fun initViewModel() {
        with(viewModel) {
            extra = this@ChangeRecordFragment.extra
            record.observe(viewLifecycleOwner, ::updatePreview)
            types.observe(viewLifecycleOwner, typesAdapter::replace)
            saveButtonEnabled.observe(viewLifecycleOwner, btnChangeRecordSave::setEnabled)
            flipTypesChooser.observe(viewLifecycleOwner) { opened ->
                rvChangeRecordType.visible = opened
                arrowChangeRecordType.apply {
                    if (opened) rotateDown() else rotateUp()
                }
            }
        }
        with(removeRecordViewModel) {
            prepare((extra as? ChangeRecordExtra.Tracked)?.id.orZero())
            deleteButtonEnabled.observe(viewLifecycleOwner, btnChangeRecordDelete::setEnabled)
            deleteIconVisibility.observe(viewLifecycleOwner, btnChangeRecordDelete::visible::set)
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun updatePreview(item: ChangeRecordViewData) {
        with(previewChangeRecord) {
            itemName = item.name
            itemIcon = item.iconId
            itemColor = item.color
            itemTimeStarted = item.timeStarted
            itemTimeEnded = item.timeFinished
            itemDuration = item.duration
        }
        tvChangeRecordTimeStarted.text = item.dateTimeStarted
        tvChangeRecordTimeEnded.text = item.dateTimeFinished
    }

    companion object {
        private const val ARGS_EXTRA = "args_extra"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is ChangeRecordParams -> {
                    val extra = when (data) {
                        is ChangeRecordParams.Tracked -> {
                            ChangeRecordExtra.Tracked(
                                transitionName = data.transitionName,
                                id = data.id
                            )
                        }
                        is ChangeRecordParams.Untracked -> {
                            ChangeRecordExtra.Untracked(
                                transitionName = data.transitionName,
                                timeStarted = data.timeStarted,
                                timeEnded = data.timeEnded
                            )
                        }
                        is ChangeRecordParams.New -> {
                            ChangeRecordExtra.New(data.daysFromToday)
                        }
                    }
                    putParcelable(ARGS_EXTRA, extra)
                }
            }
        }
    }
}