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

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.whimo.BuildConfig
import com.whimo.data.geodata.model.response.DownloadGeoDataResponse
import com.whimo.network.ErrorHandler
import com.whimo.network.EnvironmentBaseUrlInterceptor
import com.whimo.network.RequestHeadersInterceptor
import com.whimo.network.authenticator.RefreshService
import com.whimo.network.authenticator.SessionManager
import com.whimo.network.authenticator.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val AUTHORISED = named("authorized")
val UNAUTHORISED = named("unauthorized")

val networkModule = module {
    single {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    single { EnvironmentBaseUrlInterceptor(get()) }

    // OkHttpClient
    single(UNAUTHORISED) {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    factory(AUTHORISED) {
        OkHttpClient()
            .newBuilder()
            .addInterceptor(get<EnvironmentBaseUrlInterceptor>())
            .authenticator(TokenAuthenticator(get(), get()))
            .addInterceptor(RequestHeadersInterceptor(get(), get(), androidContext()))
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit
    single<Retrofit>(UNAUTHORISED) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get<OkHttpClient>(UNAUTHORISED))
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    factory<Retrofit>(AUTHORISED) {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get<OkHttpClient>(AUTHORISED))
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    //Gson
    single {
        GsonBuilder()
            .registerTypeAdapter(DownloadGeoDataResponse::class.java, JsonDeserializer { json, _, _ ->
                val obj = json.asJsonObject

                val success = obj["success"].asBoolean
                val data = obj["data"]?.toString() ?: ""
                val message = obj["message"]?.asString ?: ""

                DownloadGeoDataResponse(
                    success = success,
                    data = data,
                    message = message
                )
            })
            .create()
    }


    single { ErrorHandler(get()) }


    single { get<Retrofit>(UNAUTHORISED).create(RefreshService::class.java) }
    single {
        SessionManager(
            refreshService = get(),
            sharedPreferencesProvider = get(),
            appDatabase = get(),
        )
    }
}
