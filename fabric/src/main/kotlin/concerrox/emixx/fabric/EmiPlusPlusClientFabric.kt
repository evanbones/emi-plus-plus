package concerrox.emixx.fabric

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry
import net.fabricmc.api.ClientModInitializer
import net.minecraftforge.fml.config.ModConfig

class EmiPlusPlusClientFabric : ClientModInitializer {

    override fun onInitializeClient() {
        EmiPlusPlus.initializeClient(EmiPlusPlusPlatformFabric)
        ForgeConfigRegistry.INSTANCE.register(
            EmiPlusPlus.MOD_ID, ModConfig.Type.CLIENT, EmiPlusPlusConfig.CONFIG_SPEC, "emixx/emixx-client.toml"
        )
    }

}