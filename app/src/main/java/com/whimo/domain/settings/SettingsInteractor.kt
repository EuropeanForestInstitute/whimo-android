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
package com.whimo.domain.settings

import com.whimo.data.base.common.BaseResult
import com.whimo.data.settings.model.mappers.toData
import com.whimo.data.settings.model.request.AddGadgetRequest
import com.whimo.data.settings.model.request.ChangePasswordRequest
import com.whimo.data.settings.model.request.DeleteGadgetRequest
import com.whimo.data.settings.model.request.NotificationSettingsRequest
import com.whimo.data.settings.repository.SettingsRepository
import com.whimo.domain.settings.models.AccountModel
import com.whimo.domain.settings.models.NotificationSettingsModel
import com.whimo.network.authenticator.SessionManager

interface SettingsInteractor {
    suspend fun getAccountInfo(): BaseResult<AccountModel>
    suspend fun changePassword(currentPassword: String, newPassword: String): BaseResult<String>
    suspend fun logout()
    suspend fun deleteAccount(): BaseResult<String>
    suspend fun getNotificationSettings(): BaseResult<List<NotificationSettingsModel>>
    suspend fun getNotificationSettingsFromDB(): List<NotificationSettingsModel>
    suspend fun updateNotificationSettings(settings: List<NotificationSettingsModel>): BaseResult<String>
    suspend fun updateNotificationSettingsDB(items: List<NotificationSettingsModel>?)
    suspend fun addEmail(email: String): BaseResult<Boolean>
    suspend fun deleteEmail(email: String): BaseResult<Boolean>
    suspend fun addPhone(phone: String): BaseResult<Boolean>
    suspend fun deletePhone(phone: String): BaseResult<Boolean>
}

class SettingsInteractorImpl(
    private val repository: SettingsRepository,
    private val sessionManager: SessionManager,
) : SettingsInteractor {

    override suspend fun getAccountInfo(): BaseResult<AccountModel> {
        return repository.getAccountInfo()
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): BaseResult<String> {
        return repository.changePassword(
            ChangePasswordRequest(
                current_password = currentPassword,
                new_password = newPassword
            )
        )
    }

    override suspend fun logout() {
        sessionManager.clearAllData()
    }

    override suspend fun deleteAccount(): BaseResult<String> {
        return repository.deleteAccount()
    }

    override suspend fun getNotificationSettings(): BaseResult<List<NotificationSettingsModel>> {
        return repository.getNotificationSettings()
    }

    override suspend fun getNotificationSettingsFromDB(): List<NotificationSettingsModel> {
        return repository.getNotificationSettingsFromDB()
    }

    override suspend fun updateNotificationSettings(settings: List<NotificationSettingsModel>): BaseResult<String> {
        return repository.updateNotificationSettings(
            NotificationSettingsRequest(
                settings = settings.map { it.toData() }
            )
        )
    }

    override suspend fun updateNotificationSettingsDB(items: List<NotificationSettingsModel>?) {
        repository.updateNotificationSettingsDB(items)
    }

    override suspend fun addEmail(email: String): BaseResult<Boolean> {
        return repository.addGadget(AddGadgetRequest(email = email, phone = null))
    }

    override suspend fun deleteEmail(email: String): BaseResult<Boolean> {
        return repository.deleteGadget(DeleteGadgetRequest(email))
    }

    override suspend fun addPhone(phone: String): BaseResult<Boolean> {
        return repository.addGadget(AddGadgetRequest(email = null, phone = phone))
    }

    override suspend fun deletePhone(phone: String): BaseResult<Boolean> {
        return repository.deleteGadget(DeleteGadgetRequest(phone))
    }
}
