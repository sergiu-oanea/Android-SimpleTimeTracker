package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StatisticsFilterTest : BaseUiTest() {

    @Test
    fun statisticsFilter() {
        val name = "Test1"
        val newName = "Test2"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()
        val firstIcon = iconMapper.availableIconsNames.values.first()
        val lastIcon = iconMapper.availableIconsNames.values.last()

        // Add activities
        NavUtils.addActivity(name, firstColor, firstIcon)
        NavUtils.addActivity(newName, lastColor, lastIcon)

        // Add records
        NavUtils.openRecordsScreen()
        NavUtils.addRecord(name)
        NavUtils.addRecord(newName)

        NavUtils.openStatisticsScreen()

        // All records displayed
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2h 0m")))

        // Filter untracked
        clickOnViewWithId(R.id.btnStatisticsChartFilter)
        Thread.sleep(1000)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.viewRecordTypeItem)),
                withText(R.string.untracked_time_name)
            )
        )
        pressBack()
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2h 0m")))

        // Filter activity
        clickOnViewWithId(R.id.btnStatisticsChartFilter)
        Thread.sleep(1000)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name)))
        pressBack()
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("1h 0m")))

        // Filter all
        clickOnViewWithId(R.id.btnStatisticsChartFilter)
        Thread.sleep(1000)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(newName)))
        pressBack()
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewDoesNotExist(withId(R.id.tvStatisticsInfoText))
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Remove filters
        clickOnViewWithId(R.id.btnStatisticsEmptyFilter)
        Thread.sleep(1000)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.viewRecordTypeItem)),
                withText(R.string.untracked_time_name)
            )
        )
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name)))
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(newName)))
        pressBack()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(R.id.tvStatisticsInfoText), withText("2h 0m")))
    }
}
