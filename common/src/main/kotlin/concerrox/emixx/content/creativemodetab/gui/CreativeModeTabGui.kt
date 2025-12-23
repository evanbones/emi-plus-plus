package concerrox.emixx.content.creativemodetab.gui

import concerrox.emixx.content.ScreenManager
import concerrox.emixx.content.creativemodetab.CreativeModeTabManager
import concerrox.emixx.content.creativemodetab.gui.itemtab.ItemTabManager
import concerrox.emixx.content.creativemodetab.gui.itemtab.ItemTabNavigationBar
import concerrox.emixx.gui.components.ImageButton
import concerrox.emixx.util.pos
import dev.emi.emi.config.EmiConfig
import dev.emi.emi.config.HeaderType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object CreativeModeTabGui {

    const val CREATIVE_MODE_TAB_HEIGHT = 18
    private const val EMI_HEADER_HEIGHT = 18

    private lateinit var screen: Screen

    private val isHeaderVisible
        get() = EmiConfig.rightSidebarHeader == HeaderType.VISIBLE

    private val tabManager = ItemTabManager({ screen.addRenderableWidget(it) }, { screen.removeWidget(it) }).apply {
        onTabSelectedListener = CreativeModeTabManager::onTabSelected
    }
    internal val tabNavigationBar = ItemTabNavigationBar(tabManager).pos(0, 0)
    internal var tabCount = 0u

    private val buttonPrevious = ImageButton(16, 16, u = 0, v = 0, { true }, CreativeModeTabManager::previousPage).matchScreenManagerVisibility().pos(0, 0)
    private val buttonNext = ImageButton(16, 16, u = 16, v = 0, { true }, CreativeModeTabManager::nextPage).matchScreenManagerVisibility().pos(0, 0)

    private var scrollAccumulator = 0.0

    private fun onLayout() {
        val indexScreenSpace = ScreenManager.indexScreenSpace
        val startX = indexScreenSpace?.tx ?: 0
        val startY = (indexScreenSpace?.ty ?: 0) - (if (isHeaderVisible) EMI_HEADER_HEIGHT else 0) - CREATIVE_MODE_TAB_HEIGHT
        val tileW = indexScreenSpace?.tw ?: 0
        tabCount = (tileW.toUInt() - 2u).coerceIn(1u, UByte.MAX_VALUE.toUInt())

        buttonPrevious.pos(startX, startY + 2)
        tabNavigationBar.pos(startX + buttonPrevious.width, startY)
        buttonNext.pos(tabNavigationBar.x + tabNavigationBar.width, startY + 2)
    }

    internal fun initialize(screen: Screen) {
        this.screen = screen
        screen.addRenderableWidget(buttonPrevious)
        screen.addRenderableWidget(buttonNext)
        screen.addRenderableWidget(tabNavigationBar)
        onLayout()
    }

    internal fun contains(mouseX: Double, mouseY: Double): Boolean {
        val xRange = tabNavigationBar.x..(tabNavigationBar.x + tabNavigationBar.width)
        val yRange = tabNavigationBar.y..(tabNavigationBar.y + tabNavigationBar.height)
        return xRange.contains(mouseX.toInt()) && yRange.contains(mouseY.toInt())
    }

    internal fun onMouseScrolled(amount: Double): Boolean {
        scrollAccumulator += amount
        val sa = scrollAccumulator.toInt()
        scrollAccumulator %= 1
        if (sa > 0) CreativeModeTabManager.previousPage() else if (sa < 0) CreativeModeTabManager.nextPage()
        return true
    }

    internal fun selectTab(tabIndex: Int, playClickSound: Boolean) {
        if (tabIndex in tabNavigationBar.tabButtons.indices) {
            val selectedButton = tabNavigationBar.tabButtons[tabIndex]
            tabNavigationBar.setFocusedChild(selectedButton)
            if (playClickSound) Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
        }
    }
}