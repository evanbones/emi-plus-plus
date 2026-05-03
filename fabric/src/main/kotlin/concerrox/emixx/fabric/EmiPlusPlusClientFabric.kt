package concerrox.emixx.fabric

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry
import net.fabricmc.api.ClientModInitializer
import net.neoforged.fml.config.ModConfig

class EmiPlusPlusClientFabric : ClientModInitializer {

    override fun onInitializeClient() {
        EmiPlusPlus.initializeClient(EmiPlusPlusPlatformFabric)
        NeoForgeConfigRegistry.INSTANCE.register(
            EmiPlusPlus.MOD_ID, ModConfig.Type.CLIENT, EmiPlusPlusConfig.CONFIG_SPEC, "emixx/emixx-client.toml"
        )
    }

}