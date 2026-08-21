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
package com.whimo.presentation.settings.account.editphone

import android.content.Context
import com.whimo.R
import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.config.RegistrationPhoneRegionPolicy
import com.whimo.domain.settings.SettingsInteractor
import com.whimo.network.ErrorHandler
import com.whimo.providers.ResourceProvider
import com.whimo.providers.RemoteConfigProvider
import com.whimo.utils.PhoneNumberUtils
import com.whimo.utils.ValidationUtils
import com.whimo.utils.getLastLocation

class EditPhoneViewModel(
    private val interactor: SettingsInteractor,
    private val errorHandler: ErrorHandler,
    private val resourceProvider: ResourceProvider,
    private val remoteConfigProvider: RemoteConfigProvider,
) : BaseViewModel<EditPhoneContract.Binding>() {

    private var phone: String? = null
    private var phoneRegion = PhoneNumberUtils.getDefaultPhoneRegion()
    private var phoneNumber: String = ""
    private var phoneError: String = ""
    private var phoneRegionPolicy = RegistrationPhoneRegionPolicy.Disabled
    private var phoneRegionSelectedManually: Boolean = false

    override fun createBinding(): EditPhoneContract.Binding {
        return EditPhoneContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is EditPhoneContract.Event.OnCreate -> onCreate(event.context, event.phone)
            is EditPhoneContract.Event.OnPhoneRegionChanged -> onPhoneRegionChanged(event.phoneRegion)
            is EditPhoneContract.Event.OnPhoneChanged -> onPhoneChanged(event.phone)
            is EditPhoneContract.Event.OnSaveClick -> onSaveClick()
        }
    }

    override fun copyBinding(binding: EditPhoneContract.Binding): EditPhoneContract.Binding {
        return binding.copy()
    }

    private fun updateView() {
        val phoneRegionUnsupported = !phoneRegionPolicy.isPhoneRegionSupported(phoneRegion)

        updateBinding { b ->
            b.phoneNumber = phoneNumber
            b.phoneRegion = phoneRegion
            b.phoneError = if (phoneRegionUnsupported) {
                resourceProvider.getString(R.string.phone_verification_unavailable_settings)
            } else {
                phoneError
            }
            b.saveEnabled = !phoneRegionUnsupported
        }
    }

    private fun onCreate(context: Context, phone: String?) {
        refreshPhoneRegionPolicy()
        val hasInitialPhone = !phone.isNullOrEmpty()

        if (hasInitialPhone) {
            this.phone = phone
            val parsedPhone = PhoneNumberUtils.parsePhone(phone)
            this.phoneRegion = parsedPhone.first
            this.phoneNumber = "${parsedPhone.second}"
            this.phoneRegionSelectedManually = true
        }

        updateView()

        if (!phoneRegionSelectedManually) {
            launch {
                val location = getLastLocation(context)

                if (location == null) {
                    setEffect(EditPhoneContract.Effect.RequestLocationPermission)

                } else {
                    PhoneNumberUtils.getCountryCodeFromLocation(context, location)?.let { countryCode ->
                        if (!phoneRegionSelectedManually) {
                            phoneRegion = PhoneNumberUtils.getPhoneRegion(countryCode)
                            updateView()
                        }
                    }
                }
            }
        }

        setEffect(EditPhoneContract.Effect.ForceUpdateFields)
    }

    private fun refreshPhoneRegionPolicy() {
        phoneRegionPolicy = remoteConfigProvider.getRegistrationPhoneRegionPolicy()
        updateView()

        launch {
            remoteConfigProvider.refresh()
            phoneRegionPolicy = remoteConfigProvider.getRegistrationPhoneRegionPolicy()
            updateView()
        }
    }

    private fun onPhoneChanged(phone: String) {
        this.phoneNumber = phone
        this.phoneError = ""
        updateView()
    }

    private fun onPhoneRegionChanged(phoneRegion: PhoneNumberUtils.PhoneRegion) {
        this.phoneRegion = phoneRegion
        this.phoneRegionSelectedManually = true
        this.phoneError = ""
        updateView()
    }

    private fun onSaveClick() {
        if (!phoneRegionPolicy.isPhoneRegionSupported(phoneRegion)) {
            phoneError = resourceProvider.getString(R.string.phone_verification_unavailable_settings)
            updateView()
            return
        }

        val validationStatus = ValidationUtils.validatePhoneNumber(phoneRegion.phoneCode, phoneNumber)

        phoneError = when(validationStatus) {
            ValidationUtils.ValidationState.Empty -> "empty"
            ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_phone)
            ValidationUtils.ValidationState.Valid -> ""
        }

        if (validationStatus == ValidationUtils.ValidationState.Valid) {
            val newPhone = "+${phoneRegion.phoneCode}$phoneNumber"
            editPhone(phone, newPhone)
        } else {
            updateView()
        }
    }

    private fun editPhone(oldPhone: String?, newPhone: String) {
        launch {
            setEffect(EditPhoneContract.Effect.ToggleLoader(true))

            if (oldPhone != null) {
                interactor.deletePhone(oldPhone)
                    .onSuccess {
                        setEffect(EditPhoneContract.Effect.ToggleLoader(false))
                        addPhone(newPhone)
                    }
                    .onError {
                        val errorMessage = errorHandler.parseError(it)

                        setEffect(
                            EditPhoneContract.Effect.ToggleLoader(false),
                            EditPhoneContract.Effect.ShowMessage(errorMessage)
                        )
                    }
            } else {
                addPhone(newPhone)
            }
        }
    }

    private fun addPhone(phone: String) {
        launch {
            setEffect(EditPhoneContract.Effect.ToggleLoader(true))

            interactor.addPhone(phone)
                .onSuccess {
                    setEffect(
                        EditPhoneContract.Effect.ToggleLoader(false),
                        EditPhoneContract.Effect.NavigateToPhoneOtp(phone)
                    )
                }
                .onError {
                    val errorMessage = errorHandler.parseError(it)

                    setEffect(
                        EditPhoneContract.Effect.ToggleLoader(false),
                        EditPhoneContract.Effect.ShowMessage(errorMessage)
                    )
                }
        }
    }
}
