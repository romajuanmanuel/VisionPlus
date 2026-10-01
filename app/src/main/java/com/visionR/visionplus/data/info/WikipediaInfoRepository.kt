package com.visionR.visionplus.data.info

import com.visionR.visionplus.domain.model.ObjectInfo
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Looks up an object by its English label (what ML Kit returns): the English Wikipedia
 * page gives the Wikidata id, Wikidata gives the Spanish page title, and the Spanish
 * summary is used when it exists (English otherwise).
 *
 * Returns null when there is no suitable article; throws [IOException] on network errors.
 */
class WikipediaInfoRepository {

    private val cache = ConcurrentHashMap<String, ObjectInfo>()

    suspend fun getInfo(label: String): ObjectInfo? = withContext(Dispatchers.IO) {
        cache[label] ?: lookup(label)?.also { cache[label] = it }
    }

    private fun lookup(label: String): ObjectInfo? {
        val english = fetchSummary("en", label) ?: return null
        if (english.optString("type") == "disambiguation") return null

        val spanishTitle = english.optString("wikibase_item").takeIf { it.isNotBlank() }
            ?.let { fetchSpanishTitle(it) }
        val spanish = spanishTitle?.let { fetchSummary("es", it) }
            ?.takeIf { it.optString("type") != "disambiguation" }

        val chosen = spanish ?: english
        val summary = chosen.optString("extract").takeIf { it.isNotBlank() } ?: return null
        return ObjectInfo(
            title = chosen.optString("title"),
            description = chosen.optString("description").takeIf { it.isNotBlank() },
            summary = summary,
            pageUrl = chosen.optJSONObject("content_urls")
                ?.optJSONObject("mobile")
                ?.optString("page")
                ?.takeIf { it.isNotBlank() },
            language = if (spanish != null) "es" else "en"
        )
    }

    private fun fetchSummary(language: String, title: String): JSONObject? =
        getJson("https://$language.wikipedia.org/api/rest_v1/page/summary/${encode(title)}")

    private fun fetchSpanishTitle(wikidataId: String): String? =
        getJson(
            "https://www.wikidata.org/w/api.php?action=wbgetentities&ids=$wikidataId" +
                "&props=sitelinks&sitefilter=eswiki&format=json"
        )
            ?.optJSONObject("entities")
            ?.optJSONObject(wikidataId)
            ?.optJSONObject("sitelinks")
            ?.optJSONObject("eswiki")
            ?.optString("title")
            ?.takeIf { it.isNotBlank() }

    /** Returns null for 404 (no such page); throws [IOException] for any other failure. */
    private fun getJson(url: String): JSONObject? {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "application/json")
            return when (val code = connection.responseCode) {
                HttpURLConnection.HTTP_OK ->
                    JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                HttpURLConnection.HTTP_NOT_FOUND -> null
                else -> throw IOException("HTTP $code for $url")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(title: String) = URLEncoder.encode(title.replace(' ', '_'), "UTF-8")

    private companion object {
        const val TIMEOUT_MS = 8_000
        const val USER_AGENT = "VisionPlus/1.0 (Android; romajuanmanuel@gmail.com)"
    }
}
