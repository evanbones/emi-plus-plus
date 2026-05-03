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

    internal var scrollOffset = 0
    private var currentTab: CreativeModeTab? = null

    private var isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = false
    private var isSelectingEmiPlusPlusCreativeModeTabByVanilla = false

    private val indexCreativeModeTab = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.SEARCH)
    private val disabledCreativeModeTabs = mutableListOf(*loadDisabledTabs().toTypedArray())
    private val creativeModeTabs = getVisibleCreativeModeTabs()

    internal val maxScroll: Int
        get() = maxOf(0, creativeModeTabs.size - CreativeModeTabGui.tabCount.toInt())

    internal fun loadDisabledTabs() = EmiPlusPlusConfig.disabledCreativeModeTabs.get().map {
        ResourceLocation.parse(it)
    }.mapNotNull { creativeModeTab -> BuiltInRegistries.CREATIVE_MODE_TAB.get(creativeModeTab) }

    internal fun getVisibleCreativeModeTabs() = CreativeModeTabs.tabs().toMutableList().apply {
        removeIf { shouldHideTab(it) }
        if (indexCreativeModeTab != null) {
            add(0, indexCreativeModeTab)
        }
    }

    internal fun initialize() {
        scrollOffset = 0
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

    fun reload() {
        disabledCreativeModeTabs.clear()
        disabledCreativeModeTabs.addAll(loadDisabledTabs())
        creativeModeTabs.clear()
        creativeModeTabs.addAll(getVisibleCreativeModeTabs())
        scrollOffset = scrollOffset.coerceAtMost(maxScroll)
    }

    internal fun shouldHideTab(tab: CreativeModeTab) =
        tab.displayItems.isEmpty() || tab in HIDDEN_CREATIVE_MODE_TABS || tab in disabledCreativeModeTabs

    @Suppress("unused_parameter")
    internal fun nextPage(button: Button? = null) {
        if (scrollOffset < maxScroll) scrollOffset++
        updateTabs()
    }

    @Suppress("unused_parameter")
    internal fun previousPage(button: Button? = null) {
        if (scrollOffset > 0) scrollOffset--
        updateTabs()
    }

    internal fun onTabSelected(tab: ItemTab) {
        if (!Minecraft.isSameThread) {
            Minecraft.execute { onTabSelected(tab) }
            return
        }
        val selectedModeTab = tab.creativeModeTab ?: return
        if (currentTab == selectedModeTab) return
        currentTab = selectedModeTab

        val theme = CreativeModeTabGui.currentTheme
        val bar = if (theme == CreativeModeTabGui.TabTheme.DEFAULT)
            CreativeModeTabGui.topTabNavigationBar
        else
            CreativeModeTabGui.leftTabNavigationBar

        bar.tabButtons.forEach { it.isFocused = false }
        bar.tabButtons.forEach {
            val btnTab = it.tab() as? ItemTab
            if (btnTab != null && btnTab.creativeModeTab == currentTab) {
                bar.setFocusedChild(it)
            }
        }

        val screen = Minecraft.screen
        if (!isSelectingEmiPlusPlusCreativeModeTabByVanilla && EmiPlusPlusConfig.syncSelectedCreativeModeTab.get() && screen is CreativeModeInventoryScreen) {
            isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = true
            screen.selectTab(tab.creativeModeTab)
            screen.searchBox.setCanLoseFocus(true)
            screen.searchBox.isFocused = false
            isSelectingVanillaCreativeInventoryTabByEmiPlusPlus = false
        }

        val sourceStacks = if (tab.creativeModeTab == indexCreativeModeTab) StackManager.indexStacks else {
            tab.creativeModeTab.displayItems.map(EmiStack::of)
        }
        if (ScreenManager.isSearching) {
            StackManager.search(sourceStacks, EmiScreenManager.search.value)
        } else {
            StackManager.updateSourceStacks(sourceStacks)
        }
    }

    private fun updateTabs(): List<ItemTab> {
        val count = CreativeModeTabGui.tabCount.toInt()
        val pageTabs = creativeModeTabs.drop(scrollOffset).take(count).map { ItemTab(it) }
        val bar = when (CreativeModeTabGui.currentTheme) {
            CreativeModeTabGui.TabTheme.BERRY, CreativeModeTabGui.TabTheme.VANILLA -> {
                CreativeModeTabGui.leftTabNavigationBar.apply { setTabs(pageTabs.toMutableList()) }
            }

            else -> {
                CreativeModeTabGui.topTabNavigationBar.apply { setTabs(pageTabs.toMutableList()) }
            }
        }

        bar.tabButtons.forEach { it.isFocused = false }
        bar.tabButtons.forEach { btn ->
            val tab = btn.tab() as? ItemTab
            if (tab != null && tab.creativeModeTab == currentTab && currentTab != null) {
                bar.setFocusedChild(btn)
            }
        }

        return pageTabs
    }

    internal fun onCreativeModeInventoryScreenTabSelected(tab: CreativeModeTab) {
        if (CreativeModeTabGui.tabCount == 0u) return

        val screen = Minecraft.screen as? CreativeModeInventoryScreen ?: return
        if (!EmiPlusPlusConfig.syncSelectedCreativeModeTab.get()) return
        var notHiddenTab = tab
        if (isSelectingVanillaCreativeInventoryTabByEmiPlusPlus) return

        if (shouldHideTab(tab) && indexCreativeModeTab != null) {
            notHiddenTab = indexCreativeModeTab
        }

        val tabIndex = creativeModeTabs.indexOf(notHiddenTab)
        if (tabIndex != -1) {
            if (tabIndex < scrollOffset) {
                scrollOffset = tabIndex
            } else if (tabIndex >= scrollOffset + CreativeModeTabGui.tabCount.toInt()) {
                scrollOffset = tabIndex - CreativeModeTabGui.tabCount.toInt() + 1
            }

            val page = updateTabs()
            val localIndex = tabIndex - scrollOffset

            if (localIndex in page.indices) {
                CreativeModeTabGui.selectTab(localIndex, false)

                isSelectingEmiPlusPlusCreativeModeTabByVanilla = true
                onTabSelected(page[localIndex])
                screen.searchBox.setCanLoseFocus(true)
                screen.searchBox.isFocused = false
                isSelectingEmiPlusPlusCreativeModeTabByVanilla = false
            }
        }
    }
}