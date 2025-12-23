package concerrox.emixx.forge

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.content.villagertrade.VillagerTradeManager
import concerrox.emixx.forge.mixin.forge.BasicItemListingAccessor
import dev.emi.emi.api.stack.EmiStack
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.BasicItemListing

@Mod(EmiPlusPlus.MOD_ID)
class EmiPlusPlusForge(eventBus: IEventBus, container: ModContainer) {

    init {
        EmiPlusPlus.initialize(EmiPlusPlusPlatformForge)

        VillagerTradeManager.addCustomVillagerTradeType(BasicItemListing::class.java) { itemListing ->
            val accessor = itemListing as BasicItemListingAccessor
            val inputs = mutableListOf(EmiStack.of(accessor.price), EmiStack.of(accessor.price2))
            val output = mutableListOf(EmiStack.of(accessor.forSale))
            inputs to output
        }
    }

}