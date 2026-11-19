
package com.jorld.controller

import com.jorld.service.SpotifyTokenService
import jakarta.servlet.http.HttpSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.security.SecureRandom
import java.util.Base64

@RestController
class SpotifyAuthController(
    @Value("\${spotify.client-id}")
    private val clientId: String,

    @Value("\${spotify.redirect-uri}")
    private val redirectUri: String,

    private val spotifyTokenService: SpotifyTokenService
) {
    @GetMapping("/spotify/login")
    fun login(session: HttpSession): ResponseEntity<Void> {
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)

        val state = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes)

        session.setAttribute("spotify_oauth_state", state)

        val authorizationUrl = UriComponentsBuilder
            .fromUriString("https://accounts.spotify.com/authorize")
            .queryParam("client_id", clientId)
            .queryParam("response_type", "code")
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", "playlist-read-private")
            .queryParam("state", state)
            .build()
            .encode()
            .toUri()

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(authorizationUrl)
            .build()
    }

    @GetMapping("/spotify/callback")
    fun callback(
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) error: String?,
        session: HttpSession
    ): ResponseEntity<String> {
        val expectedState =
            session.getAttribute("spotify_oauth_state") as? String

        session.removeAttribute("spotify_oauth_state")

        if (error != null) {
            return ResponseEntity.badRequest()
                .body("Spotify authorization denied: $error")
        }

        if (
            code == null ||
            state == null ||
            expectedState == null ||
            state != expectedState
        ) {
            return ResponseEntity.badRequest()
                .body("Invalid Spotify authorization state.")
        }

        spotifyTokenService.exchangeAuthorizationCode(code)

        return ResponseEntity.ok(
            "Spotify authorization successful! Jorld is connected."
        )
    }
}
