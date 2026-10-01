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
package com.whimo.presentation.balances

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.transactions.models.HarvestSeasonModel
import com.whimo.domain.transactions.models.HarvestSeasonStatus
import com.whimo.domain.transactions.models.TraceabilityStatus
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.domain.transactions.models.getCommodityVolumeText
import com.whimo.domain.transactions.models.getShortName
import com.whimo.extensions.isNetworkAvailable
import com.whimo.extensions.toFormattedDateString
import com.whimo.navigation.Screens
import com.whimo.presentation.main.components.TransactionItem
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.transactions.transactiondetails.components.HarvestSeasonInfoDialog
import com.whimo.presentation.transactions.transactiondetails.components.SectionHeader
import com.whimo.presentation.transactions.transactiondetails.components.TitleDescriptionView
import com.whimo.presentation.transactions.transactiondetails.components.TraceabilityStatusesDialog
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoHarvestSeasonItem
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoItem1
import com.whimo.presentation.transactions.transactiondetails.components.TransactionInfoTraceabilityItem
import com.whimo.presentation.ui.theme.TextStyleBodyM
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.toJsonArgs
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        BalanceDetailsScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            args = BalanceDetailsArgs(
                commodity = CommodityModel(
                    id = "1",
                    code = "0901",
                    name = "Ripe cherry",
                    unit = "kg",
                    hasRecipe = true,
                    group = CommodityGroupModel("coffee", "Coffee", null),
                    balance = 2000f,
                ),
                harvestSeason = HarvestSeasonModel(
                    id = "1",
                    name = "Harvest season 2025/26",
                    status = HarvestSeasonStatus.Past,
                ),
            ),
            viewModel = null,
        )
    }
}

@Composable
fun BalanceDetailsScreen(
    modifier: Modifier,
    navController: NavHostController,
    args: BalanceDetailsArgs?,
    viewModel: BalanceDetailsViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: BalanceDetailsContract.Binding(
        commodity = args?.commodity,
        harvestSeason = args?.harvestSeason,
    )
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<BalanceDetailsDialog?>(null) }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is BalanceDetailsContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        viewModel?.setEvent(BalanceDetailsContract.Event.OnCreate(args))
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Toolbar2(
            navController = navController,
            title = stringResource(R.string.balance_details),
            iconRes = R.drawable.ic_convert_24,
            onIconClick = binding.commodity
                ?.takeIf { it.hasRecipe && context.isNetworkAvailable() }
                ?.let { commodity ->
                    {
                        navController.navigate(
                            Screens.ConvertRecipes.putArgs(
                                Screens.ARG_KEY_JSON to commodity.toJsonArgs()
                            )
                        )
                    }
                },
        )

        BalanceDetailsContent(
            modifier = Modifier.fillMaxSize(),
            binding = binding,
            onHarvestSeasonClick = { dialog = BalanceDetailsDialog.HarvestSeason(it) },
            onTraceabilityClick = { dialog = BalanceDetailsDialog.Traceability },
        )
    }

    when (val currentDialog = dialog) {
        is BalanceDetailsDialog.HarvestSeason -> {
            HarvestSeasonInfoDialog(
                season = currentDialog.season,
                onDismiss = { dialog = null },
            )
        }
        BalanceDetailsDialog.Traceability -> {
            TraceabilityStatusesDialog(
                status = null,
                onDismiss = { dialog = null },
            )
        }
        null -> Unit
    }
}

@Composable
private fun BalanceDetailsContent(
    modifier: Modifier,
    binding: BalanceDetailsContract.Binding,
    onHarvestSeasonClick: (HarvestSeasonModel) -> Unit,
    onTraceabilityClick: (TraceabilityStatus) -> Unit,
) {
    val commodity = binding.commodity

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (commodity != null) {
            item {
                TitleDescriptionView(
                    title = stringResource(R.string.commodity_type),
                    description = "${commodity.code} ${commodity.name} ${commodity.getBalanceText()}",
                )
            }
        }

        binding.harvestSeason?.let { season ->
            item {
                TransactionInfoHarvestSeasonItem(
                    season = season,
                    onClick = { onHarvestSeasonClick(season) },
                )
            }
        }

        binding.traceabilityStatus?.let { traceabilityStatus ->
            item {
                TransactionInfoTraceabilityItem(
                    status = traceabilityStatus,
                    onClick = { onTraceabilityClick(traceabilityStatus) },
                )
            }
        }

        binding.lastActivity?.let { lastActivity ->
            item {
                TransactionInfoItem1(
                    title = stringResource(R.string.last_activity),
                    description = lastActivity.toFormattedDateString(),
                )
            }
        }

        item {
            SectionHeader(title = stringResource(R.string.source_transactions))
        }

        if (binding.sourceTransactions.isEmpty() && !binding.isLoading) {
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    text = stringResource(R.string.no_source_transactions),
                    style = TextStyleBodyM,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        items(
            items = binding.sourceTransactions,
            key = { it.id },
        ) { transaction ->
            BalanceSourceTransactionItem(transaction = transaction)
        }

        if (binding.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun BalanceSourceTransactionItem(
    transaction: TransactionModel,
) {
    val icon = if (transaction.action == TransactionAction.Buying) {
        R.drawable.ic_status_buy
    } else {
        R.drawable.ic_status_sell
    }

    TransactionItem(
        iconRes = icon,
        title = transaction.getBalanceSourceTitle(),
        description = transaction.createdDate.toFormattedDateString(),
        harvestSeasonText = transaction.harvestSeason?.getShortName(),
        status = transaction.status,
    )
}

private fun TransactionModel.getBalanceSourceTitle(): String {
    val commodityName = commodity.group?.name ?: commodity.name
    return "$commodityName, ${getCommodityVolumeText()}"
}

private sealed class BalanceDetailsDialog {
    data class HarvestSeason(val season: HarvestSeasonModel) : BalanceDetailsDialog()
    data object Traceability : BalanceDetailsDialog()
}
