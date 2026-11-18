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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.launcher3.widgetpicker.shared.model.WidgetAppId
import com.android.launcher3.widgetpicker.ui.WidgetInteractionInfo
import com.android.launcher3.widgetpicker.ui.components.WidgetsSearchBar
import com.android.launcher3.widgetpicker.ui.fullcatalog.screens.landing.LandingScreenDimensions.WEIGHT_FILL_REMAINING_SPACE

/**
 * View displayed when user opens the full catalog of widgets in widget picker.
 *
 * @param isCompact indicates whether to show the compact single pane layout or the two pane layout.
 * @param onEnterSearchMode callback for when user focuses on the search bar.
 * @param onWidgetInteraction callback for when user interacts with a widget.
 * @param showDragShadow indicates whether to show the drag shadow when user long presses on a
 *   widget to drag it.
 * @param viewModel the view model backing the state and data for the landing screen.
 */
@Composable
fun LandingScreen(
    isCompact: Boolean,
    onEnterSearchMode: () -> Unit,
    onWidgetInteraction: (WidgetInteractionInfo) -> Unit,
    showDragShadow: Boolean,
    viewModel: LandingScreenViewModel,
) {
    val browseState = viewModel.browseWidgetsState

    if (browseState is BrowseWidgetsState.Data) {
        val topSearchRow: @Composable () -> Unit =
            {
                TopSearchRow(
                    resetSectionSelections = viewModel::resetSelections,
                    onEnterSearchMode = onEnterSearchMode,
                    onWidgetInteraction = onWidgetInteraction,
                )
            }

        LandingScreen(
            isCompact = isCompact,
            selectedSubSection = viewModel.selectedSubSection,
            searchBarContent = topSearchRow,
            widgetAppIconsState = viewModel.appIconsState,
            browseWidgetsState = browseState,
            personalWidgetPreviewsState = viewModel.personalWidgetsPreviewsState,
            workWidgetPreviewsState = viewModel.workWidgetsPreviewState,
            selectedPersonalWidgetAppId = viewModel.selectedPersonalAppId,
            onPersonalWidgetAppToggle = viewModel::onSelectedPersonalAppToggle,
            selectedWorkWidgetAppId = viewModel.selectedWorkAppId,
            onWorkWidgetAppToggle = viewModel::onSelectedWorkAppToggle,
            onWidgetInteraction = onWidgetInteraction,
            showDragShadow = showDragShadow,
            onSelectedSubSectionChange = viewModel::onSelectedSubSectionChange,
        )
    }
}

@Composable
private fun TopSearchRow(
    resetSectionSelections: () -> Unit,
    onEnterSearchMode: () -> Unit,
    onWidgetInteraction: (WidgetInteractionInfo) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(WEIGHT_FILL_REMAINING_SPACE)) {
            WidgetsSearchBar(
                text = "",
                isSearching = false,
                onSearch = {},
                onToggleSearchMode = { enter ->
                    if (enter) {
                        resetSectionSelections()
                        onEnterSearchMode()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LandingScreen(
    isCompact: Boolean,
    selectedSubSection: LandingScreenSubSection,
    searchBarContent: @Composable () -> Unit,
    widgetAppIconsState: AppIconsState,
    browseWidgetsState: BrowseWidgetsState.Data,
    personalWidgetPreviewsState: PreviewsState,
    workWidgetPreviewsState: PreviewsState,
    selectedPersonalWidgetAppId: WidgetAppId?,
    onPersonalWidgetAppToggle: (WidgetAppId?) -> Unit,
    selectedWorkWidgetAppId: WidgetAppId?,
    onWorkWidgetAppToggle: (WidgetAppId?) -> Unit,
    onWidgetInteraction: (WidgetInteractionInfo) -> Unit,
    showDragShadow: Boolean,
    onSelectedSubSectionChange: (LandingScreenSubSection) -> Unit,
) {
    when {
        isCompact ->
            LandingScreenSinglePane(
                selectedSubSection = selectedSubSection,
                searchBarContent = searchBarContent,
                widgetAppIconsState = widgetAppIconsState,
                browseWidgetsState = browseWidgetsState,
                personalWidgetPreviewsState = personalWidgetPreviewsState,
                workWidgetPreviewsState = workWidgetPreviewsState,
                selectedPersonalWidgetAppId = selectedPersonalWidgetAppId,
                onPersonalWidgetAppToggle = onPersonalWidgetAppToggle,
                selectedWorkWidgetAppId = selectedWorkWidgetAppId,
                onWorkWidgetAppToggle = onWorkWidgetAppToggle,
                onWidgetInteraction = onWidgetInteraction,
                showDragShadow = showDragShadow,
                onSelectedSubSectionChange = onSelectedSubSectionChange,
            )

        else ->
            LandingScreenTwoPane(
                selectedSubSection = selectedSubSection,
                searchBar = searchBarContent,
                widgetAppIconsState = widgetAppIconsState,
                browseWidgetsState = browseWidgetsState,
                personalWidgetPreviewsState = personalWidgetPreviewsState,
                workWidgetPreviewsState = workWidgetPreviewsState,
                selectedPersonalWidgetAppId = selectedPersonalWidgetAppId,
                onPersonalWidgetAppToggle = onPersonalWidgetAppToggle,
                selectedWorkWidgetAppId = selectedWorkWidgetAppId,
                onWorkWidgetAppToggle = onWorkWidgetAppToggle,
                onWidgetInteraction = onWidgetInteraction,
                showDragShadow = showDragShadow,
                onSelectedSubSectionChange = onSelectedSubSectionChange,
            )
    }
}

private object LandingScreenDimensions {
    val TopSearchRowItemSpacing = 8.dp

    const val WEIGHT_FILL_REMAINING_SPACE = 1f
}
