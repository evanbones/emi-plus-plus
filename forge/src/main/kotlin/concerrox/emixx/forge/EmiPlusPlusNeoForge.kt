package concerrox.emixx.forge

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.content.villagertrade.VillagerTradeManager
import concerrox.emixx.forge.mixin.neoforge.BasicItemListingAccessor
import dev.emi.emi.api.stack.EmiStack
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.BasicItemListing

@Mod(EmiPlusPlus.MOD_ID)
class EmiPlusPlusNeoForge(eventBus: IEventBus, container: ModContainer) {

    init {
        EmiPlusPlus.initialize(EmiPlusPlusPlatformNeoForge)

        VillagerTradeManager.addCustomVillagerTradeType(BasicItemListing::class.java) { itemListing ->
            val accessor = itemListing as BasicItemListingAccessor
            val inputs = mutableListOf(EmiStack.of(accessor.price), EmiStack.of(accessor.price2))
            val output = mutableListOf(EmiStack.of(accessor.forSale))
            inputs to output
        }
    }

}