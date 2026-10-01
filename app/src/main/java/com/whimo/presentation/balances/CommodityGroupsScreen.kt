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

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.whimo.extensions.isNetworkAvailable
import com.whimo.navigation.Screens
import com.whimo.presentation.balances.components.BalanceCommodityList
import com.whimo.presentation.main.components.EmptyState
import com.whimo.presentation.main.components.HarvestSeasonFilterBottomSheet
import com.whimo.presentation.main.components.HarvestSeasonFilterChip
import com.whimo.presentation.main.components.SearchFilterBar
import com.whimo.presentation.main.components.Toolbar
import com.whimo.presentation.notifications.NotificationsActivity
import com.whimo.presentation.ui.baseScreen.MainIconButton
import com.whimo.presentation.ui.theme.WhimoTheme
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        CommodityGroupsScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            haveUnreadNotifications = false,
            viewModel = null,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommodityGroupsScreen(
    modifier: Modifier,
    navController: NavHostController,
    haveUnreadNotifications: Boolean,
    viewModel: CommodityGroupsViewModel? = koinViewModel(),
) {
    val binding = viewModel?.observeViewBinding() ?: CommodityGroupsContract.Binding()

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel?.setEvent(CommodityGroupsContract.Event.Refresh)
            }
        }
    )

    var showSeasonFilter by remember { mutableStateOf(false) }
    val networkAvailable = context.isNetworkAvailable()

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is CommodityGroupsContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        viewModel?.setEvent(CommodityGroupsContract.Event.OnCreate)
    }

    Column(
        modifier = modifier
    ) {
        Toolbar(
            title = stringResource(R.string.balances),
            iconRes = if (haveUnreadNotifications) R.drawable.ic_notification_dot else R.drawable.ic_notification,
        ) {
            NotificationsActivity.openNotifications(navController.context, launcher)
        }

        val harvestSeasonFilterIsActive = binding.selectedCommodityGroup != null || binding.selectedHarvestSeason != null

        SearchFilterBar(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 16.dp,
                end = 16.dp,
                bottom = if (harvestSeasonFilterIsActive) 8.dp else 16.dp,
            ),
            query = binding.query ?: "",
            hintText = stringResource(R.string.search_balance),
            filterIsActive = harvestSeasonFilterIsActive,
            onSearch = {
                viewModel?.setEvent(CommodityGroupsContract.Event.QueryChanged(it))
            },
            onFilterClick = {
                showSeasonFilter = true
            },
        )

        if (harvestSeasonFilterIsActive) {
            HarvestSeasonFilterChip(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                commodityGroup = binding.selectedCommodityGroup,
                harvestSeason = binding.selectedHarvestSeason,
                onClear = {
                    viewModel?.setEvent(CommodityGroupsContract.Event.HarvestSeasonFilterChanged(null, null))
                },
            )
        }

        val balances = binding.balances

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = binding.isRefreshing,
            onRefresh = {
                viewModel?.setEvent(CommodityGroupsContract.Event.Refresh)
            },
        ) {
            when {
                balances == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = MaterialTheme.colorScheme.surface)
                    )
                }
                balances.isNotEmpty() -> {
                    BalanceCommodityList(
                        modifier = Modifier.fillMaxHeight(),
                        networkAvailable = networkAvailable,
                        items = balances,
                        onBalanceClick = { item ->
                            CommodityGroupBalancesActivity.openBalanceDetails(
                                context = context,
                                launcher = launcher,
                                args = BalanceDetailsArgs(
                                    commodity = item.commodity,
                                    harvestSeason = binding.selectedHarvestSeason ?: item.harvestSeason,
                                    traceabilityStatus = item.traceabilityStatus,
                                ),
                            )
                        },
                        onConvertClick = {
                            CommodityGroupBalancesActivity.openConvertRecipes(context, launcher, it)
                        },
                    )
                }
                else -> {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        iconRes = R.drawable.ic_empty,
                        title = stringResource(R.string.no_balance),
                        description = stringResource(R.string.no_transactions_description),
                    ) {
                        MainIconButton(
                            modifier = Modifier.padding(top = 24.dp),
                            iconRes = R.drawable.ic_add_transaction,
                            title = stringResource(R.string.add_transaction),
                            onClick = {
                                navController.navigate(Screens.CreateTransaction.route)
                            },
                        )
                    }
                }
            }
        }
    }

    if (showSeasonFilter) {
        HarvestSeasonFilterBottomSheet(
            commodityGroups = binding.commodityGroups,
            harvestSeasons = binding.harvestSeasons,
            selectedCommodityGroup = binding.selectedCommodityGroup,
            selectedSeason = binding.selectedHarvestSeason,
            onCommodityGroupPreviewChanged = {
                viewModel?.setEvent(CommodityGroupsContract.Event.HarvestSeasonFilterCommodityGroupChanged(it))
            },
            onApply = { commodityGroup, harvestSeason ->
                viewModel?.setEvent(
                    CommodityGroupsContract.Event.HarvestSeasonFilterChanged(
                        commodityGroup = commodityGroup,
                        harvestSeason = harvestSeason,
                    )
                )
            },
            onReset = {
                viewModel?.setEvent(CommodityGroupsContract.Event.HarvestSeasonFilterChanged(null, null))
            },
            onDismissRequest = { showSeasonFilter = false },
        )
    }
}
