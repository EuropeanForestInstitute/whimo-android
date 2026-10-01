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

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.whimo.BuildConfig
import com.whimo.R
import com.whimo.base.ObserveEffects
import com.whimo.extensions.findActivity
import com.whimo.presentation.auth.AuthActivity
import com.whimo.presentation.createtransaction.components.CreateTransactionWarning
import com.whimo.presentation.main.components.Toolbar
import com.whimo.presentation.settings.components.DialogButtonsItem
import com.whimo.presentation.settings.components.DialogButton
import com.whimo.presentation.settings.components.SettingsInfoItem
import com.whimo.presentation.settings.components.SettingsItem
import com.whimo.presentation.settings.components.SettingsOptionsBottomSheet
import com.whimo.presentation.settings.components.SwitchItem1
import com.whimo.presentation.transactions.transactiondetails.components.BaseDialog
import com.whimo.presentation.transactions.transactiondetails.components.DialogTextItem
import com.whimo.presentation.ui.theme.ColorLightOrange
import com.whimo.presentation.ui.theme.ColorWarning
import com.whimo.presentation.ui.theme.ColorWarning10
import com.whimo.presentation.ui.theme.WhimoTheme
import org.koin.androidx.compose.koinViewModel

@Preview
@Composable
private fun Preview() {
    WhimoTheme {
        SettingsScreen(
            modifier = Modifier.fillMaxSize(),
            navController = rememberNavController(),
            viewModel = null
        )
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier,
    navController: NavHostController,
    viewModel: SettingsViewModel? = koinViewModel(),
) {
    val context = LocalContext.current

    var showSheet by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }
    var showEnteringTestEnvironment by remember { mutableStateOf(false) }
    var showUnsyncedChanges by remember { mutableStateOf(false) }

    val binding = viewModel?.observeViewBinding() ?: SettingsContract.Binding()

    if (viewModel != null) {
        ObserveEffects(viewModel) { effect ->
            when (effect) {
                is SettingsContract.Effect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                SettingsContract.Effect.ShowEnteringTestEnvironmentDialog -> {
                    showEnteringTestEnvironment = true
                }
                SettingsContract.Effect.ShowUnsyncedChangesDialog -> {
                    showUnsyncedChanges = true
                }
                is SettingsContract.Effect.NavigateAuth -> {
                    AuthActivity.openAuth(context.findActivity())
                }
                is SettingsContract.Effect.ToggleLoader -> Unit
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
        viewModel?.setEvent(SettingsContract.Event.OnCreate)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Toolbar(
            title = stringResource(R.string.settings),
            iconRes = R.drawable.ic_options,
        ) {
            showSheet = true
        }

        SwitchItem1(
            title = stringResource(R.string.test_environment),
            description = stringResource(R.string.test_environment_description),
            isChecked = binding.isTestEnvironmentEnabled,
            onChecked = {
                viewModel?.setEvent(SettingsContract.Event.TestEnvironmentChanged(it))
            },
        )

        SettingsItem(
            iconRes = R.drawable.ic_user_circle,
            title = stringResource(R.string.account_info),
            isEnabled = !binding.isTestEnvironmentEnabled,
        ) {
            SettingsActivity.openAccount(navController.context)
        }

        SettingsItem(
            iconRes = R.drawable.ic_lock,
            title = stringResource(R.string.change_password),
            isEnabled = !binding.isTestEnvironmentEnabled,
        ) {
            SettingsActivity.openPassword(navController.context)
        }

        SettingsItem(
            iconRes = R.drawable.ic_notification,
            title = stringResource(R.string.notifications),
            isEnabled = !binding.isTestEnvironmentEnabled,
        ) {
            SettingsActivity.openNotificationSettings(navController.context)
        }

        SettingsItem(
            iconRes = R.drawable.ic_language,
            title = stringResource(R.string.language),
        ) {
            SettingsActivity.openLanguage(navController.context)
        }

        SettingsItem(
            iconRes = R.drawable.ic_chat,
            title = stringResource(R.string.feedback),
        ) {

            val intent = Intent(Intent.ACTION_SEND).apply {
                selector = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
                putExtra(Intent.EXTRA_EMAIL, arrayOf(BuildConfig.FEEDBACK_EMAIL))
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.whimo_app_feedback))
            }

            try {
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_email)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, context.getString(R.string.no_email_app_is_installed), Toast.LENGTH_SHORT).show()
            }
        }

        if (binding.isTestEnvironmentEnabled) {
            SettingsInfoItem(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.test_environment_settings_unavailable),
            )
        }
    }

    if (showSheet) {
        SettingsOptionsBottomSheet(
            onLegalInfoClick = {
                showSheet = false
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        BuildConfig.TERMS_OF_USE.toUri()
                    )
                )
            },
            onLogoutClick = {
                showSheet = false
                showLogout = true
            },
            onDeleteAccountClick = {
                showSheet = false
                showDeleteAccount = true
            },
            isAccountActionsEnabled = !binding.isTestEnvironmentEnabled,
            onDismissRequest = {
                showSheet = false
            },
        )
    }

    if (showLogout) {
        BaseDialog(
            title = stringResource(R.string.logout_confirm_title),
            onDismiss = {
                showLogout = false
            }
        ) {
            DialogTextItem(
                title = stringResource(R.string.logout_confirm_message),
            )
            DialogButtonsItem(
                actionButtonTitle = stringResource(R.string.logout),
                actionButtonBackgroundColor = MaterialTheme.colorScheme.primary,
                actionButtonTitleColor = MaterialTheme.colorScheme.onPrimary,
                onActionClick = {
                    showLogout = false
                    viewModel?.setEvent(SettingsContract.Event.Logout)
                },
                onSecondClick = {
                    showLogout = false
                },
            )
        }
    }

    if (showDeleteAccount) {
        BaseDialog(
            title = stringResource(R.string.delete_account),
            onDismiss = {
                showDeleteAccount = false
            }
        ) {
            DialogTextItem(
                title = stringResource(R.string.delete_account_confirm_message),
            )
            DialogButtonsItem(
                actionButtonTitle = stringResource(R.string.delete),
                actionButtonBackgroundColor = MaterialTheme.colorScheme.error,
                actionButtonTitleColor = MaterialTheme.colorScheme.onPrimary,
                onActionClick = {
                    showDeleteAccount = false
                    viewModel?.setEvent(SettingsContract.Event.DeleteAccount)
                },
                onSecondClick = {
                    showDeleteAccount = false
                },
            )
        }
    }

    if (showEnteringTestEnvironment) {
        BaseDialog(
            title = stringResource(R.string.entering_test_environment_title),
            onDismiss = {
                showEnteringTestEnvironment = false
            },
        ) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
            ) {
                CreateTransactionWarning(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                    iconRes = R.drawable.ic_information,
                    title = stringResource(R.string.entering_test_environment_notice),
                )

                DialogTextItem(title = stringResource(R.string.entering_test_environment_description))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
            ) {
                DialogButton(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.got_it),
                    titleColor = MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        showEnteringTestEnvironment = false
                    },
                )
            }
        }
    }

    if (showUnsyncedChanges) {
        BaseDialog(
            title = stringResource(R.string.entering_test_environment_title),
            onDismiss = {
                showUnsyncedChanges = false
            },
        ) {

            Column(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
            ) {
                CreateTransactionWarning(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .padding(horizontal = 16.dp),
                    backgroundColor = ColorLightOrange,
                    borderColor = ColorWarning10,
                    iconRes = R.drawable.ic_warning,
                    iconTint = ColorWarning,
                    title = stringResource(R.string.test_environment_unsynced_changes_title),
                )

                DialogTextItem(title = stringResource(R.string.test_environment_unsynced_changes_description))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
            ) {
                DialogButton(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.got_it),
                    titleColor = MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        showUnsyncedChanges = false
                    },
                )
            }
        }
    }
}
