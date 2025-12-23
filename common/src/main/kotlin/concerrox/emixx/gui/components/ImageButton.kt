package concerrox.emixx.gui.components

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.res
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.runtime.EmiDrawContext
import dev.emi.emi.screen.EmiScreenManager
import dev.emi.emi.screen.widget.SizedButtonWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import java.util.function.BooleanSupplier


class ImageButton : SizedButtonWidget {

    // Main constructor
    constructor(
        width: Int, height: Int, u: Int, v: Int, isActive: BooleanSupplier, action: OnPress
    ) : super(0, 0, width, height, u, v, isActive, action)

    companion object {
        internal val TEXTURE = res("textures/gui/buttons.png")
    }

    init {
        texture = TEXTURE
    }

    private var matchScreenManagerVisibility = false

    fun matchScreenManagerVisibility(): ImageButton {
        matchScreenManagerVisibility = true
        return this
    }

    override fun renderWidget(raw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (matchScreenManagerVisibility && EmiScreenManager.isDisabled()) return

        val context = EmiDrawContext.wrap(raw)
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        context.drawTexture(texture, x, y, getU(mouseX, mouseY), getV(mouseX, mouseY), width, height)
        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && text != null && active) {
            context.push()
            RenderSystem.disableDepthTest()
            val client = Minecraft.getInstance()
            val texts = text.get().map(EmiPort::ordered).map(ClientTooltipComponent::create)
            EmiRenderHelper.drawTooltip(client.screen, context, texts, mouseX, mouseY)
            context.pop()
        }
        RenderSystem.disableBlend()
    }

}