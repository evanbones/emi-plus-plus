package concerrox.emixx.neoforge

import concerrox.emixx.EmiPlusPlus
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod

@Mod(EmiPlusPlus.MOD_ID)
class EmiPlusPlusNeoForge(eventBus: IEventBus, container: ModContainer) {

    init {
        EmiPlusPlus.initialize(EmiPlusPlusPlatformNeoForge)
    }

}