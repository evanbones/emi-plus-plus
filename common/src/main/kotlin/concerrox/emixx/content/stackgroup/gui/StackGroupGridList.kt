package concerrox.emixx.content.stackgroup.gui

import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.content.stackgroup.EmiGroupStack
import concerrox.emixx.content.stackgroup.StackGroupManager
import concerrox.emixx.gui.GridList
import concerrox.emixx.gui.ListEntry
import concerrox.emixx.gui.components.Switch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class StackGroupGridList(
    screen: StackGroupConfigScreen, private val disabledStackGroups: MutableSet<ResourceLocation>
) : GridList<EmiGroupStack>(screen) {

    override fun getContents(): Collection<EmiGroupStack> {
        if (StackGroupManager.stackGroups.isEmpty()) {
            StackGroupManager.reload()
        }
        return StackGroupManager.stackGroups.map {
            StackGroupManager.groupToGroupStacks[it] ?: EmiGroupStack(it, listOf())
        }
    }

    override fun getEntryForContent(
        content: EmiGroupStack?,
        triple: TripleEntry<EmiGroupStack>
    ): ListEntry {
        return StackGroupEntry(content, triple, disabledStackGroups)
    }

    class StackGroupEntry(
        private val stack: EmiGroupStack?,
        triple: TripleEntry<EmiGroupStack>,
        private val disabledStackGroups: MutableSet<ResourceLocation>
    ): ListEntry(triple) {

        override val switch = Switch.Builder(Component.empty())
            .setChecked(stack != null && !disabledStackGroups.contains(stack.group.id)).apply {
                onCheckedChangeListener = Switch.OnCheckedChangeListener { _, isChecked ->
                    if (stack != null) {
                        if (isChecked) {
                            disabledStackGroups.remove(stack.group.id)
                        } else {
                            disabledStackGroups.add(stack.group.id)
                        }
                    }
                }
            }.build()
        override val children = mutableListOf<AbstractWidget>(switch)

        override fun shouldRenderSwitch(): Boolean = stack != null

        override fun getEntryTitle(): Component? =
            if (stack != null) Component.literal(stack.group.id.toString()) else null

        override fun renderEntry(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, startX: Int, startY: Int, partialTick: Float) {
            stack?.let {
                var itemX = startX
                val itemY = startY + ENTRY_SIZE
                stack.items.take(8).forEach {
                    it.render(guiGraphics, itemX, itemY, partialTick)
                    itemX += ENTRY_SIZE
                }
            }
        }

    }

}