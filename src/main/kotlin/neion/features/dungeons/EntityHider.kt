package neion.features.dungeons

import neion.Config
import neion.Neion.Companion.mc
import neion.events.CheckRenderEntityEvent
import neion.utils.Location.inDungeons
import neion.utils.TextUtils.containsAny
import neion.utils.TextUtils.matchesAny
import neion.utils.Utils.cleanName
import neion.utils.Utils.equalsOneOf
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.StringUtils
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object EntityHider {

    fun ItemStack.getSkullTextured(): String? {
        if (item != Items.skull) return null
        if (tagCompound == null) return null
        val nbt = tagCompound
        if (!nbt.hasKey("SkullOwner")) return null
        return nbt.getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0).getString("Value")
    }

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
    private val cheapCoinsTextures = listOf(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGZhMDg3ZWI3NmU3Njg3YTgxZTRlZjgxYTdlNjc3MjY0OTk5MGY2MTY3Y2ViMGY3NTBhNGM1ZGViNmM0ZmJhZCJ9fX0=",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjM2UzMWNmYzY2NzMzMjc1YzQyZmNmYjVkOWE0NDM0MmQ2NDNiNTVjZDE0YzljNzdkMjczYTIzNTIifX19",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RlZTYyMWViODJiMGRhYjQxNjYzMzBkMWRhMDI3YmEyYWMxMzI0NmE0YzFlN2Q1MTc0ZjYwNWZkZGYxMGExMCJ9fX0=",
        "ewogICJ0aW1lc3RhbXAiIDogMTU5ODg0NzA4MjYxMywKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQwZDZlMzYyYmM3ZWVlNGY5MTFkYmQwNDQ2MzA3ZTc0NThkMTA1MGQwOWFlZTUzOGViY2IwMjczY2Y3NTc0MiIKICAgIH0KICB9Cn0="
    )

    @SubscribeEvent
    fun onCheckRender(e: CheckRenderEntityEvent) {
        val entity = e.entity
        if (Config.hidegrayDamage && e.entity.name.matchesAny("^§6[\\d,]+$".toRegex(), "^§9[\\d,]+$".toRegex(), "^§2[\\d,]+$".toRegex(), "^§7[\\d,]+\$".toRegex())) mc.theWorld.removeEntity(e.entity)
        if (!inDungeons) return
        if (Config.randomStuff) {
            for (coinTexture in cheapCoinsTextures) if (entity is EntityItem && (entity.entityItem?.cleanName() == "Revive Stone" || (entity.entityItem?.itemDamage == 15 && entity.entityItem?.item == Items.dye) || entity.entityItem?.getSkullTextured() == coinTexture)) mc.theWorld.removeEntity(entity)
            if (entity is EntityFallingBlock) e.isCanceled = true
            if ((entity as? EntityArmorStand)?.inventory?.get(4)?.getSkullTextured().equalsOneOf(abilityOrbTexture,
                supportOrbTexture,
                damageOrbTexture,
                blessingTexture,
                reviveStoneTexture,
                premiumFleshTexture,
                soulWeaverTexture)) mc.theWorld.removeEntity(entity)
            if ((entity as? EntityArmorStand)?.inventory?.get(0)?.getSkullTextured() == healerFairyTexture) e.isCanceled = true
        }
        if (!Config.hideTags) return
        val name = StringUtils.stripControlCodes(e.entity.customNameTag)
        if (name.containsAny(
                "Wither Miner", "Wither Guard",
                "Lurker", "Dreadlord", "Souleater", "Zombie",
                "Skeleton", "Skeletor", "Sniper", "Super Archer") && !name.startsWith("✯") && name.contains("❤") || name.containsAny(
                "DEFENSE",
                "DAMAGE",
                "Blessing",
                "Revive Stone", "Premium Flesh")) mc.theWorld.removeEntity(e.entity)
    }
}