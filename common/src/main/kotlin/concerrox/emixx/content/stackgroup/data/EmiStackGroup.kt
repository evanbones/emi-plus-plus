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

        private fun normalizeIngredientJson(element: JsonElement): JsonElement {
            return if (element.isJsonPrimitive) {
                JsonObject().apply {
                    addProperty("type", "item")
                    addProperty("id", element.asString)
                }
            } else {
                element
            }
        }

        private fun deserialize(element: JsonElement): EmiIngredient {
            return EmiIngredientSerializer.getDeserialized(
                normalizeIngredientJson(element)
            )
        }

        private fun MutableSet<EmiIngredient>.addIngredientWithStacks(ingredient: EmiIngredient) {
            add(ingredient)
            addAll(ingredient.emiStacks)
        }

        private fun MutableSet<EmiIngredient>.removeIngredientWithStacks(ingredient: EmiIngredient) {
            remove(ingredient)
            removeAll(ingredient.emiStacks)
        }

        fun parse(json: JsonElement, id: ResourceLocation): EmiStackGroup? {
            return try {
                if (json !is JsonObject) error("Not a JSON object")
                if (!GsonHelper.isArrayNode(json, "contents"))
                    error("Contents are either not present or not a list")

                val targets = Sets.newHashSet<EmiIngredient>()

                for (element in json.getAsJsonArray("contents")) {
                    targets.addIngredientWithStacks(deserialize(element))
                }

                if (GsonHelper.isArrayNode(json, "exclusions")) {
                    for (element in json.getAsJsonArray("exclusions")) {
                        targets.removeIngredientWithStacks(deserialize(element))
                    }
                }

                EmiStackGroup(id, targets)
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to parse stack group $id: {}", e.message)
                null
            }
        }
    }

    override fun match(stack: EmiIngredient): Boolean {
        return targets.any { target ->
            if (target is EmiStack && stack is EmiStack) {
                target.id == stack.id
            } else {
                target == stack
            }
        }
    }
}
