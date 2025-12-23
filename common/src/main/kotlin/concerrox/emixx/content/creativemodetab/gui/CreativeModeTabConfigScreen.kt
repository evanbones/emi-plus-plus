package concerrox.emixx.content.creativemodetab.gui

import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.creativemodetab.CreativeModeTabManager
import concerrox.emixx.gui.GridList
import concerrox.emixx.gui.GridListConfigScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation

class CreativeModeTabConfigScreen(parent: Screen) : GridListConfigScreen("creative_mode_tab_config", parent) {

    private lateinit var disabledCreativeModeTabs: MutableSet<ResourceLocation>

    override fun createList(): GridList<*> {
        EmiPlusPlusConfig.ensureLoaded()
        disabledCreativeModeTabs = EmiPlusPlusConfig.disabledCreativeModeTabs.get().map { ResourceLocation(it) }.toMutableSet()
        return CreativeModeTabGridList(this, disabledCreativeModeTabs)
    }

    override fun save() {
        if (::disabledCreativeModeTabs.isInitialized) {
            EmiPlusPlusConfig.disabledCreativeModeTabs.set(disabledCreativeModeTabs.map { it.toString() }.toList())
            EmiPlusPlusConfig.save()
        }
    }

    override fun reload() {
        CreativeModeTabManager.reload()
    }
}