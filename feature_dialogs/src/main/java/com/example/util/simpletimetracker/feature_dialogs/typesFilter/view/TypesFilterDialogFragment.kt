package com.example.util.simpletimetracker.feature_dialogs.typesFilter.view

import android.content.Context
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
import com.example.util.simpletimetracker.core.dialog.TypesFilterDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.adapter.TypesFilterAdapter
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.di.TypesFilterComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.extra.TypesFilterExtra
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.viewModel.TypesFilterViewModel
import com.example.util.simpletimetracker.navigation.params.TypesFilterDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.types_filter_dialog_fragment.btnTypesFilterHideAll
import kotlinx.android.synthetic.main.types_filter_dialog_fragment.btnTypesFilterShowAll
import kotlinx.android.synthetic.main.types_filter_dialog_fragment.rvTypesFilterContainer
import javax.inject.Inject

class TypesFilterDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<TypesFilterViewModel>

    private val viewModel: TypesFilterViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val recordTypesAdapter: TypesFilterAdapter by lazy {
        TypesFilterAdapter(viewModel::onRecordTypeClick)
    }

    private var behavior: BottomSheetBehavior<View>? = null
    private var typesFilterDialogListener: TypesFilterDialogListener? = null

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
            R.layout.types_filter_dialog_fragment,
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
            is TypesFilterDialogListener -> {
                typesFilterDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is TypesFilterDialogListener && it.isResumed }
                    ?.let { typesFilterDialogListener = it as? TypesFilterDialogListener }
            }
        }
    }

    private fun initDialog() {
        dialog?.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.let { bottomSheet ->
            behavior = BottomSheetBehavior.from(bottomSheet)
        }
        behavior?.apply {
            peekHeight = 0
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initDi() {
        (activity?.application as TypesFilterComponentProvider)
            .typesFilterComponent
            ?.inject(this)
    }

    private fun initUi() {
        rvTypesFilterContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    private fun initUx() {
        btnTypesFilterShowAll.setOnClick(viewModel::onShowAllClick)
        btnTypesFilterHideAll.setOnClick(viewModel::onHideAllClick)
    }

    private fun initViewModel(): Unit = with(viewModel) {
        extra = TypesFilterExtra(arguments?.getLongArray(ARGS_SELECTED_TYPES)?.toList().orEmpty())
        recordTypes.observe(viewLifecycleOwner, recordTypesAdapter::replace)
        typesSelected.observe(viewLifecycleOwner) { typesFilterDialogListener?.onTypesSelected(it) }
    }

    companion object {
        private const val ARGS_SELECTED_TYPES = "selected_types"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is TypesFilterDialogParams -> {
                    putLongArray(ARGS_SELECTED_TYPES, data.selectedTypes.toLongArray())
                }
            }
        }
    }
}