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
package com.whimo.presentation.createtransaction.geodata

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
import com.google.android.gms.maps.model.LatLng
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.createtransaction.models.LocationProvider
import com.whimo.domain.createtransaction.models.MFile
import com.whimo.domain.transactions.models.TransactionModel
import com.whimo.extensions.getParcelableCompatExtra
import com.whimo.navigation.Screens
import com.whimo.navigation.navgraphs.FarmGeoDataNavGraph
import com.whimo.network.authenticator.SessionManager
import com.whimo.presentation.auth.AuthActivity
import com.whimo.presentation.ui.components.TestEnvironmentFrame
import com.whimo.presentation.ui.theme.WhimoTheme
import org.koin.android.ext.android.inject

class FarmGeoDataActivity : ComponentActivity(), OnBackPressedDispatcherOwner {
    private val sessionManager: SessionManager by inject()
    private var transactionModel: TransactionModel? = null
    private var createTransactionModel: CreateTransactionModel? = null

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactionModel = intent.getParcelableCompatExtra<TransactionModel>(TRANSACTION_MODEL)
        createTransactionModel = intent.getParcelableCompatExtra<CreateTransactionModel>(CREATE_TRANSACTION_MODEL)

        var startDestination = Screens.FarmGeoData.route

        if (transactionModel != null) {
            if (transactionModel!!.isAutomatic || !transactionModel!!.isBuyingFromFarmer) {
                startDestination = Screens.UploadFile.route
            }
        }

        if (createTransactionModel != null) {
            if (!createTransactionModel!!.isBuyingFromFarmer) {
                startDestination = Screens.UploadFile.route
            }
        }

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
                            FarmGeoDataNavGraph(
                                modifier = Modifier,
                                navController = navController,
                                startDestination = startDestination,
                                transactionModel = transactionModel,
                                createTransactionModel = createTransactionModel,
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

    fun setResult(
        locationProvider: LocationProvider? = null,
        location: LatLng? = null,
        qrData: String? = null,
        file: MFile? = null,
    ) {
        setResult(
            RESULT_OK,
            Intent()
                .putExtra(RESULT_LOCATION_PROVIDER, locationProvider?.providerName)
                .putExtra(RESULT_LOCATION, location)
                .putExtra(RESULT_QR_DATA, qrData)
                .putExtra(RESULT_FILE, file)
        )
    }

    companion object {
        private const val TRANSACTION_MODEL = "transaction_model"
        private const val CREATE_TRANSACTION_MODEL = "create_transaction_model"

        const val RESULT_LOCATION_PROVIDER = "result_location_provider"
        const val RESULT_LOCATION = "result_location"
        const val RESULT_QR_DATA = "result_qr_data"
        const val RESULT_FILE = "result_file"

        fun openFarmGeoData(
            context: Context,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
            transactionModel: TransactionModel,
        ) {
            val intent = Intent(context, FarmGeoDataActivity::class.java)
            intent.putExtra(TRANSACTION_MODEL, transactionModel)
            launcher.launch(intent)
        }

        fun openFarmGeoData(
            context: Context,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
            createTransactionModel: CreateTransactionModel,
        ) {
            val intent = Intent(context, FarmGeoDataActivity::class.java)
            intent.putExtra(CREATE_TRANSACTION_MODEL, createTransactionModel)
            launcher.launch(intent)
        }
    }
}
