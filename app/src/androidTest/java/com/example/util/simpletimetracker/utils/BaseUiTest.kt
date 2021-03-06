package com.example.util.simpletimetracker.utils

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.util.simpletimetracker.TimeTrackerApp
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment
import com.example.util.simpletimetracker.ui.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

open class BaseUiTest {

    @Inject
    lateinit var testUtils: TestUtils

    @Inject
    lateinit var iconMapper: IconMapper

    @Inject
    lateinit var timeMapper: TimeMapper

    @Rule
    @JvmField
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    init {
        inject()
    }

    @Before
    fun setUp() {
        clearData()
        disableAnimations()
        registerIdlingResource()
    }

    @After
    fun after() {
        enableAnimations()
        unregisterIdlingResource()
    }

    private fun inject() {
        val app = ApplicationProvider.getApplicationContext() as TimeTrackerApp
        DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .build()
            .inject(this)
    }

    private fun clearData() {
        testUtils.clearDatabase()
        testUtils.clearPrefs()
    }

    private fun disableAnimations() {
        RecordsContainerFragment.viewPagerSmoothScroll = false
        StatisticsContainerFragment.viewPagerSmoothScroll = false
    }

    private fun enableAnimations() {
        RecordsContainerFragment.viewPagerSmoothScroll = true
        StatisticsContainerFragment.viewPagerSmoothScroll = true
    }

    private fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(CountingIdlingResourceProvider.countingIdlingResource)
    }

    private fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(CountingIdlingResourceProvider.countingIdlingResource)
    }
}