
package com.jorld.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import java.time.Instant
import java.util.Base64

@Service
class SpotifyTokenService(
    @Value("\${spotify.client-id}")
    private val clientId: String,

    @Value("\${spotify.client-secret}")
    private val clientSecret: String,

    @Value("\${spotify.redirect-uri}")
    private val redirectUri: String
) {
    private val tokenClient =
        RestClient.create("https://accounts.spotify.com")

    private val refreshTokenPath =
        Path.of("/var/lib/jorld/spotify-refresh-token")

    private var cachedAccessToken: String? = null
    private var expiresAt: Instant = Instant.MIN

    private val basicAuth: String
        get() {
            val credentials = "$clientId:$clientSecret"
            val encoded = Base64.getEncoder().encodeToString(
                credentials.toByteArray(StandardCharsets.UTF_8)
            )
            return "Basic $encoded"
        }

    @Synchronized
    fun getAccessToken(): String {
        if (
            cachedAccessToken != null &&
            Instant.now().isBefore(expiresAt)
        ) {
            return cachedAccessToken!!
        }

        val refreshToken = readRefreshToken()
            ?: throw IllegalStateException(
                "Spotify is not authorized. Visit /spotify/login"
            )

        val form = LinkedMultiValueMap<String, String>()
        form.add("grant_type", "refresh_token")
        form.add("refresh_token", refreshToken)

        val response = tokenClient.post()
            .uri("/api/token")
            .header("Authorization", basicAuth)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(Map::class.java)
            ?: throw IllegalStateException("Spotify token refresh failed")

        val accessToken = response["access_token"] as? String
            ?: throw IllegalStateException("Spotify returned no access token")

        val expiresIn = (response["expires_in"] as? Number)?.toLong()
            ?: 3600L

        // Spotify may or may not return a replacement refresh token.
        (response["refresh_token"] as? String)?.let {
            saveRefreshToken(it)
        }

        cachedAccessToken = accessToken
        expiresAt = Instant.now().plusSeconds(expiresIn - 60)

        return accessToken
    }

    @Synchronized
    fun exchangeAuthorizationCode(code: String) {
        val form = LinkedMultiValueMap<String, String>()
        form.add("grant_type", "authorization_code")
        form.add("code", code)
        form.add("redirect_uri", redirectUri)

        val response = tokenClient.post()
            .uri("/api/token")
            .header("Authorization", basicAuth)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .body(Map::class.java)
            ?: throw IllegalStateException("Spotify authorization failed")

        val accessToken = response["access_token"] as? String
            ?: throw IllegalStateException("Spotify returned no access token")

        val refreshToken = response["refresh_token"] as? String
            ?: throw IllegalStateException("Spotify returned no refresh token")

        val expiresIn = (response["expires_in"] as? Number)?.toLong()
            ?: 3600L

        saveRefreshToken(refreshToken)

        cachedAccessToken = accessToken
        expiresAt = Instant.now().plusSeconds(expiresIn - 60)
    }

    private fun readRefreshToken(): String? {
        return if (Files.exists(refreshTokenPath)) {
            Files.readString(refreshTokenPath).trim().takeIf {
                it.isNotEmpty()
            }
        } else {
            null
        }
    }

    private fun saveRefreshToken(token: String) {
        Files.createDirectories(refreshTokenPath.parent)
        Files.writeString(refreshTokenPath, token)

        Files.setPosixFilePermissions(
            refreshTokenPath,
            PosixFilePermissions.fromString("rw-------")
        )
    }
}
