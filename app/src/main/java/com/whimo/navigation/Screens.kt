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
package com.whimo.navigation

import android.net.Uri

private fun String.addArgKeys(vararg keys: String): String {
    var route = this
    keys.forEach { route = "$route/{$it}" }
    return route
}

sealed class Screens(val route: String) {
    //Auth
    data object LoginScreen : Screens("login_screen")

    data object RegistrationScreen : Screens("registration_screen")
    data object RegistrationConfirmEmailCodeScreen : Screens("registration_confirm_email_code_screen".addArgKeys(ARG_KEY_EMAIL))
    data object RegistrationConfirmPhoneCodeScreen : Screens("registration_confirm_phone_code_screen".addArgKeys(ARG_KEY_PHONE))

    data object ForgotPasswordScreen : Screens("forgot_password_screen")
    data object ForgotPasswordConfirmEmailCodeScreen : Screens("forgot_password_confirm_email_code_screen".addArgKeys(ARG_KEY_EMAIL))
    data object ForgotPasswordConfirmPhoneCodeScreen : Screens("forgot_password_confirm_phone_code_screen".addArgKeys(ARG_KEY_PHONE))

    data object CreateNewPasswordScreen : Screens("create_password_screen".addArgKeys(ARG_KEY_USERNAME, ARG_KEY_CODE))

    // Main
    data object Home : Screens("screen_home")
    data object Balances : Screens("screen_balances")
    data object CreateTransaction : Screens("screen_create_transaction")
    data object Settings : Screens("screen_settings")

    // Transaction details
    data object TransactionDetails : Screens("screen_transaction_details")
    data object InitialSuppliersHistory : Screens("screen_initial_suppliers_history".addArgKeys(ARG_KEY_JSON))
    data object SuppliersHistory : Screens("screen_suppliers_history".addArgKeys(ARG_KEY_JSON))

    // Balances
    data object GroupBalances : Screens("screen_group_balances")
    data object ConvertRecipes : Screens("screen_convert_recipes".addArgKeys(ARG_KEY_JSON))
    data object ConvertCommodity : Screens("screen_convert_commodity".addArgKeys(ARG_KEY_JSON))


    // Create transaction
    data object CreateTransaction2 : Screens("screen_create_transaction2")
    data object CreateTransaction3 : Screens("screen_create_transaction3")
    data object CreateTransaction4 : Screens("screen_create_transaction4")
    data object CreateTransactionForm : Screens("screen_create_transaction_form")
    data object CommodityTypes : Screens("screen_commodity_types")
    data object CommodityVolume : Screens("screen_commodity_volume")
    data object SupplierInfo : Screens("screen_supplier_info")
    data object InviteUser : Screens("screen_invite_user")
    data object FarmGeoData : Screens("screen_farm_geo_data")
    data object UploadFile : Screens("screen_upload_file")
    data object Map : Screens("screen_map")
    data object QrScan : Screens("screen_qr_scan")

    // Settings
    data object AccountInfo : Screens("screen_account_info")
    data object ChangePassword : Screens("screen_change_password")
    data object NotificationSettings : Screens("screen_notification_settings")
    data object Language : Screens("screen_language")

    // Account
    data object EditEmail : Screens("screen_edit_email".addArgKeys(ARG_KEY_EMAIL))
    data object EditPhone : Screens("screen_edit_phone".addArgKeys(ARG_KEY_PHONE))
    data object ConfirmEmailCodeScreen : Screens("confirm_email_code_screen".addArgKeys(ARG_KEY_EMAIL))
    data object ConfirmPhoneCodeScreen : Screens("confirm_phone_code_screen".addArgKeys(ARG_KEY_PHONE))

    // Notifications
    data object Notifications : Screens("screen_notifications")


    fun putArgs(vararg args: Pair<String, String>): String {
        var route = route
        args.forEach { route = route.replace("{${it.first}}", Uri.encode(it.second)) }
        return route
    }

    companion object {
        const val ARG_KEY_PHONE = "phone"
        const val ARG_KEY_EMAIL = "email"
        const val ARG_KEY_JSON = "json"
        const val ARG_KEY_USERNAME = "username"
        const val ARG_KEY_CODE = "code"
    }
}
