package com.jorld.service

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset



@Service
class SpotifyService(
    private val spotifyTokenService: SpotifyTokenService,
) {
    val client = HttpClient.newBuilder().build()
    /*
    val jodioLink = "https://open.spotify.com/playlist/5lcqvWNgzb2DKDAzkKidqA"
    val jodioEndpoint = "v1/playlists/5lcqvWNgzb2DKDAzkKidqA/items"
    val archiveLink = "https://open.spotify.com/playlist/6AdPbvjc4QjFUaYfHbElAr"



    fun getJodioLink(): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(jodioLink))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
    */
    private val restClient = RestClient.create("https://api.spotify.com")

    fun getPlaylistItems(playlistId: String): JsonNode {
	val accessToken = spotifyTokenService.getAccessToken()

        return restClient.get()
            .uri("/v1/playlists/{playlistId}/items", playlistId)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(JsonNode::class.java)
            ?: throw RuntimeException("Could not retrieve playlist")
    }

    fun addToArchive() {}
}
