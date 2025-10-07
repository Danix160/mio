package com.toonitalia

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.Jsoup

class ToonitaliaPlugin : MainAPI() {
    override var mainUrl = "https://toonitalia.xyz"
    override var name = "Toonitalia"
    override val hasMainPage = true
    override var lang = "it"
    override val supportedTypes = setOf(TvType.Anime, TvType.TvSeries, TvType.Movie)

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document

        val items = document.select(".post").mapNotNull {
            val title = it.select("h2").text()
            val href = it.select("a").attr("href")
            val img = it.select("img").attr("src")
            newAnimeSearchResponse(title, href, TvType.Anime) {
                this.posterUrl = img
            }
        }

        return newHomePageResponse(listOf(HomePageList("Ultimi Aggiornamenti", items)))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?s=${query.replace(" ", "+")}").document

        return document.select(".post").mapNotNull {
            val title = it.select("h2").text()
            val href = it.select("a").attr("href")
            val img = it.select("img").attr("src")
            newAnimeSearchResponse(title, href, TvType.Anime) {
                this.posterUrl = img
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.select("h1").text()
        val poster = document.select(".post img").attr("src")

        val episodes = document.select("a[href*=\"toonitalia.xyz/\"]").mapNotNull {
            val name = it.text()
            val link = it.attr("href")
            Episode(link, name)
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.episodes = episodes
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val document = app.get(data).document
        val iframe = document.select("iframe").attr("src")

        if (iframe.isNotEmpty()) {
            loadExtractor(iframe, data, subtitleCallback, callback)
            return true
        }

        return false
    }
}
