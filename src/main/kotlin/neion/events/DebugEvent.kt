package neion.events

import neion.utils.TextUtils
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event

abstract class DebugEvent : Event() {

    fun postAndCatch(): Boolean {
        return runCatching {
            MinecraftForge.EVENT_BUS.post(this)
        }.onFailure {
            it.printStackTrace()
            TextUtils.info("${it::class.simpleName ?: "error"} at ${this::class.qualifiedName}. Please report this.")
        }.getOrDefault(isCanceled)
    }
}