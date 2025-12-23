package concerrox.emixx.forge

import concerrox.emixx.EmiPlusPlus
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.loading.FMLEnvironment

@Mod(EmiPlusPlus.MOD_ID)
class EmiPlusPlusForge {

    init {
        EmiPlusPlus.initialize(EmiPlusPlusPlatformForge)

        // Delegate to client init if on physical client
        if (FMLEnvironment.dist == Dist.CLIENT) {
            EmiPlusPlusClientForge.init()
        }
    }

}