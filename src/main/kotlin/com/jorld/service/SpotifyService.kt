package com.jorld.service

import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset

@Service
class SpotifyService {
    val client = HttpClient.newBuilder().build()

    val jodioLink = "https://open.spotify.com/playlist/5lcqvWNgzb2DKDAzkKidqA"
    val archiveLink = "https://open.spotify.com/playlist/6AdPbvjc4QjFUaYfHbElAr"

    val jodioRespons = getJodio()

    //println("Status Code: ${jodioResponse.statusCode()}")
    //println("RESPONSE\n${response.body()}")


    fun getJodio(): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(jodioLink))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }
}