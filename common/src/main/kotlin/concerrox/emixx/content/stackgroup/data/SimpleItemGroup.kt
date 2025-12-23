package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Ingredient

class SimpleItemGroup(
    id: ResourceLocation,
    val targets: List<Ingredient>,
) : StackGroup(id) {

    @Deprecated("Use constructor that takes a ResourceLocation")
    constructor(id: String, targets: List<Ingredient>) : this(
        ResourceLocation(id),
        targets
    )

    companion object;

    override fun match(stack: EmiIngredient): Boolean {
        if (stack !is EmiStack) return false
        return targets.any { it.test(stack.itemStack) }
    }

}