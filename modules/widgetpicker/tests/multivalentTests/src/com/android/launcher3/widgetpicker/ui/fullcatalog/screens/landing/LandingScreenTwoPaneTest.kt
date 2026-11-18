/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.widgetpicker.ui.fullcatalog.screens.landing

import android.platform.test.rule.AllowedDevices
import android.platform.test.rule.DeviceProduct
import android.platform.test.rule.LimitDevicesRule
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.launcher3.widgetpicker.R
import com.android.launcher3.widgetpicker.TestUtils
import com.android.launcher3.widgetpicker.TestUtils.PERSONAL_LABEL
import com.android.launcher3.widgetpicker.TestUtils.PERSONAL_TEST_APPS
import com.android.launcher3.widgetpicker.TestUtils.WORK_LABEL
import com.android.launcher3.widgetpicker.TestUtils.WORK_TEST_APPS
import com.android.launcher3.widgetpicker.TestUtils.workUser
import com.android.launcher3.widgetpicker.domain.interactor.WidgetAppIconsInteractor
import com.android.launcher3.widgetpicker.domain.interactor.WidgetsInteractor
import com.android.launcher3.widgetpicker.domain.usecase.FilterWidgetsForHostUseCase
import com.android.launcher3.widgetpicker.domain.usecase.GroupWidgetAppsByProfileUseCase
import com.android.launcher3.widgetpicker.repository.FakeWidgetAppIconsRepository
import com.android.launcher3.widgetpicker.repository.FakeWidgetUsersRepository
import com.android.launcher3.widgetpicker.repository.FakeWidgetsRepository
import com.android.launcher3.widgetpicker.shared.model.WidgetHostInfo
import com.android.launcher3.widgetpicker.shared.model.WidgetUserProfiles
import com.android.launcher3.widgetpicker.ui.rememberViewModel
import com.android.launcher3.widgetpicker.ui.theme.WidgetPickerTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@AllowedDevices(allowed = [DeviceProduct.CF_TABLET])
class LandingScreenTwoPaneTest {
    @get:Rule val limitDevicesRule = LimitDevicesRule()

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @OptIn(ExperimentalCoroutinesApi::class) private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val context = InstrumentationRegistry.getInstrumentation().context

    private lateinit var viewModel: LandingScreenViewModel
    private val widgetsRepository = FakeWidgetsRepository()
    private val widgetAppIconsRepository = FakeWidgetAppIconsRepository()
    private val widgetsUsersRepository = FakeWidgetUsersRepository()

    private lateinit var browseTabLabel: String
    private val featuredWidgetA = PERSONAL_TEST_APPS[0].widgets[0]
    private val featuredWidgetB = WORK_TEST_APPS[0].widgets[0]

    @Before
    fun setUp() {
        viewModel =
            LandingScreenViewModel(
                widgetsInteractor =
                    WidgetsInteractor(
                        widgetsRepository = widgetsRepository,
                        widgetUsersRepository = widgetsUsersRepository,
                        filterWidgetsForHostUseCase = FilterWidgetsForHostUseCase(WidgetHostInfo()),
                        getWidgetAppsByProfileUseCase = GroupWidgetAppsByProfileUseCase(),
                        backgroundContext = testDispatcher,
                    ),
                widgetAppIconsInteractor =
                    WidgetAppIconsInteractor(
                        widgetAppIconsRepository = widgetAppIconsRepository,
                        widgetsRepository = widgetsRepository,
                        backgroundContext = testDispatcher,
                    ),
            )

        browseTabLabel = context.resources.getString(R.string.browse_widgets_tab_label)

        widgetsRepository.seedWidgets(PERSONAL_TEST_APPS + WORK_TEST_APPS)
        widgetsUsersRepository.seedUserProfiles(
            WidgetUserProfiles(
                personal = TestUtils.widgetUserProfilePersonal,
                work = TestUtils.widgetUserProfileWork,
            ),
            workProfileUser = workUser,
        )
    }

    @Composable
    private fun TwoPaneTestContent() {
        val viewModel = rememberViewModel { viewModel }

        WidgetPickerTheme {
            LandingScreen(
                isCompact = false,
                onEnterSearchMode = {},
                onWidgetInteraction = {},
                showDragShadow = true,
                viewModel = viewModel,
            )
        }

        LaunchedEffect(Unit) { viewModel.onUiReady() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun showsBrowseSectionByDefault() =
        testScope.runTest {
            composeTestRule.setContent { TwoPaneTestContent() }

            runCurrent()
            composeTestRule.waitForIdle()

            // Has left pane
            val expectedLeftPaneTitle =
                context.resources.getString(R.string.widget_picker_left_pane_accessibility_label)
            composeTestRule
                .onNode(
                    SemanticsMatcher("Left paneTitle") {
                        it.config.getOrNull(SemanticsProperties.PaneTitle) == expectedLeftPaneTitle
                    }
                )
                .assertExists()
            // Browse (personal) tab as list item is selected by default
            composeTestRule
                .onNode(hasTextExactly(PERSONAL_LABEL))
                .assertExists()
                .assertContentDescriptionEquals("$PERSONAL_LABEL widgets")
                .assertIsSelected()
            composeTestRule
                .onNode(hasTextExactly(WORK_LABEL))
                .assertExists()
                .assertContentDescriptionEquals("$WORK_LABEL widgets")
                .assertIsNotSelected()
            // List on left showing personal apps
            composeTestRule
                .onNode(hasText(PERSONAL_TEST_APPS[0].title!!.toString()))
                .assertExists()
                .assertIsNotSelected()
        }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
    @Test
    fun clickingOnWorkTab_updatesAppsListInLeftPane() =
        testScope.runTest {
            composeTestRule.setContent { TwoPaneTestContent() }

            runCurrent()
            composeTestRule.waitForIdle()

            composeTestRule.onNode(hasTextExactly(PERSONAL_LABEL)).assertExists().assertIsSelected()
            composeTestRule.onNode(hasTextExactly(WORK_LABEL)).assertExists().assertIsNotSelected()

            composeTestRule.onNode(hasTextExactly(WORK_LABEL)).assertExists().performClick()
            runCurrent()
            composeTestRule.waitForIdle()

            composeTestRule.waitUntilAtLeastOneExists(hasTextExactly(WORK_LABEL) and isSelected())

            // Toolbar tabs state
            composeTestRule.onNode(hasTextExactly(WORK_LABEL)).assertExists().assertIsSelected()
            composeTestRule
                .onNode(hasTextExactly(PERSONAL_LABEL))
                .assertExists()
                .assertIsNotSelected()
            // Selecting toolbar tabs in 2-pane view doesn't reset what's showing in right pane; one
            // has to click on specific app header to see the widgets.
            // No recommendations showing
            composeTestRule
                .onNode(hasContentDescription(featuredWidgetA.label, substring = true))
                .assertIsNotDisplayed()
            // Has list of work apps showing
            composeTestRule
                .onNode(hasText(WORK_TEST_APPS[0].title!!.toString()))
                .assertExists()
                .assertIsNotSelected()
            // But not the personal apps
            composeTestRule
                .onNode(hasText(PERSONAL_TEST_APPS[0].title!!.toString()))
                .assertIsNotDisplayed()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun selectingAppOnLeft_updatesRightPane() =
        testScope.runTest {
            val appToSelect = PERSONAL_TEST_APPS[1].title!!.toString()

            composeTestRule.setContent { TwoPaneTestContent() }

            runCurrent()
            composeTestRule.waitForIdle()

            // Browse tab selected and right pane title indicating the same.
            composeTestRule.onNode(hasTextExactly(PERSONAL_LABEL)).assertIsSelected()

            composeTestRule.onNode(hasText(appToSelect)).assertExists().performClick()
            runCurrent()
            composeTestRule.waitForIdle()

            // Now app is selected and browse tab is not.
            composeTestRule.onNode(hasText(appToSelect)).assertIsSelected()
            composeTestRule.onNode(hasTextExactly(PERSONAL_LABEL)).assertIsNotSelected()
            // widgets for the selected app are showing
            composeTestRule
                .onNode(
                    hasContentDescription(PERSONAL_TEST_APPS[1].widgets[0].label, substring = true)
                )
                .assertIsDisplayed()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun noWidgets_showsError() =
        testScope.runTest {
            widgetsRepository.seedWidgets(listOf())

            composeTestRule.setContent { TwoPaneTestContent() }

            runCurrent()
            composeTestRule.waitForIdle()

            composeTestRule
                .onNode(hasText("Widgets and shortcuts aren\'t available"))
                .assertExists()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun noWorkProfile_noFloatingToolbar() =
        testScope.runTest {
            widgetsRepository.seedWidgets(PERSONAL_TEST_APPS)
            widgetsUsersRepository.seedUserProfiles(
                profiles =
                    WidgetUserProfiles(personal = TestUtils.widgetUserProfilePersonal, work = null),
                workProfileUser = null,
            )

            composeTestRule.setContent { TwoPaneTestContent() }

            runCurrent()
            composeTestRule.waitForIdle()

            // No toolbar i.e. single browse page
            composeTestRule.onNode(hasTextExactly(browseTabLabel)).assertDoesNotExist()
            // Widget apps list shown on left
            composeTestRule
                .onNode(hasText(PERSONAL_TEST_APPS[0].title!!.toString()))
                .assertIsDisplayed()
                .assertIsNotSelected()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pausedWorkProfile_workWidgetsNotShown() =
        testScope.runTest {
            val pausedError = "work apps paused"
            widgetsRepository.seedWidgets(PERSONAL_TEST_APPS + WORK_TEST_APPS)
            widgetsUsersRepository.seedUserProfiles(
                WidgetUserProfiles(
                    personal = TestUtils.widgetUserProfilePersonal,
                    work =
                        TestUtils.widgetUserProfileWork.copy(
                            paused = true,
                            pausedProfileMessage = "work apps paused",
                        ),
                ),
                workProfileUser = workUser,
            )

            composeTestRule.setContent { TwoPaneTestContent() }

            runCurrent()
            composeTestRule.waitForIdle()

            composeTestRule.onNode(hasTextExactly(WORK_LABEL)).assertExists().performClick()

            runCurrent()
            composeTestRule.waitForIdle()

            composeTestRule.onNode(hasText(pausedError)).assertIsDisplayed()
            // And work apps aren't displayed
            composeTestRule
                .onNode(hasText(WORK_TEST_APPS[0].title!!.toString()))
                .assertIsNotDisplayed()
        }

}
