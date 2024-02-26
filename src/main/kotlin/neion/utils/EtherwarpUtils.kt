package neion.utils

import neion.Neion.Companion.mc
import neion.utils.Utils.extraAttributes
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos
import kotlin.math.*

object EtherwarpUtils {

    class Vector3(var x: Double = 0.0, var y: Double = 0.0, var z : Double = 0.0) {

        fun fromPitchYaw(pitch: Float, yaw: Float): Vector3 {
            val f = cos(-yaw * 0.017453292 - Math.PI)
            val f1 = sin(-yaw * 0.017453292 - Math.PI)
            val f2 = -cos(-pitch * 0.017453292)
            val f3 = sin(-pitch * 0.017453292)
            return Vector3(f1 * f2, f3, f * f2).normalize()
        }

        fun getComponents() = arrayOf(this.x, this.y, this.z)

        fun normalize(): Vector3 {
            val len = sqrt(this.x.pow(2) + this.y.pow(2) + this.z.pow(2))
            this.x /= len
            this.y /= len
            this.z /= len
            return this
        }

        fun multiply(): Vector3 {
            val factor = 57 + (mc.thePlayer.heldItem?.extraAttributes?.getInteger("tuned_transmission") ?: 0) - 1
            this.x *= factor
            this.y *= factor
            this.z *= factor
            return this
        }
    }


    // For etherwarp shit to be perfectly accurate
    var lastSentCoords: Array<Double>? = null
    var lastSentLook: Array<Float>? = null

    fun getPacketCoord(packet: C03PacketPlayer) = arrayOf(packet.positionX, packet.positionY, packet.positionZ)
    fun getPacketLook(packet: C03PacketPlayer) = arrayOf(packet.pitch, packet.yaw)


    fun sss(packet: C03PacketPlayer) {
        when (packet) {
            is C03PacketPlayer.C04PacketPlayerPosition -> lastSentCoords = getPacketCoord(packet)
            is C03PacketPlayer.C05PacketPlayerLook -> lastSentLook = getPacketLook(packet)
            is C03PacketPlayer.C06PacketPlayerPosLook -> {
                lastSentCoords = getPacketCoord(packet)
                lastSentLook = getPacketLook(packet)
            }
        }
    }

    fun traverseVoxels(): List<Int>? {
        if (lastSentCoords == null || lastSentLook == null) return null
        val (pitch, yaw) = lastSentLook!!
        // Initialize Shit
        val start = lastSentCoords!!.map { it.toFloat() }.toMutableList()
        start[1] += mc.thePlayer.getEyeHeight()
        val end = Vector3().fromPitchYaw(pitch, yaw).multiply().getComponents().mapIndexed { i,v -> v + start[i] }
        val direction = end.mapIndexed { i, v -> v - start[i] }
        val step = direction.map { a -> sign(a).toInt() }
        val thing = direction.map { a -> 1/a }
        val tMax = thing.mapIndexed { i, v -> abs((floor(start[i]) + max(step[i], 0) - start[i]) * v) }.toMutableList()

        // Ints

        val currentPos = start.map { a -> floor(a).toInt() }.toMutableList()
        var iters = 0
        while (iters < 1000) {
            iters++

            // Do block check function stuff
            val currentBlock = BlockPos(currentPos[0],currentPos[1],currentPos[2])
            if (isValidEtherwarpBlock(currentBlock)) return currentPos
            if (mc.theWorld.getBlockState(currentBlock).block != Blocks.air) break
            if (currentPos == end.map { a -> floor(a) }) break

            // Find the next direction to step in
            val minIndex = tMax.indexOf(tMax.minOrNull())
            tMax[minIndex] += thing.mapIndexed { i, v -> min(v * step[i], 1.0) }[minIndex]
            currentPos[minIndex] += step[minIndex]
        }
        return null
    }

    fun isValidEtherwarpBlock(block: BlockPos): Boolean {
        if (block == Blocks.air) return false

        // Checking the actual block to etherwarp ontop of
        // Can be at foot level, but not etherwarped onto directly.
        if (validEtherwarpFeetBlocks.contains(mc.theWorld.getBlockState(block).block.registryName)) return false
        if (!validEtherwarpFeetBlocks.contains(mc.theWorld.getBlockState(block.up()).block.registryName)) return false
        return validEtherwarpFeetBlocks.contains(mc.theWorld.getBlockState(block.up(2)).block.registryName)
    }

// If one of these blocks is above the targeted etherwarp block, it is a valid teleport.
// However if the block itself is being targetted, then it is not a valid block to etherwarp to.
    val validEtherwarpFeetBlocks = setOf(
    "minecraft:air",
    "minecraft:fire",
    "minecraft:carpet",
    "minecraft:skull",
    "minecraft:lever",
    "minecraft:stone_button",
    "minecraft:wooden_button",
    "minecraft:torch",
    "minecraft:string",
    "minecraft:tripwire_hook",
    "minecraft:tripwire",
    "minecraft:rail",
    "minecraft:activator_rail",
    "minecraft:snow_layer",
    "minecraft:carrots",
    "minecraft:wheat",
    "minecraft:potatoes",
    "minecraft:nether_wart",
    "minecraft:pumpkin_stem",
    "minecraft:melon_stem",
    "minecraft:redstone_torch",
    "minecraft:redstone_wire",
    "minecraft:red_flower",
    "minecraft:yellow_flower",
    "minecraft:sapling",
    "minecraft:flower_pot",
    "minecraft:deadbush",
    "minecraft:tallgrass",
    "minecraft:ladder",
    "minecraft:double_plant",
    "minecraft:unpowered_repeater",
    "minecraft:powered_repeater",
    "minecraft:unpowered_comparator",
    "minecraft:powered_comparator",
    "minecraft:web",
    "minecraft:waterlily",
    "minecraft:water",
    "minecraft:lava",
    "minecraft:torch",
    "minecraft:vine",
    "minecraft:brown_mushroom",
    "minecraft:red_mushroom",
    )
}