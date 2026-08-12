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
package com.whimo.di

import com.whimo.data.AppDatabase
import com.whimo.data.settings.repository.SettingsRepository
import com.whimo.data.settings.repository.SettingsRepositoryImpl
import com.whimo.data.settings.service.SettingsService
import com.whimo.domain.settings.SettingsInteractor
import com.whimo.domain.settings.SettingsInteractorImpl
import com.whimo.presentation.settings.SettingsViewModel
import com.whimo.presentation.settings.account.AccountInfoViewModel
import com.whimo.presentation.settings.account.editemail.EditEmailViewModel
import com.whimo.presentation.settings.account.editphone.EditPhoneViewModel
import com.whimo.presentation.settings.changepassword.ChangePasswordViewModel
import com.whimo.presentation.settings.language.LanguageViewModel
import com.whimo.presentation.settings.notificationsettings.NotificationSettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit

val settingsModule = module {
    // Services
    single { get<Retrofit>(AUTHORISED).create(SettingsService::class.java) }

    // DB
    single { get<AppDatabase>().notificationSettingsDao() }

    // Repositories
    factory<SettingsRepository> { SettingsRepositoryImpl(service = get(), dao = get()) }

    // Interactors
    factory<SettingsInteractor> { SettingsInteractorImpl(repository = get(), sessionManager = get()) }

    // ViewModels
    viewModel { SettingsViewModel(interactor = get(), errorHandler = get(), resourceProvider = get()) }
    viewModel { AccountInfoViewModel(interactor = get(), errorHandler = get(), sharedPreferencesProvider = get(), resourceProvider = get()) }
    viewModel { EditEmailViewModel(interactor = get(), errorHandler = get(), resourceProvider = get()) }
    viewModel { EditPhoneViewModel(interactor = get(), errorHandler = get(), resourceProvider = get(), remoteConfigProvider = get()) }
    viewModel { ChangePasswordViewModel(interactor = get(), errorHandler = get(), resourceProvider = get()) }
    viewModel { NotificationSettingsViewModel(interactor = get(), errorHandler = get(), sharedPreferencesProvider = get(), resourceProvider = get()) }
    viewModel { LanguageViewModel(appLocaleManager = get()) }
}
