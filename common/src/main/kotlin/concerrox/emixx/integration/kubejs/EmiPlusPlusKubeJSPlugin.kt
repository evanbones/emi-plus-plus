package concerrox.emixx.integration.kubejs

import concerrox.emixx.content.stackgroup.StackGroupManager
import concerrox.emixx.content.stackgroup.data.EmiStackGroup
import dev.emi.emi.api.stack.EmiIngredient
import dev.latvian.mods.kubejs.KubeJSPlugin
import dev.latvian.mods.kubejs.event.EventGroup
import dev.latvian.mods.kubejs.event.EventJS
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient

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

    fun register(id: String, ingredient: Ingredient) {
        val resourceLocation = ResourceLocation(id)

        val emiIngredient = EmiIngredient.of(ingredient)

        val targets = mutableSetOf<EmiIngredient>()
        targets.add(emiIngredient)
        targets.addAll(emiIngredient.emiStacks)

        val group = EmiStackGroup(resourceLocation, targets)

        StackGroupManager.stackGroups.add(group)
    }
}