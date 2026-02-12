package concerrox.emixx.content.stackgroup

import concerrox.emixx.content.ScreenManager.ENTRY_SIZE
import concerrox.emixx.content.stackgroup.data.StackGroup
import concerrox.emixx.text
import concerrox.emixx.util.push
import dev.emi.emi.EmiPort
import dev.emi.emi.EmiRenderHelper
import dev.emi.emi.api.stack.Comparison
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.runtime.EmiDrawContext
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.locale.Language
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

class EmiGroupStack(val group: StackGroup, internal var itemsNew: MutableList<GroupedEmiStack<EmiStack>>) : EmiStack() {

    var isExpanded = false
    private val contentLookup = HashSet<StackWrapper>()

    init {
        itemsNew.forEach {
            contentLookup.add(StackWrapper(it.realStack))
        }
    }

    /**
     * Tries to append a stack to this group.
     * @return true if the stack was added, false if it was already present.
     */
    fun append(stack: GroupedEmiStack<EmiStack>): Boolean {
        if (contentLookup.add(StackWrapper(stack.realStack))) {
            itemsNew.add(stack)
            return true
        }
        return false
    }

    private class StackWrapper(val stack: EmiStack) {
        override fun equals(other: Any?): Boolean {
            return other is StackWrapper && stack.isEqual(other.stack, Comparison.compareNbt())
        }
        override fun hashCode(): Int = stack.id.hashCode()
    }

    val items: List<GroupedEmiStack<EmiStack>> get() = itemsNew

    override fun isEmpty() = itemsNew.isEmpty()
    override fun getKey() = group
    @Deprecated("")
    override fun getId() = group.id
    override fun equals(other: Any?) = this === other
    override fun toString() = getKey().toString()

    override fun getTooltip() = listOf(
        ClientTooltipComponent.create(name.visualOrderText),
        ClientTooltipComponent.create(
            Component.literal(itemsNew.size.toString()).withStyle(ChatFormatting.DARK_GRAY)
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

            if (itemsNew.size == 1) {
                itemsNew[0].render(raw, 0, 0, delta, flags)
            } else if (itemsNew.size == 2) {
                matrices().translate(0.5F, 0F, 0F)
                itemsNew[1].render(raw, 1, -1, delta, flags)
                matrices().translate(0F, 0F, 10F)
                itemsNew[0].render(raw, -2, 1, delta, flags)
            } else if (itemsNew.size >= 3) {
                itemsNew[2].render(raw, 3, -2, delta, flags)
                matrices().translate(0F, 0F, 10F)
                itemsNew[1].render(raw, 0, 0, delta, flags)
                matrices().translate(0F, 0F, 10F)
                itemsNew[0].render(raw, -3, 2, delta, flags)
            }
            matrices().popPose()

            EmiRenderHelper.renderAmount(this, x, y, EmiPort.literal(if (isExpanded) "-" else "+"))
        }
    }

    override fun copy(): EmiGroupStack {
        val copiedList = ArrayList(itemsNew)
        return EmiGroupStack(group, copiedList).apply {
            isExpanded = this@EmiGroupStack.isExpanded
        }
    }

    override fun getName(): MutableComponent {
        val key = "stackgroup.emixx.${group.id.path}"
        if (Language.getInstance().has(key)) {
            return Component.translatable(key)
        }
        return Component.literal(
            group.id.path.split('_').joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        )
    }

    override fun getTooltipText(): MutableList<Component> {
        return mutableListOf(name, text("stackgroup", "tooltip").withStyle(ChatFormatting.DARK_GRAY))
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + isExpanded.hashCode()
        result = 31 * result + itemsNew.hashCode()
        return result
    }
}