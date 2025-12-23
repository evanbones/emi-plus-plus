package concerrox.emixx.content.stackgroup

import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.content.stackgroup.data.StackGroup
import concerrox.emixx.text
import concerrox.emixx.util.push
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class EmiGroupStack(val group: StackGroup, internal val itemsNew: List<GroupedEmiStack<EmiStack>>) : EmiStack() {

    var isExpanded = false

    @Deprecated("")
    internal val items = mutableListOf<GroupedEmiStack<EmiStack>>()

    override fun isEmpty() = false

    override fun getKey() = group

    @Deprecated("")
    override fun getId() = group.id

    override fun equals(other: Any?) = this === other

    override fun toString() = getKey().toString()

    override fun getTooltip() = listOf(
        ClientTooltipComponent.create(name.visualOrderText),
        ClientTooltipComponent.create(
            Component.literal(items.size.toString()).withStyle(ChatFormatting.DARK_GRAY)
                .append(text("stackgroup", "tooltip").withStyle(ChatFormatting.DARK_GRAY)).visualOrderText
        ),
    )

    override fun getNbt(): CompoundTag? = null

    override fun render(raw: GuiGraphics, x: Int, y: Int, delta: Float, flags: Int) {
        EmiDrawContext.wrap(raw).push {
            if (isExpanded) {
                fill(x - 1, y - 1, 1, ENTRY_SIZE, 0xFFFFFFFF.toInt())
                fill(x - 1, y - 1, ENTRY_SIZE, 1, 0xFFFFFFFF.toInt())
                fill(x + ENTRY_SIZE - 2, y - 1, 1, ENTRY_SIZE, 0xFFFFFFFF.toInt())
                fill(x - 1, y + ENTRY_SIZE - 2, ENTRY_SIZE, 1, 0xFFFFFFFF.toInt())
                fill(x, y, ENTRY_SIZE - 2, ENTRY_SIZE - 2, 0x30FFFFFF)
            }

            matrices().pushPose()
            matrices().translate(x.toFloat() + 1.6F, y.toFloat() + 1.6F, 0F)
            matrices().scale(0.8F, 0.8F, 0.8F)
            if (items.size == 1) {
                items[0].render(raw, 0, 0, delta, flags)
            } else if (items.size == 2) {
                matrices().translate(0.5F, 0F, 0F)
                items[1].render(raw, 1, -1, delta, flags)
                matrices().translate(0F, 0F, 10F)
                items[0].render(raw, -2, 1, delta, flags)
            } else if (items.size >= 3) {
                items[2].render(raw, 3, -2, delta, flags)
                matrices().translate(0F, 0F, 10F)
                items[1].render(raw, 0, 0, delta, flags)
                matrices().translate(0F, 0F, 10F)
                items[0].render(raw, -3, 2, delta, flags)
            }
            matrices().popPose()

            EmiRenderHelper.renderAmount(this, x, y, EmiPort.literal(if (isExpanded) "-" else "+"))
        }
    }

    override fun copy() = EmiGroupStack(group, listOf()).apply {
        isExpanded = this@EmiGroupStack.isExpanded
        items.addAll(this@EmiGroupStack.items)
    }

    override fun getName(): MutableComponent {
        return Component.translatable("stackgroup.emixx.${group.id.path}")
    }

    override fun getTooltipText(): MutableList<Component> {
        return mutableListOf(name, text("stackgroup", "tooltip").withStyle(ChatFormatting.DARK_GRAY))
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + isExpanded.hashCode()
        result = 31 * result + items.hashCode()
        return result
    }
}