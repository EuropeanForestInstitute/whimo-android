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

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.whimo.domain.commodity.models.CommodityGroupModel
import com.whimo.domain.commodity.models.CommodityModel
import com.whimo.extensions.getParcelableCompatExtra
import com.whimo.navigation.navgraphs.CommodityBalancesNavGraph
import com.whimo.network.authenticator.SessionManager
import com.whimo.presentation.auth.AuthActivity
import com.whimo.presentation.ui.components.TestEnvironmentFrame
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.fromJsonArgs
import com.whimo.utils.toJsonArgs
import org.koin.android.ext.android.inject

class CommodityGroupBalancesActivity : ComponentActivity(), OnBackPressedDispatcherOwner {
    private val sessionManager: SessionManager by inject()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val commodityGroup = intent.getParcelableCompatExtra<CommodityGroupModel>(COMMODITY_GROUP)
        val balanceDetailsArgs = intent
            .getStringExtra(BALANCE_DETAILS_ARGS)
            ?.fromJsonArgs<BalanceDetailsArgs>()
        val convertCommodity = intent.getParcelableCompatExtra<CommodityModel>(CONVERT_COMMODITY)

        setContent {
            val navController = rememberNavController()
            WhimoTheme {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        TestEnvironmentFrame(modifier = Modifier.fillMaxSize()) {
                            CommodityBalancesNavGraph(
                                modifier = Modifier,
                                navController = navController,
                                commodityGroup = commodityGroup,
                                balanceDetailsArgs = balanceDetailsArgs,
                                convertCommodity = convertCommodity,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sessionManager.setLogoutCallback {
            AuthActivity.openAuth(this)
        }
    }

    companion object {
        private const val COMMODITY_GROUP = "commodity_group"
        private const val BALANCE_DETAILS_ARGS = "balance_details_args"
        private const val CONVERT_COMMODITY = "convert_commodity"

        fun openCommodityGroupBalances(
            context: Context,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
            commodityGroupModel: CommodityGroupModel,
        ) {
            val intent = Intent(context, CommodityGroupBalancesActivity::class.java)
            intent.putExtra(COMMODITY_GROUP, commodityGroupModel)
            launcher.launch(intent)
        }

        fun openBalanceDetails(
            context: Context,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
            args: BalanceDetailsArgs,
        ) {
            val intent = Intent(context, CommodityGroupBalancesActivity::class.java)
            intent.putExtra(BALANCE_DETAILS_ARGS, args.toJsonArgs())
            launcher.launch(intent)
        }

        fun openConvertRecipes(
            context: Context,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
            commodityModel: CommodityModel,
        ) {
            val intent = Intent(context, CommodityGroupBalancesActivity::class.java)
            intent.putExtra(CONVERT_COMMODITY, commodityModel)
            launcher.launch(intent)
        }
    }
}
