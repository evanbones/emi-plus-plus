package concerrox.emixx.content.creativemodetab.gui

import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.ScreenManager
import concerrox.emixx.content.creativemodetab.CreativeModeTabManager
import concerrox.emixx.content.creativemodetab.gui.itemtab.ItemTabManager
import concerrox.emixx.content.creativemodetab.gui.itemtab.ItemTabNavigationBar
import concerrox.emixx.gui.components.ImageButton
import concerrox.emixx.res
import concerrox.emixx.util.pos
import dev.emi.emi.config.EmiConfig
import dev.emi.emi.config.HeaderType
import dev.emi.emi.config.SidebarTheme
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvents

object CreativeModeTabGui {
    const val CREATIVE_MODE_TAB_HEIGHT = 18
    private const val EMI_HEADER_HEIGHT = 18
    const val VERTICAL_TAB_WIDTH = 35

    enum class TabTheme { DEFAULT, VANILLA, BERRY }

    val currentTheme: TabTheme
        get() {
            if (EmiConfig.rightSidebarTheme == SidebarTheme.VANILLA) {
                return if (EmiPlusPlusConfig.enableBerryTheme.get()) TabTheme.BERRY else TabTheme.VANILLA
            }
            return TabTheme.DEFAULT
        }

    private lateinit var screen: Screen
    private val isHeaderVisible
        get() = EmiConfig.rightSidebarHeader == HeaderType.VISIBLE

    private val tabManager = ItemTabManager({ screen.addRenderableWidget(it) }, { screen.removeWidget(it) }).apply {
        onTabSelectedListener = CreativeModeTabManager::onTabSelected
    }

    internal val topTabNavigationBar = ItemTabNavigationBar(tabManager, isVertical = false)
    internal val leftTabNavigationBar = ItemTabNavigationBar(tabManager, isVertical = true, isRightSide = false)
    internal val rightTabNavigationBar = ItemTabNavigationBar(tabManager, isVertical = true, isRightSide = true)

    internal var tabCount = 0u

    private val buttonPrevious = ImageButton(16, 16, u = 0, v = 0, { true }) {
        CreativeModeTabManager.previousPage()
    }.matchScreenManagerVisibility().pos(0, 0)

    private val buttonNext = ImageButton(16, 16, u = 16, v = 0, { true }) {
        CreativeModeTabManager.nextPage()
    }.matchScreenManagerVisibility().pos(0, 0)

    private val buttonScrollDown = ImageButton(8, 4, u = 0, v = 0, { CreativeModeTabManager.scrollOffset < CreativeModeTabManager.maxScroll }) {
        CreativeModeTabManager.nextPage()
    }.matchScreenManagerVisibility().withTexture(res("textures/gui/scroll_down.png"), 8, 4).pos(0, 0)

    private var scrollAccumulator = 0.0

    private fun onLayout() {
        val indexScreenSpace = ScreenManager.indexScreenSpace
        val theme = currentTheme
        buttonPrevious.visible = false
        buttonNext.visible = false
        buttonScrollDown.visible = false
        topTabNavigationBar.visible = false
        leftTabNavigationBar.visible = false
        rightTabNavigationBar.visible = false
        if (indexScreenSpace == null) return

        if (theme == TabTheme.DEFAULT) {
            val startX = indexScreenSpace.tx
            val startY =
                indexScreenSpace.ty - (if (isHeaderVisible) EMI_HEADER_HEIGHT else 0) - CREATIVE_MODE_TAB_HEIGHT
            val tileW = indexScreenSpace.tw
            tabCount = (tileW.toUInt() - 2u).coerceIn(1u, UByte.MAX_VALUE.toUInt())

            buttonPrevious.visible = true
            buttonPrevious.pos(startX, startY + 2)
            topTabNavigationBar.visible = true
            topTabNavigationBar.pos(startX + buttonPrevious.width, startY)
            buttonNext.visible = true
            buttonNext.pos(topTabNavigationBar.x + topTabNavigationBar.width, startY + 2)
        } else {
            val startY = indexScreenSpace.ty - 26
            val margin = 8
            val horizontalOffset = 8

            val leftX = indexScreenSpace.tx - VERTICAL_TAB_WIDTH - margin + horizontalOffset

            if (theme == TabTheme.BERRY) {
                val availableHeight = indexScreenSpace.th * ScreenManager.ENTRY_SIZE
                tabCount = (availableHeight / 27).toUInt().coerceAtLeast(1u)

                leftTabNavigationBar.visible = true
                leftTabNavigationBar.pos(leftX, startY)

                buttonScrollDown.visible = true
                buttonScrollDown.pos(leftX + 13, startY + (tabCount.toInt() * 27) + 6)
            } else {
                tabCount = 11u
                leftTabNavigationBar.visible = true
                leftTabNavigationBar.pos(leftX, startY)
            }
        }
    }

    internal fun initialize(screen: Screen) {
        this.screen = screen
        screen.removeWidget(buttonPrevious)
        screen.removeWidget(buttonNext)
        screen.removeWidget(buttonScrollDown)
        screen.removeWidget(topTabNavigationBar)
        screen.removeWidget(leftTabNavigationBar)
        screen.removeWidget(rightTabNavigationBar)

        screen.addRenderableWidget(buttonPrevious)
        screen.addRenderableWidget(buttonNext)
        screen.addRenderableWidget(buttonScrollDown)
        screen.addRenderableWidget(topTabNavigationBar)
        screen.addRenderableWidget(leftTabNavigationBar)
        screen.addRenderableWidget(rightTabNavigationBar)
        onLayout()
    }

    internal fun contains(mouseX: Double, mouseY: Double): Boolean {
        fun check(bar: ItemTabNavigationBar): Boolean =
            bar.visible && (bar.x..(bar.x + bar.width)).contains(mouseX.toInt()) && (bar.y..(bar.y + bar.height)).contains(
                mouseY.toInt()
            )
        return check(topTabNavigationBar) || check(leftTabNavigationBar) || check(rightTabNavigationBar)
    }

    internal fun onMouseScrolled(amount: Double): Boolean {
        scrollAccumulator += amount
        val sa = scrollAccumulator.toInt()
        scrollAccumulator %= 1
        if (sa > 0) {
            CreativeModeTabManager.previousPage()
        } else if (sa < 0) {
            CreativeModeTabManager.nextPage()
        }
        return true
    }

    internal fun selectTab(tabIndex: Int, playClickSound: Boolean) {
        val theme = currentTheme
        var targetBar: ItemTabNavigationBar = topTabNavigationBar

        if (theme == TabTheme.DEFAULT) {
            targetBar = topTabNavigationBar
        } else if (theme == TabTheme.VANILLA || theme == TabTheme.BERRY) {
            targetBar = leftTabNavigationBar
        }

        if (tabIndex in targetBar.tabButtons.indices) {
            val selectedButton = targetBar.tabButtons[tabIndex]
            targetBar.setFocusedChild(selectedButton)
            if (playClickSound) Minecraft.getInstance().soundManager.play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f)
            )
        }
    }
}