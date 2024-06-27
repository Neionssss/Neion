package neion.features.dungeons

import neion.events.CheckRenderEntityEvent
import neion.ui.clickgui.Module
import neion.utils.Location.inDungeons
import neion.utils.TextUtils.containsAny
import neion.utils.Utils.cleanName
import neion.utils.Utils.getSkullTexture
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import net.minecraft.util.StringUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RemoveUselessNameTags: Module("Cleaner Screen") {

    private val names = listOf(
        "Wither Miner", "Wither Guard",
        "Lurker", "Dreadlord", "Souleater", "Zombie",
        "Skeleton", "Skeletor", "Sniper", "Super Archer",
        "Spider", "Fels", "Withermancer") // Now it fits with other stuff here. xd

    // https://github.com/hannibal002/SkyHanni/blob/beta/src/main/java/at/hannibal2/skyhanni/features/dungeon/DungeonHideItems.kt
    private val soulWeaverTexture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYyNGVkNjg3NTMwNGZhNGExZjBjNzg1YjJjYjZhNmE3MjU2M2U5ZjNlMjRlYTU1ZTE4MTc4NDUyMTE5YWE2NiJ9fX0="
    private val blessingTexture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTkzZTIwNjg2MTc4NzJjNTQyZWNkYTFkMjdkZjRlY2U5MWM2OTk5MDdiZjMyN2M0ZGRiODUzMDk0MTJkMzkzOSJ9fX0="
    private val reviveStoneTexture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZhNzZjYzIyZTdjMmFiOWM1NDBkMTI0NGVhZGJhNTgxZjVkZDllMThmOWFkYWNmMDUyODBhNWI0OGI4ZjYxOCJ9fX0K"
    private val premiumFleshTexture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE3NWU4YjA0NGM3MjAxYTRiMmU4NTZiZTRmYzMxNmE1YWFlYzY2NTc2MTY5YmFiNTg3MmE4ODUzNGI4MDI1NiJ9fX0K"
    private val abilityOrbTexture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTAxZTA0MGNiMDFjZjJjY2U0NDI4MzU4YWUzMWQyZTI2NjIwN2M0N2NiM2FkMTM5NzA5YzYyMDEzMGRjOGFkNCJ9fX0="
    private val supportOrbTexture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTMxYTRmYWIyZjg3ZGI1NDMzMDEzNjUxN2I0NTNhYWNiOWQ3YzBmZTc4NDMwMDcwOWU5YjEwOWNiYzUxNGYwMCJ9fX0="
    private val damageOrbTexture =
        "eyJ0aW1lc3RhbXAiOjE1NzQ5NTEzMTkwNDQsInByb2ZpbGVJZCI6IjE5MjUyMWI0ZWZkYjQyNWM4OTMxZjAyYTg0OTZlMTFiIiwicHJvZmlsZU5hbWUiOiJTZXJpYWxpemFibGUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FiODZkYTJlMjQzYzA1ZGMwODk4YjBjYzVkM2U2NDg3NzE3MzE3N2UwYTIzOTQ0MjVjZWMxMDAyNTljYjQ1MjYifX19"
    private val healerFairyTexture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjM2UzMWNmYzY2NzMzMjc1YzQyZmNmYjVkOWE0NDM0MmQ2NDNiNTVjZDE0YzljNzdkMjczYTIzNTIifX19"

    @SubscribeEvent
    fun onCheckRender(e: CheckRenderEntityEvent) {
        if (!inDungeons) return
        val entity = e.entity
        if (entity is EntityItem && (entity.entityItem?.cleanName() == "Revive Stone" || (entity.entityItem?.itemDamage == 15 && entity.entityItem?.item == Items.dye))) mc.theWorld?.removeEntity(entity)
        if (entity is EntityFallingBlock) e.isCanceled = true
        if (entity !is EntityArmorStand) return
        when (entity.inventory[4]?.getSkullTexture()) {
            abilityOrbTexture,
            supportOrbTexture,
            damageOrbTexture,
            blessingTexture,
            reviveStoneTexture,
            premiumFleshTexture,
            soulWeaverTexture,
            -> mc.theWorld.removeEntity(entity)
        }
        if (entity.inventory[0]?.getSkullTexture() == healerFairyTexture) e.isCanceled = true
            val name = StringUtils.stripControlCodes((e.entity as? EntityArmorStand ?: return).customNameTag)
            if (name.containsAny(names) && !name.startsWith("✯") && name.contains("❤") || name.containsAny(
                    "DEFENSE",
                    "DAMAGE",
                    "Blessing",
                    "Revive Stone", "Premium Flesh"
                )
            ) e.isCanceled = true
    }
}