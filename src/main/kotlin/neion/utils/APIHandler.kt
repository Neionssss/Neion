// Credit Danker's Skyblock Mod
// https://github.com/bowser0000/SkyblockMod

package neion.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import neion.features.ClickGui.apiKey
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.util.UUID

object APIHandler {

    var profitData: JsonObject? = null
    var auctionData: JsonObject? = null
    var quizdata: JsonObject? = null

    fun getResponse(url: String): JsonObject {
        val conn = URI(url).toURL().openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        if (conn.responseCode != HttpURLConnection.HTTP_OK) return JsonObject()
        val m = BufferedReader(InputStreamReader(conn.inputStream))
        var input: String?
        val response = StringBuilder()
        while ((m.readLine().also { input = it }) != null) response.append(input)
        m.close()
        return Gson().fromJson(response.toString(), JsonObject::class.java)
    }

    fun refreshData() {
        profitData = getResponse("https://api.hypixel.net/skyblock/bazaar")
        auctionData = getResponse("https://moulberry.codes/lowestbin.json")
    }

    fun hasBonusPaulScore(): Boolean {
        val response = getResponse("https://api.hypixel.net/resources/skyblock/election")
        if (response["success"].asJsonPrimitive?.asBoolean == true) {
            val mayor = response["mayor"].asJsonObject ?: return false
            if (mayor["name"].asJsonPrimitive?.asString == "Paul") return mayor["perks"].asJsonArray?.any { it.asJsonObject?.get("name")?.asJsonPrimitive?.asString == "EZPZ" } ?: false
        }
        return false
    }

    fun getSpirit(id: UUID): Boolean {
        val uuid = id.toString().replace("-", "")
        val response = getResponse("https://api.hypixel.net/skyblock/profiles?key=${apiKey.text}&uuid=$uuid")
        return if (response["success"]?.asBoolean ?: return false) response["profiles"].asJsonArray?.find { it.asJsonObject["selected"].asBoolean }?.asJsonObject?.get("members")?.asJsonObject?.get(uuid)?.asJsonObject?.get("pets")?.asJsonArray?.any { pet -> pet.asJsonObject["type"]?.asString == "SPIRIT" && pet.asJsonObject["tier"]?.asString == "LEGENDARY" } ?: false else false
    }
}