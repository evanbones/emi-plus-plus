package concerrox.emixx.gui.components

import com.mojang.blaze3d.systems.RenderSystem
import concerrox.emixx.res
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.OptionInstance
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.client.gui.components.Tooltip
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

@Environment(EnvType.CLIENT)
class Switch private constructor(x: Int, y: Int, message: Component, private var isChecked: Boolean) :
    AbstractButton(x, y, 0, 0, message) {

    companion object {
        private val SWITCH_SPRITE = res("textures/gui/switch.png")
    }

    private var isEnabled = true
    private var onCheckedChangeListener = OnCheckedChangeListener.NO_OPERATION

    init {
        width = 29
        height = 17
    }

    override fun onPress() {
        if (!isEnabled) return
        isChecked = !isChecked
        onCheckedChangeListener.onCheckedChanged(this, isChecked)
    }

    public override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, createNarrationMessage())
        if (active) {
            val component =
                Component.translatable(if (isFocused) "narration.switch.usage.focused" else "narration.switch.usage.hovered")
            narrationElementOutput.add(NarratedElementType.USAGE, component)
        }
    }

    public override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        RenderSystem.enableDepthTest()
        guiGraphics.setColor(1F, 1F, 1F, alpha)
        RenderSystem.enableBlend()
        val u = if (isChecked) width else 0
        val v = if (!isEnabled) height * 3 else if (isHovered) height else if (isFocused) height * 2 else 0
        guiGraphics.blit(SWITCH_SPRITE, x - 1, y - 1, u.toFloat(), v.toFloat(), width, height, width * 2, height * 4)
    }

    @Environment(EnvType.CLIENT)
    fun interface OnCheckedChangeListener {
        companion object {
            val NO_OPERATION = OnCheckedChangeListener { _, _ -> }
        }

        fun onCheckedChanged(switch: Switch, isChecked: Boolean)
    }

    @Environment(EnvType.CLIENT)
    class Builder(private val message: Component) {

        var x = 0
        var y = 0
        var onCheckedChangeListener = OnCheckedChangeListener.NO_OPERATION
        private var isChecked = false
        private var option: OptionInstance<Boolean>? = null
        private var tooltip: Tooltip? = null

        fun setChecked(isChecked: Boolean): Builder {
            this.isChecked = isChecked
            this.option = null
            return this
        }

        fun build(): Switch {
            val builder = this
            return Switch(builder.x, builder.y, builder.message, builder.isChecked).apply {
                onCheckedChangeListener = if (builder.option == null) {
                    builder.onCheckedChangeListener
                } else OnCheckedChangeListener { switch, isChecked ->
                    builder.option?.set(isChecked)
                    builder.onCheckedChangeListener.onCheckedChanged(switch, isChecked)
                }
                tooltip = builder.tooltip
            }
        }
    }

}