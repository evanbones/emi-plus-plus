package concerrox.emixx.gui

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.gui.components.Switch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractContainerWidget
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

abstract class ListEntry(private val container: ContainerEventHandler) :
    AbstractContainerWidget(
        0, 0, WIDTH, HEIGHT, Component.empty()
    ) {

    companion object {
        private val BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png")
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
                renderScrollingString(
                    guiGraphics,
                    font,
                    it,
                    startX,
                    startY,
                    startX + maxWidth,
                    startY + 2 + font.lineHeight,
                    0xFFFFFF
                )
            }
        }
        renderEntry(guiGraphics, mouseX, mouseY, startX, startY, partialTick)
        if (shouldRenderSwitch()) {
            switch.setPosition(x + WIDTH - switch.width - BORDER_WIDTH - PADDING, startY)
            switch.render(guiGraphics, mouseX, mouseY, partialTick)
        }
    }

    override fun setFocused(isFocused: Boolean) {
        if (!isFocused) children.forEach {
            it.isFocused = false
        }
    }

    private fun renderBorders(guiGraphics: GuiGraphics) {
        RenderSystem.enableBlend()
        val resourceLocation = Screen.INWORLD_HEADER_SEPARATOR
        val resourceLocation2 = Screen.INWORLD_FOOTER_SEPARATOR
        guiGraphics.blit(resourceLocation, x, y, 0f, 0f, getWidth(), 2, 32, 2)
        guiGraphics.blit(resourceLocation2, x, bottom, 0f, 0f, getWidth(), 2, 32, 2)
        RenderSystem.disableBlend()
    }

    private fun renderBackground(guiGraphics: GuiGraphics) {
        RenderSystem.enableBlend()
        guiGraphics.blit(BACKGROUND, x, y, right.toFloat(), bottom.toFloat(), getWidth(), getHeight(), 32, 32)
        RenderSystem.disableBlend()
    }

    override fun isFocused() = container.focused === this
    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
    override fun children() = children

}