package concerrox.emixx.content.creativemodetab.gui.itemtab

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.content.ScreenManager
import concerrox.emixx.res
import concerrox.emixx.util.GuiGraphicsUtils
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.TabButton

class ItemTabButton(
    private val tabManager: ItemTabManager,
    private val tab: ItemTab,
    width: Int,
    height: Int,
    private val style: ButtonStyle = ButtonStyle.TOP,
    private val isFirst: Boolean = false
) : TabButton(tabManager, tab, width, height) {

    enum class ButtonStyle { TOP, LEFT, RIGHT }

    private val isVisible
        get() = tab.creativeModeTab != null

    private val title = tab.creativeModeTab?.displayName

    companion object {
        private val TEXTURE_DEFAULT = res("textures/gui/buttons.png")
        private val TEXTURE_LEFT = res("textures/gui/tab_button.png")
        private val TEXTURE_RIGHT = res("textures/gui/tab_button_right.png")
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (!isVisible) return
        super.onClick(mouseX, mouseY)
        tabManager.onTabSelected(tab)
    }

    override fun renderWidget(raw: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        val context = EmiDrawContext.wrap(raw)

        if (isVisible) {
            if (style == ButtonStyle.TOP) {
                val yOff: Float
                if (isSelected) {
                    context.drawTexture(TEXTURE_DEFAULT, x, y, 32, if (isHoveredOrFocused) 50 else 32, width, 18)
                    yOff = 4F
                } else {
                    context.drawTexture(TEXTURE_DEFAULT, x, y + 2, 32, if (isHoveredOrFocused) 16 else 0, width, 16)
                    yOff = 5F
                }
                GuiGraphicsUtils.renderItem(raw, tab.creativeModeTab?.iconItem, x + 4F, y + yOff, 10F)
            } else {
                val u = if (isSelected) 188 else 152
                val v = if (isSelected && isFirst) 30 else 2
                val texture = if (style == ButtonStyle.RIGHT) TEXTURE_RIGHT else TEXTURE_LEFT

                raw.pose().pushPose()
                raw.pose().translate(0.0, 0.0, if (isSelected) 100.0 else 0.0)
                context.drawTexture(texture, x, y, u, v, width, height)
                raw.pose().popPose()

                tab.creativeModeTab?.iconItem?.let { stack ->
                    val iconX = if (style == ButtonStyle.RIGHT) x + 6F else x + 8F
                    GuiGraphicsUtils.renderItem(raw, stack, iconX, y + 5F, 16F)
                }
            }

            if (isHovered && title != null) {
                ScreenManager.customIndexTitle = title
            } else {
                ScreenManager.removeCustomIndexTitle(title)
            }
        } else if (style == ButtonStyle.TOP) {
            context.drawTexture(TEXTURE_DEFAULT, x, y + height - 2, 32, 14, width, 2)
            context.fill(x, y + 2, width, height - 4, 0xDB000000.toInt())
        }
        RenderSystem.disableBlend()
    }
}