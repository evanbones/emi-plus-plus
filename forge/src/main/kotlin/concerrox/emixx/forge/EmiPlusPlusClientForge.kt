package concerrox.emixx.forge

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig
import net.minecraftforge.client.ConfigScreenHandler
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig

object EmiPlusPlusClientForge {

    fun init() {
        EmiPlusPlus.initializeClient(EmiPlusPlusPlatformForge)

        // Register Config
        ModLoadingContext.get().registerConfig(
            ModConfig.Type.CLIENT,
            EmiPlusPlusConfig.CONFIG_SPEC,
            "emixx/emixx-client.toml"
        )

        // Register Config Screen
        /*
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory::class.java) {
            ConfigScreenHandler.ConfigScreenFactory { _, parent ->
                ConfigurationScreen(parent)
            }
        }
        */
    }

}