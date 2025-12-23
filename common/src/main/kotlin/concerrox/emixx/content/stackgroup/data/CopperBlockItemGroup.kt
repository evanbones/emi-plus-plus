package concerrox.emixx.content.stackgroup.data

import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.WeatheringCopperFullBlock

internal class CopperBlockItemGroup : StackGroup(ResourceLocation("copper_blocks")) {

    companion object {
        private val WAXED_COPPER_BLOCK_ITEMS = arrayOf(
            Items.WAXED_COPPER_BLOCK,
            Items.WAXED_CUT_COPPER,

            Items.WAXED_EXPOSED_COPPER,
            Items.WAXED_EXPOSED_CUT_COPPER,

            Items.WAXED_WEATHERED_COPPER,
            Items.WAXED_WEATHERED_CUT_COPPER,

            Items.WAXED_OXIDIZED_COPPER,
            Items.WAXED_OXIDIZED_CUT_COPPER
        )
    }

    override fun match(stack: EmiIngredient): Boolean {
        if (stack !is EmiStack) return false
        val item = stack.itemStack.item
        return item is BlockItem && (item.block is WeatheringCopperFullBlock ||
                item in WAXED_COPPER_BLOCK_ITEMS)
    }

}