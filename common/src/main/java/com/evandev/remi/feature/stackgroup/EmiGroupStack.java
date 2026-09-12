package com.evandev.remi.feature.stackgroup;

import com.evandev.ReliableEmi;
import com.evandev.remi.feature.stackgroup.data.EmiStackGroup;
import com.evandev.remi.feature.stackgroup.data.StackGroup;
import com.evandev.remi.integration.emi.ScreenManager;
import com.evandev.remi.integration.sodium.SodiumCompat;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class EmiGroupStack extends EmiStack implements StackBatcher.Batchable {
    private static final ResourceLocation EXPANDED_TEXTURE = ReliableEmi.res("textures/gui/stack_group_expanded.png");
    private static final ResourceLocation EXPANDED_INDICATOR_TEXTURE = ReliableEmi.res("textures/gui/stack_group_indicator_expanded.png");
    private static final ResourceLocation COLLAPSED_INDICATOR_TEXTURE = ReliableEmi.res("textures/gui/stack_group_indicator_collapsed.png");
    private static int visibilityVersion = 0;

    public final StackGroup group;
    public boolean isExpanded = false;
    public List<GroupedEmiStack<EmiStack>> itemsNew;
    private HashSet<StackWrapper> contentLookup;

    private List<GroupedEmiStack<EmiStack>> visibleItems;
    private int visibleItemsVersion = -1;

    private boolean unbatchable;
    private boolean batchStateDirty = true;
    private boolean batchEditMode;
    private int batchStateVersion = -1;
    private boolean batchable;
    private boolean sideLit;
    private List<TextureAtlasSprite> batchedSprites;

    public EmiGroupStack(StackGroup group, List<GroupedEmiStack<EmiStack>> items) {
        this.group = group;
        this.itemsNew = items;
    }

    public static void onStackFilterChanged() {
        visibilityVersion++;
    }

    private static void collectSprites(EmiStack stack, Set<TextureAtlasSprite> out) {
        ItemStack is = stack.getItemStack();
        if (is.isEmpty()) return;
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(is, null, null, 0);
        for (BakedQuad quad : EmiPort.getQuads(model)) {
            if (quad != null) out.add(quad.getSprite());
        }
    }

    /**
     * Must be called after any direct mutation of {@link #itemsNew}.
     */
    public void invalidateCaches() {
        visibleItems = null;
        batchStateDirty = true;
    }

    public boolean append(GroupedEmiStack<EmiStack> stack) {
        if (contentLookup == null) {
            contentLookup = new HashSet<>();
            for (var item : itemsNew) contentLookup.add(new StackWrapper(item.realStack));
        }
        if (contentLookup.add(new StackWrapper(stack.realStack))) {
            itemsNew.add(stack);
            invalidateCaches();
            return true;
        }
        return false;
    }

    /**
     * Returns the non-hidden items. Callers must not mutate the returned list
     */
    public List<GroupedEmiStack<EmiStack>> getItems() {
        if (EmiConfig.editMode) return itemsNew;
        if (visibleItems == null || visibleItemsVersion != visibilityVersion) {
            List<GroupedEmiStack<EmiStack>> result = new ArrayList<>(itemsNew.size());
            for (var item : itemsNew) {
                if (!EmiHidden.isHidden(item.realStack)) result.add(item);
            }
            visibleItems = result;
            visibleItemsVersion = visibilityVersion;
        }
        return visibleItems;
    }

    @Override
    public boolean isEmpty() {
        return getItems().isEmpty();
    }

    @Override
    public Object getKey() {
        return group;
    }

    @Override
    public ResourceLocation getId() {
        return group.getId();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public String toString() {
        return getKey().toString();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        return List.of(
                ClientTooltipComponent.create(getName().getVisualOrderText()),
                ClientTooltipComponent.create(
                        Component.literal(String.valueOf(getItems().size()))
                                .withStyle(ChatFormatting.DARK_GRAY)
                                .append(ReliableEmi.text("stackgroup", "tooltip").withStyle(ChatFormatting.DARK_GRAY))
                                .getVisualOrderText()
                )
        );
    }

    @Override
    public DataComponentPatch getComponentChanges() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    public void render(GuiGraphics raw, int x, int y, float delta, int flags) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);
        int es = ScreenManager.ENTRY_SIZE;

        if (isExpanded) {
            RenderSystem.enableBlend();
            context.drawTexture(EXPANDED_TEXTURE, x - 1, y - 1, 0, 0, 0, es, es, es, es);
            RenderSystem.disableBlend();
        }

        if (batchedSprites != null && (flags & RENDER_ICON) == 0) {
            for (TextureAtlasSprite sprite : batchedSprites) {
                SodiumCompat.markSpriteActive(sprite);
            }
        }

        List<GroupedEmiStack<EmiStack>> items = getItems();
        if (!items.isEmpty()) {
            context.push();
            context.matrices().translate(x + 1.6F, y + 1.6F, 0F);
            context.matrices().scale(0.8F, 0.8F, 0.8F);

            if (items.size() == 1) {
                items.getFirst().render(raw, 0, 0, delta, flags);
            } else if (items.size() == 2) {
                context.matrices().translate(0.5F, 0F, 0F);
                items.get(1).render(raw, 1, -1, delta, flags);
                context.matrices().translate(0F, 0F, 10F);
                items.getFirst().render(raw, -2, 1, delta, flags);
            } else if (items.size() >= 3) {
                items.get(2).render(raw, 3, -2, delta, flags);
                context.matrices().translate(0F, 0F, 10F);
                items.get(1).render(raw, 0, 0, delta, flags);
                context.matrices().translate(0F, 0F, 10F);
                items.get(0).render(raw, -3, 2, delta, flags);
            }
            context.pop();
        }

        RenderSystem.enableBlend();
        context.drawTexture(isExpanded ? EXPANDED_INDICATOR_TEXTURE : COLLAPSED_INDICATOR_TEXTURE, x - 1, y - 1, 200, 0, 0, es, es, es, es);
        RenderSystem.disableBlend();
    }

    @Override
    public boolean isSideLit() {
        refreshBatchState();
        return sideLit;
    }

    @Override
    public boolean isUnbatchable() {
        if (unbatchable) return true;
        refreshBatchState();
        return !batchable;
    }

    @Override
    public void setUnbatchable() {
        unbatchable = true;
    }

    @Override
    public void renderForBatch(MultiBufferSource vcp, GuiGraphics draw, int x, int y, int z, float delta) {
        List<GroupedEmiStack<EmiStack>> items = getItems();
        if (items.isEmpty()) return;
        EmiDrawContext context = EmiDrawContext.wrap(draw);
        context.push();
        context.matrices().translate(x + 1.6F, y + 1.6F, 0F);
        context.matrices().scale(0.8F, 0.8F, 0.8F);

        if (items.size() == 1) {
            items.getFirst().renderForBatch(vcp, draw, 0, 0, z, delta);
        } else if (items.size() == 2) {
            context.matrices().translate(0.5F, 0F, 0F);
            items.get(1).renderForBatch(vcp, draw, 1, -1, z, delta);
            items.getFirst().renderForBatch(vcp, draw, -2, 1, z + 10, delta);
        } else {
            items.get(2).renderForBatch(vcp, draw, 3, -2, z, delta);
            items.get(1).renderForBatch(vcp, draw, 0, 0, z + 10, delta);
            items.get(0).renderForBatch(vcp, draw, -3, 2, z + 20, delta);
        }
        context.pop();

        if (SodiumCompat.isLoaded()) {
            Set<TextureAtlasSprite> sprites = new LinkedHashSet<>();
            int count = Math.min(3, items.size());
            for (int i = 0; i < count; i++) {
                collectSprites(items.get(i).realStack, sprites);
            }
            batchedSprites = new ArrayList<>(sprites);
        }
    }

    private void refreshBatchState() {
        if (!batchStateDirty && batchStateVersion == visibilityVersion && batchEditMode == EmiConfig.editMode) return;
        batchStateDirty = false;
        batchStateVersion = visibilityVersion;
        batchEditMode = EmiConfig.editMode;
        batchable = false;
        sideLit = false;

        List<GroupedEmiStack<EmiStack>> items = getItems();
        int count = Math.min(3, items.size());
        for (int i = 0; i < count; i++) {
            GroupedEmiStack<EmiStack> item = items.get(i);
            if (item.isUnbatchable()) return;
            if (i == 0) {
                sideLit = item.isSideLit();
            } else if (item.isSideLit() != sideLit) {
                return;
            }
        }
        batchable = count > 0;
    }

    @Override
    public EmiGroupStack copy() {
        EmiGroupStack copy = new EmiGroupStack(group, new ArrayList<>(itemsNew));
        copy.isExpanded = this.isExpanded;
        return copy;
    }

    @Override
    public MutableComponent getName() {
        if (group.name != null) return (MutableComponent) group.name;

        if (group instanceof EmiStackGroup esg && esg.getTagKey() != null) {
            TagKey<?> tk = esg.getTagKey();
            String regName = tk.registry().location().getPath().replace('/', '.');
            String ns = tk.location().getNamespace();
            String path = tk.location().getPath().replace('/', '.');

            String k1 = "tag." + regName + "." + ns + "." + path;
            if (Language.getInstance().has(k1)) return Component.translatable(k1);

            String k2 = "tag." + tk.registry().location().getNamespace() + "." + regName + "." + ns + "." + path;
            if (Language.getInstance().has(k2)) return Component.translatable(k2);

            String k3 = "tag." + ns + "." + path;
            if (Language.getInstance().has(k3)) return Component.translatable(k3);
        }

        String key = "stackgroup." + ReliableEmi.MOD_ID + "." + group.getId().getPath();
        if (Language.getInstance().has(key)) return Component.translatable(key);

        String fallbackKey = "stackgroup.emixx." + group.getId().getPath();
        if (Language.getInstance().has(fallbackKey)) return Component.translatable(fallbackKey);

        String itemTagKey = "tag.item." + group.getId().getNamespace() + "." + group.getId().getPath().replace('/', '.');
        if (Language.getInstance().has(itemTagKey)) return Component.translatable(itemTagKey);

        String blockTagKey = "tag.block." + group.getId().getNamespace() + "." + group.getId().getPath().replace('/', '.');
        if (Language.getInstance().has(blockTagKey)) return Component.translatable(blockTagKey);

        String entityTagKey = "tag.entity_type." + group.getId().getNamespace() + "." + group.getId().getPath().replace('/', '.');
        if (Language.getInstance().has(entityTagKey)) return Component.translatable(entityTagKey);

        String fluidTagKey = "tag.fluid." + group.getId().getNamespace() + "." + group.getId().getPath().replace('/', '.');
        if (Language.getInstance().has(fluidTagKey)) return Component.translatable(fluidTagKey);

        String path = group.getId().getPath();
        for (String pfx : List.of("minecraft_item_", "minecraft_block_", "minecraft_entity_type_", "minecraft_fluid_")) {
            if (path.startsWith(pfx)) {
                path = path.substring(pfx.length());
                break;
            }
        }

        String[] parts = path.split("[_/]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            if (!part.isEmpty()) sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return Component.literal(sb.toString());
    }

    @Override
    public List<Component> getTooltipText() {
        return List.of(getName(), ReliableEmi.text("stackgroup", "tooltip").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public int hashCode() {
        return group.hashCode();
    }

    private record StackWrapper(EmiStack stack) {
        @Override
        public boolean equals(Object o) {
            return o instanceof StackWrapper(EmiStack stack1) && stack.isEqual(stack1, Comparison.compareComponents());
        }

        @Override
        public int hashCode() {
            return stack.getId().hashCode();
        }
    }
}
