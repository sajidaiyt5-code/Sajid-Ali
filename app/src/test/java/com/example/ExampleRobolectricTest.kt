package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNameMatchesUkVirtualPhone() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("UK Virtual Phone", appName)
    }

    @Test
    fun homeScreenAndCalculatorCujWorks() {
        composeRule.onNodeWithTag("virtual_status_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("virtual_navigation_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("open_app_drawer_button").performClick()
        composeRule.onNodeWithTag("app_drawer_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("app_icon_calculator").performClick()
        composeRule.onNodeWithTag("calc_btn_7").performScrollTo().performClick()
        composeRule.onNodeWithTag("calc_btn_+").performScrollTo().performClick()
        composeRule.onNodeWithTag("calc_btn_5").performScrollTo().performClick()
        composeRule.onNodeWithTag("calc_btn_=").performScrollTo().performClick()
        composeRule.onNodeWithTag("calculator_result").assertTextEquals("12")
        composeRule.onNodeWithTag("nav_home_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("open_app_drawer_button").assertIsDisplayed()
    }
}
