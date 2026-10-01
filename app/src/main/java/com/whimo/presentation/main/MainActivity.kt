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
package com.whimo.presentation.main

import android.app.Activity
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
import com.whimo.navigation.bottombar.BottomNavItem.Companion.MainNavItems
import com.whimo.navigation.bottombar.BottomNavigationBar
import com.whimo.navigation.navgraphs.MainNavGraph
import com.whimo.network.authenticator.SessionManager
import com.whimo.presentation.auth.AuthActivity
import com.whimo.presentation.ui.components.TestEnvironmentFrame
import com.whimo.presentation.ui.theme.WhimoTheme
import com.whimo.utils.PostNotificationPermissionRequester
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity(), OnBackPressedDispatcherOwner {
    private val viewModel: MainViewModel by viewModel()
    private val sessionManager: SessionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val binding = viewModel.observeViewBinding() ?: MainContract.Binding()

            WhimoTheme {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surface,
                    bottomBar = {
                        BottomNavigationBar(
                            navController = navController,
                            MainNavItems,
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        TestEnvironmentFrame(modifier = Modifier.fillMaxSize()) {
                            MainNavGraph(
                                modifier = Modifier,
                                navController = navController,
                                haveUnreadNotifications = binding.haveUnreadNotifications,
                            )
                        }
                    }
                }
            }

            PostNotificationPermissionRequester {
                viewModel.setEvent(MainContract.Event.PostNotificationPermissionChanged(it))
            }
        }

        viewModel.setEvent(MainContract.Event.OnCreate)
    }

    override fun onResume() {
        super.onResume()
        sessionManager.setLogoutCallback {
            AuthActivity.openAuth(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.setLogoutCallback(null)
    }

    companion object {
        fun openMain(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            activity.startActivity(intent)
            activity.finishAffinity()
        }
    }
}
