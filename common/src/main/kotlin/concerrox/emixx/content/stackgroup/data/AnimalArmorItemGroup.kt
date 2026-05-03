package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.AnimalArmorItem

internal class AnimalArmorItemGroup: StackGroup(ResourceLocation.withDefaultNamespace("animal_armors")) {

    override fun match(stack: EmiIngredient): Boolean {
        if (stack !is EmiStack) return false
        return stack.itemStack.item is AnimalArmorItem
    }

}