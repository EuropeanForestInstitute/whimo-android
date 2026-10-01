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
package com.whimo.navigation.navgraphs

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.domain.commodity.models.ConvertRecipeModel
import com.whimo.navigation.Screens
import com.whimo.presentation.balances.BalanceDetailsArgs
import com.whimo.presentation.balances.BalanceDetailsScreen
import com.whimo.presentation.balances.CommodityGroupBalancesScreen
import com.whimo.presentation.balances.convert.ConvertCommodityScreen
import com.whimo.presentation.balances.convert.ConvertRecipesScreen
import com.whimo.utils.fromJsonArgs

@Composable
fun CommodityBalancesNavGraph(
    modifier: Modifier,
    navController: NavHostController,
    commodityGroup: CommodityGroupModel?,
    balanceDetailsArgs: BalanceDetailsArgs? = null,
    convertCommodity: CommodityModel? = null,
) {
    NavHost(
        modifier = modifier.fillMaxSize(),
        navController = navController,
        startDestination = Screens.GroupBalances.route,
    ) {
        composable(route = Screens.GroupBalances.route) {
            when {
                balanceDetailsArgs != null -> {
                    BalanceDetailsScreen(
                        modifier = modifier.fillMaxSize(),
                        navController = navController,
                        args = balanceDetailsArgs,
                    )
                }
                convertCommodity != null -> {
                    ConvertRecipesScreen(
                        modifier = modifier.fillMaxSize(),
                        navController = navController,
                        commodityModel = convertCommodity,
                    )
                }
                else -> {
                    CommodityGroupBalancesScreen(
                        modifier = modifier.fillMaxSize(),
                        navController = navController,
                        commodityGroup = commodityGroup,
                    )
                }
            }
        }
        composable(
            route = Screens.ConvertRecipes.route,
            arguments = listOf(
                navArgument(Screens.ARG_KEY_JSON) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            val json = it.arguments?.getString(Screens.ARG_KEY_JSON)

            ConvertRecipesScreen(
                modifier = modifier.fillMaxSize(),
                navController = navController,
                commodityModel = json?.fromJsonArgs<CommodityModel>(),
            )
        }
        composable(
            route = Screens.BalanceDetails.route,
            arguments = listOf(
                navArgument(Screens.ARG_KEY_JSON) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            val json = it.arguments?.getString(Screens.ARG_KEY_JSON)

            BalanceDetailsScreen(
                modifier = modifier.fillMaxSize(),
                navController = navController,
                args = json?.fromJsonArgs<BalanceDetailsArgs>(),
            )
        }
        composable(
            route = Screens.ConvertCommodity.route,
            arguments = listOf(
                navArgument(Screens.ARG_KEY_JSON) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            val json = it.arguments?.getString(Screens.ARG_KEY_JSON)

            ConvertCommodityScreen(
                modifier = modifier.fillMaxSize(),
                navController = navController,
                recipe = json?.fromJsonArgs<ConvertRecipeModel>(),
            )
        }
    }
}
