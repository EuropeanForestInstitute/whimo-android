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
package com.whimo.network

import com.whimo.BuildConfig
import com.whimo.providers.TestEnvironmentManager
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class EnvironmentBaseUrlInterceptor(
    private val testEnvironmentManager: TestEnvironmentManager,
) : Interceptor {
    private val liveBaseUrl = BuildConfig.BASE_URL.toHttpUrl()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val targetBaseUrl = testEnvironmentManager.baseUrl().toHttpUrl()

        val updatedRequest = request.newBuilder()
            .url(request.url.withBaseUrl(liveBaseUrl, targetBaseUrl))
            .build()

        return chain.proceed(updatedRequest)
    }

    private fun HttpUrl.withBaseUrl(
        sourceBaseUrl: HttpUrl,
        targetBaseUrl: HttpUrl,
    ): HttpUrl {
        val sourceBasePathSegments = sourceBaseUrl.nonEmptyEncodedPathSegments()
        val originalPathSegments = encodedPathSegments
        val relativePathSegments = if (
            originalPathSegments.size >= sourceBasePathSegments.size &&
            originalPathSegments.take(sourceBasePathSegments.size) == sourceBasePathSegments
        ) {
            originalPathSegments.drop(sourceBasePathSegments.size)
        } else {
            originalPathSegments
        }

        return newBuilder()
            .scheme(targetBaseUrl.scheme)
            .host(targetBaseUrl.host)
            .port(targetBaseUrl.port)
            .encodedPath(buildEncodedPath(targetBaseUrl, relativePathSegments))
            .build()
    }

    private fun HttpUrl.nonEmptyEncodedPathSegments(): List<String> {
        return encodedPathSegments.filter { it.isNotEmpty() }
    }

    private fun buildEncodedPath(
        targetBaseUrl: HttpUrl,
        relativePathSegments: List<String>,
    ): String {
        val pathSegments = targetBaseUrl.nonEmptyEncodedPathSegments() +
                relativePathSegments.dropWhile { it.isEmpty() }

        if (pathSegments.isEmpty()) {
            return "/"
        }

        return "/" + pathSegments.joinToString("/")
    }
}
