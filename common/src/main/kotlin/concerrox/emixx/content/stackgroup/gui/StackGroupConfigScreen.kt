package concerrox.emixx.content.stackgroup.gui

import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.StackManager
import concerrox.emixx.content.stackgroup.StackGroupManager
import concerrox.emixx.gui.GridList
import concerrox.emixx.gui.GridListConfigScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation

class StackGroupConfigScreen(parent: Screen) : GridListConfigScreen("stack_group_config", parent) {

    private lateinit var disabledStackGroups: MutableSet<ResourceLocation>

    override fun createList(): GridList<*> {
        EmiPlusPlusConfig.ensureLoaded()
        disabledStackGroups = EmiPlusPlusConfig.disabledStackGroups.get().map { ResourceLocation(it) }.toMutableSet()
        return StackGroupGridList(this, disabledStackGroups)
    }

    override fun save() {
        if (::disabledStackGroups.isInitialized) {
            EmiPlusPlusConfig.disabledStackGroups.set(disabledStackGroups.map { it.toString() }.toList())
            EmiPlusPlusConfig.save()
        }
    }

    override fun reload() {
        StackGroupManager.reload()
        StackManager.reload()
    }
}