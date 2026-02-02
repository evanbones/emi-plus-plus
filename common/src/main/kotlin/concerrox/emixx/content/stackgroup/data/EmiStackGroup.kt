package concerrox.emixx.content.stackgroup.data

import com.google.common.collect.Sets
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import concerrox.emixx.EmiPlusPlus
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper

class EmiStackGroup(
    id: ResourceLocation,
    val targets: Set<EmiIngredient>,
) : StackGroup(id) {

    companion object {
        fun parse(json: JsonElement, id: ResourceLocation): EmiStackGroup? {
            try {
                if (json !is JsonObject) throw Exception("Not a JSON object")

                if (!GsonHelper.isArrayNode(json, "contents"))
                    throw Exception("Contents are either not present or not a list")

                val targets: MutableSet<EmiIngredient> = Sets.newHashSet()
                for (element in json.getAsJsonArray("contents")) {
                    val ingredient = EmiIngredientSerializer.getDeserialized(element)
                    targets.add(ingredient)
                    targets.addAll(ingredient.emiStacks)
                }

                if (GsonHelper.isArrayNode(json, "exclusions")) {
                    for (element in json.getAsJsonArray("exclusions")) {
                        val ingredient = EmiIngredientSerializer.getDeserialized(element)
                        targets.remove(ingredient)
                        targets.removeAll(ingredient.emiStacks)
                    }
                }

                return EmiStackGroup(id, targets)
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to parse stack group $id: {}", e.message)
                return null
            }
        }
    }

    override fun match(stack: EmiIngredient): Boolean {
        return targets.any { target ->
            if (target is EmiStack && stack is EmiStack) {
                return@any target.id == stack.id
            }
            target == stack
        }
    }
}