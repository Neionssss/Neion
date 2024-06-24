/* Originally Made by Aton
* https://github.com/FloppaCoding/FloppaClient
 */

package neion.features.dungeons

import neion.Neion
import neion.events.ClickEvent
import neion.funnymap.Dungeon
import neion.funnymap.MapUpdate
import neion.funnymap.map.Room
import neion.funnymap.map.RoomType
import neion.funnymap.map.ScanUtils
import neion.utils.TextUtils
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EditMode {

    var currentBlockID = 20
    var enabled = false

    fun getOrPutBlocks(room: Room): ScanUtils.ExtrasData {
        return ScanUtils.extraRooms.getOrPut(room.data.name) { ScanUtils.ExtrasData(room.core) }
    }

    fun getRelativePos(blockPos: BlockPos, roomPair: Pair<Room, Int>): BlockPos {
        return ScanUtils.getRotatedPos(blockPos.add(-roomPair.first.x, 0, -roomPair.first.z), -roomPair.second)
    }

    fun getCurrentRoomPair(): Pair<Room, Int>? {
        val room = Dungeon.dungeonTeammates[Neion.mc.thePlayer.name]?.run { getCurrentRoom() } ?: return null
        return if (room.data.type == RoomType.BOSS) Pair(room,0) else MapUpdate.rooms.entries.find { it.key.data.name == room.data.name }?.toPair()
    }

    @SubscribeEvent
    fun onLeftClick(e: ClickEvent.LeftClickEvent) {
        if (!enabled || Neion.mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
        var removedBlock = false
        val roomPair = getCurrentRoomPair() ?: return
        getOrPutBlocks(roomPair.first).run {
            // this for each will remove all entries at those coordinates, there should only be one
            val relativeCoords = getRelativePos(Neion.mc.objectMouseOver.blockPos, roomPair)
            this.preBlocks.forEach { (blockID, _) -> if (this.preBlocks[blockID]?.remove(relativeCoords)!!) removedBlock = true }
            // https://i.imgur.com/KJbIYb1.png
            // https://i.imgur.com/Y2Tpev9.png
            if (!removedBlock) this.preBlocks.getOrPut(0) { mutableSetOf() }.add(relativeCoords)
            e.isCanceled = Neion.mc.theWorld.setBlockToAir(Neion.mc.objectMouseOver.blockPos)
        }
    }

    @SubscribeEvent
    fun onMiddleClick(e: ClickEvent.MiddleClickEvent) {
        if (!enabled || Neion.mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
        val state = Neion.mc.theWorld.getBlockState(Neion.mc.objectMouseOver.blockPos)
        if (state.block == Blocks.air) return
        currentBlockID = Block.getStateId(state)
        TextUtils.info("Set block to: ${state.block.localizedName}")
        e.isCanceled = true
    }

    @SubscribeEvent
    fun onRightClick(e: ClickEvent.RightClickEvent) {
        if (!enabled || Neion.mc.objectMouseOver?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
        val roomPair = getCurrentRoomPair() ?: return
        val relativeCoords = getRelativePos(Neion.mc.objectMouseOver.blockPos.add(Neion.mc.objectMouseOver.sideHit.directionVec), roomPair)
        var blockstate = adjustBlockState(relativeCoords, currentBlockID)
        e.isCanceled = true
        getOrPutBlocks(roomPair.first).run {
            if (this.preBlocks[0]?.remove(relativeCoords) != true) {
                Neion.mc.theWorld.setBlockState(relativeCoords, blockstate)
                blockstate = ScanUtils.getStateFromIDWithRotation(blockstate, -roomPair.second)
                this.preBlocks.getOrPut(Block.getStateId(blockstate)) { mutableSetOf() }.add(relativeCoords)
            }
        }
    }

    private fun adjustBlockState(blockPos: BlockPos, blockID: Int): IBlockState {
        return try {
            val hitVec = Neion.mc.objectMouseOver.hitVec
            val block = Block.getBlockById(blockID)
            block.onBlockPlaced(Neion.mc.theWorld, blockPos, Neion.mc.objectMouseOver.sideHit,
                (hitVec.xCoord - blockPos.x.toDouble()).toFloat(), (hitVec.yCoord - blockPos.y.toDouble()).toFloat(),
                (hitVec.zCoord - blockPos.z.toDouble()).toFloat(), block.getMetaFromState(Block.getStateById(blockID)), Neion.mc.thePlayer)
        } catch (e: IllegalArgumentException) {
            Block.getStateById(blockID)
        }
    }
}