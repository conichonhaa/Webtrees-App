package com.genealogie.webtrees

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class WebtreesApi(
    private val siteUrl: String,
    private val clientId: String,
    private val clientSecret: String
) {
    private var cachedToken: String? = null
    private var tokenExpiry: Long = 0

    private fun apiUrl(path: String) = "$siteUrl/index.php?route=$path"

    private suspend fun getToken(): String = withContext(Dispatchers.IO) {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiry) {
            return@withContext cachedToken!!
        }
        val conn = (URL(apiUrl("/oauth/token")).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("User-Agent", "WebtreesApp/1.0")
            val creds = Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)
            setRequestProperty("Authorization", "Basic $creds")
            outputStream.write("grant_type=client_credentials&scope=api_read_member".toByteArray())
        }
        val body = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(body)
        val expiresIn = json.optLong("expires_in", 3600)
        cachedToken = json.getString("access_token")
        tokenExpiry = System.currentTimeMillis() + (expiresIn - 60) * 1000
        cachedToken!!
    }

    private suspend fun get(path: String, params: Map<String, String> = emptyMap()): JSONObject {
        val token = getToken()
        val query = if (params.isEmpty()) "" else "&" + params.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        val conn = (URL(apiUrl(path) + query).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 15000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "WebtreesApp/1.0")
        }
        return JSONObject(conn.inputStream.bufferedReader().readText())
    }

    suspend fun getTrees(): List<Tree> = withContext(Dispatchers.IO) {
        val json = get("/api/get-trees")
        val arr = json.getJSONArray("trees")
        (0 until arr.length()).map { i ->
            val t = arr.getJSONObject(i)
            Tree(t.getInt("id"), t.getString("name"), t.getString("title"))
        }
    }

    suspend fun searchPersons(tree: String, query: String): List<Person> = withContext(Dispatchers.IO) {
        val json = get("/api/search-general", mapOf("tree" to tree, "query" to query))
        val arr = json.optJSONArray("records") ?: return@withContext emptyList()
        val xrefs = (0 until arr.length()).map { arr.getJSONObject(it).getString("xref") }
        xrefs.map { getPersonDetail(tree, it) }
    }

    suspend fun getPersonDetail(tree: String, xref: String): Person = withContext(Dispatchers.IO) {
        val json = get("/api/get-record", mapOf("tree" to tree, "xref" to xref, "format" to "json"))
        parsePerson(tree, xref, json)
    }

    private fun parsePerson(tree: String, xref: String, json: JSONObject): Person {
        val persons = json.optJSONArray("persons") ?: return Person(xref, tree)
        if (persons.length() == 0) return Person(xref, tree)
        val p = persons.getJSONObject(0)

        var givenName = ""
        var surname = ""
        val parts = p.optJSONArray("names")
            ?.optJSONObject(0)
            ?.optJSONArray("nameForms")
            ?.optJSONObject(0)
            ?.optJSONArray("parts")
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val type = part.optString("type")
                val value = part.optString("value")
                when {
                    type.contains("Given")   -> givenName = value
                    type.contains("Surname") -> surname = value
                }
            }
        }

        val gender = when (p.optJSONObject("gender")?.optString("type")) {
            "http://gedcomx.org/Male"   -> "Homme"
            "http://gedcomx.org/Female" -> "Femme"
            else -> ""
        }

        var birthDate = ""; var birthPlace = ""
        var deathDate = ""; var deathPlace = ""
        val facts = p.optJSONArray("facts")
        if (facts != null) {
            for (i in 0 until facts.length()) {
                val fact = facts.getJSONObject(i)
                val type  = fact.optString("type")
                val date  = fact.optJSONObject("date")?.optString("original") ?: ""
                val place = fact.optJSONObject("place")?.optString("original") ?: ""
                when {
                    type.contains("Birth") -> { birthDate = date; birthPlace = place }
                    type.contains("Death") -> { deathDate = date; deathPlace = place }
                }
            }
        }

        return Person(xref, tree, givenName, surname, birthDate, birthPlace, deathDate, deathPlace, gender)
    }
}
