/* Originally Made by Aton
* https://github.com/FloppaCoding/FloppaClient
 */

package neion.features.dungeons

import neion.Neion.Companion.mc
import neion.events.ClickEvent
import neion.funnymap.Dungeon
import neion.funnymap.MapUpdate
import neion.funnymap.map.Room
import neion.funnymap.map.RoomType
import neion.ui.clickgui.Category
import neion.ui.clickgui.Module
import neion.utils.ExtrasConfig
import neion.utils.MapUtils.getRelativePos
import neion.utils.MapUtils.getStateFromIDWithRotation
import neion.utils.TextUtils
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PreBlocks: Module("Extras", category = Category.DUNGEON)

object EditMode {

    var currentBlockID = 20
    var enabled = false

    private fun getOrPutBlocks(room: Room) = ExtrasConfig.extraRooms.getOrPut(room.data.name) { ExtrasConfig.ExtrasData(room.core) }

    fun getCurrentRoomPair(): Pair<Room, Int>? {
        val room = Dungeon.dungeonTeammates[mc.thePlayer.name]?.run { getCurrentRoom() } ?: return null
        return if (room.data.type == RoomType.BOSS) Pair(room,0) else MapUpdate.rooms.entries.find { it.key.data.name == room.data.name }?.toPair()
    }

    @SubscribeEvent
    fun onLeftClick(e: ClickEvent.LeftClickEvent) {
        if (!enabled || mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
        var removedBlock = false
        val roomPair = getCurrentRoomPair() ?: return
        getOrPutBlocks(roomPair.first).preBlocks.run {
            val relativeCoords = getRelativePos(mc.objectMouseOver.blockPos, roomPair)
            forEach { (blockID, _) -> if (get(blockID)?.remove(relativeCoords)!!) removedBlock = true }
            // https://i.imgur.com/KJbIYb1.png
            // https://i.imgur.com/Y2Tpev9.png
            if (!removedBlock) getOrPut(0) { mutableSetOf() }.add(relativeCoords)
            e.isCanceled = mc.theWorld.setBlockToAir(mc.objectMouseOver.blockPos)
        }
    }

    @SubscribeEvent
    fun onMiddleClick(e: ClickEvent.MiddleClickEvent) {
        if (!enabled || mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
        val state = mc.theWorld.getBlockState(mc.objectMouseOver.blockPos)
        if (state.block == Blocks.air) return
        currentBlockID = Block.getStateId(state)
        TextUtils.info("Set block to: ${state.block.localizedName}")
        e.isCanceled = true
    }

    @SubscribeEvent
    fun onRightClick(e: ClickEvent.RightClickEvent) {
        if (!enabled || mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
        val roomPair = getCurrentRoomPair() ?: return
        val relativeCoords = getRelativePos(mc.objectMouseOver.blockPos.add(mc.objectMouseOver.sideHit.directionVec), roomPair)
        var blockstate = adjustBlockState(relativeCoords, currentBlockID)
        e.isCanceled = true
        getOrPutBlocks(roomPair.first).run {
            if (preBlocks[0]?.remove(relativeCoords) != true) {
                mc.theWorld.setBlockState(relativeCoords, blockstate)
                blockstate = getStateFromIDWithRotation(blockstate, -roomPair.second)
                preBlocks.getOrPut(Block.getStateId(blockstate)) { mutableSetOf() }.add(relativeCoords)
            }
        }
    }

    private fun adjustBlockState(blockPos: BlockPos, blockID: Int) = try {
        val hitVec = mc.objectMouseOver.hitVec
        val block = Block.getBlockById(blockID)
        block.onBlockPlaced(
            mc.theWorld,
            blockPos,
            mc.objectMouseOver.sideHit,
            (hitVec.xCoord - blockPos.x.toDouble()).toFloat(),
            (hitVec.yCoord - blockPos.y.toDouble()).toFloat(),
            (hitVec.zCoord - blockPos.z.toDouble()).toFloat(),
            block.getMetaFromState(Block.getStateById(blockID)),
            mc.thePlayer
        )
    } catch (e: IllegalArgumentException) {
        Block.getStateById(blockID)
    }
}