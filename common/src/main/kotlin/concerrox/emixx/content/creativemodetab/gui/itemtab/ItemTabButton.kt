package concerrox.emixx.content.creativemodetab.gui.itemtab

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.content.ScreenManager
import concerrox.emixx.res
import concerrox.emixx.util.GuiGraphicsUtils
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.TabButton

class ItemTabButton(private val tabManager: ItemTabManager, private val tab: ItemTab, width: Int, height: Int) :
    TabButton(tabManager, tab, width, height) {

    private val isVisible
        get() = tab.creativeModeTab != null

    private val title = tab.creativeModeTab?.displayName

    companion object {
        private val TEXTURE = res("textures/gui/buttons.png")
    }

    override fun onClick(mouseX: Double, mouseY: Double) {
        if (!isVisible) return
        super.onClick(mouseX, mouseY)
        tabManager.onTabSelected(tab)
    }

    override fun renderWidget(raw: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        RenderSystem.enableBlend()
        val context = EmiDrawContext.wrap(raw)

        val right = x + width
        val bottom = y + height

        if (isVisible) {
            if (isSelected) {
                context.drawTexture(TEXTURE, x, y, 32, if (isHoveredOrFocused) 50 else 32, width, 18)
            } else {
                context.drawTexture(TEXTURE, x, y + 2, 32, if (isHoveredOrFocused) 16 else 0, width, 16)
            }
            GuiGraphicsUtils.renderItem(raw, tab.creativeModeTab?.iconItem, x + 4F, y + 5F, 10F)

            if (isHovered && title != null) {
                ScreenManager.customIndexTitle = title
            } else {
                ScreenManager.removeCustomIndexTitle(title)
            }
        } else {
            context.drawTexture(TEXTURE, x, bottom - 2, 32, 14, width, 2)
            context.fill(x, y + 2, width, height - 4, 0xDB000000.toInt())
        }
        RenderSystem.disableBlend()
    }

}