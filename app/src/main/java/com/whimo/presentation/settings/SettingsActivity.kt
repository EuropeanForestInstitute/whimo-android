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
package com.whimo.presentation.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.whimo.navigation.navgraphs.AccountNavGraph
import com.whimo.navigation.navgraphs.LanguageNavGraph
import com.whimo.navigation.navgraphs.NotificationSettingsNavGraph
import com.whimo.navigation.navgraphs.PasswordNavGraph
import com.whimo.network.authenticator.SessionManager
import com.whimo.presentation.auth.AuthActivity
import com.whimo.presentation.ui.components.TestEnvironmentFrame
import com.whimo.presentation.ui.theme.WhimoTheme
import org.koin.android.ext.android.inject

class SettingsActivity : ComponentActivity(), OnBackPressedDispatcherOwner {
    private val sessionManager: SessionManager by inject()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navGraph = intent.extras?.getString(NAV_GRAPH_KEY)

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
                            when (navGraph) {
                                ACCOUNT_NAVIGATION -> AccountNavGraph(Modifier, navController)
                                PASSWORD_NAVIGATION -> PasswordNavGraph(Modifier, navController)
                                NOTIFICATION_SETTINGS_NAVIGATION -> NotificationSettingsNavGraph(Modifier, navController)
                                LANGUAGE_NAVIGATION -> LanguageNavGraph(Modifier, navController)
                            }
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
        private const val NAV_GRAPH_KEY = "nav_graph_key"
        private const val ACCOUNT_NAVIGATION = "account_navigation"
        private const val PASSWORD_NAVIGATION = "password_navigation"
        private const val NOTIFICATION_SETTINGS_NAVIGATION = "notification_settings_navigation"
        private const val LANGUAGE_NAVIGATION = "language_navigation"

        fun openAccount(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(NAV_GRAPH_KEY, ACCOUNT_NAVIGATION)
            }
            context.startActivity(intent)
        }

        fun openPassword(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(NAV_GRAPH_KEY, PASSWORD_NAVIGATION)
            }
            context.startActivity(intent)
        }

        fun openNotificationSettings(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(NAV_GRAPH_KEY, NOTIFICATION_SETTINGS_NAVIGATION)
            }
            context.startActivity(intent)
        }

        fun openLanguage(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java).apply {
                putExtra(NAV_GRAPH_KEY, LANGUAGE_NAVIGATION)
            }
            context.startActivity(intent)
        }
    }
}
