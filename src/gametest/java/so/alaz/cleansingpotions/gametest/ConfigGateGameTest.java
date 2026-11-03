package so.alaz.cleansingpotions.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
//? if <26.3 {
import net.minecraft.core.BlockPos;
//?}
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if <26.3 {
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.item.MilkVariant;
import so.alaz.cleansingpotions.item.ModItems;
//?} else {
/*import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.config.CleansingConfig;

import java.util.Arrays;
import java.util.List;
*///?}

/**
 * Changes the shared config, and on 26.3 reloads the data packs, inside one test tick, and restores both
 * before returning. Runs in the isolated test environment, which keeps it out of the default environment's
 * batch. Each test in that environment restores the shared state before its tick ends.
 */
public class ConfigGateGameTest {

    private static final String ISOLATED = "cleansingpotions-gametest:isolated";

    //? if <26.3 {
    @GameTest(environment = ISOLATED)
    public void mixesFollowConfig(GameTestHelper helper) {
        PotionBrewing brewing = helper.getLevel().potionBrewing();
        ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        ItemStack cleansing = new ItemStack(ModItems.get(MilkVariant.REGULAR, CleanseMode.ALL));
        ItemStack splash = new ItemStack(ModItems.get(MilkVariant.SPLASH, CleanseMode.ALL));
        ItemStack milk = new ItemStack(Items.MILK_BUCKET);
        ItemStack honey = new ItemStack(Items.HONEY_BOTTLE);
        ItemStack gunpowder = new ItemStack(Items.GUNPOWDER);
        ItemStack dragonBreath = new ItemStack(Items.DRAGON_BREATH);

        CleansingConfig cfg = CleansingConfig.get();
        boolean brewingEnabled = cfg.brewingEnabled;
        boolean splashEnabled = cfg.splashEnabled;
        boolean lingeringEnabled = cfg.lingeringEnabled;
        try {
            set(cfg, true, true, true);
            helper.assertTrue(brewing.hasMix(water, milk), "the milk brew is missing while brewing is on");
            helper.assertTrue(brewing.hasMix(cleansing, honey), "the honey brew is missing while brewing is on");
            helper.assertTrue(brewing.hasMix(cleansing, gunpowder), "the splash brew is missing while brewing is on");
            helper.assertTrue(brewing.hasMix(splash, dragonBreath), "the lingering brew is missing while brewing is on");
            helper.assertTrue(brewing.isIngredient(milk), "milk must be an ingredient while brewing is on");
            helper.assertTrue(brewing.isIngredient(honey), "honey must be an ingredient while brewing is on");

            set(cfg, true, false, true);
            helper.assertTrue(brewing.hasMix(water, milk), "splashEnabled=false stopped the milk brew");
            helper.assertFalse(brewing.hasMix(cleansing, gunpowder), "the splash brew survived splashEnabled=false");
            helper.assertTrue(brewing.hasMix(splash, dragonBreath), "splashEnabled=false stopped the lingering brew");

            set(cfg, true, true, false);
            helper.assertTrue(brewing.hasMix(cleansing, gunpowder), "lingeringEnabled=false stopped the splash brew");
            helper.assertFalse(brewing.hasMix(splash, dragonBreath), "the lingering brew survived lingeringEnabled=false");

            set(cfg, false, true, true);
            helper.assertFalse(brewing.hasMix(water, milk), "the milk brew survived brewingEnabled=false");
            helper.assertFalse(brewing.hasMix(cleansing, honey), "the honey brew survived brewingEnabled=false");
            helper.assertFalse(brewing.hasMix(cleansing, gunpowder), "the splash brew survived brewingEnabled=false");
            helper.assertFalse(brewing.hasMix(splash, dragonBreath), "the lingering brew survived brewingEnabled=false");
            helper.assertFalse(brewing.isIngredient(milk), "milk stayed an ingredient with brewingEnabled=false");
            helper.assertFalse(brewing.isIngredient(honey), "honey stayed an ingredient with brewingEnabled=false");
        } finally {
            set(cfg, brewingEnabled, splashEnabled, lingeringEnabled);
        }
        helper.succeed();
    }

    @GameTest(environment = ISOLATED)
    public void bottleSlotsFollowConfig(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.BREWING_STAND);
        BrewingStandBlockEntity stand = helper.getBlockEntity(pos, BrewingStandBlockEntity.class);
        ItemStack cleansing = new ItemStack(ModItems.get(MilkVariant.REGULAR, CleanseMode.ALL));
        ItemStack soothing = new ItemStack(ModItems.get(MilkVariant.REGULAR, CleanseMode.NEGATIVE_ONLY));
        ItemStack splash = new ItemStack(ModItems.get(MilkVariant.SPLASH, CleanseMode.ALL));

        CleansingConfig cfg = CleansingConfig.get();
        boolean brewingEnabled = cfg.brewingEnabled;
        boolean splashEnabled = cfg.splashEnabled;
        boolean lingeringEnabled = cfg.lingeringEnabled;
        try {
            set(cfg, true, true, true);
            helper.assertTrue(stand.canPlaceItem(0, cleansing), "Cleansing was refused with every brew on");
            helper.assertTrue(stand.canPlaceItem(0, soothing), "Soothing was refused with every brew on");
            helper.assertTrue(stand.canPlaceItem(0, splash), "a splash potion was refused with every brew on");

            set(cfg, true, false, true);
            helper.assertTrue(stand.canPlaceItem(0, cleansing), "splashEnabled=false refused Cleansing, which other brews take");
            helper.assertFalse(stand.canPlaceItem(0, soothing), "splashEnabled=false left Soothing accepted");
            helper.assertTrue(stand.canPlaceItem(0, splash), "splashEnabled=false refused a splash potion");

            set(cfg, true, true, false);
            helper.assertTrue(stand.canPlaceItem(0, soothing), "lingeringEnabled=false refused Soothing");
            helper.assertFalse(stand.canPlaceItem(0, splash), "lingeringEnabled=false left a splash potion accepted");

            set(cfg, false, true, true);
            helper.assertFalse(stand.canPlaceItem(0, cleansing), "brewingEnabled=false left Cleansing accepted");
            helper.assertFalse(stand.canPlaceItem(0, soothing), "brewingEnabled=false left Soothing accepted");
            helper.assertFalse(stand.canPlaceItem(0, splash), "brewingEnabled=false left a splash potion accepted");
        } finally {
            set(cfg, brewingEnabled, splashEnabled, lingeringEnabled);
        }
        helper.succeed();
    }

    private static void set(CleansingConfig cfg, boolean brewing, boolean splash, boolean lingering) {
        cfg.brewingEnabled = brewing;
        cfg.splashEnabled = splash;
        cfg.lingeringEnabled = lingering;
    }
    //?} else {
    /*private static final List<ResourceKey<Recipe<?>>> BASE = keys(
        "cleansing_from_water", "soothing_from_cleansing", "purging_from_cleansing");
    private static final List<ResourceKey<Recipe<?>>> SPLASH = keys(
        "cleansing_splash", "soothing_splash", "purging_splash");
    private static final List<ResourceKey<Recipe<?>>> LINGERING = keys(
        "cleansing_lingering", "soothing_lingering", "purging_lingering");

    @GameTest(environment = ISOLATED)
    public void recipesFollowConfig(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        CleansingConfig cfg = CleansingConfig.get();
        boolean brewing = cfg.brewingEnabled;
        boolean splash = cfg.splashEnabled;
        boolean lingering = cfg.lingeringEnabled;
        try {
            set(server, cfg, true, true, true);
            expect(helper, server, BASE, true);
            expect(helper, server, SPLASH, true);
            expect(helper, server, LINGERING, true);
            helper.assertTrue(milkIsReagent(server), "milk must be a reagent while brewing is on");

            set(server, cfg, true, false, true);
            expect(helper, server, BASE, true);
            expect(helper, server, SPLASH, false);
            expect(helper, server, LINGERING, true);

            set(server, cfg, true, true, false);
            expect(helper, server, BASE, true);
            expect(helper, server, SPLASH, true);
            expect(helper, server, LINGERING, false);

            set(server, cfg, false, true, true);
            expect(helper, server, BASE, false);
            expect(helper, server, SPLASH, false);
            expect(helper, server, LINGERING, false);
            helper.assertFalse(milkIsReagent(server), "milk must not be a reagent while brewing is off");
        } finally {
            set(server, cfg, brewing, splash, lingering);
        }
        helper.succeed();
    }

    // reloadResources blocks when called on the server thread, so the new RecipeManager is live on return.
    private static void set(MinecraftServer server, CleansingConfig cfg, boolean brewing, boolean splash, boolean lingering) {
        cfg.brewingEnabled = brewing;
        cfg.splashEnabled = splash;
        cfg.lingeringEnabled = lingering;
        server.reloadResources(server.getPackRepository().getSelectedIds()).join();
    }

    private static void expect(GameTestHelper helper, MinecraftServer server, List<ResourceKey<Recipe<?>>> keys, boolean present) {
        RecipeManager recipes = server.getRecipeManager();
        for (ResourceKey<Recipe<?>> key : keys) {
            helper.assertTrue(recipes.byKey(key).isPresent() == present,
                key.identifier() + " expected present=" + present);
        }
    }

    private static boolean milkIsReagent(MinecraftServer server) {
        return server.getRecipeManager().propertySet(RecipePropertySet.BREWING_REAGENTS)
            .test(new ItemStack(Items.MILK_BUCKET));
    }

    private static List<ResourceKey<Recipe<?>>> keys(String... names) {
        return Arrays.stream(names)
            .<ResourceKey<Recipe<?>>>map(name -> ResourceKey.create(Registries.RECIPE, CleansingPotions.id("brewing/" + name)))
            .toList();
    }
    *///?}
}
