package neion.funnymap.map

data class RoomData(val name: String, val type: RoomType, val cores: List<Int> = listOf(), val shape: String = "", val crypts: Int = 0, val secrets: Int = 0, val trappedChests: Int = 0)