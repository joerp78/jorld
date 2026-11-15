package com.jorld.controller

import com.jorld.service.SpotifyService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import tools.jackson.databind.JsonNode


@RestController
@RequestMapping("/")
class HomeController(val spotifyService: SpotifyService) {
    val jodioPlaylistID = "5lcqvWNgzb2DKDAzkKidqA"

    @GetMapping()
    fun home(): JsonNode {
        return spotifyService.getPlaylistItems(jodioPlaylistID)
    }
}