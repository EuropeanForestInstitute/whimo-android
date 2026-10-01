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
package com.whimo.presentation.createtransaction

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
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.navigation.Screens
import com.whimo.navigation.navgraphs.CreateTransactionNavGraph
import com.whimo.network.authenticator.SessionManager
import com.whimo.presentation.auth.AuthActivity
import com.whimo.presentation.ui.components.TestEnvironmentFrame
import com.whimo.presentation.ui.theme.WhimoTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class CreateTransactionActivity : ComponentActivity(), OnBackPressedDispatcherOwner {
    private val sessionManager: SessionManager by inject()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transactionActionName = intent.getStringExtra(TRANSACTION_ACTION)
        val transactionAction = transactionActionName?.let { TransactionAction.valueOf(it) } ?: TransactionAction.Buying

        val startDestination = when (transactionAction) {
            TransactionAction.Buying -> Screens.CreateTransaction2.route
            TransactionAction.Selling -> Screens.CreateTransactionForm.route
        }

        setContent {
            val navController = rememberNavController()
            val sharedViewModel: SharedTransactionViewModel = koinViewModel()

            sharedViewModel.setAction(transactionAction)
            if (transactionAction == TransactionAction.Selling) {
                sharedViewModel.setIsProducerTransaction(false)
            }

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
                            CreateTransactionNavGraph(
                                modifier = Modifier,
                                navController = navController,
                                sharedViewModel = sharedViewModel,
                                startDestination = startDestination,
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
        private const val TRANSACTION_ACTION = "transaction_action"

        fun openCreateTransaction(context: Context, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>, transactionAction: TransactionAction) {
            val intent = Intent(context, CreateTransactionActivity::class.java)
            intent.putExtra(TRANSACTION_ACTION, transactionAction.name)
            launcher.launch(intent)
        }
    }
}
