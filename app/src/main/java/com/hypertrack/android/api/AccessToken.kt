package com.hypertrack.android.api

import android.os.Build
import android.util.Log
import com.hypertrack.android.repository.AUTH_HEADER_KEY
import com.hypertrack.android.repository.AccessTokenRepository
import com.hypertrack.logistics.android.github.BuildConfig
import okhttp3.internal.userAgent
import okhttp3.*
import java.lang.IllegalStateException

const val TAG = "AccessToken"

class AccessTokenInterceptor(private val accessTokenRepository: AccessTokenRepository): Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        val token = accessTokenRepository.getAccessToken()
        val request = chain.request().newBuilder().addHeader("Authorization","Bearer $token").build()
        return chain.proceed(request)

    }

}

class UserAgentInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val networkingLibrary = userAgent
        val request = chain.request().newBuilder()
            .addHeader("User-Agent",
                "VisitsApp/${BuildConfig.VERSION_NAME} $networkingLibrary Android/${Build.VERSION.RELEASE}"
            )
            .build()
        return chain.proceed(request)

    }

}

class AccessTokenAuthenticator(private val accessTokenRepository: AccessTokenRepository) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val accessToken = accessTokenRepository.getAccessToken()

        if (response.authenticatedWithSameToken(accessToken)) {
            return null
        }

        synchronized(this) {
            return try {
                var updatedToken = accessTokenRepository.getAccessToken()
                if (updatedToken == accessToken) {
                    updatedToken = accessTokenRepository.refreshToken()
                }
                response.request.newBuilder()
                    .addHeader(AUTH_HEADER_KEY, "Bearer $updatedToken")
                    .build()
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Authentication call failed", e)
                null
            }
        }
    }
}

fun Response.authenticatedWithSameToken(token : String) : Boolean = header(AUTH_HEADER_KEY, "")?.endsWith(token)?:false