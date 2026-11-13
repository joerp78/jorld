package com.jorld.controller

import com.jorld.service.SpotifyService

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping


@RestController
@RequestMapping("/")
class HomeController(val spotifyService: SpotifyService) {

    @GetMapping
    fun home(): String {
        /*
        val jodioResponse = spotifyService.getJodio()
        val responseStatus = jodioResponse.getStatus
        val responseBody = jodioResponse.body()
        */
        return spotifyService.getJodio()
    }
}