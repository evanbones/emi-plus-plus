package concerrox.emixx.integration.kubejs

import concerrox.emixx.content.stackgroup.StackGroupManager
import concerrox.emixx.content.stackgroup.data.EmiStackGroup
import dev.emi.emi.api.stack.EmiIngredient
import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.event.EventGroup
import dev.latvian.mods.kubejs.event.EventJS
import dev.latvian.mods.kubejs.item.InputItem
import net.minecraft.resources.ResourceLocation
import java.lang.reflect.Field

class EmiPlusPlusKubeJSPlugin : KubeJSPlugin() {

    companion object {
        val GROUP = EventGroup.of("EmiPlusPlusEvents")
        val REGISTER_GROUPS = GROUP.client("registerGroups") { RegisterStackGroupsEventJS::class.java }
    }

    override fun registerEvents() {
        GROUP.register()
    }
}

class RegisterStackGroupsEventJS : EventJS() {

    fun register(id: String, ingredient: Any) {
        val resourceLocation = ResourceLocation(id)

        val vanillaIngredient = InputItem.of(ingredient).ingredient
        val emiIngredient = EmiIngredient.of(vanillaIngredient)

        val group = EmiStackGroup(resourceLocation, setOf(emiIngredient))
        addStackGroup(group)
    }

    private fun addStackGroup(group: EmiStackGroup) {
        try {
            val field: Field = StackGroupManager::class.java.getDeclaredField("stackGroups")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val list = field.get(StackGroupManager) as MutableList<Any>
            list.add(group)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}