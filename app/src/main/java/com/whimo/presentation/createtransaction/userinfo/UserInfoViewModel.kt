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
package com.whimo.presentation.createtransaction.userinfo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import com.whimo.R
import com.whimo.base.BaseViewModel
import com.whimo.base.CoreViewEvent
import com.whimo.data.base.common.onError
import com.whimo.data.base.common.onSuccess
import com.whimo.domain.createtransaction.UserInfoInteractor
import com.whimo.domain.createtransaction.models.CreateTransactionModel
import com.whimo.domain.createtransaction.models.UserInfoModel
import com.whimo.domain.transactions.models.TransactionAction
import com.whimo.extensions.getContactEmail
import com.whimo.extensions.getContactPhone
import com.whimo.network.ErrorHandler
import com.whimo.network.error.ServerError
import com.whimo.providers.ResourceProvider
import com.whimo.utils.PhoneNumberUtils
import com.whimo.utils.ValidationUtils
import com.whimo.utils.getLastLocation

class UserInfoViewModel(
    private val resourceProvider: ResourceProvider,
    private val interactor: UserInfoInteractor,
    private val errorHandler: ErrorHandler,
) : BaseViewModel<UserInfoContract.Binding>() {

    private var action: TransactionAction = TransactionAction.Buying
    private var isProducerTransaction: Boolean = false
    private var isInvite: Boolean = false

    private var username: String? = null
    private var email: String? = null
    private var phoneRegion: PhoneNumberUtils.PhoneRegion? = null
    private var phoneNumber: String? = null
    private var phone: String? = null

    private var usernameError: String = ""
    private var emailError: String = ""
    private var phoneError: String = ""

    private var isEmailContactsRequest = false
    private var phoneRegionSelectedManually: Boolean = false

    override fun createBinding(): UserInfoContract.Binding {
        return UserInfoContract.Binding()
    }

    override fun handleEvents(event: CoreViewEvent) {
        super.handleEvents(event)
        when (event) {
            is UserInfoContract.Event.OnCreate -> onCreate(event.context, event.transaction, event.isInvite)
            is UserInfoContract.Event.OnUsernameChanged -> onUsernameChanged(event.username)
            is UserInfoContract.Event.OnEmailChanged -> onEmailChanged(event.email)
            is UserInfoContract.Event.OnPhoneRegionChanged -> onPhoneRegionChanged(event.phoneRegion)
            is UserInfoContract.Event.OnPhoneNumberChanged -> onPhoneNumberChanged(event.phoneNumber)
            is UserInfoContract.Event.OnEmailContactsClicked -> onContactsClicked(event.context, true)
            is UserInfoContract.Event.OnPhoneContactsClicked -> onContactsClicked(event.context, false)
            is UserInfoContract.Event.OnContactsPermissionResult -> onContactsPermissionResult(event.context, event.result)
            is UserInfoContract.Event.OnConfirm -> onConfirm()
            is UserInfoContract.Event.OnDownstreamClick -> onDownstreamClick()
            is UserInfoContract.Event.OnInitialClick -> onInitialClick()
        }
    }

    override fun copyBinding(binding: UserInfoContract.Binding): UserInfoContract.Binding {
        return binding.copy()
    }

    private fun updateView() {
        updateBinding { b ->
            if (action == TransactionAction.Buying) {
                b.title = resourceProvider.getString(R.string.supplier_information)
                b.nameLabel = resourceProvider.getString(R.string.supplier_s_username)
            } else {
                b.title = resourceProvider.getString(R.string.buyer_information)
                b.nameLabel = resourceProvider.getString(R.string.buyer_s_username)
            }

            b.username = username ?: ""
            b.email = email ?: ""
            b.phoneRegion = phoneRegion ?: PhoneNumberUtils.getDefaultPhoneRegion()
            b.phoneNumber = phoneNumber ?: ""

            b.usernameError = usernameError
            b.emailError = emailError
            b.phoneError = phoneError
        }
    }

    private fun onCreate(context: Context, transaction: CreateTransactionModel, isInvite: Boolean) {
        action = transaction.action ?: TransactionAction.Buying
        isProducerTransaction = transaction.isProducerTransaction
        this.isInvite = isInvite

        if (username == null) username = transaction.userInfo?.name
        if (email == null) email = transaction.userInfo?.email

        if (transaction.userInfo?.phone != null) {
            val parsedPhone = PhoneNumberUtils.parsePhone(transaction.userInfo.phone)

            if (phoneRegion == null) phoneRegion = parsedPhone.first
            if (phoneNumber == null) phoneNumber = "${parsedPhone.second}"
            phoneRegionSelectedManually = true

            updateView()
        }

        if (phoneRegion == null && !phoneRegionSelectedManually) {
            launch {
                val location = getLastLocation(context)

                if (location == null) {
                    setEffect(UserInfoContract.Effect.RequestLocationPermission)

                } else {
                    PhoneNumberUtils.getCountryCodeFromLocation(context, location)
                        ?.let { countryCode ->
                            if (!phoneRegionSelectedManually) {
                                phoneRegion = PhoneNumberUtils.getPhoneRegion(countryCode)
                                updateView()
                            }
                        }
                }
            }
        }

        setEffect(UserInfoContract.Effect.ForceUpdateFields)
    }

    private fun onUsernameChanged(username: String) {
        this.username = username
        this.usernameError = ""
        updateView()
    }

    private fun onEmailChanged(email: String) {
        this.email = email
        this.emailError = ""
        updateView()
    }

    private fun onPhoneRegionChanged(phoneRegion: PhoneNumberUtils.PhoneRegion) {
        this.phoneRegion = phoneRegion
        this.phoneRegionSelectedManually = true
        this.phoneError = ""
        updateView()
    }

    private fun onPhoneNumberChanged(phoneNumber: String) {
        this.phoneNumber = phoneNumber
        this.phoneError = ""
        updateView()
    }

    private fun onContactsClicked(context: Context, isEmailContactsRequest: Boolean = false) {
        this.isEmailContactsRequest = isEmailContactsRequest

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if (isEmailContactsRequest) {
                setEffect(UserInfoContract.Effect.OpenEmailContacts)
            } else {
                setEffect(UserInfoContract.Effect.OpenPhoneContacts)
            }
        } else {
            setEffect(UserInfoContract.Effect.RequestContactsPermission)
        }
    }

    private fun onContactsPermissionResult(context: Context, result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val uri = intent?.data

            if (uri != null) {
                if (isEmailContactsRequest) {
                    val email = context.getContactEmail(uri)

                    if (email != null) {
                        this.email = email

                        updateView()

                        setEffect(UserInfoContract.Effect.ForceUpdateFields)
                    }

                } else {
                    val phone = context.getContactPhone(uri)

                    if (phone != null) {
                        try {
                            val parsedPhone = PhoneNumberUtils.parsePhone(phone)

                            phoneRegion = parsedPhone.first
                            phoneNumber = "${parsedPhone.second}"
                            phoneRegionSelectedManually = true

                            updateView()

                            setEffect(UserInfoContract.Effect.ForceUpdateFields)

                        } catch (e: Exception) {
                            setEffect(UserInfoContract.Effect.ShowMessage(resourceProvider.getString(R.string.invalid_phone)))
                        }
                    }
                }
            }
        }
    }

    private fun onConfirm() {
        val emailValidationStatus = ValidationUtils.validateEmail(email ?: "")
        val phoneValidationStatus = ValidationUtils.validatePhoneNumber(phoneRegion?.phoneCode ?: 1, phoneNumber ?: "")

        if (emailValidationStatus == ValidationUtils.ValidationState.Invalid ||
            phoneValidationStatus == ValidationUtils.ValidationState.Invalid) {

            emailError = when(emailValidationStatus) {
                ValidationUtils.ValidationState.Empty -> ""
                ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_email)
                ValidationUtils.ValidationState.Valid -> ""
            }

            phoneError = when(phoneValidationStatus) {
                ValidationUtils.ValidationState.Empty -> ""
                ValidationUtils.ValidationState.Invalid -> resourceProvider.getString(R.string.invalid_phone)
                ValidationUtils.ValidationState.Valid -> ""
            }

            updateView()

        } else {
            phone = if (phoneNumber.isNullOrEmpty()) {
                null
            } else {
                "+${phoneRegion?.phoneCode}$phoneNumber"
            }

            if (isInvite) {
                if (emailValidationStatus == ValidationUtils.ValidationState.Valid) {
                    checkUserExist(email)

                } else if (phoneValidationStatus == ValidationUtils.ValidationState.Valid) {
                    checkUserExist(phone)
                }

            } else if (action == TransactionAction.Buying && !isProducerTransaction) {
                if (emailValidationStatus == ValidationUtils.ValidationState.Valid) {
                    checkUserNotExist(email) {
                        setEffect(
                            if (it) {
                                UserInfoContract.Effect.ShowEmailNotExist
                            } else {
                                UserInfoContract.Effect.UserInfoConfirmed(
                                    UserInfoModel(username, email, phone)
                                )
                            }
                        )
                    }

                } else if (phoneValidationStatus == ValidationUtils.ValidationState.Valid) {
                    checkUserNotExist(phone) {
                        setEffect(
                            if (it) {
                                UserInfoContract.Effect.ShowPhoneNotExist
                            } else {
                                UserInfoContract.Effect.UserInfoConfirmed(
                                    UserInfoModel(username, email, phone)
                                )
                            }
                        )
                    }
                }

            } else {
                setEffect(
                    UserInfoContract.Effect.UserInfoConfirmed(
                        UserInfoModel(username, email, phone)
                    )
                )
            }
        }
    }

    private fun checkUserExist(identifier: String?) {
        if (identifier != null) {
            launch {
                setEffect(UserInfoContract.Effect.ToggleLoader(true))

                interactor.checkUserExist(identifier, false)
                    .onSuccess {
                        setEffect(UserInfoContract.Effect.ToggleLoader(false))

                        if (it == true) {
                            setEffect(UserInfoContract.Effect.ShowUserExist)

                        } else {
                            setEffect(
                                UserInfoContract.Effect.UserInfoConfirmed(
                                    UserInfoModel(username, email, phone)
                                )
                            )
                        }
                    }
                    .onError {
                        if (it is ServerError && it.code == 404) {
                            setEffect(
                                UserInfoContract.Effect.ToggleLoader(false),
                                UserInfoContract.Effect.UserInfoConfirmed(
                                    UserInfoModel(username, email, phone)
                                )
                            )

                        } else {
                            val errorMessage = errorHandler.parseError(it)

                            setEffect(
                                UserInfoContract.Effect.ToggleLoader(false),
                                UserInfoContract.Effect.ShowMessage(errorMessage)
                            )
                        }
                    }
            }
        }
    }

    private fun checkUserNotExist(identifier: String?, callback: (Boolean) -> Unit) {
        if (identifier != null) {
            launch {
                setEffect(UserInfoContract.Effect.ToggleLoader(true))

                interactor.checkUserExist(identifier, true)
                    .onSuccess {
                        setEffect(UserInfoContract.Effect.ToggleLoader(false))

                        callback(it == false)
                    }
                    .onError {
                        setEffect(UserInfoContract.Effect.ToggleLoader(false))

                        if (it is ServerError && it.code == 404) {
                            callback(true)

                        } else {
                            val errorMessage = errorHandler.parseError(it)

                            setEffect(UserInfoContract.Effect.ShowMessage(errorMessage))
                        }
                    }
            }
        }
    }

    private fun  onDownstreamClick() {
        setEffect(
            UserInfoContract.Effect.ConvertToDownstream(
                UserInfoModel(
                    email = email,
                    phone = phone
                )
            )
        )
    }

    private fun  onInitialClick() {
        setEffect(
            UserInfoContract.Effect.UserInfoConfirmed(
                UserInfoModel(
                    name = username,
                    email = email,
                    phone = phone
                )
            )
        )
    }
}
