package neion.events

import net.minecraft.client.model.ModelBase
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
class CheckRenderEntityEvent<T : Entity>(
    val entity: T,
    val camera: ICamera,
    val camX: Double,
    val camY: Double,
    val camZ: Double
) : DebugEvent()

@Cancelable
class RenderLivingEntityEvent(
    var entity: EntityLivingBase,
    var p_77036_2_: Float,
    var p_77036_3_: Float,
    var p_77036_4_: Float,
    var p_77036_5_: Float,
    var p_77036_6_: Float,
    var scaleFactor: Float,
    var modelBase: ModelBase
) : DebugEvent()