package concerrox.emixx.content.creativemodetab

import concerrox.emixx.Minecraft
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.ScreenManager
import concerrox.emixx.content.StackManager
import concerrox.emixx.content.creativemodetab.gui.CreativeModeTabGui
import concerrox.emixx.content.creativemodetab.gui.itemtab.ItemTab
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.screen.EmiScreenManager
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs

object CreativeModeTabManager {

    val HIDDEN_CREATIVE_MODE_TABS = listOf(
        CreativeModeTabs.INVENTORY, CreativeModeTabs.HOTBAR, CreativeModeTabs.SEARCH
    ).mapNotNull(BuiltInRegistries.CREATIVE_MODE_TAB::get)

    private var currentTabPage = 0u
    private var lastTabPage = 0u
    private var currentTab: UInt? = null

    private var isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = false
    private var isSelectingEmiPlusPlusCreativeModeTabByVanilla = false

    private val indexCreativeModeTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH)
    private val disabledCreativeModeTabs = mutableListOf(*loadDisabledTabs().toTypedArray())
    private val creativeModeTabs = getVisibleCreativeModeTabs()

    internal fun loadDisabledTabs() = EmiPlusPlusConfig.disabledCreativeModeTabs.get().map {
        ResourceLocation(it)
    }.mapNotNull { creativeModeTab -> BuiltInRegistries.CREATIVE_MODE_TAB.get(creativeModeTab) }

    internal fun getVisibleCreativeModeTabs() = BuiltInRegistries.CREATIVE_MODE_TAB.toMutableList().apply {
        removeIf { shouldHideTab(it) }
        if (indexCreativeModeTab != null) {
            add(0, indexCreativeModeTab)
        }
    }

    internal fun initialize() {
        val tabCount = CreativeModeTabGui.tabCount.coerceAtLeast(1u)
        lastTabPage = (creativeModeTabs.size - 1).toUInt() / tabCount
        currentTabPage = 0u
        val page = updateTabs()

        if (page.isEmpty()) return

        if (currentTab == null) {
            CreativeModeTabGui.selectTab(0, false)
            val bar = if (CreativeModeTabGui.currentTheme == CreativeModeTabGui.TabTheme.DEFAULT)
                CreativeModeTabGui.topTabNavigationBar
            else CreativeModeTabGui.leftTabNavigationBar

            if (bar.visibleTabs.isNotEmpty()) {
                onTabSelected(bar.visibleTabs[0])
            }
        }
    }

    internal fun reload() {
        disabledCreativeModeTabs.clear()
        disabledCreativeModeTabs.addAll(loadDisabledTabs())

        creativeModeTabs.clear()
        creativeModeTabs.addAll(getVisibleCreativeModeTabs())
    }

    internal fun shouldHideTab(tab: CreativeModeTab) =
        tab.displayItems.isEmpty() || tab in HIDDEN_CREATIVE_MODE_TABS || tab in disabledCreativeModeTabs

    @Suppress("unused_parameter")
    internal fun nextPage(button: Button? = null) {
        if (currentTabPage < lastTabPage) currentTabPage++ else currentTabPage = 0u
        currentTab = null
        updateTabs()
    }

    @Suppress("unused_parameter")
    internal fun previousPage(button: Button? = null) {
        if (currentTabPage > 0u) currentTabPage-- else currentTabPage = lastTabPage
        currentTab = null
        updateTabs()
    }

    internal fun onTabSelected(tab: ItemTab) {
        if (!Minecraft.isSameThread) {
            Minecraft.execute { onTabSelected(tab) }
            return
        }

        val theme = CreativeModeTabGui.currentTheme
        var newTab: UInt? = null

        if (theme == CreativeModeTabGui.TabTheme.BERRY) {
            val leftIndex = CreativeModeTabGui.leftTabNavigationBar.visibleTabs.indexOf(tab)
            val rightIndex = CreativeModeTabGui.rightTabNavigationBar.visibleTabs.indexOf(tab)
            if (leftIndex != -1) newTab = leftIndex.toUInt()
            else if (rightIndex != -1) newTab = (rightIndex + 5).toUInt()
        } else {
            // Default or Vanilla (Single bar)
            val bar = if (theme == CreativeModeTabGui.TabTheme.DEFAULT) CreativeModeTabGui.topTabNavigationBar else CreativeModeTabGui.leftTabNavigationBar
            val index = bar.visibleTabs.indexOf(tab)
            if (index != -1) newTab = index.toUInt()
        }

        if (newTab == null) return
        if (currentTab == newTab) return
        currentTab = newTab

        // Unfocus other tabs
        val resetFocus = { bar: concerrox.emixx.content.creativemodetab.gui.itemtab.ItemTabNavigationBar, offset: Int ->
            bar.tabButtons.forEachIndexed { i, it ->
                if ((i + offset).toUInt() != currentTab) it.isFocused = false
            }
        }

        if (theme == CreativeModeTabGui.TabTheme.BERRY) {
            resetFocus(CreativeModeTabGui.leftTabNavigationBar, 0)
            resetFocus(CreativeModeTabGui.rightTabNavigationBar, 5)
        } else if (theme == CreativeModeTabGui.TabTheme.DEFAULT) {
            resetFocus(CreativeModeTabGui.topTabNavigationBar, 0)
        } else {
            resetFocus(CreativeModeTabGui.leftTabNavigationBar, 0)
        }

        val screen = Minecraft.screen
        if (!isSelectingEmiPlusPlusCreativeModeTabByVanilla && EmiPlusPlusConfig.syncSelectedCreativeModeTab.get() && tab.creativeModeTab != null && screen is CreativeModeInventoryScreen) {
            isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = true
            screen.selectTab(tab.creativeModeTab)
            screen.searchBox.setCanLoseFocus(true)
            screen.searchBox.isFocused = false
            isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = false
        }

        val sourceStacks = if (tab.creativeModeTab == indexCreativeModeTab) StackManager.indexStacks else {
            tab.creativeModeTab!!.displayItems.map(EmiStack::of)
        }
        if (ScreenManager.isSearching) {
            StackManager.search(sourceStacks, EmiScreenManager.search.value)
        } else {
            StackManager.updateSourceStacks(sourceStacks)
        }
    }

    internal fun onCreativeModeInventoryScreenTabSelected(tab: CreativeModeTab) {
        val screen = Minecraft.screen as? CreativeModeInventoryScreen ?: return

        if (!EmiPlusPlusConfig.syncSelectedCreativeModeTab.get()) return
        var notHiddenTab = tab
        if (isSelectingVanillaCreativeInventoryTabByEmiPlusPlus) return
        if (shouldHideTab(tab) && indexCreativeModeTab != null) {
            notHiddenTab = indexCreativeModeTab
        }
        for (i in 0u..lastTabPage) {
            val tabIndex = getTabPage(i).indexOfFirst { it.creativeModeTab == notHiddenTab }
            if (tabIndex != -1) {
                currentTabPage = i
                val page = updateTabs()
                CreativeModeTabGui.selectTab(tabIndex, false)
                isSelectingEmiPlusPlusCreativeModeTabByVanilla = true
                onTabSelected(page[tabIndex])
                screen.searchBox.setCanLoseFocus(true)
                screen.searchBox.isFocused = false
                isSelectingEmiPlusPlusCreativeModeTabByVanilla = false
                return
            }
        }
    }

    private fun getTabPage(page: UInt): MutableList<ItemTab> {
        val count = CreativeModeTabGui.tabCount.toInt()
        val start = (page.toInt() * count)
        val list = mutableListOf<ItemTab>()
        for (i in 0 until count) {
            val tab = creativeModeTabs.getOrNull(start + i) ?: break
            list.add(ItemTab(tab))
        }
        return list
    }

    private fun updateTabs(): List<ItemTab> {
        val page = getTabPage(currentTabPage)

        if (CreativeModeTabGui.currentTheme == CreativeModeTabGui.TabTheme.BERRY) {
            val leftTabs = page.take(5).toMutableList()
            val rightTabs = page.drop(5).take(6).toMutableList()

            CreativeModeTabGui.leftTabNavigationBar.setTabs(leftTabs)
            CreativeModeTabGui.rightTabNavigationBar.setTabs(rightTabs)
        } else if (CreativeModeTabGui.currentTheme == CreativeModeTabGui.TabTheme.VANILLA) {
            CreativeModeTabGui.leftTabNavigationBar.setTabs(page.toMutableList())
        } else {
            CreativeModeTabGui.topTabNavigationBar.setTabs(page.toMutableList())
        }

        return page
    }
}