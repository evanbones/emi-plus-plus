package concerrox.emixx.fabric

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig
import dev.emi.emi.config.EmiConfig
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.neoforged.fml.config.ModConfig

class EmiPlusPlusClientFabric : ClientModInitializer {
    override fun onInitializeClient() {
        EmiPlusPlus.initializeClient(EmiPlusPlusPlatformFabric)
        NeoForgeConfigRegistry.INSTANCE.register(
            EmiPlusPlus.MOD_ID, ModConfig.Type.CLIENT, EmiPlusPlusConfig.CONFIG_SPEC, "emixx/emixx-client.toml"
        )

        ScreenEvents.BEFORE_INIT.register { client, screen, scaledWidth, scaledHeight ->
            if (EmiPlusPlusConfig.emiOnlyInRecipeBook.get()) {
                EmiConfig.enabled = false
            }
        }
    }
}