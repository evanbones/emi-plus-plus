package concerrox.emixx.content.stackgroup

import com.google.gson.JsonParser
import concerrox.emixx.config.EmiPlusPlusConfig
import concerrox.emixx.content.stackgroup.data.*
import concerrox.emixx.registry.ModTags
import dev.emi.emi.api.stack.EmiStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import concerrox.emixx.integration.kubejs.EmiPlusPlusKubeJSPlugin
import concerrox.emixx.integration.kubejs.RegisterStackGroupsEventJS
import kotlin.io.path.*

object StackGroupManager {

    private val STACK_GROUP_DIRECTORY_PATH = EmiPlusPlusConfig.CONFIG_DIRECTORY_PATH / "groups"
    private val defaultStackGroups get() = listOf(
        SimpleItemGroup("enchanted_books", listOf(Ingredient.of(Items.ENCHANTED_BOOK))),
        SimpleItemGroup(
            "potions", listOf(Ingredient.of(Items.POTION))
        ),
        SimpleItemGroup("splash_potions", listOf(Ingredient.of(Items.SPLASH_POTION))),
        SimpleItemGroup("lingering_potions", listOf(Ingredient.of(Items.LINGERING_POTION))),
        SimpleItemGroup("tipped_arrows", listOf(Ingredient.of(Items.TIPPED_ARROW))),

        EmiStackGroup.of(ModTags.Item.MUSIC_DISCS),

        EmiStackGroup.of(ItemTags.SHOVELS),
        EmiStackGroup.of(ItemTags.PICKAXES),
        EmiStackGroup.of(ItemTags.AXES),
        EmiStackGroup.of(ItemTags.SWORDS),
        EmiStackGroup.of(ItemTags.HOES),
        AnimalArmorItemGroup(),

        InfestedBlockItemGroup(),
        EmiStackGroup.of(ModTags.Item.RAW_MATERIALS),
        EmiStackGroup.of(ModTags.Item.FOODS),
        EmiStackGroup.of(ItemTags.PLANKS),
        EmiStackGroup.of(ItemTags.STAIRS),
        EmiStackGroup.of(ItemTags.SLABS),
        EmiStackGroup.of(ItemTags.FENCES),
        EmiStackGroup.of(ItemTags.FENCE_GATES),
        EmiStackGroup.of(ItemTags.DOORS),
        EmiStackGroup.of(ItemTags.TRAPDOORS),
        PressurePlateItemGroup(),
        MinecartItemGroup(),
        EmiStackGroup.of(ItemTags.RAILS),
        EmiStackGroup.of(ModTags.Item.DYES),
        EmiStackGroup.of(ItemTags.BUTTONS),
        EmiStackGroup.of(ItemTags.SAPLINGS),
        EmiStackGroup.of(ModTags.Item.ORES),
        EmiStackGroup.of(ModTags.Item.SEEDS),
        EmiStackGroup.of(ItemTags.LOGS),
        EmiStackGroup.of(ItemTags.LEAVES),
        EmiStackGroup.of(ItemTags.SIGNS),
        EmiStackGroup.of(ItemTags.HANGING_SIGNS),
        EmiStackGroup.of(ItemTags.BOATS),
        EmiStackGroup.of(ItemTags.WALLS),
        EmiStackGroup.of(ModTags.Item.GLASS_BLOCKS),
        EmiStackGroup.of(ModTags.Item.GLASS_PANES),
        EmiStackGroup.of(ItemTags.WOOL),
        EmiStackGroup.of(ItemTags.FLOWERS),
        EmiStackGroup.of(ItemTags.TERRACOTTA),
        EmiStackGroup.of(ItemTags.WOOL_CARPETS),
        SimpleItemGroup("goat_horns", listOf(Ingredient.of(Items.GOAT_HORN))),
        SimpleItemGroup("suspicious_stews", listOf(Ingredient.of(Items.SUSPICIOUS_STEW))),
        EmiStackGroup.of(ItemTags.BANNERS),
        EmiStackGroup.of(ModTags.Item.SHULKER_BOXES),
        EmiStackGroup.of(ModTags.Item.CONCRETES),
        EmiStackGroup.of(ModTags.Item.CONCRETE_POWDERS),
        EmiStackGroup.of(ModTags.Item.GLAZED_TERRACOTTAS),
        EmiStackGroup.of(ItemTags.BEDS),
        EmiStackGroup.of(ItemTags.CANDLES),
        SimpleItemGroup("paintings", listOf(Ingredient.of(Items.PAINTING))),
        EmiStackGroup.of(ItemTags.DECORATED_POT_SHERDS),
        EmiStackGroup.of(ItemTags.TRIM_TEMPLATES),
        EmiStackGroup.of(ModTags.Item.BUCKETS),
        EmiStackGroup.of(ModTags.Item.DUSTS),
        EmiStackGroup.of(ModTags.Item.NUGGETS),
        EmiStackGroup.of(ModTags.Item.INGOTS),
        BannerPatternItemGroup(),
        SpawnEggItemGroup(),
        CopperBlockItemGroup(),

        // Mekanism
        EmiStackGroup.of(ModTags.Item.MEKANISM_UNITS),
        EmiStackGroup.of(ModTags.Item.MEKANISM_DIRTY_DUSTS),
        EmiStackGroup.of(ModTags.Item.MEKANISM_CLUMPS),
        EmiStackGroup.of(ModTags.Item.MEKANISM_CRYSTALS),
        EmiStackGroup.of(ModTags.Item.MEKANISM_ENRICHED),
        EmiStackGroup.of(ModTags.Item.MEKANISM_SHARDS),
    )

    private val stackGroups = mutableListOf<StackGroup>()
    internal var groupToGroupStacks = mapOf<StackGroup, EmiGroupStack>()

    internal fun reload() {
        stackGroups.clear()
        stackGroups.addAll(defaultStackGroups)
        // TODO: check this
        stackGroups.forEach {
            it.isEnabled = true
        }
        STACK_GROUP_DIRECTORY_PATH.createDirectories().listDirectoryEntries("*.json").forEach {
            val json = JsonParser.parseString(it.readText())
            val result = EmiStackGroup.parse(json, it.fileName)
            if (result != null) stackGroups += result
        }
        EmiPlusPlusConfig.disabledStackGroups.get().forEach { disabledStackGroupId ->
            stackGroups.firstOrNull { it.id == ResourceLocation(disabledStackGroupId) }?.isEnabled = false
        }
        val isKubeJSLoaded = try {
            Class.forName("dev.latvian.mods.kubejs.KubeJSPlugin")
            true
        } catch (_: Throwable) {
            false
        }

        if (isKubeJSLoaded) {
            EmiPlusPlusKubeJSPlugin.REGISTER_GROUPS.post(
                RegisterStackGroupsEventJS()
            )
        }
    }

    internal fun buildGroupedStacks(source: List<EmiStack>): List<EmiStack> {
        val result = mutableListOf<EmiStack>()
        val addedStackGroups = mutableSetOf<StackGroup>()

        val localGroupToGroupStacks = stackGroups.associateWith { group -> EmiGroupStack(group, listOf()) }
        val groupsToCheck = stackGroups.toList()

        for (emiStack in source) {
            if (emiStack !in groupedEmiStacks) {
                result += emiStack
                continue
            }
            for (stackGroup in groupsToCheck) {
                val groupStack = localGroupToGroupStacks[stackGroup]!!
                if (stackGroup.match(emiStack)) {
                    groupStack.items += GroupedEmiStack(emiStack, stackGroup)
                    if (stackGroup !in addedStackGroups) {
                        addedStackGroups += stackGroup
                        if (stackGroup.isEnabled) result += groupStack
                    }
                }
            }
        }

        groupToGroupStacks = localGroupToGroupStacks
        return result
    }

    internal val groupedEmiStacks = hashSetOf<EmiStack>()
    internal var stackGroupToGroupStacks = mapOf<StackGroup, EmiGroupStack>()

    internal fun buildGroupedEmiStacksAndStackGroupToContents(source: List<EmiStack>) {
        groupedEmiStacks.clear()
        val stackGroupToGroupStacks = stackGroups.associateWith { EmiGroupStack(it, listOf()) }
        for (emiStack in source) {
            for (stackGroup in stackGroups) {
                if (!stackGroup.match(emiStack)) continue
                if (stackGroup.isEnabled) groupedEmiStacks.add(emiStack)
                stackGroupToGroupStacks[stackGroup]!!.items += GroupedEmiStack(emiStack, stackGroup)
            }
        }
        this.stackGroupToGroupStacks = stackGroupToGroupStacks
    }

    @Deprecated("Will be removed after the refactor of stack groups")
    internal fun create(tag: TagKey<Item>) {
        val filename = tag.location.toString().replace(":", "__").replace("/", "__") + ".json"
        (STACK_GROUP_DIRECTORY_PATH / filename).writeText(EmiStackGroup.of(tag).serialize().toString())
    }

}