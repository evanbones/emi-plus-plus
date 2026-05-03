package concerrox.emixx.content.creativemodetab.gui

import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.creativemodetab.CreativeModeTabManager
import concerrox.emixx.gui.GridList
import concerrox.emixx.gui.GridListConfigScreen
import net.minecraft.resources.ResourceLocation

class CreativeModeTabConfigScreen : GridListConfigScreen("creative_mode_tab_config") {

    private val disabledCreativeModeTabs =
        EmiPlusPlusConfig.disabledCreativeModeTabs.get().map { ResourceLocation.parse(it) }.toMutableSet()

    override fun createList(): GridList<*> = CreativeModeTabGridList(this, disabledCreativeModeTabs)

    override fun save() {
        EmiPlusPlusConfig.disabledCreativeModeTabs.set(disabledCreativeModeTabs.map { it.toString() }.toList())
        EmiPlusPlusConfig.save()
    }

    override fun reload() {
        CreativeModeTabManager.reload()
    }

}