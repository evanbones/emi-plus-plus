package concerrox.emixx.util

import net.minecraft.CrashReport
import net.minecraft.ReportedException
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.item.ItemStack

object GuiGraphicsUtils {

    fun renderItem(
        guiGraphics: GuiGraphics,
        stack: ItemStack?,
        x: Float,
        y: Float,
        size: Float = 16F
    ) {
        if (stack == null || stack.isEmpty) return

        val pose = guiGraphics.pose
        pose.pushPose()

        pose.translate(x, y, 150f)

        val scale = size / 16f
        pose.scale(scale, scale, 1.0f)

        try {
            guiGraphics.renderItem(stack, 0, 0)
        } catch (throwable: Throwable) {
            val report = CrashReport.forThrowable(throwable, "Rendering item")
            report.addCategory("Item being rendered").apply {
                setDetail("Item Type") { stack.item.toString() }
                setDetail("Item Components") { stack.tag.toString() }
            }
            throw ReportedException(report)
        }

        pose.popPose()
    }
}