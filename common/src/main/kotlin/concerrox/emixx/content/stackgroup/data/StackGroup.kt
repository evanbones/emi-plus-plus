package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiIngredient
import net.minecraft.resources.ResourceLocation

abstract class StackGroup(val id: ResourceLocation) {

    internal var isEnabled = true

    abstract fun match(stack: EmiIngredient): Boolean

    open fun getOptimizedIds(): Set<ResourceLocation>? {
        return null
    }

    companion object {
    }
}