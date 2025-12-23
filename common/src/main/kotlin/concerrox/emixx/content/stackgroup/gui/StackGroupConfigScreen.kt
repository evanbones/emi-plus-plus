package concerrox.emixx.content.stackgroup.gui

import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.StackManager
import concerrox.emixx.content.stackgroup.StackGroupManager
import concerrox.emixx.gui.GridList
import concerrox.emixx.gui.GridListConfigScreen
import net.minecraft.resources.ResourceLocation

class StackGroupConfigScreen : GridListConfigScreen("stack_group_config") {

    private val disabledStackGroups =
        EmiPlusPlusConfig.disabledStackGroups.get().map { ResourceLocation(it) }.toMutableSet()

    override fun createList(): GridList<*> = StackGroupGridList(this, disabledStackGroups)

    override fun save() {
        EmiPlusPlusConfig.disabledStackGroups.set(disabledStackGroups.map { it.toString() }.toList())
        EmiPlusPlusConfig.save()
    }

    override fun reload() {
        StackGroupManager.reload()
        StackManager.reload()
    }

}