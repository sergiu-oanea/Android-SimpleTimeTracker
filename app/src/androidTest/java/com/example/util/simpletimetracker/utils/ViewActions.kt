package com.example.util.simpletimetracker.utils

import android.view.View
import android.view.ViewConfiguration
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.MotionEvents
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.ScrollToAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import com.google.android.material.tabs.TabLayout
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

fun selectTabAtPosition(tabIndex: Int): ViewAction {
    return object : ViewAction {
        override fun getDescription() = "with tab at index $tabIndex"

        override fun getConstraints() = allOf(
            isDisplayed(), isAssignableFrom(TabLayout::class.java)
        )

        override fun perform(uiController: UiController, view: View) {
            val tabAtIndex: TabLayout.Tab = (view as TabLayout).getTabAt(tabIndex)
                ?: throw PerformException.Builder()
                    .withCause(Throwable("No tab at index $tabIndex"))
                    .build()

            tabAtIndex.select()
        }
    }
}

fun unconstrainedClick(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = ViewMatchers.isEnabled()

        override fun getDescription(): String = "unconstrained click"

        override fun perform(uiController: UiController, view: View) {
            view.performClick()
        }
    }
}

fun nestedScrollTo(): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> = allOf(
            withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
            isDescendantOfA(
                anyOf(
                    isAssignableFrom(ScrollView::class.java),
                    isAssignableFrom(HorizontalScrollView::class.java),
                    isAssignableFrom(NestedScrollView::class.java)
                )
            )
        )

        override fun getDescription(): String = "nested scroll to"

        override fun perform(uiController: UiController, view: View) {
            ScrollToAction().perform(uiController, view)
        }
    }
}

fun drag(direction: Direction, offset: Int): ViewAction {
    return object : ViewAction {
        private val SWIPE_EVENT_COUNT = 10

        override fun getConstraints(): Matcher<View> = isDisplayed()

        override fun getDescription(): String = "dragging"

        override fun perform(uiController: UiController, view: View) {
            uiController.loopMainThreadUntilIdle()

            val coordinatesProvider: CoordinatesProvider = getCoordinatesProvider()
            val viewCoordinates = coordinatesProvider.calculateCoordinates(view)
            val destCoordinates = getDestinationCoordinates(viewCoordinates)

            val precision = Press.PINPOINT.describePrecision()
            val downEvent =
                MotionEvents.sendDown(uiController, viewCoordinates, precision).down

            try {
                val longPressTimeout = (ViewConfiguration.getLongPressTimeout() * 1.5).toLong()

                uiController.loopMainThreadForAtLeast(longPressTimeout)

                val steps: Array<FloatArray> = interpolate(
                    viewCoordinates,
                    destCoordinates
                )

                uiController.loopMainThreadUntilIdle()

                for (step in steps) {
                    if (!MotionEvents.sendMovement(uiController, downEvent, step)) {
                        MotionEvents.sendCancel(uiController, downEvent)
                    }
                }
                if (!MotionEvents.sendUp(uiController, downEvent, destCoordinates)) {
                    MotionEvents.sendCancel(uiController, downEvent)
                }
            } catch (e: Exception) {
                println(e)
            } finally {
                downEvent.recycle()
            }
        }

        private fun getCoordinatesProvider(): CoordinatesProvider {
            return CoordinatesProvider { view ->
                val location = IntArray(2)
                view.getLocationInWindow(location)
                val x = location[0] + (view.measuredWidth / 2).toFloat()
                val y = location[1] + (view.measuredHeight / 2).toFloat()
                floatArrayOf(x, y)
            }
        }

        private fun interpolate(start: FloatArray, end: FloatArray): Array<FloatArray> {
            val res = Array(SWIPE_EVENT_COUNT) { FloatArray(2) }

            for (i in 1..SWIPE_EVENT_COUNT) {
                res[i - 1][0] = start[0] + (end[0] - start[0]) * i / SWIPE_EVENT_COUNT
                res[i - 1][1] = start[1] + (end[1] - start[1]) * i / SWIPE_EVENT_COUNT
            }

            return res
        }

        private fun getDestinationCoordinates(initial: FloatArray): FloatArray {
            val destination = initial.clone()

            when (direction) {
                Direction.UP -> destination[1] = destination[1] - offset
                Direction.DOWN -> destination[1] = destination[1] + offset
                Direction.LEFT -> destination[0] = destination[0] - offset
                Direction.RIGHT -> destination[0] = destination[0] + offset
            }

            return destination
        }
    }
}