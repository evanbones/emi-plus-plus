package concerrox.emixx.neoforge

import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig
import dev.emi.emi.config.EmiConfig
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import net.neoforged.neoforge.client.event.ScreenEvent
import net.neoforged.neoforge.client.gui.ConfigurationScreen
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.common.NeoForge

@Mod(EmiPlusPlus.MOD_ID, dist = [Dist.CLIENT])
class EmiPlusPlusClientNeoForge(eventBus: IEventBus, container: ModContainer) {
    init {
        EmiPlusPlus.initializeClient(EmiPlusPlusPlatformNeoForge)
        container.registerConfig(ModConfig.Type.CLIENT, EmiPlusPlusConfig.CONFIG_SPEC, "emixx/emixx-client.toml")
        container.registerExtensionPoint(IConfigScreenFactory::class.java, IConfigScreenFactory(::ConfigurationScreen))

        NeoForge.EVENT_BUS.addListener<ScreenEvent.Opening> { event ->
            if (EmiPlusPlusConfig.emiOnlyInRecipeBook.get()) {
                EmiConfig.enabled = false
            }
        }
    }
}