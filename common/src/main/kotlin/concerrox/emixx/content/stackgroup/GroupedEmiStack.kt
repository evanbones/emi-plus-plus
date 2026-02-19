package concerrox.emixx.content.stackgroup

import concerrox.emixx.content.stackgroup.data.StackGroup
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

class GroupedEmiStack<T : EmiStack>(val realStack: T, val stackGroup: StackGroup) : EmiStack() {

    init {
        this.amount = realStack.amount
        this.chance = realStack.chance
        this.remainder = realStack.remainder
    }

    override fun render(draw: GuiGraphics, x: Int, y: Int, delta: Float, flags: Int) =
        realStack.render(draw, x, y, delta, flags)

    override fun isEqual(stack: EmiStack?): Boolean {
        if (stack is GroupedEmiStack<*>) {
            return realStack.isEqual(stack.realStack, Comparison.compareNbt())
        }
        return realStack.isEqual(stack, Comparison.compareNbt())
    }

    override fun isEqual(stack: EmiStack?, comparison: Comparison?): Boolean {
        if (stack is GroupedEmiStack<*>) {
            return realStack.isEqual(stack.realStack, comparison)
        }
        return realStack.isEqual(stack, comparison)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is GroupedEmiStack<*>) {
            return realStack.isEqual(other.realStack, Comparison.compareNbt())
        }
        return realStack == other
    }

    override fun hashCode(): Int = realStack.hashCode()

    override fun getEmiStacks(): MutableList<EmiStack> = realStack.emiStacks
    override fun isEmpty() = realStack.isEmpty

    @Suppress("UNCHECKED_CAST")
    override fun copy(): EmiStack {
        val copy = GroupedEmiStack(realStack.copy() as T, stackGroup)
        copy.amount = this.amount
        copy.chance = this.chance
        copy.remainder = this.remainder
        return copy
    }

    override fun getNbt(): CompoundTag? = realStack.nbt

    override fun getKey(): Any = realStack.key
    override fun getId(): ResourceLocation = realStack.id
    override fun getTooltipText(): MutableList<Component> = realStack.tooltipText
    override fun getName(): Component = realStack.name

    override fun <T> getKeyOfType(clazz: Class<T>?): T? = realStack.getKeyOfType(clazz)
    override fun getItemStack(): ItemStack = realStack.itemStack

    override fun getTooltip(): MutableList<ClientTooltipComponent> = realStack.tooltip
    override fun toString(): String = realStack.toString()
}