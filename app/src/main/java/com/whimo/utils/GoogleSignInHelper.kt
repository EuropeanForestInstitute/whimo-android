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
package com.whimo.utils

import android.app.Activity
import android.util.Base64
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.whimo.BuildConfig.GOOGLE_AUTH_CLIENT_ID
import java.security.SecureRandom

sealed interface GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data object NoCredential : GoogleSignInResult
    data object Failed : GoogleSignInResult
}

object GoogleSignInHelper {

    suspend fun signIn(
        activity: Activity,
        enableNonce: Boolean = true
    ): GoogleSignInResult {
        val nonce = generateNonce()

        val googleIdOptionBuilder = GetGoogleIdOption.Builder()
            .setServerClientId(GOOGLE_AUTH_CLIENT_ID)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)

        if (enableNonce) {
            googleIdOptionBuilder.setNonce(nonce)
        }

        val googleIdOption = googleIdOptionBuilder.build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(activity)
        return try {
            val result = credentialManager.getCredential(activity, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Log.i("GoogleSignIn", "Google ID token received")
            GoogleSignInResult.Success(googleIdTokenCredential.idToken)
        } catch (e: GetCredentialCancellationException) {
            Log.w("GoogleSignIn", "Sign-in cancelled by user")
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            Log.w("GoogleSignIn", "No Google credentials available", e)
            GoogleSignInResult.NoCredential
        } catch (e: GetCredentialException) {
            Log.e("GoogleSignIn", "Sign-in failed: ${e.message}", e)
            GoogleSignInResult.Failed
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("GoogleSignIn", "Failed to parse Google credential", e)
            GoogleSignInResult.Failed
        }
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
