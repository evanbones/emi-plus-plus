package dev.emi.emi.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiAlias;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
import dev.emi.emi.screen.EmiScreenManager;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.searchtree.SuffixArray;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EmiSearch {
    public static final Pattern TOKENS = Pattern.compile("-?[@#$]?(/(\\\\.|[^\\\\/])+/|\"(\\.|[^\"])+\"|[^\\s|]+|\\||&)");
    private static volatile SearchWorker currentWorker = null;
    public static volatile Thread searchThread = null;
    public static volatile List<? extends EmiIngredient> stacks;
    public static volatile CompiledQuery compiledQuery;
    public static Set<EmiStack> bakedStacks;
    public static SuffixArray<SearchStack> names;
    public static SuffixArray<SearchStack> tooltips;
    public static SuffixArray<SearchStack> mods;
    public static SuffixArray<EmiStack> aliases;

    public EmiSearch() {
    }

    public static void bake() {
        SuffixArray<SearchStack> names = new SuffixArray<>();
        SuffixArray<SearchStack> tooltips = new SuffixArray<>();
        SuffixArray<SearchStack> mods = new SuffixArray<>();
        SuffixArray<EmiStack> aliases = new SuffixArray<>();
        Set<EmiStack> bakedStacks = Sets.newIdentityHashSet();
        boolean old = EmiConfig.appendItemModId;
        EmiConfig.appendItemModId = false;

        for (EmiStack stack : EmiStackList.stacks) {
            try {
                SearchStack searchStack = new SearchStack(stack);
                bakedStacks.add(stack);
                Component name = NameQuery.getText(stack);
                if (name != null) {
                    names.add(searchStack, name.getString().toLowerCase());
                }

                List<Component> tooltip = stack.getTooltipText();
                if (tooltip != null) {
                    for (int i = 1; i < tooltip.size(); ++i) {
                        Component text = tooltip.get(i);
                        if (text != null) {
                            tooltips.add(searchStack, text.getString().toLowerCase());
                        }
                    }
                }

                ResourceLocation id = stack.getId();
                if (id != null) {
                    mods.add(searchStack, EmiUtil.getModName(id.getNamespace()).toLowerCase());
                    mods.add(searchStack, id.getNamespace().toLowerCase());
                    names.add(searchStack, id.getPath().toLowerCase());
                }

                if (stack.getItemStack().getItem() == Items.ENCHANTED_BOOK) {
                    ListTag storedEnchantments = EnchantedBookItem.getEnchantments(stack.getItemStack());
                    Map<Enchantment, Integer> enchants = EnchantmentHelper.deserializeEnchantments(storedEnchantments);

                    for (Enchantment e : enchants.keySet()) {
                        ResourceLocation eid = EmiPort.getEnchantmentRegistry().getKey(e);
                        if (eid != null && !eid.getNamespace().equals("minecraft")) {
                            mods.add(searchStack, EmiUtil.getModName(eid.getNamespace()).toLowerCase());
                        }
                    }
                }
            } catch (Exception e) {
                EmiLog.error("EMI caught an exception while baking search for " + stack);
            }
        }

        for (Supplier<EmiAlias> supplier : EmiData.aliases) {
            EmiAlias alias = supplier.get();

            for (String key : alias.keys()) {
                if (!I18n.exists(key)) {
                    EmiReloadLog.warn("Untranslated alias " + key);
                }

                String text = I18n.get(key).toLowerCase();

                for (EmiIngredient ing : alias.stacks()) {
                    for (EmiStack stack : ing.getEmiStacks()) {
                        aliases.add(stack.copy().comparison(Comparison.compareNbt()), text);
                    }
                }
            }
        }

        EmiConfig.appendItemModId = old;
        names.generate();
        tooltips.generate();
        mods.generate();
        aliases.generate();
        EmiSearch.names = names;
        EmiSearch.tooltips = tooltips;
        EmiSearch.mods = mods;
        EmiSearch.aliases = aliases;
        EmiSearch.bakedStacks = bakedStacks;
    }

    public static void update() {
        search(EmiScreenManager.search.getValue());
    }

    public static void search(String query) {
        synchronized(EmiSearch.class) {
            SearchWorker worker = new SearchWorker(query, EmiScreenManager.getSearchSource());
            currentWorker = worker;
            searchThread = new Thread(worker);
            searchThread.setDaemon(true);
            searchThread.start();
        }
    }

    public static void apply(SearchWorker worker, List<? extends EmiIngredient> stacks) {
        synchronized(EmiSearch.class) {
            if (worker == currentWorker) {
                EmiSearch.stacks = stacks;
                currentWorker = null;
                searchThread = null;
            }
        }
    }

    static {
        stacks = EmiStackList.stacks;
    }

    public static class CompiledQuery {
        public final Query fullQuery;

        public CompiledQuery(String query) {
            List<Query> full = Lists.newArrayList();
            List<Query> queries = Lists.newArrayList();
            Matcher matcher = EmiSearch.TOKENS.matcher(query);

            while(matcher.find()) {
                String q = matcher.group();
                boolean negated = q.startsWith("-");
                if (negated) {
                    q = q.substring(1);
                }

                if (!q.isEmpty() && !q.equals("&")) {
                    if (q.equals("|")) {
                        if (!queries.isEmpty()) {
                            full.add(new LogicalAndQuery(queries));
                            queries = Lists.newArrayList();
                        }
                    } else {
                        QueryType type = QueryType.fromString(q);
                        Function<String, Query> constructor = type.queryConstructor;
                        Function<String, Query> regexConstructor = type.regexQueryConstructor;
                        if (type == QueryType.DEFAULT) {
                            List<Function<String, Query>> constructors = Lists.newArrayList();
                            List<Function<String, Query>> regexConstructors = Lists.newArrayList();
                            constructors.add(constructor);
                            regexConstructors.add(regexConstructor);
                            if (EmiConfig.searchTooltipByDefault) {
                                constructors.add(QueryType.TOOLTIP.queryConstructor);
                                regexConstructors.add(QueryType.TOOLTIP.regexQueryConstructor);
                            }

                            if (EmiConfig.searchModNameByDefault) {
                                constructors.add(QueryType.MOD.queryConstructor);
                                regexConstructors.add(QueryType.MOD.regexQueryConstructor);
                            }

                            if (EmiConfig.searchTagsByDefault) {
                                constructors.add(QueryType.TAG.queryConstructor);
                                regexConstructors.add(QueryType.TAG.regexQueryConstructor);
                            }

                            constructors.add(AliasQuery::new);
                            constructor = (name) -> new LogicalOrQuery(constructors.stream().map((c) -> c.apply(name)).toList());
                            regexConstructor = (name) -> new LogicalOrQuery(regexConstructors.stream().map((c) -> c.apply(name)).toList());
                        }

                        addQuery(q.substring(type.prefix.length()), negated, queries, constructor, regexConstructor);
                    }
                }
            }

            if (!queries.isEmpty()) {
                full.add(new LogicalAndQuery(queries));
            }

            if (!full.isEmpty()) {
                this.fullQuery = new LogicalOrQuery(full);
            } else {
                this.fullQuery = null;
            }
        }

        public boolean isEmpty() {
            return this.fullQuery == null;
        }

        public boolean test(EmiStack stack) {
            if (this.fullQuery == null) {
                return true;
            } else {
                return EmiSearch.bakedStacks.contains(stack) ? this.fullQuery.matches(stack) : this.fullQuery.matchesUnbaked(stack);
            }
        }

        private static void addQuery(String s, boolean negated, List<Query> queries, Function<String, Query> normal, Function<String, Query> regex) {
            Query q;
            if (s.length() > 1 && s.startsWith("/") && s.endsWith("/")) {
                q = regex.apply(s.substring(1, s.length() - 1));
            } else if (s.length() > 1 && s.startsWith("\"") && s.endsWith("\"")) {
                q = normal.apply(s.substring(1, s.length() - 1));
            } else {
                q = normal.apply(s);
            }

            q.negated = negated;
            queries.add(q);
        }
    }

    public static class SearchWorker implements Runnable {
        private final String query;
        private final List<? extends EmiIngredient> source;

        public SearchWorker(String query, List<? extends EmiIngredient> source) {
            this.query = query;
            this.source = source;
        }

        public void run() {
            try {
                CompiledQuery compiled = new CompiledQuery(this.query);
                EmiSearch.compiledQuery = compiled;
                if (compiled.isEmpty()) {
                    EmiSearch.apply(this, this.source);
                    return;
                }

                List<EmiIngredient> stacks = Lists.newArrayList();
                int processed = 0;

                for (EmiIngredient stack : this.source) {
                    if (processed++ >= 1024) {
                        processed = 0;
                        if (this != EmiSearch.currentWorker) {
                            return;
                        }
                    }

                    List<EmiStack> ess = stack.getEmiStacks();
                    if (ess.size() == 1) {
                        EmiStack es = ess.get(0);
                        if (compiled.test(es)) {
                            stacks.add(stack);
                        }
                    }
                }

                EmiSearch.apply(this, List.copyOf(stacks));
            } catch (Exception e) {
                EmiLog.error("Error when attempting to search:");
            }
        }
    }
}