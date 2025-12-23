package concerrox.emixx.content.stackgroup.data

import com.google.common.collect.Sets
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import concerrox.emixx.EmiPlusPlus
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.GsonHelper
import java.nio.file.Path

class EmiStackGroup(
    id: ResourceLocation,
    val targets: Set<EmiIngredient>,
) : StackGroup(id) {

    companion object {
        fun parse(json: JsonElement, fileName: Path): EmiStackGroup? {
            try {
                if (json !is JsonObject)
                    throw Exception("Not a JSON object")

                if (!GsonHelper.isStringValue(json, "id"))
                    throw Exception("ID is either not present or not a string")

                val id = ResourceLocation(json.get("id").asString)

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
                EmiPlusPlus.LOGGER.error("Failed to parse {}: {}", fileName, e)
                return null
            }
        }

        fun <T> of(tag: TagKey<T>): EmiStackGroup {
            val targets = mutableSetOf<EmiIngredient>()
            if (Minecraft.getInstance().level != null) {
                val ingredient = EmiIngredient.of(tag)
                targets.add(ingredient)
                targets.addAll(ingredient.emiStacks)
            }
            return EmiStackGroup(tag.location, targets)
        }

    }

    fun serialize(): JsonElement {
        val json = JsonObject()
        json.addProperty("id", id.toString())
        val contents = com.google.gson.JsonArray()
        for (ingredient in targets) {
            contents.add(EmiIngredientSerializer.getSerialized(ingredient))
        }
        json.add("contents", contents)
        return json
    }

    override fun match(stack: EmiIngredient): Boolean {
        return targets.any { it == stack }
    }

}