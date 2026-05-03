package concerrox.emixx.gui

import concerrox.emixx.text
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.tabs.GridLayoutTab
import net.minecraft.client.gui.components.tabs.TabManager
import net.minecraft.client.gui.components.tabs.TabNavigationBar
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents

abstract class GridListConfigScreen(val name: String) : Screen(text("gui", name)) {

    private val layout = HeaderAndFooterLayout(this)
    private lateinit var list: GridList<*>
    private val tabManager = TabManager(::addRenderableWidget, ::removeWidget)
    private val tabNavigationBar = TabNavigationBar.builder(tabManager, width).addTabs(
        PrebuiltTab(name)
    ).build()

    abstract fun createList(): GridList<*>
    abstract fun save()
    abstract fun reload()

    override fun init() {
        list = createList()
        layout.addTitleHeader(title, font)
        layout.addToContents(list)
        layout.addToFooter(Button.builder(CommonComponents.GUI_DONE) {
            save()
            onClose()
            reload()
        }.width(200).build())
        layout.visitWidgets(::addRenderableWidget)
        repositionElements()
        list.add()
    }

    override fun repositionElements() {
        list.updateSize(width, layout)
        layout.arrangeElements()
        tabNavigationBar.setWidth(width)
        tabNavigationBar.arrangeElements()
        tabManager.setTabArea(ScreenRectangle(0, tabNavigationBar.rectangle.bottom(), width, height))
    }

    @Environment(EnvType.CLIENT)
    private class PrebuiltTab(name: String) : GridLayoutTab(text("gui", "$name.prebuilt"))

}