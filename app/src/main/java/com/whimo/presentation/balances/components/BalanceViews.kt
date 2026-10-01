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
package com.whimo.presentation.balances.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.domain.transactions.models.getShortName
import com.whimo.presentation.balances.BalanceCommodityItemModel
import com.whimo.presentation.createtransaction.components.DashedDivider
import com.whimo.presentation.createtransaction.components.LineDivider
import com.whimo.presentation.main.components.HarvestSeasonTag
import com.whimo.presentation.main.components.TraceabilityStatusView
import com.whimo.presentation.ui.theme.ColorGray40
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.TextStyleMediumM
import com.whimo.presentation.ui.theme.TextStyleMediumS
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    val sampleData = listOf(
        CommodityGroupModel(
            id = "",
            name = "Cattle",
            commodities = listOf(
                CommodityModel(
                    id = "",
                    code = "1701",
                    name = "Type1",
                    unit = "kg",
                    hasRecipe = true,
                    group = null,
                    balance = 1.5f,
                ),
                CommodityModel(
                    id = "",
                    code = "1702",
                    name = "Type2",
                    unit = "kg",
                    hasRecipe = true,
                    group = null,
                    balance = 1.5f,
                ),
            )
        ),
        CommodityGroupModel(
            id = "",
            name = "Cocoa",
            commodities = listOf(
                CommodityModel(
                    id = "",
                    code = "1801",
                    name = "Cocoa beans, whole or broken, raw or roasted",
                    unit = "kg",
                    hasRecipe = true,
                    group = null,
                    balance = 1.5f,
                ),
                CommodityModel(
                    id = "",
                    code = "1802",
                    name = "Cocoa shells, husks, skins and other cocoa waste",
                    unit = "kg",
                    hasRecipe = true,
                    group = null,
                    balance = 1.5f,
                ),
                CommodityModel(
                    id = "",
                    code = "1803",
                    name = "Cocoa paste, whether or not defatted",
                    unit = "kg",
                    hasRecipe = true,
                    group = null,
                    balance = 1.5f,
                ),
                CommodityModel(
                    id = "",
                    code = "1804",
                    name = "Cocoa butter, fat and oil",
                    unit = "kg",
                    hasRecipe = false,
                    group = null,
                    balance = 1.5f,
                ),
                CommodityModel(
                    id = "",
                    code = "1805",
                    name = "Cocoa powder, not containing added sugar or other sweetening matter",
                    unit = "kg",
                    hasRecipe = false,
                    group = null,
                    balance = 1.5f,
                ),
                CommodityModel(
                    id = "",
                    code = "180612",
                    name = "Chocolate and other food preparations containing cocoa",
                    unit = "kg",
                    hasRecipe = false,
                    group = null,
                    balance = 1.5f,
                ),
            )
        ),
        CommodityGroupModel(
            id = "",
            name = "Coffee",
            commodities = listOf(
                CommodityModel(
                    id = "",
                    code = "1901",
                    name = "Coffee beans",
                    unit = "kg",
                    hasRecipe = false,
                    group = null,
                    balance = 1.5f,
                ),
            )
        ),
        CommodityGroupModel(
            id = "",
            name = "Palm Oil",
            commodities = listOf()
        ),
    )
    WhimoTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CommodityGroupList(
                modifier = Modifier.wrapContentHeight(),
                sections = sampleData
            )
            BalanceCommodityList(
                modifier = Modifier.wrapContentHeight(),
                items = sampleData[1].commodities!!.mapIndexed { index, commodity ->
                    BalanceCommodityItemModel(
                        commodity = commodity,
                        harvestSeason = HarvestSeasonModel(
                            id = "$index",
                            name = "Harvest season 2026/27",
                            status = HarvestSeasonStatus.Active,
                        ),
                        traceabilityStatus = when (index % 3) {
                            0 -> TraceabilityStatus.Full
                            1 -> TraceabilityStatus.Partial
                            else -> TraceabilityStatus.Conditional
                        },
                    )
                }
            )
        }
    }
}

@Composable
fun CommodityGroupList(
    modifier: Modifier = Modifier,
    sections: List<CommodityGroupModel>,
    onSelect: (CommodityGroupModel) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(sections) { section ->
            CommoditySectionItem(
                section = section,
                onClick = {
                    onSelect(section)
                }
            )
        }
    }
}

@Composable
fun BalanceCommodityList(
    modifier: Modifier = Modifier,
    networkAvailable: Boolean = true,
    items: List<BalanceCommodityItemModel>,
    onBalanceClick: (BalanceCommodityItemModel) -> Unit = {},
    onConvertClick: (CommodityModel) -> Unit = {},
) {
    val size = items.size

    LazyColumn(
        modifier = modifier,
    ) {
        itemsIndexed(items) { index, item ->
            BalanceCommodityItem(
                isEnabled = networkAvailable,
                item = item,
                onClick = { onBalanceClick(item) },
                onConvertClick = { onConvertClick(item.commodity) },
            )

            if (index < size - 1) {
                DashedDivider()
            } else {
                LineDivider()
            }
        }
    }
}

@Composable
fun CommodityBalancesList(
    modifier: Modifier = Modifier,
    networkAvailable: Boolean = true,
    commodities: List<CommodityModel>,
    onSelect: (CommodityModel) -> Unit = {},
) {
    BalanceCommodityList(
        modifier = modifier,
        networkAvailable = networkAvailable,
        items = commodities.map { BalanceCommodityItemModel(commodity = it) },
        onConvertClick = onSelect,
    )
}

@Composable
fun CommoditySectionItem(
    section: CommodityGroupModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
//        val icon = when (section.name) {
//            "Rubber" -> R.drawable.ic_rubber
//            "Soy" -> R.drawable.ic_soy
//            "Cattle" -> R.drawable.ic_cattle
//            "Palm Oil" -> R.drawable.ic_palm_oil
//            "Coffee" -> R.drawable.ic_coffee
//            "Cocoa" -> R.drawable.ic_cocoa
//            "Olive" -> R.drawable.ic_soy
//
//            else -> null
//        }
//
//        if (icon != null) {
//            Icon(
//                modifier = Modifier
//                    .size(40.dp)
//                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
//                    .padding(8.dp),
//                painter = painterResource(id = icon),
//                contentDescription = null,
//                tint = Color.Unspecified,
//            )
//        }

        Text(
            modifier = Modifier.weight(1f),
            text = section.name,
            style = TextStyleMediumS,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = ColorGray40,
        )
    }
}

@Composable
fun BalanceCommodityItem(
    isEnabled: Boolean = true,
    item: BalanceCommodityItemModel,
    onClick: () -> Unit,
    onConvertClick: () -> Unit,
) {
    val commodity = item.commodity

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.width(40.dp),
            text = commodity.code,
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = commodity.name,
                    style = TextStyleMediumM,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = commodity.getBalanceText(),
                    style = TextStyleMediumM,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (item.harvestSeason != null || item.traceabilityStatus != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    item.harvestSeason?.let { season ->
                        HarvestSeasonTag(
                            text = season.getShortName(),
                            status = season.status,
                        )
                    }

                    item.traceabilityStatus?.let { status ->
                        TraceabilityStatusView(status = status, shortName = true)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(24.dp)
                .alpha(if (isEnabled) 1f else 0.5f),
            contentAlignment = Alignment.Center,
        ) {
            if (commodity.hasRecipe) {
                Icon(
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(isEnabled) { onConvertClick() },
                    painter = painterResource(id = R.drawable.ic_convert),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun CommodityItem(
    isEnabled: Boolean = true,
    commodity: CommodityModel,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = commodity.code,
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            modifier = Modifier.weight(1f),
            text = commodity.name,
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = commodity.getBalanceText(),
            style = TextStyleMediumS,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .padding(start = 18.dp)
                .size(20.dp)
                .alpha(if (isEnabled) 1f else 0.5f),
        ) {
            if (commodity.hasRecipe) {
                Icon(
                    modifier = Modifier.size(20.dp)
                        .clickable(isEnabled) { onSelect() },
                    painter = painterResource(id = R.drawable.ic_convert),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
