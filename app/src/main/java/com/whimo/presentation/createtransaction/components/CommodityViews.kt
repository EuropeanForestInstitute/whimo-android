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
package com.whimo.presentation.createtransaction.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.presentation.ui.theme.ColorSuccess
import com.whimo.presentation.ui.theme.ColorWarning
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.TextStyleBodyXS
import com.whimo.presentation.ui.theme.TextStyleMediumM
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    val testCommodityModel = CommodityModel(
        id = "123",
        code = "1701",
        name = "Type1",
        unit = "kg",
        hasRecipe = true,
        group = null,
        balance = 1.5f,
    )
    val sampleData = listOf(
        CommodityGroupModel(
            id = "",
            name = "Cattle",
            commodities = listOf(
                testCommodityModel,
                CommodityModel(
                    id = "",
                    code = "1702",
                    name = "Type2",
                    unit = "kg",
                    hasRecipe = true,
                    group = null,
                    balance = null,
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
                    hasRecipe = true,
                    group = null,
                    balance = 1.5f,
                ),
                CommodityModel(
                    id = "",
                    code = "1805",
                    name = "Cocoa powder, not containing added sugar or other sweetening matter",
                    unit = "kg",
                    hasRecipe = true,
                    group = null,
                    balance = 1.5f,
                ),
                CommodityModel(
                    id = "",
                    code = "1806",
                    name = "Chocolate and other food preparations containing cocoa",
                    unit = "kg",
                    hasRecipe = true,
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
                    hasRecipe = true,
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
        CommodityList(
            sections = sampleData,
            selectedCommodity = testCommodityModel,
        )
    }
}

@Composable
fun CommodityList(
    modifier: Modifier = Modifier,
    sections: List<CommodityGroupModel>,
    selectedCommodity: CommodityModel? = null,
    onSelect: (CommodityModel) -> Unit = {},
) {
    var selectedCommodityState by remember { mutableStateOf(selectedCommodity) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        sections.forEach { section ->
            CommoditySectionItem(
                section = section,
                selectedCommodity = selectedCommodityState,
                onSelect = { commodity ->
                    selectedCommodityState = commodity
                    onSelect(commodity)
                }
            )
        }
    }
}

@Composable
fun CommoditySectionItem(
    section: CommodityGroupModel,
    selectedCommodity: CommodityModel?,
    onSelect: (CommodityModel) -> Unit
) {
    var isExpanded by remember { mutableStateOf(section.commodities?.contains(selectedCommodity) ?: false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isExpanded) MaterialTheme.colorScheme.surfaceBright else MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = section.name,
                style = if (isExpanded) TextStyleMediumM else TextStyleBodyM,
                color = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = if (isExpanded) R.drawable.ic_chevron_up else R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                section.commodities?.forEachIndexed { index, commodity ->
                    CommodityItem(
                        commodity = commodity,
                        isSelected = commodity == selectedCommodity,
                        onSelect = { onSelect(commodity) }
                    )

                    if (index < section.commodities.size - 1) {
                        DashedDivider()
                    } else {
                        LineDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun CommodityItem(
    commodity: CommodityModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
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
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = commodity.name,
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (commodity.balance == null || commodity.balance == 0f) {
                Text(
                    text = stringResource(R.string.your_balance, commodity.getBalanceText()),
                    style = TextStyleBodyXS,
                    color = ColorWarning
                )
            } else {
                Text(
                    text = stringResource(R.string.your_balance, commodity.getBalanceText()),
                    style = TextStyleBodyXS,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isSelected) {
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
fun DashedDivider(
    modifier: Modifier = Modifier,
    lineHeight: Dp = 1.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.outline
) {
    val density = LocalDensity.current
    val strokeWidthPx = density.run { strokeWidth.toPx() }
    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(strokeWidthPx, strokeWidthPx), 0f)

    Canvas(modifier.fillMaxWidth().height(lineHeight)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect
        )
    }
}

@Composable
fun LineDivider(
    modifier: Modifier = Modifier,
    lineHeight: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline
) {
    Canvas(modifier.fillMaxWidth().height(lineHeight)) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
        )
    }
}