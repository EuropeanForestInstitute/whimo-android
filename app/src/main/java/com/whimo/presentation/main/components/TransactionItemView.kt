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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whimo.R
import com.whimo.domain.transactions.models.HarvestSeasonStatus
import com.whimo.domain.transactions.models.TransactionStatus
import com.whimo.presentation.ui.theme.TextStyleBodyS
import com.whimo.presentation.ui.theme.TextStyleButtonM
import com.whimo.presentation.ui.theme.TextStyleMediumM
import com.whimo.presentation.ui.theme.WhimoTheme

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            TransactionItem(
                iconRes = R.drawable.ic_status_buy,
                title = "Cocoa, 300kg",
                description = "April 22, 2025 11:10am",
                harvestSeasonText = "2025/26",
                status = TransactionStatus.Accepted
            )

            TransactionItem(
                iconRes = R.drawable.ic_status_sell,
                title = "Cattle, 200pcs",
                description = "April 22, 2025 11:10am",
                status = TransactionStatus.Rejected
            )

            TransactionItem(
                iconRes = R.drawable.ic_status_buy,
                title = "Cocoa, 100kg",
                description = "April 22, 2025 11:10am",
                status = TransactionStatus.Pending
            )

            TransactionItem(
                iconRes = R.drawable.ic_status_sell,
                title = "Cocoa, 100kg",
                description = "April 22, 2025 11:10am",
                status = TransactionStatus.Recorded
            )

            TransactionItem(
                iconRes = R.drawable.ic_status_buy,
                title = "Cocoa, 100kg",
                description = "April 22, 2025 11:10am",
                status = TransactionStatus.NoResponse
            )

            TransactionItem(
                iconRes = R.drawable.ic_status_buy,
                title = "Cocoa, 100kg",
                description = "April 22, 2025 11:10am",
                status = TransactionStatus.Automatic,
                showAddLocation = true,
            )

            TransactionItem(
                iconRes = R.drawable.ic_status_buy,
                title = "Cocoa, 100kg",
                description = "April 22, 2025 11:10am",
            )
        }
    }
}

@Composable
fun TransactionItem(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    description: String,
    harvestSeasonText: String? = null,
    harvestSeasonStatus: HarvestSeasonStatus? = null,
    status: TransactionStatus? = null,
    showAddLocation: Boolean = false,
    onClick: () -> Unit = {},
    onAddLocationClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                modifier = modifier.fillMaxWidth(),
                text = title,
                style = TextStyleMediumM,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                modifier = modifier.fillMaxWidth(),
                text = description,
                style = TextStyleBodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!harvestSeasonText.isNullOrBlank()) {
                HarvestSeasonTag(
                    modifier = Modifier.padding(top = 2.dp),
                    text = harvestSeasonText,
                    status = harvestSeasonStatus,
                )
            }

            if (showAddLocation) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    modifier = Modifier
                        .clickable {
                            onAddLocationClick()
                        },
                    text = stringResource(R.string.add_geolocation),
                    style = TextStyleButtonM,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (status != null) {
            TransactionStatusView(
                status = status,
            )
        }
    }
}
