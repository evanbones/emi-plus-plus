package concerrox.emixx.content.stackgroup

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import concerrox.emixx.EmiPlusPlus
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.stackgroup.data.*
import concerrox.emixx.integration.kubejs.EmiPlusPlusKubeJSPlugin
import concerrox.emixx.integration.kubejs.RegisterStackGroupsEventJS
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.GsonHelper
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.div

object StackGroupManager {

    private val typeRegistry = mutableMapOf<String, (ResourceLocation, JsonObject) -> StackGroup?>()

    internal val stackGroups = mutableListOf<StackGroup>()
    internal var groupToGroupStacks = mapOf<StackGroup, EmiGroupStack>()

    internal val groupedEmiStacks = hashSetOf<EmiStack>()

    // Cache map to store which groups an item belongs to
    private val itemToGroups = mutableMapOf<EmiStack, MutableList<StackGroup>>()

    internal var stackGroupToGroupStacks = mapOf<StackGroup, EmiGroupStack>()

    init {
        registerType("emixx:group") { id, json -> EmiStackGroup.parse(json, id) }

        registerType("emixx:tag") { id, json ->
            val tagName = GsonHelper.getAsString(json, "tag")
            val tagKey = TagKey.create(Registries.ITEM, ResourceLocation(tagName))

            val targets = mutableSetOf<EmiIngredient>()
            if (Minecraft.getInstance().level != null) {
                val ingredient = EmiIngredient.of(tagKey)
                targets.add(ingredient)
                targets.addAll(ingredient.emiStacks)
            }
            EmiStackGroup(id, targets)
        }

        registerType("emixx:spawn_eggs") { _, _ -> SpawnEggItemGroup() }
        registerType("emixx:pressure_plates") { _, _ -> PressurePlateItemGroup() }
        registerType("emixx:minecarts") { _, _ -> MinecartItemGroup() }
        registerType("emixx:infested_blocks") { _, _ -> InfestedBlockItemGroup() }
        registerType("emixx:copper_blocks") { _, _ -> CopperBlockItemGroup() }
        registerType("emixx:banner_patterns") { _, _ -> BannerPatternItemGroup() }
        registerType("emixx:animal_armors") { _, _ -> AnimalArmorItemGroup() }
    }

    fun registerType(type: String, factory: (ResourceLocation, JsonObject) -> StackGroup?) {
        typeRegistry[type] = factory
    }

    fun getGroupPath(tag: ResourceLocation): Path {
        val name = tag.path.replace('/', '_')
        val filename = "${tag.namespace}_$name.json"
        return EmiPlusPlusConfig.CONFIG_DIRECTORY_PATH / "stack_groups" / filename
    }

    fun hasGroup(tag: ResourceLocation): Boolean {
        return stackGroups.any { it.id == tag }
    }

    fun toggleTagGroup(tag: ResourceLocation) {
        val isActive = hasGroup(tag)
        val file = getGroupPath(tag)

        if (isActive) {
            saveGroupConfig(tag, false)
        } else {
            saveGroupConfig(tag, true)
        }
        reload()
    }

    private fun saveGroupConfig(tag: ResourceLocation, enabled: Boolean) {
        val file = getGroupPath(tag)
        val directory = file.parent

        val json = JsonObject()
        if (enabled) {
            json.addProperty("type", "emixx:tag")
            json.addProperty("id", tag.toString())
            json.addProperty("tag", tag.toString())
            json.addProperty("enabled", true)
        } else {
            json.addProperty("id", tag.toString())
            json.addProperty("enabled", false)
        }

        try {
            Files.createDirectories(directory)
            Files.newBufferedWriter(file).use { writer ->
                com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json, writer)
            }
        } catch (e: Exception) {
            EmiPlusPlus.LOGGER.error("Failed to save stack group", e)
        }
    }

    fun deleteTagGroup(tag: ResourceLocation) {
        try {
            val file = getGroupPath(tag)
            Files.deleteIfExists(file)
            reload()
        } catch (e: Exception) {
            EmiPlusPlus.LOGGER.error("Failed to delete stack group", e)
        }
    }

    fun reload() {
        stackGroups.clear()

        if (!EmiPlusPlusConfig.enableStackGroups.get()) return

        val loadedGroups = mutableMapOf<ResourceLocation, StackGroup>()

        val resourceManager = Minecraft.getInstance().resourceManager
        val resources = resourceManager.listResources("stack_groups") { it.path.endsWith(".json") }

        for ((location, resource) in resources) {
            val namespace = location.namespace
            val path = location.path.removePrefix("stack_groups/").removeSuffix(".json")
            val id = ResourceLocation(namespace, path)
            loadGroup(id, resource.openAsReader().use { JsonParser.parseReader(it).asJsonObject }, loadedGroups)
        }

        val configDir = EmiPlusPlusConfig.CONFIG_DIRECTORY_PATH / "stack_groups"
        if (Files.exists(configDir)) {
            try {
                Files.list(configDir).forEach { path ->
                    if (path.toString().endsWith(".json")) {
                        try {
                            val json = Files.newBufferedReader(path).use { JsonParser.parseReader(it).asJsonObject }
                            var idString = if (json.has("id")) json.get("id").asString else null
                            if (idString == null && json.has("tag")) idString = json.get("tag").asString

                            if (idString != null) {
                                val id = ResourceLocation(idString)
                                loadGroup(id, json, loadedGroups)
                            }
                        } catch (e: Exception) {
                            EmiPlusPlus.LOGGER.error("Failed to load user stack group $path", e)
                        }
                    }
                }
            } catch (e: Exception) {
                EmiPlusPlus.LOGGER.error("Failed to list user stack groups", e)
            }
        }

        stackGroups.addAll(loadedGroups.values.sortedBy { it.id.toString() })

        if (isKubeJSLoaded()) {
            EmiPlusPlusKubeJSPlugin.REGISTER_GROUPS.post(RegisterStackGroupsEventJS())
        }
    }

    private fun loadGroup(
        id: ResourceLocation,
        json: JsonObject,
        loadedGroups: MutableMap<ResourceLocation, StackGroup>
    ) {
        try {
            val enabled = GsonHelper.getAsBoolean(json, "enabled", true)
            if (!enabled) {
                loadedGroups.remove(id)
                return
            }

            val type = GsonHelper.getAsString(json, "type", "emixx:group")
            val factory = typeRegistry[type]

            if (factory != null) {
                val group = factory(id, json)
                if (group != null) {
                    loadedGroups[id] = group
                }
            } else {
                EmiPlusPlus.LOGGER.error("Unknown stack group type '$type' for $id")
            }
        } catch (e: Exception) {
            EmiPlusPlus.LOGGER.error("Failed to parse stack group $id", e)
        }
    }

    internal fun buildGroupedStacks(source: List<EmiStack>): List<EmiStack> {
        val result = mutableListOf<EmiStack>()
        val addedStackGroups = mutableSetOf<StackGroup>()

        val localGroupToGroupStacks = stackGroups.associateWith { group -> EmiGroupStack(group, mutableListOf()) }

        for (emiStack in source) {
            val groups = itemToGroups[emiStack]

            if (groups == null) {
                result += emiStack
                continue
            }

            for (stackGroup in groups) {
                val groupStack = localGroupToGroupStacks[stackGroup]!!
                groupStack.itemsNew += GroupedEmiStack(emiStack, stackGroup)

                if (stackGroup !in addedStackGroups) {
                    addedStackGroups += stackGroup
                    if (stackGroup.isEnabled) result += groupStack
                }
            }
        }
        groupToGroupStacks = localGroupToGroupStacks
        return result
    }

    internal fun buildGroupedEmiStacksAndStackGroupToContents(source: List<EmiStack>) {
        groupedEmiStacks.clear()
        itemToGroups.clear()

        val stackGroupToGroupStacks = stackGroups.associateWith { EmiGroupStack(it, mutableListOf()) }

        val idToStacks = mutableMapOf<ResourceLocation, MutableList<EmiStack>>()
        for (stack in source) {
            idToStacks.computeIfAbsent(stack.id) { mutableListOf() }.add(stack)
        }

        val (fastGroups, slowGroups) = stackGroups.partition { it.getSafeMatchingIds() != null }

        for (group in fastGroups) {
            val ids = group.getSafeMatchingIds()!!
            for (id in ids) {
                val stacks = idToStacks[id] ?: continue
                for (stack in stacks) {
                    registerMatch(group, stack, stackGroupToGroupStacks)
                }
            }
        }

        if (slowGroups.isNotEmpty()) {
            for (stack in source) {
                for (group in slowGroups) {
                    if (group.match(stack)) {
                        registerMatch(group, stack, stackGroupToGroupStacks)
                    }
                }
            }
        }

        this.stackGroupToGroupStacks = stackGroupToGroupStacks
    }

    private fun registerMatch(
        group: StackGroup,
        stack: EmiStack,
        groupStacksMap: Map<StackGroup, EmiGroupStack>
    ) {
        if (group.isEnabled) {
            groupedEmiStacks.add(stack)
            itemToGroups.computeIfAbsent(stack) { mutableListOf() }.add(group)
        }
        groupStacksMap[group]!!.itemsNew.add(GroupedEmiStack(stack, group))
    }

    private fun isKubeJSLoaded(): Boolean {
        return try {
            Class.forName("dev.latvian.mods.kubejs.KubeJSPlugin")
            true
        } catch (_: Throwable) {
            false
        }
    }
}