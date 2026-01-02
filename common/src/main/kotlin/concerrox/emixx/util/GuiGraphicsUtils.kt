package concerrox.emixx.util

import com.mojang.blaze3d.platform.Lighting
import net.minecraft.CrashReport
import net.minecraft.ReportedException
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

object GuiGraphicsUtils {

    fun renderItem(
        guiGraphics: GuiGraphics, stack: ItemStack?, x: Float, y: Float, size: Float = 16F
    ) {
        if (stack == null || stack.isEmpty) return
        val minecraft = Minecraft.getInstance()
        val bakedModel = minecraft.itemRenderer.getModel(stack, minecraft.level, null, 0)
        guiGraphics.pose.pushPose()
        guiGraphics.pose.translate(x + size / 2F, y + size / 2F, 150F)
        try {
            guiGraphics.pose.scale(size, -size, size)
            val bl = !bakedModel.usesBlockLight()
            if (bl) {
                Lighting.setupForFlatItems()
            } else {
                Lighting.setupFor3DItems()
            }
            minecraft.itemRenderer.render(stack, ItemDisplayContext.GUI, false, guiGraphics.pose,
                guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, bakedModel)
            guiGraphics.flush()
            if (bl) {
                Lighting.setupFor3DItems()
            }
        } catch (throwable: Throwable) {
            val crashReport = CrashReport.forThrowable(throwable, "Rendering item")
            crashReport.addCategory("Item being rendered").apply {
                setDetail("Item Type") { stack.item.toString() }
                setDetail("Item Components") { stack.tag.toString() }
                setDetail("Item Foil") { stack.hasFoil().toString() }
            }
            throw ReportedException(crashReport)
        }
        guiGraphics.pose.popPose()

    }

}