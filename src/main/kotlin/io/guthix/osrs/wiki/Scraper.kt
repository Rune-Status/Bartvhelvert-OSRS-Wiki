package io.guthix.osrs.wiki

import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import java.io.IOException

private val logger = KotlinLogging.logger {}

private const val wikiUrl = "https://oldschool.runescape.wiki"

/** Scrapes the wiki and retrieves the wiki text.*/
@KtorExperimentalAPI
suspend fun scrapeWikiText(type: WikiTextParser<*>, id: Int, name: String): String {
    HttpClient(CIO) {
        followRedirects = false
    }.use { client ->
        val queryUrl = if(name.contains("%")) {
            "$wikiUrl/w/Special:Lookup?type=${type.queryString}&id=$id"
        } else {
            "$wikiUrl/w/Special:Lookup?type=${type.queryString}&id=$id&name=$name"
        }
        logger.info("REQUEST - QUERY - $queryUrl")
        val redirect = client.call(queryUrl).response.headers["location"]
            ?: throw IOException("Could not retrieve redirect for $queryUrl")
        redirect.let {
            val rawUrl = "${it.substringBefore("#")}?action=raw"
            logger.info("REQUEST - RAW - $rawUrl")
            return client.get(rawUrl)
        }
    }
}