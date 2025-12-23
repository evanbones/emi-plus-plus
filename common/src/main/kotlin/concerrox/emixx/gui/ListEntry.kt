package concerrox.emixx.gui

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.gui.components.Switch
import concerrox.emixx.res
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

abstract class ListEntry(private val container: ContainerEventHandler) :
    AbstractWidget(0, 0, WIDTH, HEIGHT, Component.empty()) {

    companion object {
        private val BACKGROUND = res("textures/gui/inworld_menu_list_background.png")

        private val INWORLD_HEADER_SEPARATOR = ResourceLocation("textures/gui/header_separator.png")
        private val INWORLD_FOOTER_SEPARATOR = ResourceLocation("textures/gui/footer_separator.png")

        private const val PADDING = 8
        private const val BORDER_WIDTH = 1
        const val WIDTH = ENTRY_SIZE * 8 + PADDING * 2 + BORDER_WIDTH * 2
        const val HEIGHT = ENTRY_SIZE * 2 + PADDING * 2 + BORDER_WIDTH * 2
    }

    protected abstract val switch: Switch
    protected abstract val children: MutableList<AbstractWidget>

    abstract fun shouldRenderSwitch(): Boolean

    abstract fun getEntryTitle(): Component?

    abstract fun renderEntry(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, startX: Int, startY: Int, partialTick: Float)

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val startX = x + BORDER_WIDTH + PADDING
        val startY = y + BORDER_WIDTH + PADDING
        renderBackground(guiGraphics)
        renderBorders(guiGraphics)
        getEntryTitle()?.let {
            val maxWidth = WIDTH - BORDER_WIDTH - PADDING - 46
            val font = Minecraft.getInstance().font
            if (font.width(it) < maxWidth) {
                guiGraphics.drawString(
                    font,
                    it,
                    startX,
                    startY + 2,
                    0xFFFFFF
                )
            } else {
                guiGraphics.drawString(font, it, startX, startY + 2, 0xFFFFFF)
            }
        }
        renderEntry(guiGraphics, mouseX, mouseY, startX, startY, partialTick)
        if (shouldRenderSwitch()) {
            switch.setPosition(x + WIDTH - switch.width - BORDER_WIDTH - PADDING, startY)
            switch.render(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun setFocused(isFocused: Boolean) {
        super.setFocused(isFocused)
        if (!isFocused) children.forEach {
            it.isFocused = false
        }
    }

    private fun renderBorders(guiGraphics: GuiGraphics) {
        RenderSystem.enableBlend()
        val resourceLocation = INWORLD_HEADER_SEPARATOR
        val resourceLocation2 = INWORLD_FOOTER_SEPARATOR
        guiGraphics.blit(resourceLocation, x, y, 0f, 0f, width, 2, 32, 2)
        guiGraphics.blit(resourceLocation2, x, y + height - 2, 0f, 0f, width, 2, 32, 2)
        RenderSystem.disableBlend()
    }

    private fun renderBackground(guiGraphics: GuiGraphics) {
        RenderSystem.enableBlend()
        val right = x + width
        val bottom = y + height
        guiGraphics.blit(BACKGROUND, x, y, right.toFloat(), bottom.toFloat(), width, height, 32, 32)
        RenderSystem.disableBlend()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (child in children) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(true)
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun isFocused() = container.focused === this
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}

}