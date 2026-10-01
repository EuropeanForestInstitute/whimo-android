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
package com.whimo.presentation.transactions.transactiondetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.HarvestSeasonStatus
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.domain.transactions.models.getCommodityVolumeText
import com.whimo.domain.transactions.models.getShortName
import com.whimo.extensions.toFormattedDateString
import com.whimo.presentation.main.components.HarvestSeasonStatusBadge
import com.whimo.presentation.main.components.TraceabilityStatusView
import com.whimo.presentation.main.components.TransactionStatusView
import com.whimo.presentation.ui.theme.ColorGray40
import com.whimo.presentation.ui.theme.ColorMulberryPurple
import com.whimo.presentation.ui.theme.ColorSeaBlue
import com.whimo.presentation.ui.theme.ColorSuccess
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.TextStyleBodyXS
import com.whimo.presentation.ui.theme.TextStyleButtonM
import com.whimo.presentation.ui.theme.TextStyleMediumL
import com.whimo.presentation.ui.theme.TextStyleMediumM
import com.whimo.presentation.ui.theme.TextStyleMediumS
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    val items = listOf(
        PieChartItem("Partial", 40, ColorMulberryPurple),
        PieChartItem("Conditional", 28, ColorSeaBlue),
        PieChartItem("Full", 12, ColorSuccess),
    )

    WhimoTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {

            TitleDescriptionView(
                title = "Commodity type",
                description = "1801 Cocoa beans, whole or broken, raw or roasted, 800kg"
            )

            TransactionInfoItem1(
                title = "Buyer ID",
                description = "#155796 (You)",
            )

            TransactionInfoItem2(
                title = "Supplier information",
                description = "#124856",
            )

            TransactionInfoItem3(
                title = "Farm geodata",
                description = "Data is missing",
            )

            TransactionInfoStatusItem(
                status = TransactionStatus.Pending,
            )

            TransactionInfoTraceabilityItem(
                status = TraceabilityStatus.Full,
            )

            TransactionInfoHarvestSeasonItem(
                season = HarvestSeasonModel(
                    id = "1",
                    name = "Harvest season 2026/27",
                    status = HarvestSeasonStatus.Active,
                ),
            )

            TransactionInfoTraceabilityItem(
                status = TraceabilityStatus.Partial,
                chartTitle = "traders",
                chartItems = items,
            )

            SectionHeader(
                title = "My supplier’s history",
                onIconClick = {}
            )

            SupplierHistoryItem(
                status = TraceabilityStatus.Partial,
                title = "from Trader #596572",
                description = "April 25, 2025 11:10am",
                value = "60kg"
            ) {}

            SupplierHistoryItem(
                status = TraceabilityStatus.Full,
                title = "from Trader #558963",
                description = "April 24, 2025 11:10am",
                value = "40kg"
            )
        }
    }
}


@Composable
fun TitleDescriptionView(
    title: String,
    description: String,
    warningText: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = description,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyleBodyM,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!warningText.isNullOrBlank()) {
            Text(
                text = warningText,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
fun TransactionInfoHarvestSeasonItem(
    season: HarvestSeasonModel,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.harvest_season),
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = season.getShortName(),
                    style = TextStyleBodyM,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                season.status?.let { status ->
                    HarvestSeasonStatusBadge(status = status)
                }
            }
        }

        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = ColorGray40,
        )
    }
}

@Composable
fun TransactionInfoItem1(
    title: String,
    description: String,
) {
    TransactionInfoItemBase(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        title = title,
    ) {

        Text(
            text = description,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            style = TextStyleBodyM,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TransactionInfoItem2(
    title: String,
    description: String,
    onClick: () -> Unit = {},
) {
    TransactionInfoItemBase(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        title = title,
    ) {

        Text(
            text = description,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            style = TextStyleButtonM,
            color = MaterialTheme.colorScheme.onSurface
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
fun TransactionInfoItem3(
    title: String,
    description: String,
    onClick: () -> Unit = {},
) {
    TransactionInfoItemBase(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        title = title,
    ) {

        Text(
            text = description,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            style = TextStyleBodyM,
            color = MaterialTheme.colorScheme.error
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
fun TransactionInfoStatusItem(
    status: TransactionStatus,
    onClick: () -> Unit = {},
) {
    TransactionInfoItemBase(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        title = stringResource(R.string.transaction_status),
    ) {

        Spacer(modifier = Modifier.weight(1f))

        TransactionStatusView(status = status)

        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_chevron_forward),
            contentDescription = null,
            tint = ColorGray40,
        )
    }
}

@Composable
fun TransactionInfoTraceabilityItem(
    status: TraceabilityStatus,
    chartItems: List<PieChartItem> = emptyList(),
    chartTitle: String = "Items",
    onClick: () -> Unit = {},
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TransactionInfoItemBase(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.traceability_status),
        ) {

            Spacer(modifier = Modifier.weight(1f))

            TraceabilityStatusView(status = status)

            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = ColorGray40,
            )
        }

        if (chartItems.isNotEmpty()) {
            Row(
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    PieChart2(
                        items = chartItems,
                        chartSize = 80.dp,
                        strokeWidth = 10.dp,
                        gapSize = 1.dp,
                        strokeCapRadius = 4.dp,
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${chartItems.sumOf { it.value }}",
                            textAlign = TextAlign.Center,
                            style = TextStyleMediumM,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = chartTitle,
                            textAlign = TextAlign.Center,
                            style = TextStyleBodyXS,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyHorizontalGrid(
                    modifier = Modifier
                        .height(80.dp)
                        .wrapContentWidth(),
                    userScrollEnabled = false,
                    rows = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(chartItems) {
                        Row(
                            modifier = Modifier.width(80.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(8.dp)
                                    .background(color = it.color, shape = CircleShape)
                            )
                            Column {
                                Text(
                                    text = "${it.value}",
                                    style = TextStyleMediumM,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = it.name,
                                    textAlign = TextAlign.Center,
                                    style = TextStyleBodyXS,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionInfoItemBase(
    modifier: Modifier,
    title: String,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            style = TextStyleBodyS,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        content()
    }
}

@Composable
fun SectionHeader(
    title: String,
    onIconClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onIconClick != null) { onIconClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = title,
            style = TextStyleMediumL,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (onIconClick != null) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_chevron_forward),
                contentDescription = null,
                tint = ColorGray40,
            )
        }
    }
}

@Composable
fun SupplierHistoryItem(
    modifier: Modifier = Modifier,
    transaction: TransactionModel,
    onTraceabilityStatusClick: ((TraceabilityStatus?) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {

    val value = transaction.getCommodityVolumeText()
    val title = if (transaction.isBuyingFromFarmer) {
        stringResource(R.string.from_initial_seller)
    } else if (transaction.seller != null) {
        stringResource(R.string.from_trader, transaction.seller.username ?: "")
    } else {
        stringResource(R.string.from_a_cooperative)
    }

    SupplierHistoryItem(
        modifier = modifier,
        status = transaction.traceability,
        title = title,
        description = transaction.createdDate.toFormattedDateString(),
        value = value,
        onTraceabilityStatusClick = onTraceabilityStatusClick,
        onClick = onClick,
    )
}

@Composable
fun SupplierHistoryItem(
    modifier: Modifier = Modifier,
    status: TraceabilityStatus?,
    title: String,
    description: String,
    value: String,
    onTraceabilityStatusClick: ((TraceabilityStatus?) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val iconRes = when (status) {
            TraceabilityStatus.Full -> R.drawable.ic_traceability_status_full
            TraceabilityStatus.Conditional -> R.drawable.ic_traceability_status_conditional
            TraceabilityStatus.Partial -> R.drawable.ic_traceability_status_partial
            TraceabilityStatus.Incomplete -> R.drawable.ic_traceability_status_incomplete
            else -> R.drawable.ic_traceability_status_none
        }

        Icon(
            modifier = Modifier
                .size(24.dp)
                .clickable(onTraceabilityStatusClick != null) {
                    onTraceabilityStatusClick?.invoke(status)
                },
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                modifier = modifier.fillMaxWidth(),
                text = title,
                style = TextStyleMediumS,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                modifier = modifier.fillMaxWidth(),
                text = description,
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = value,
            style = TextStyleMediumS,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (onClick != null) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = ColorGray40,
            )
        }
    }
}
