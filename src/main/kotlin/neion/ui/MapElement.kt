package neion.ui

import neion.FMConfig
import neion.funnymap.MapRender
import neion.utils.Location
import neion.utils.Location.inDungeons

object MapElement : MovableGuiElement() {
    override var x: Int by FMConfig::mapX
    override var y: Int by FMConfig::mapY
    override val h: Int
        get() = if (FMConfig.mapShowRunInformation == 1) 142 else 128
    override val w: Int
        get() = 135
    override var scale: Float by FMConfig::mapScale
    override fun render() {
        MapRender.renderMap()
    }

    override fun shouldRender(): Boolean {
        if (!FMConfig.mapEnabled || !inDungeons) return false
        if (FMConfig.mapHideInBoss && Location.inBoss) return false
        return super.shouldRender()
    }
}
