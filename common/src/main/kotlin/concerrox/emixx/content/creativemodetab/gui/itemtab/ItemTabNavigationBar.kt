package concerrox.emixx.content.creativemodetab.gui.itemtab

import com.google.common.collect.ImmutableList
import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.res
import dev.emi.emi.runtime.EmiDrawContext
import dev.emi.emi.screen.EmiScreenManager
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.TabButton
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.layouts.GridLayout
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

class ItemTabNavigationBar(
    private val tabManager: ItemTabManager,
    private val isVertical: Boolean = false,
    private val isRightSide: Boolean = false
) : AbstractWidget(0, 0, 0, 0, Component.empty()) {

    companion object {
        private val TEXTURE = res("textures/gui/buttons.png")
    }

    private var layout = GridLayout()

    internal var tabButtons: List<TabButton> = emptyList()

    private var focusedChild: GuiEventListener? = null

    var visibleTabs: List<ItemTab> = emptyList()
        private set

    fun pos(x: Int, y: Int): ItemTabNavigationBar {
        this.x = x
        this.y = y
        this.arrangeElements()
        return this
    }

    fun setTabs(tabs: MutableList<ItemTab>) {
        this.visibleTabs = tabs

        val newLayout = GridLayout()
        newLayout.defaultCellSetting().padding(0)

        val buttonBuilder = ImmutableList.builder<TabButton>()

        tabs.forEachIndexed { index, tab ->
            val buttonStyle = if (!isVertical) ItemTabButton.ButtonStyle.TOP
            else if (isRightSide) ItemTabButton.ButtonStyle.RIGHT
            else ItemTabButton.ButtonStyle.LEFT

            val width = if(isVertical) 35 else ENTRY_SIZE
            val height = if(isVertical) 27 else ENTRY_SIZE

            val button = ItemTabButton(tabManager, tab, width, height, buttonStyle, index == 0)
            buttonBuilder.add(button)

            if (isVertical) {
                newLayout.addChild(button, index, 0)
            } else {
                newLayout.addChild(button, 0, index)
            }
        }

        this.tabButtons = buttonBuilder.build()
        this.layout = newLayout

        arrangeElements()
    }

    fun arrangeElements() {
        if (!isVertical) {
            tabButtons.forEach { it.width = ENTRY_SIZE }
        }

        layout.x = if (!isVertical) this.x + 2 else this.x
        layout.y = this.y
        layout.arrangeElements()

        if (isVertical) {
            this.width = 35
            setWidgetHeight(layout.height)
        } else {
            this.width = layout.width + 4
            this.height = layout.height
        }
    }

    private fun setWidgetHeight(newHeight: Int) {
        try {
            val field = AbstractWidget::class.java.getDeclaredField("height")
            field.isAccessible = true
            field.setInt(this, newHeight)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun renderWidget(raw: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (EmiScreenManager.isDisabled()) return

        if (!isVertical) {
            RenderSystem.enableBlend()
            EmiDrawContext.wrap(raw).apply {
                drawTexture(TEXTURE, x, y + 2, 32, 0, 1, 16)
                drawTexture(TEXTURE, x + 1, y + 2, 32, 0, 1, 16)
                drawTexture(TEXTURE, x + width - 1, y + 2, 32, 0, 1, 16)
                drawTexture(TEXTURE, x + width - 2, y + 2, 32, 0, 1, 16)
            }
            RenderSystem.disableBlend()
        }

        tabButtons.forEach { it.render(raw, mouseX, mouseY, partialTick) }
    }

    fun setFocusedChild(child: GuiEventListener?) {
        focusedChild?.isFocused = false

        if (child != null) {
            child.isFocused = true
            focusedChild = child
            if (child is TabButton) {
                tabManager.setCurrentTab(child.tab(), false)
            }
        } else {
            focusedChild = null
        }
    }

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        if (!focused) {
            setFocusedChild(null)
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        for (child in tabButtons) {
            if (child.mouseClicked(mouseX, mouseY, button)) {
                setFocusedChild(child)
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {}
}