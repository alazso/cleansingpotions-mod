package so.alaz.cleansingpotions.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.item.MilkVariant;
import so.alaz.cleansingpotions.item.ModItems;

import java.util.ArrayList;
import java.util.List;

public class BrewingGameTest {

    /** Ticks a test allows for one brew: the 400 ticks a brew takes, plus 20 spare. */
    private static final int ONE_BREW = 420;

    private static final int BOTTLE_SLOT = 0;
    private static final int REAGENT_SLOT = 3;
    private static final int FUEL_SLOT = 4;

    private record Step(ItemStack input, Item reagent, Item output) {
    }

    private static ItemStack waterBottle() {
        return PotionContents.createItemStack(Items.POTION, Potions.WATER);
    }

    private static Item potion(MilkVariant variant, CleanseMode mode) {
        return ModItems.get(variant, mode);
    }

    private static ItemStack stack(MilkVariant variant, CleanseMode mode) {
        return new ItemStack(potion(variant, mode));
    }

    private static BrewingStandBlockEntity loadedStand(GameTestHelper helper, BlockPos pos, ItemStack input, Item reagent) {
        helper.setBlock(pos, Blocks.BREWING_STAND);
        BrewingStandBlockEntity stand = helper.getBlockEntity(pos, BrewingStandBlockEntity.class);
        stand.setItem(BOTTLE_SLOT, input);
        stand.setItem(REAGENT_SLOT, new ItemStack(reagent));
        stand.setItem(FUEL_SLOT, new ItemStack(Items.BLAZE_POWDER));
        return stand;
    }

    @GameTest(maxTicks = ONE_BREW + 40)
    public void milkTurnsWaterIntoCleansing(GameTestHelper helper) {
        BrewingStandBlockEntity stand = loadedStand(helper, new BlockPos(1, 1, 1), waterBottle(), Items.MILK_BUCKET);
        helper.succeedWhen(() -> {
            helper.assertTrue(stand.getItem(BOTTLE_SLOT).is(potion(MilkVariant.REGULAR, CleanseMode.ALL)),
                "the water bottle did not become a Potion of Cleansing");
            //? if <26.3 {
            // 26.2 doBrew reads the crafting remainder after shrinking the stack, so a one-item
            // reagent reads it from an empty stack and leaves nothing behind.
            helper.assertTrue(stand.getItem(REAGENT_SLOT).isEmpty(),
                "the reagent slot holds " + stand.getItem(REAGENT_SLOT));
            //?} else {
            /*helper.assertTrue(stand.getItem(REAGENT_SLOT).is(Items.BUCKET),
                "the reagent slot holds " + stand.getItem(REAGENT_SLOT));
            *///?}
        });
    }

    @GameTest(maxTicks = ONE_BREW + 40)
    public void everyOutcomeBrews(GameTestHelper helper) {
        List<Step> steps = new ArrayList<>();
        steps.add(new Step(waterBottle(), Items.MILK_BUCKET, potion(MilkVariant.REGULAR, CleanseMode.ALL)));
        steps.add(new Step(stack(MilkVariant.REGULAR, CleanseMode.ALL), Items.HONEY_BOTTLE,
            potion(MilkVariant.REGULAR, CleanseMode.NEGATIVE_ONLY)));
        steps.add(new Step(stack(MilkVariant.REGULAR, CleanseMode.ALL), Items.FERMENTED_SPIDER_EYE,
            potion(MilkVariant.REGULAR, CleanseMode.POSITIVE_ONLY)));
        for (CleanseMode mode : CleanseMode.values()) {
            steps.add(new Step(stack(MilkVariant.REGULAR, mode), Items.GUNPOWDER, potion(MilkVariant.SPLASH, mode)));
            steps.add(new Step(stack(MilkVariant.SPLASH, mode), Items.DRAGON_BREATH, potion(MilkVariant.LINGERING, mode)));
        }

        List<BrewingStandBlockEntity> stands = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            stands.add(loadedStand(helper, new BlockPos(1 + (i % 3) * 2, 1, 1 + (i / 3) * 2), step.input(), step.reagent()));
        }

        helper.succeedWhen(() -> {
            for (int i = 0; i < steps.size(); i++) {
                Step step = steps.get(i);
                helper.assertTrue(stands.get(i).getItem(BOTTLE_SLOT).is(step.output()),
                    "stand " + i + " holds " + stands.get(i).getItem(BOTTLE_SLOT) + ", expected " + step.output());
            }
        });
    }

    @GameTest(maxTicks = ONE_BREW + 40)
    public void honeyOnSoothingDoesNothing(GameTestHelper helper) {
        BrewingStandBlockEntity soothing = loadedStand(helper, new BlockPos(1, 1, 1),
            stack(MilkVariant.REGULAR, CleanseMode.NEGATIVE_ONLY), Items.HONEY_BOTTLE);
        BrewingStandBlockEntity purging = loadedStand(helper, new BlockPos(3, 1, 1),
            stack(MilkVariant.REGULAR, CleanseMode.POSITIVE_ONLY), Items.HONEY_BOTTLE);
        BrewingStandBlockEntity splash = loadedStand(helper, new BlockPos(5, 1, 1),
            stack(MilkVariant.SPLASH, CleanseMode.ALL), Items.HONEY_BOTTLE);

        helper.startSequence().thenExecuteAfter(ONE_BREW, () -> {
            helper.assertTrue(soothing.getItem(BOTTLE_SLOT).is(potion(MilkVariant.REGULAR, CleanseMode.NEGATIVE_ONLY)),
                "honey changed a Soothing potion");
            helper.assertTrue(purging.getItem(BOTTLE_SLOT).is(potion(MilkVariant.REGULAR, CleanseMode.POSITIVE_ONLY)),
                "honey changed a Purging potion");
            helper.assertTrue(splash.getItem(BOTTLE_SLOT).is(potion(MilkVariant.SPLASH, CleanseMode.ALL)),
                "honey changed a Cleansing splash potion");
            helper.assertTrue(soothing.getItem(REAGENT_SLOT).is(Items.HONEY_BOTTLE), "the honey was consumed");
        }).thenSucceed();
    }

    @GameTest
    public void bottleSlotsAcceptEveryPotionExceptLingering(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.BREWING_STAND);
        BrewingStandBlockEntity stand = helper.getBlockEntity(pos, BrewingStandBlockEntity.class);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BrewingStandMenu menu = new BrewingStandMenu(0, player.getInventory(), stand,
            new SimpleContainerData(BrewingStandBlockEntity.NUM_DATA_VALUES));

        for (MilkVariant variant : MilkVariant.values()) {
            boolean accepted = variant != MilkVariant.LINGERING;
            for (CleanseMode mode : CleanseMode.values()) {
                ItemStack potion = stack(variant, mode);
                helper.assertTrue(stand.canPlaceItem(BOTTLE_SLOT, potion) == accepted,
                    "canPlaceItem(" + potion + ") should be " + accepted);
                helper.assertTrue(menu.getSlot(BOTTLE_SLOT).mayPlace(potion) == accepted,
                    "mayPlace(" + potion + ") should be " + accepted);
            }
        }
        helper.succeed();
    }

    @GameTest
    public void anOccupiedBottleSlotRefusesAPotion(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.BREWING_STAND);
        BrewingStandBlockEntity stand = helper.getBlockEntity(pos, BrewingStandBlockEntity.class);
        stand.setItem(BOTTLE_SLOT, stack(MilkVariant.REGULAR, CleanseMode.ALL));
        helper.assertFalse(stand.canPlaceItem(BOTTLE_SLOT, stack(MilkVariant.REGULAR, CleanseMode.ALL)),
            "canPlaceItem accepted a potion for an occupied slot");
        helper.succeed();
    }
}
