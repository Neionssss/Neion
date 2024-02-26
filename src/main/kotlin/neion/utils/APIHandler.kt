// Credit Danker's Skyblock Mod
// https://github.com/bowser0000/SkyblockMod

package neion.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import neion.FMConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*

object APIHandler {

    var profitData: JsonObject? = null
    var auctionData: JsonObject? = null
    var quizdata: JsonObject? = null

    fun getResponse(url: String): JsonObject {
            val conn = URI(url).toURL().openConnection() as HttpURLConnection
            conn.setRequestMethod("GET")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                val m = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8))
                var input: String?
                val response = StringBuilder()
                while ((m.readLine().also { input = it }) != null) response.append(input)
                m.close()
                return Gson().fromJson(response.toString(), JsonObject::class.java)
            }
        return JsonObject()
    }


    fun refreshData() {
        profitData = getResponse("https://api.hypixel.net/skyblock/bazaar")
        auctionData = getResponse("https://moulberry.codes/lowestbin.json")
        quizdata = getResponse("https://data.skytils.gg/solvers/oruotrivia.json")
        hasBonusPaulScore()
    }

    fun getSecrets(uuid: String): Int {
        val response = getResponse("https://api.hypixel.net/player?key=${FMConfig.apiKey}&uuid=${uuid}")
        if (response["success"]?.asBoolean == true) return response["player"].asJsonObject?.get("achievements")?.asJsonObject?.get("skyblock_treasure_hunter")?.asJsonPrimitive?.asInt ?: return 0
        return 0
    }

    fun hasBonusPaulScore(): Boolean {
        val response = getResponse("https://api.hypixel.net/resources/skyblock/election")
        if (response["success"].asJsonPrimitive?.asBoolean == true) {
            val mayor = response["mayor"].asJsonObject ?: return false
            if (mayor["name"].asJsonPrimitive?.asString == "Paul") return mayor["perks"].asJsonArray?.any { it.asJsonObject?.get("name")?.asJsonPrimitive?.asString == "EZPZ" } ?: false
        }
        return false
    }
}