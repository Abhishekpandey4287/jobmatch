package com.jobmatch.data.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun createHttpClient(
    baseUrl: String,
    tokenProvider: suspend () -> String?,
): HttpClient = HttpClient {

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues  = true
        })
    }

    install(Logging) {
        level = LogLevel.BODY
        logger = Logger.DEFAULT
    }

    install(Auth) {
        bearer {
            loadTokens {
                val token = tokenProvider() ?: return@loadTokens null
                BearerTokens(token, "")
            }
            sendWithoutRequest { request ->
                val path = request.url.encodedPath
                !path.contains("api/v1/auth")
            }
        }
    }

    install(DefaultRequest) {
       // url(baseUrl)
        url.takeFrom("http://192.168.0.101:8080/")
    }

    install(HttpTimeout) {
        requestTimeoutMillis  = 30_000
        connectTimeoutMillis  = 15_000
        socketTimeoutMillis   = 30_000
    }
}