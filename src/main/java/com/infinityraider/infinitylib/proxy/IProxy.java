package com.infinityraider.infinitylib.proxy;

import com.infinityraider.infinitylib.InfinityMod;
import com.infinityraider.infinitylib.block.IInfinityBlock;
import com.infinityraider.infinitylib.block.IInfinityBlockWithTile;
import com.infinityraider.infinitylib.config.InfinityConfigurationHandler;
import com.infinityraider.infinitylib.entity.EntityRegistryEntry;
import com.infinityraider.infinitylib.item.IInfinityItem;
import com.infinityraider.infinitylib.modules.Module;
import com.infinityraider.infinitylib.proxy.base.IProxyBase;
import com.infinityraider.infinitylib.utility.IRecipeRegister;
import com.infinityraider.infinitylib.utility.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.oredict.OreDictionary;

public interface IProxy extends IProxyBase {
    default <T extends IForgeRegistryEntry<T>> void register(InfinityMod mod, IForgeRegistry<T> registry, T object, String name) {
        object.setRegistryName(new ResourceLocation(mod.getModId().toLowerCase(), name.toLowerCase()));
        registry.register(object);
    }

    default void registerBlocks(InfinityMod mod, IForgeRegistry<Block> registry) {
        //blocks
        ReflectionHelper.forEachIn(mod.getModBlockRegistry(), IInfinityBlock.class, (IInfinityBlock block) -> {
            if ((block instanceof Block) && block.isEnabled()) {
                mod.getLogger().debug("Registering Block: " + block.getInternalName());
                String unlocalized = mod.getModId().toLowerCase() + ':' + block.getInternalName();
                ((Block) block).setUnlocalizedName(unlocalized);
                register(mod, registry, (Block) block, block.getInternalName());
                for (String tag : block.getOreTags()) {
                    OreDictionary.registerOre(tag, (Block) block);
                }
            }
        });
        //tile entities
        ReflectionHelper.forEachIn(mod.getModBlockRegistry(), IInfinityBlockWithTile.class, (IInfinityBlockWithTile block) -> {
            if (block.isEnabled()) {
                mod.getLogger().debug("Registering Tile for Block: " + block.getInternalName());
                TileEntity te = block.createNewTileEntity(null, 0);
                assert (te != null);
                GameRegistry.registerTileEntity(te.getClass(), mod.getModId().toLowerCase() + ":tile." + block.getInternalName());
            }
        });
    }

    default void registerItems(InfinityMod mod, IForgeRegistry<Item> registry) {
        //items
        ReflectionHelper.forEachIn(mod.getModItemRegistry(), IInfinityItem.class, (IInfinityItem item) -> {
            if ((item instanceof Item) && item.isEnabled()) {
                mod.getLogger().debug("Registering Item: " + item.getInternalName());
                String unlocalized = mod.getModId().toLowerCase() + ':' + item.getInternalName();
                ((Item) item).setUnlocalizedName(unlocalized);
                register(mod, registry, (Item) item, item.getInternalName());
                for (String tag : item.getOreTags()) {
                    OreDictionary.registerOre(tag, (Item) item);
                }
            }
        });
    }

    default void registerBiomes(InfinityMod mod, IForgeRegistry<Biome> registry) {
        ReflectionHelper.forEachIn(mod.getModBiomeRegistry(), Biome.class,
                biome -> register(mod, registry, biome, biome.getBiomeName()));
    }

    default void registerEnchantments(InfinityMod mod, IForgeRegistry<Enchantment> registry) {
        ReflectionHelper.forEachIn(mod.getModEnchantmentRegistry(), Enchantment.class,
                enchantment -> register(mod, registry, enchantment, enchantment.getName()));
    }

    default void registerEntities(InfinityMod mod) {
        ReflectionHelper.forEachIn(mod.getModEntityRegistry(), EntityRegistryEntry.class, (EntityRegistryEntry entry) -> {
            if(entry.isEnabled()) {
                entry.register(mod);
                entry = null;
            }
        });
    }

    default void registerPotions(InfinityMod mod, IForgeRegistry<Potion> registry) {
        ReflectionHelper.forEachIn(mod.getModPotionRegistry(), Potion.class,
                potion -> register(mod, registry, potion, potion.getName()));
    }

    default void registerPotionTypes(InfinityMod mod, IForgeRegistry<PotionType> registry) {
        ReflectionHelper.forEachIn(mod.getModPotionTypeRegistry(), PotionType.class,
                type -> register(mod, registry, type, type.getNamePrefixed("type")));
    }

    default void registerSounds(InfinityMod mod, IForgeRegistry<SoundEvent> registry) {
        ReflectionHelper.forEachIn(mod.getModSoundRegistry(), SoundEvent.class,
                sound -> register(mod, registry, sound, sound.getSoundName().getResourcePath()));
    }

    default void registerVillagerProfessions(InfinityMod mod, IForgeRegistry<VillagerRegistry.VillagerProfession> registry) {
        ReflectionHelper.forEachIn(mod.getModVillagerProfessionRegistry(), VillagerRegistry.VillagerProfession.class,
                profession -> register(mod, registry, profession, profession.toString()));
    }

    default void registerRecipes(InfinityMod mod) {
        //blocks
        ReflectionHelper.forEachIn(mod.getModBlockRegistry(), IRecipeRegister.class, IRecipeRegister::registerRecipes);
        //items
        ReflectionHelper.forEachIn(mod.getModItemRegistry(), IRecipeRegister.class, IRecipeRegister::registerRecipes);
    }

    @Override
    default void initEnd(FMLInitializationEvent event) {
        Module.getActiveModules().forEach(Module::init);
    }
    @Override
    default void postInitEnd(FMLPostInitializationEvent event) {
        Module.getActiveModules().forEach(Module::postInit);
    }

    default void initModConfiguration(InfinityConfigurationHandler handler, FMLPreInitializationEvent event) {
        handler.initializeConfiguration(event);
    }

    @Override
    default void registerEventHandlers() {
        for(Module module : Module.getActiveModules()) {
            module.getCommonEventHandlers().forEach(this::registerEventHandler);
        }
    }

    @Override
    default void registerCapabilities() {
        for(Module module : Module.getActiveModules()) {
            module.getCapabilities().forEach(this::registerCapability);
        }
    }

    @Override
    default void activateRequiredModules() {}
}
