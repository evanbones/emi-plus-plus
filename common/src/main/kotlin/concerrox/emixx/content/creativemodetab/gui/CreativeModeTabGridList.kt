package concerrox.emixx.content.creativemodetab.gui

import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.content.creativemodetab.CreativeModeTabManager
import concerrox.emixx.gui.GridList
import concerrox.emixx.gui.ListEntry
import concerrox.emixx.gui.components.Switch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab

class CreativeModeTabGridList(
    screen: CreativeModeTabConfigScreen, private val disabledCreativeModeTabs: MutableSet<ResourceLocation>
) : GridList<ResourceLocation>(screen) {

    override fun getContents(): Collection<ResourceLocation> =
        BuiltInRegistries.CREATIVE_MODE_TAB.keySet().filter {
            val tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(it) ?: return@filter false
            val isNotEmpty = tab.displayItems.isNotEmpty() || Minecraft.getInstance().level == null
            return@filter isNotEmpty && tab !in CreativeModeTabManager.HIDDEN_CREATIVE_MODE_TABS
        }

    override fun getEntryForContent(
        content: ResourceLocation?,
        triple: TripleEntry<ResourceLocation>
    ): ListEntry {
        return StackGroupEntry(content, triple, disabledCreativeModeTabs, BuiltInRegistries.CREATIVE_MODE_TAB.get(content))
    }

    class StackGroupEntry(
        private val id: ResourceLocation?,
        triple: TripleEntry<ResourceLocation>,
        private val disabledCreativeModeTabs: MutableSet<ResourceLocation>,
        private val tab: CreativeModeTab?
    ): ListEntry(triple) {

        override val switch = Switch.Builder(Component.empty())
            .setChecked(id != null && !disabledCreativeModeTabs.contains(id)).apply {
                onCheckedChangeListener = Switch.OnCheckedChangeListener { _, isChecked ->
                    if (id != null) {
                        if (isChecked) {
                            disabledCreativeModeTabs.remove(id)
                        } else {
                            disabledCreativeModeTabs.add(id)
                        }
                    }
                }
            }.build()
        override val children = mutableListOf<AbstractWidget>(switch)

        override fun shouldRenderSwitch(): Boolean = tab != null && id != null

        override fun getEntryTitle(): Component? = tab?.displayName

        override fun renderEntry(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, startX: Int, startY: Int, partialTick: Float) {
            tab?.let {
                var itemX = startX
                val itemY = startY + ENTRY_SIZE
                val font = Minecraft.getInstance().font
                tab.displayItems.take(8).forEach {
                    guiGraphics.renderItem(it, itemX, itemY)
                    guiGraphics.renderItemDecorations(font, it, itemX, itemY, "")
                    itemX += ENTRY_SIZE
                }
            }
        }
    }
}