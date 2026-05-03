package concerrox.emixx.integration.kubejs

import concerrox.emixx.content.stackgroup.StackGroupManager
import concerrox.emixx.content.stackgroup.data.EmiStackGroup
import dev.emi.emi.api.stack.EmiIngredient
import dev.latvian.mods.kubejs.event.EventGroup
import dev.latvian.mods.kubejs.event.EventGroupRegistry
import dev.latvian.mods.kubejs.event.KubeEvent
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient

class EmiPlusPlusKubeJSPlugin : KubeJSPlugin {

    companion object {
        val GROUP = EventGroup.of("EmiPlusPlusEvents")
        val REGISTER_GROUPS = GROUP.client("registerGroups") { RegisterStackGroupsEventJS::class.java }
    }

    override fun registerEvents(registry: EventGroupRegistry) {
        registry.register(GROUP)
    }
}

class RegisterStackGroupsEventJS : KubeEvent {

    fun register(id: String, ingredient: Ingredient) {
        val resourceLocation = ResourceLocation.parse(id)

        val emiIngredient = EmiIngredient.of(ingredient)

        val targets = mutableSetOf<EmiIngredient>()
        targets.add(emiIngredient)
        targets.addAll(emiIngredient.emiStacks)

        val group = EmiStackGroup(resourceLocation, targets)

        StackGroupManager.stackGroups.add(group)
    }
}