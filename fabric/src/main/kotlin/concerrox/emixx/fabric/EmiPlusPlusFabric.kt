package concerrox.emixx.fabric

import concerrox.emixx.EmiPlusPlus
import net.fabricmc.api.ModInitializer

class EmiPlusPlusFabric : ModInitializer {

    override fun onInitialize() {
        EmiPlusPlus.initialize(EmiPlusPlusPlatformFabric)
    }

}