/*
 * Copyright (c) 2025 EFI (https://efi.int/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.whimo.presentation.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.HarvestSeasonStatus
import com.whimo.domain.transactions.models.getShortName
import com.whimo.presentation.createtransaction.components.DashedDivider
import com.whimo.presentation.createtransaction.components.LineDivider
import com.whimo.presentation.settings.components.BottomSheetBase
import com.whimo.presentation.settings.components.DialogButtonsItem
import com.whimo.presentation.ui.theme.ColorBerryBlue
import com.whimo.presentation.ui.theme.ColorBirchWhite
import com.whimo.presentation.ui.theme.ColorGray30
import com.whimo.presentation.ui.theme.ColorGray60
import com.whimo.presentation.ui.theme.ColorHarvestSeasonActiveBackground
import com.whimo.presentation.ui.theme.ColorHarvestSeasonActiveContent
import com.whimo.presentation.ui.theme.ColorHarvestSeasonPastBackground
import com.whimo.presentation.ui.theme.ColorHarvestSeasonPastContent
import com.whimo.presentation.ui.theme.ColorSuccess
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.TextStyleMediumM
import com.whimo.presentation.ui.theme.TextStyleMediumXS
import com.whimo.presentation.ui.theme.WhimoTheme
import java.time.LocalDate

@Preview
@Composable
private fun PreviewHarvestSeasonViews() {
    val seasons = listOf(
        HarvestSeasonModel("1", "Harvest season 2026/27", HarvestSeasonStatus.Active),
        HarvestSeasonModel("2", "Harvest season 2025/26", HarvestSeasonStatus.Past),
        HarvestSeasonModel("3", "Harvest season 2024/25", HarvestSeasonStatus.Archived),
    )

    WhimoTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HarvestSeasonSelector(
                seasons = seasons,
                selectedSeason = seasons.first(),
            )
            HarvestSeasonTag(
                text = seasons.first().getShortName(),
                status = seasons.first().status,
            )
            HarvestSeasonStatusBadge(status = HarvestSeasonStatus.Active)
        }
    }
}

@Preview
@Composable
private fun PreviewHarvestSeasonFilterBottomSheetInitial() {
    WhimoTheme {
        HarvestSeasonFilterBottomSheet(
            commodityGroups = previewCommodityGroups(),
            harvestSeasons = previewHarvestSeasons(),
            selectedCommodityGroup = null,
            selectedSeason = null,
            onCommodityGroupPreviewChanged = {},
            onApply = { _, _ -> },
            onReset = {},
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun PreviewHarvestSeasonFilterBottomSheetSelectedGroup() {
    val commodityGroups = previewCommodityGroups()

    WhimoTheme {
        HarvestSeasonFilterBottomSheet(
            commodityGroups = commodityGroups,
            harvestSeasons = previewHarvestSeasons(),
            selectedCommodityGroup = commodityGroups.first(),
            selectedSeason = null,
            onCommodityGroupPreviewChanged = {},
            onApply = { _, _ -> },
            onReset = {},
            onDismissRequest = {},
        )
    }
}

@Composable
fun HarvestSeasonFilterBottomSheet(
    commodityGroups: List<CommodityGroupModel>,
    harvestSeasons: List<HarvestSeasonModel>,
    selectedCommodityGroup: CommodityGroupModel?,
    selectedSeason: HarvestSeasonModel?,
    onCommodityGroupPreviewChanged: (CommodityGroupModel?) -> Unit,
    onApply: (CommodityGroupModel?, HarvestSeasonModel?) -> Unit,
    onReset: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    var draftCommodityGroup by remember(selectedCommodityGroup) {
        mutableStateOf(selectedCommodityGroup)
    }
    var draftHarvestSeason by remember(selectedSeason) {
        mutableStateOf(selectedSeason)
    }
    var commodityGroupsExpanded by remember {
        mutableStateOf(false)
    }
    var harvestSeasonsExpanded by remember {
        mutableStateOf(false)
    }
    val harvestSeasonSelectorEnabled = draftCommodityGroup != null
    val filterButtonsEnabled = draftCommodityGroup != null

    LaunchedEffect(harvestSeasons, draftHarvestSeason) {
        if (draftHarvestSeason != null && harvestSeasons.none { it.sameSeasonAs(draftHarvestSeason) }) {
            draftHarvestSeason = null
        }
    }

    LaunchedEffect(harvestSeasonSelectorEnabled) {
        if (!harvestSeasonSelectorEnabled) {
            draftHarvestSeason = null
            harvestSeasonsExpanded = false
        }
    }

    BottomSheetBase(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp),
                text = stringResource(R.string.filter),
                style = TextStyleMediumM,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                HarvestSeasonFilterSectionHeader(
                    title = draftCommodityGroup?.name ?: stringResource(R.string.all_commodity_groups),
                    expanded = commodityGroupsExpanded,
                    enabled = true,
                    onClick = {
                        commodityGroupsExpanded = !commodityGroupsExpanded
                        if (commodityGroupsExpanded) harvestSeasonsExpanded = false
                    },
                )

                AnimatedVisibility(visible = commodityGroupsExpanded) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val allCommodityGroupsSelected = draftCommodityGroup == null

                        FilterOptionRow(
                            title = stringResource(R.string.all_commodity_groups),
                            selected = allCommodityGroupsSelected,
                            onClick = {
                                draftCommodityGroup = null
                                draftHarvestSeason = null
                                harvestSeasonsExpanded = false
                            },
                        )
                        FilterOptionDivider(isLast = commodityGroups.isEmpty())

                        commodityGroups.forEachIndexed { index, group ->
                            FilterOptionRow(
                                title = group.name,
                                selected = group.id == draftCommodityGroup?.id,
                                onClick = {
                                    draftCommodityGroup = group
                                    draftHarvestSeason = null
                                    harvestSeasonsExpanded = false
                                    onCommodityGroupPreviewChanged(group)
                                },
                            )
                            FilterOptionDivider(isLast = index == commodityGroups.lastIndex)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                HarvestSeasonFilterSectionHeader(
                    title = draftHarvestSeason?.name ?: stringResource(R.string.all_harvest_seasons),
                    expanded = harvestSeasonsExpanded,
                    enabled = harvestSeasonSelectorEnabled,
                    onClick = {
                        harvestSeasonsExpanded = !harvestSeasonsExpanded
                        if (harvestSeasonsExpanded) commodityGroupsExpanded = false
                    },
                )

                AnimatedVisibility(visible = harvestSeasonsExpanded && harvestSeasonSelectorEnabled) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val allHarvestSeasonsSelected = draftHarvestSeason == null

                        FilterOptionRow(
                            title = stringResource(R.string.all_harvest_seasons),
                            selected = allHarvestSeasonsSelected,
                            onClick = {
                                draftHarvestSeason = null
                                harvestSeasonsExpanded = false
                            },
                        )
                        FilterOptionDivider(isLast = harvestSeasons.isEmpty())

                        harvestSeasons.forEachIndexed { index, season ->
                            HarvestSeasonFilterRow(
                                season = season,
                                selected = season.sameSeasonAs(draftHarvestSeason),
                                onClick = {
                                    draftHarvestSeason = season
                                    harvestSeasonsExpanded = false
                                },
                            )
                            FilterOptionDivider(isLast = index == harvestSeasons.lastIndex)
                        }
                    }
                }
            }

            DialogButtonsItem(
                actionButtonTitle = stringResource(R.string.apply),
                actionButtonBackgroundColor = MaterialTheme.colorScheme.primary,
                actionButtonTitleColor = MaterialTheme.colorScheme.onPrimary,
                actionButtonEnabled = filterButtonsEnabled,
                secondButtonTitle = stringResource(R.string.reset),
                secondButtonEnabled = filterButtonsEnabled,
                onActionClick = {
                    onApply(draftCommodityGroup, draftHarvestSeason)
                    onDismissRequest()
                },
                onSecondClick = {
                    draftCommodityGroup = null
                    draftHarvestSeason = null
                    onReset()
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
fun HarvestSeasonFilterChip(
    commodityGroup: CommodityGroupModel?,
    harvestSeason: HarvestSeasonModel?,
    modifier: Modifier = Modifier,
    onClear: () -> Unit,
) {
    val title = when {
        commodityGroup != null && harvestSeason != null -> "${commodityGroup.name}, ${harvestSeason.getShortName()}"
        commodityGroup != null -> commodityGroup.name
        harvestSeason != null -> harvestSeason.getShortName()
        else -> return
    }

    Row(
        modifier = modifier
            .background(color = ColorBirchWhite, shape = RoundedCornerShape(4.dp))
            .clickable { onClear() }
            .padding(start = 12.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = TextStyleMediumXS,
            color = ColorBerryBlue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.ic_close),
            contentDescription = stringResource(R.string.clear),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun HarvestSeasonSelector(
    modifier: Modifier = Modifier,
    seasons: List<HarvestSeasonModel>,
    selectedSeason: HarvestSeasonModel?,
    enabled: Boolean = true,
    onSelect: (HarvestSeasonModel) -> Unit = {},
    onSeasonSelected: (HarvestSeasonModel?) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var selectorWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                selectorWidth = with(density) { coordinates.size.width.toDp() }
            },
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            HarvestSeasonSelectorHeader(
                title = selectedSeason?.name ?: stringResource(R.string.select_harvest_season),
                expanded = expanded,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    ),
                enabled = enabled && seasons.isNotEmpty(),
                onClick = { expanded = !expanded },
            )
        }

        DropdownMenu(
            modifier = Modifier.width(selectorWidth),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                seasons.forEach { season ->
                    HarvestSeasonSelectorRow(
                        season = season,
                        selected = season.sameSeasonAs(selectedSeason),
                        onClick = {
                            expanded = false
                            onSelect(season)
                            onSeasonSelected(season)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HarvestSeasonFilterSectionHeader(
    title: String,
    expanded: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = when {
        !enabled -> ColorGray60
        expanded -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (expanded) MaterialTheme.colorScheme.surfaceBright else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = if (expanded) TextStyleMediumM else TextStyleBodyM,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(
                id = if (expanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
            ),
            contentDescription = null,
            tint = contentColor,
        )
    }
}

@Composable
private fun FilterOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = TextStyleBodyM,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (selected) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = ColorSuccess,
            )
        }
    }
}

@Composable
private fun FilterOptionDivider(
    isLast: Boolean,
) {
    if (isLast) {
        LineDivider()
    } else {
        DashedDivider()
    }
}

@Composable
private fun HarvestSeasonFilterRow(
    season: HarvestSeasonModel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = season.name,
            style = TextStyleBodyM,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        season.status?.let { status ->
            HarvestSeasonStatusBadge(status = status)
        }

        if (selected) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = ColorSuccess,
            )
        }
    }
}

@Composable
private fun HarvestSeasonSelectorHeader(
    title: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_calendar),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(
                id = if (expanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down
            ),
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}

@Composable
private fun HarvestSeasonSelectorRow(
    season: HarvestSeasonModel,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = season.name,
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.onSurface,
            )

            season.status?.let { status ->
                HarvestSeasonStatusBadge(status = status)
            }
        }

        if (selected) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = Color.Unspecified,
            )
        }
    }
}

@Composable
fun HarvestSeasonTag(
    text: String,
    modifier: Modifier = Modifier,
    status: HarvestSeasonStatus? = null,
) {
    val colors = harvestSeasonBadgeColors(status)

    Box(
        modifier = modifier
            .background(color = colors.background, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = colors.content,
            style = TextStyleMediumXS,
        )
    }
}

@Composable
fun HarvestSeasonStatusBadge(
    status: HarvestSeasonStatus,
    modifier: Modifier = Modifier,
) {
    val colors = harvestSeasonBadgeColors(status)
    val text: String

    when (status) {
        HarvestSeasonStatus.Active -> {
            text = stringResource(R.string.active)
        }
        HarvestSeasonStatus.Past -> {
            text = stringResource(R.string.past)
        }
        HarvestSeasonStatus.Archived -> {
            text = stringResource(R.string.archived)
        }
    }

    Box(
        modifier = modifier
            .background(color = colors.background, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = colors.content,
            style = TextStyleMediumXS,
        )
    }
}

private data class HarvestSeasonBadgeColors(
    val background: Color,
    val content: Color,
)

@Composable
private fun harvestSeasonBadgeColors(status: HarvestSeasonStatus?): HarvestSeasonBadgeColors {
    return when (status) {
        HarvestSeasonStatus.Active -> HarvestSeasonBadgeColors(
            background = ColorHarvestSeasonActiveBackground,
            content = ColorHarvestSeasonActiveContent,
        )
        HarvestSeasonStatus.Past -> HarvestSeasonBadgeColors(
            background = ColorHarvestSeasonPastBackground,
            content = ColorHarvestSeasonPastContent,
        )
        HarvestSeasonStatus.Archived -> HarvestSeasonBadgeColors(
            background = ColorGray30,
            content = Color.White,
        )
        null -> HarvestSeasonBadgeColors(
            background = MaterialTheme.colorScheme.surfaceVariant,
            content = ColorGray60,
        )
    }
}

private fun HarvestSeasonModel.sameSeasonAs(other: HarvestSeasonModel?): Boolean {
    if (other == null) return false
    if (id == other.id) return true
    return name == other.name
}

private fun previewCommodityGroups(): List<CommodityGroupModel> {
    return listOf(
        previewCommodityGroup("cocoa", "Cocoa"),
        previewCommodityGroup("coffee", "Coffee"),
        previewCommodityGroup("palm-oil", "Palm oil"),
        previewCommodityGroup("cattle", "Cattle"),
        previewCommodityGroup("rubber", "Rubber"),
        previewCommodityGroup("soy", "Soy"),
    )
}

private fun previewCommodityGroup(
    id: String,
    name: String,
): CommodityGroupModel {
    return CommodityGroupModel(
        id = id,
        name = name,
        commodities = listOf(
            CommodityModel(
                id = "$id-commodity",
                code = "1801",
                name = "$name commodity",
                unit = "kg",
                hasRecipe = false,
                group = null,
                balance = null,
            )
        ),
    )
}

private fun previewHarvestSeasons(): List<HarvestSeasonModel> {
    return listOf(
        HarvestSeasonModel(
            id = "2025-26",
            name = "Cocoa 2025/26",
            status = HarvestSeasonStatus.Active,
            startDate = LocalDate.of(2025, 9, 1),
            endDate = LocalDate.of(2026, 9, 1),
        ),
        HarvestSeasonModel(
            id = "2024-25",
            name = "Cocoa 2024/25",
            status = HarvestSeasonStatus.Past,
            startDate = LocalDate.of(2024, 9, 1),
            endDate = LocalDate.of(2025, 9, 1),
        ),
        HarvestSeasonModel(
            id = "2023-24",
            name = "Cocoa 2023/24",
            status = HarvestSeasonStatus.Archived,
            startDate = LocalDate.of(2023, 9, 1),
            endDate = LocalDate.of(2024, 9, 1),
        ),
    )
}
