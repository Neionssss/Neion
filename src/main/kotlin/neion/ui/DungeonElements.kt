package neion.ui

import neion.Config
import neion.Neion.Companion.mc
import neion.features.CustomGUI
import neion.features.dungeons.DungeonChestProfit
import neion.funnymap.Dungeon
import neion.utils.Location
import neion.utils.MathUtil
import neion.utils.RenderUtil
import java.awt.Color

object ChestProfitElement : MovableGuiElement() {
    override var x: Int by Config::x
    override var y: Int by Config::y
    override val w: Int
        get() = 87
    override val h: Int
        get() = 70
    override var scale: Float by Config::scale
    override fun render() {
        DungeonChestProfit.DungeonChest.entries.forEachIndexed { i, chest -> RenderUtil.renderText(chest.displayText + ": " + "§f§"  + (if (chest.profit > 0) "a" else "c") + MathUtil.fn(chest.profit), 0, 0 + i * 11) }
    }

    override fun shouldRender(): Boolean {
        if (!DungeonChestProfit.canOpen) return false
        return super.shouldRender()
    }
}

object DungeonSecretDisplay : MovableGuiElement() {
    override var x: Int by Config::xY
    override var y: Int by Config::yY
    override val w: Int = mc.fontRendererObj.getStringWidth("${CustomGUI.secretss}/${CustomGUI.maxSecretss} Secrets") + 1
    override val h: Int = mc.fontRendererObj.FONT_HEIGHT
    override var scale: Float by Config::secretsScale
    override fun render() {
        val color = when (CustomGUI.secretss / CustomGUI.maxSecretss.toDouble()) {
            in 0.0..0.5 -> Color.red
            in 0.5..0.75 -> Color.yellow
            else -> Color.green
        }

        RenderUtil.renderText(
            "Secrets: ${CustomGUI.secretss}/${CustomGUI.maxSecretss}",
            0,
            0,
            color = color.rgb,
        )
    }

    override fun shouldRender(): Boolean {
        if (!Location.inDungeons || !Dungeon.Info.started || !Config.showSecretsFocus) return false
        return super.shouldRender()
    }
}

object ClearedDisplay : MovableGuiElement() {
    override var x: Int by Config::clearedX
    override var y: Int by Config::clearedY
    override val w: Int = mc.fontRendererObj.getStringWidth("Cleared 100% (300)")
    override val h: Int = mc.fontRendererObj.FONT_HEIGHT
    override var scale: Float by Config::clearedScale

    override fun render() {
        RenderUtil.renderText(cleared(), 0, 0, color = Config.percentColor.toJavaColor().rgb)
    }

        override fun shouldRender(): Boolean {
            if (!Location.inDungeons || !Dungeon.Info.started || !Config.showClearedFocus) return false
            return super.shouldRender()
        }

        fun cleared(): String {
            Location.getLines().find {
                Location.cleanLine(it).run { contains("Cleared: ") }
            }?.let {
                return it.substringBefore("(")
            }
            return "Cleared 100% (300)"
        }

    }
object TimeDisplay : MovableGuiElement() {
    var timeString = "Time Elapsed: 2m 40s"
    override var x: Int by Config::timeX
    override var y: Int by Config::timeY
    override val w: Int = mc.fontRendererObj.getStringWidth(timeString) + 1
    override val h: Int = mc.fontRendererObj.FONT_HEIGHT
    override var scale: Float by Config::timeScale

    override fun render() {
        RenderUtil.renderText(time(), 0, 0, color = Config.timeColor.toJavaColor().rgb)
    }

    override fun shouldRender(): Boolean {
        if (!Location.inDungeons || !Dungeon.Info.started || !Config.showTimeFocus) return false
        return super.shouldRender()
    }
    fun time(): String {
        Location.getLines().find {
            Location.cleanLine(it).run {
                contains("Time Elapsed:")
            }
        }?.let {
            return it.substringBefore("(")
        }
        return "Time Elapsed: 2m 40s"
    }
}

object ManaDisplay : MovableGuiElement() {
    override var x: Int by Config::manaX
    override var y: Int by Config::manaY
    override val w: Int = mc.fontRendererObj.getStringWidth("10000/10000 Mana")
    override val h: Int = mc.fontRendererObj.FONT_HEIGHT
    override var scale: Float by Config::manaScale
    override fun render() {
        RenderUtil.renderText(
            "Mana: ${CustomGUI.leastMana}/${CustomGUI.maxedMana}",
            0,
            0,
            color = Config.manaColor.rgb,
        )
    }

    override fun shouldRender(): Boolean {
        if (!Config.showManaFocus || !Location.inSkyblock) return false
        return super.shouldRender()
    }
}