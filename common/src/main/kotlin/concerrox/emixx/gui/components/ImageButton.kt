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
import net.minecraft.resources.ResourceLocation
import java.util.function.BooleanSupplier

class ImageButton : SizedButtonWidget {
    private val isActiveSupplier: BooleanSupplier
    private val baseU: Int
    private val baseV: Int

    constructor(
        width: Int, height: Int, u: Int, v: Int, isActive: BooleanSupplier, action: OnPress
    ) : super(0, 0, width, height, u, v, isActive, action) {
        this.isActiveSupplier = isActive
        this.baseU = u
        this.baseV = v
    }

    companion object {
        internal val TEXTURE = res("textures/gui/buttons.png")
    }

    init {
        texture = TEXTURE
    }

    private var matchScreenManagerVisibility = false
    private var textureWidth = 256
    private var textureHeight = 256

    fun matchScreenManagerVisibility(): ImageButton {
        matchScreenManagerVisibility = true
        return this
    }

    fun withTexture(customTexture: ResourceLocation, width: Int = 256, height: Int = 256): ImageButton {
        this.texture = customTexture
        this.textureWidth = width
        this.textureHeight = height
        return this
    }

    override fun renderWidget(raw: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        if (matchScreenManagerVisibility && EmiScreenManager.isDisabled()) return

        this.active = isActiveSupplier.asBoolean
        val currentV = if (!this.active) baseV + height else baseV

        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        raw.blit(
            texture, x, y,
            baseU.toFloat(), currentV.toFloat(),
            width, height, textureWidth, textureHeight
        )

        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble()) && text != null && active) {
            val context = EmiDrawContext.wrap(raw)
            context.push()
            raw.pose().translate(0f, 0f, 400f)
            val client = Minecraft.getInstance()
            val texts = text.get().map(EmiPort::ordered).map(ClientTooltipComponent::create)
            EmiRenderHelper.drawTooltip(client.screen, context, texts, mouseX, mouseY)
            context.pop()
        }
        RenderSystem.disableBlend()
    }
}