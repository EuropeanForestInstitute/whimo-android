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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.base.ObserveEffects
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.extensions.isNetworkAvailable
import com.whimo.navigation.Screens
import com.whimo.presentation.balances.components.BalanceCommodityList
import com.whimo.presentation.main.components.Toolbar2
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.toJsonArgs
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        CommodityGroupBalancesScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            commodityGroup = null,
            viewModel = null,
        )
    }
}

@Composable
fun CommodityGroupBalancesScreen(
    modifier: Modifier,
    navController: NavHostController,
    commodityGroup: CommodityGroupModel?,
    viewModel: CommodityGroupBalancesViewModel? = koinViewModel()
) {
    val binding = viewModel?.observeViewBinding() ?: CommodityGroupBalancesContract.Binding()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is CommodityGroupBalancesContract.Effect.ToggleLoader -> {
                    isLoading = effect.isLoading
                }
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        if (commodityGroup != null) {
            viewModel?.setEvent(CommodityGroupBalancesContract.Event.OnCreate(commodityGroup))
        }
    }

    Column(
        modifier = modifier
    ) {
        Toolbar2(
            navController = navController,
            title = binding.title,
        )

        if (binding.balances != null) {
            BalanceCommodityList(
                modifier = Modifier.fillMaxHeight(),
                networkAvailable = context.isNetworkAvailable(),
                items = binding.balances!!,
                onBalanceClick = {
                    navController.navigate(
                        Screens.BalanceDetails.putArgs(
                            Screens.ARG_KEY_JSON to BalanceDetailsArgs(
                                commodity = it.commodity,
                                harvestSeason = it.harvestSeason,
                                traceabilityStatus = it.traceabilityStatus,
                            ).toJsonArgs()
                        )
                    )
                },
                onConvertClick = {
                    navController.navigate(
                        Screens.ConvertRecipes.putArgs(Screens.ARG_KEY_JSON to it.toJsonArgs())
                    )
                },
            )
        }
    }
}
