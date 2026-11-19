package com.jorld.controller

import com.jorld.service.SpotifyService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping


@Controller
@RequestMapping("/")
class HomeController(val spotifyService: SpotifyService) {
    val jodioPlaylistID = "5lcqvWNgzb2DKDAzkKidqA"

    @GetMapping()
    fun home(model: Model): String {
        val playlist = spotifyService.getPlaylistItems(jodioPlaylistID)

	model.addAttribute("playlist", playlist)

	return "index"
    }
}
