package kurvcygnus.crispsweetberry.datagen;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.registry.objects.LangEntry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;

/**
 * Main event for generating JSON data.
 * @since 1.0 Release
 * @implNote <u>{@link GatherDataEvent}</u> doesn't belong to neither <u>{@link net.neoforged.api.distmarker.Dist#CLIENT ClientSide}</u> nor 
 * <u>{@link net.neoforged.api.distmarker.Dist#DEDICATED_SERVER ServerSide}</u>, so just remind that adding these params to 
 * <u>{@link EventBusSubscriber}</u> will cause problems(will be silent at some cases), don't do this.
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
final class CrispDataGenEvent
{
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    
    @SubscribeEvent
    static void dataGenerate(@NotNull GatherDataEvent event)
    {
        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("CRISP_DATAGEN"))
        {
            handle.changeMarker("INIT");
            LOGGER.info("Init data generation...");
            final DataGenerator generator = event.getGenerator();
            final PackOutput output = generator.getPackOutput();
            final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
            final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
            
            LOGGER.info("Initializing i18n information...");
            final List<ModFileScanData.AnnotationData> languageInfos = event.getModContainer().getModInfo().getOwningFile().getFile().getScanResult().getAnnotations().stream().
                filter(data -> Objects.equals(data.annotationType().getClassName(), AutoI18n.class.getName())).
                toList();
            LOGGER.info("Initializing completed. Total entries: {}. Start parsing...", languageInfos.size());
            
            final List<LangEntry> langEntries = new ArrayList<>();
            
            handle.changeMarker("LANG_FILTER");
            for(final ModFileScanData.AnnotationData data: languageInfos)
            {
                try
                {
                    final Class<?> clazz = Class.forName(data.clazz().getClassName());
                    final Field field = clazz.getDeclaredField(data.memberName());
                    
                    field.setAccessible(true);
                    
                    final @Nullable AutoI18n autoI18n = field.getAnnotation(AutoI18n.class);
                    
                    if(!Modifier.isStatic(field.getModifiers()))
                    {
                        handle.changeMarker("LANG_FILTER_EXPR");
                        LOGGER.warn("Field \"{}\" is not static. Did you forget it?", data.memberName());
                        continue;
                    }
                    
                    final Object value = field.get(null);
                    
                    if(autoI18n == null || value == null)
                        continue;
                    
                    String key = "";
                    
                    switch(value)
                    {
                        case DeferredHolder<?, ?> deferredHolder -> key = deferredHolder.getId().toLanguageKey();
                        case Block block -> key = block.getDescriptionId();
                        case Item item -> key = item.getDescriptionId();
                        case Supplier<?> supplier ->
                        {
                            final Object inner = supplier.get();
                            
                            switch(inner)//* Minecraft even haven't design any interface to carry `getDescriptionId()`, so yeah, enjoy boilerplate.
                            {
                                case Block block -> key = block.getDescriptionId();
                                case Item item -> key = item.getDescriptionId();
                                case ResourceLocation resourceLocation -> key = resourceLocation.getPath();
                                case null, default -> {}
                            }
                        }
                        case Component component -> key = component.getString();
                        default -> throw new IllegalArgumentException(
                            "Key happens to be empty since it matches none of objects that carries key. Current Entry: %s".formatted
                                (value.getClass().getName())
                        );
                    }
                    
                    LOGGER.info("Get the key of entry \"{}\": {}", value.getClass().getName(), key);
                    
                    parseLangInfos(langEntries, autoI18n, key);
                }
                catch(ClassNotFoundException e) { LOGGER.error("Could not find class \"{}\". Details: {}", data.clazz().getClassName(), e); }
                catch(NoSuchFieldException e) { LOGGER.error("Could not found field \"{}\". Details: {}", data.memberName(), e); }
                catch(IllegalAccessException e) { LOGGER.error("Could not access field \"{}\". Details: {}", data.memberName(), e); }
            }
            LOGGER.info("Parse completed. Start activate providers...");
            
            for(final AutoI18n.Lang lang: AutoI18n.Lang.values())
                generator.addProvider(
                    event.includeClient(),
                    new CrispI18NProvider(
                        output,
                        lang.getCode(),
                        langEntries
                    )
                );
            
            generator.addProvider(
                event.includeServer(),
                new CrispRecipeProvider(output, lookupProvider)
            );
            
            generator.addProvider(
                event.includeClient(),
                new CrispBlockstateProvider(output, existingFileHelper)
            );
            
            generator.addProvider(
                event.includeServer(),
                new LootTableProvider(
                    output,
                    Collections.emptySet(),
                    List.of(
                        new LootTableProvider.SubProviderEntry(
                            CrispBlockLootSubProvider::new,
                            LootContextParamSets.BLOCK
                        ),
                        new LootTableProvider.SubProviderEntry(
                            VanillaCoinLootTableProvider::new,
                            LootContextParamSets.BLOCK
                        )
                    ),
                    lookupProvider
                )
            );
        }
    }
    
    private static void parseLangInfos(@NotNull List<LangEntry> entries, @NotNull AutoI18n autoI18n, @NotNull String key)
    {
        Objects.requireNonNull(entries, "Param \"entries\" must not be null!");
        Objects.requireNonNull(autoI18n, "Param \"autoI18n\" must not be null!");
        
        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("LANG_PARSE"))
        {
            if(key.isEmpty())
            {
                handle.changeMarker("LANG_PARSE_FETAL");
                throw new IllegalArgumentException("Key happens to be empty!");
            }
            
            final @NotNull String[] translations = Objects.requireNonNull(autoI18n.value(), "Field \"value\" must not be null!");
            boolean hasEnglish = false;
            
            for(final String translation: translations)
            {
                final Matcher translationMatcher = AutoI18n.TRANSLATION_PATTERN.matcher(translation);
                
                if(!translationMatcher.matches())
                {
                    handle.changeMarker("LANG_PARSE_EXPR");
                    LOGGER.warn("Invalid translation \"{}\". Skipped current parse.", translation);
                    continue;
                }
                
                final String language = translationMatcher.group(1);
                final String content = translationMatcher.group(2);
                
                final AutoI18n.Lang lang = AutoI18n.Lang.parse(language);
                
                if(lang == AutoI18n.Lang.EN_US)
                    hasEnglish = true;
                
                final LangEntry entry = new LangEntry(lang, key, content);
                
                if(!entries.contains(entry))
                    entries.add(entry);
            }
            
            CrispFunctionalUtils.throwIf(!hasEnglish, () -> new IllegalArgumentException("Translations must at least include a basic support for EN_US!"));
        }
    }
}
