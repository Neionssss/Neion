package neion.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import neion.MapConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.text.Charsets.UTF_8

object APIHandler {

    var profitData: JsonObject? = null
    var auctionData: JsonObject? = null


    // https://github.com/bowser0000/SkyblockMod
    fun getResponse(url: String): JsonObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")

        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            val m = BufferedReader(InputStreamReader(conn.inputStream, UTF_8))
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
    }

    fun getSecrets(uuid: String): Int {
        val response = getResponse("https://api.hypixel.net/player?key=${MapConfig.apiKey}&uuid=${uuid}")
        if (response["success"]?.asBoolean == true) return response["player"].asJsonObject?.get("achievements")?.asJsonObject?.get("skyblock_treasure_hunter")?.asInt ?: return 0
        return 0
    }

    fun hasBonusPaulScore(): Boolean {
        val response = getResponse("https://api.hypixel.net/resources/skyblock/election")
        if (response["success"]?.asBoolean == true) {
            val mayor = response["mayor"].asJsonObject ?: return false
            if (mayor["name"]?.asString == "Paul") return mayor["perks"].asJsonArray?.any { it.asJsonObject?.get("name")?.asString == "EZPZ" } ?: false
        }
        return false
    }
}