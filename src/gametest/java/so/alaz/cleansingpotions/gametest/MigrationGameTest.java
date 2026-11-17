package so.alaz.cleansingpotions.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.item.MilkVariant;
import so.alaz.cleansingpotions.item.ModComponents;
import so.alaz.cleansingpotions.item.ModItems;

import java.util.List;

/**
 * The server never ticks a player from makeMockPlayer, so these tests drive it by hand: Inventory.tick covers
 * the main slots, and Player.aiStep also runs the equipment tick that covers the offhand.
 */
public class MigrationGameTest {

    /** A legacy stack: the Cleansing item of its variant, with the mode in a component. */
    private static ItemStack legacy(MilkVariant variant, CleanseMode mode) {
        ItemStack stack = new ItemStack(ModItems.get(variant, CleanseMode.ALL));
        stack.set(ModComponents.CLEANSE_MODE, mode);
        return stack;
    }

    @GameTest
    public void everyVariantAndModeBecomesItsOwnItem(GameTestHelper helper) {
        Inventory inventory = helper.makeMockPlayer(GameType.SURVIVAL).getInventory();
        int slot = 0;
        for (MilkVariant variant : MilkVariant.values()) {
            for (CleanseMode mode : CleanseMode.values()) {
                inventory.setItem(slot++, legacy(variant, mode));
            }
        }

        inventory.tick();

        slot = 0;
        for (MilkVariant variant : MilkVariant.values()) {
            for (CleanseMode mode : CleanseMode.values()) {
                ItemStack migrated = inventory.getItem(slot);
                helper.assertTrue(migrated.is(ModItems.get(variant, mode)),
                    "slot " + slot + " holds " + migrated + ", expected " + variant + " " + mode);
                helper.assertFalse(migrated.has(ModComponents.CLEANSE_MODE),
                    "slot " + slot + " still carries the legacy component");
                slot++;
            }
        }
        helper.succeed();
    }

    @GameTest
    public void stampedComponentsAreRemovedAndTheRestIsKept(GameTestHelper helper) {
        ItemStack stack = legacy(MilkVariant.SPLASH, CleanseMode.NEGATIVE_ONLY);
        stack.setCount(3);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Splash Potion of Soothing")
            .withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Removes harmful effects"))));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(7.0F), List.of(), List.of(), List.of()));
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        Inventory inventory = helper.makeMockPlayer(GameType.SURVIVAL).getInventory();
        inventory.setItem(5, stack);

        inventory.tick();

        ItemStack migrated = inventory.getItem(5);
        helper.assertTrue(migrated.is(ModItems.get(MilkVariant.SPLASH, CleanseMode.NEGATIVE_ONLY)),
            "the slot holds " + migrated);
        helper.assertValueEqual(migrated.getCount(), 3, "the count");
        helper.assertFalse(migrated.has(DataComponents.CUSTOM_NAME), "the stamped name survived");
        helper.assertFalse(migrated.has(DataComponents.LORE), "the lore survived");
        helper.assertFalse(migrated.has(DataComponents.CUSTOM_MODEL_DATA), "the custom model data survived");
        helper.assertTrue(migrated.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE), "an unrelated component was lost");
        helper.succeed();
    }

    @GameTest
    public void aPlainCustomNameSurvives(GameTestHelper helper) {
        ItemStack stack = legacy(MilkVariant.REGULAR, CleanseMode.POSITIVE_ONLY);
        Component name = Component.literal("Grandma Remedy");
        stack.set(DataComponents.CUSTOM_NAME, name);
        Inventory inventory = helper.makeMockPlayer(GameType.SURVIVAL).getInventory();
        inventory.setItem(5, stack);

        inventory.tick();

        ItemStack migrated = inventory.getItem(5);
        helper.assertTrue(migrated.is(ModItems.get(MilkVariant.REGULAR, CleanseMode.POSITIVE_ONLY)),
            "the slot holds " + migrated);
        helper.assertTrue(name.equals(migrated.get(DataComponents.CUSTOM_NAME)),
            "the custom name became " + migrated.get(DataComponents.CUSTOM_NAME));
        helper.succeed();
    }

    @GameTest
    public void aStackWithoutTheLegacyComponentIsNotReplaced(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModItems.get(MilkVariant.REGULAR, CleanseMode.NEGATIVE_ONLY));
        Inventory inventory = helper.makeMockPlayer(GameType.SURVIVAL).getInventory();
        inventory.setItem(5, stack);

        inventory.tick();

        helper.assertTrue(inventory.getItem(5) == stack, "the slot holds a different stack");
        helper.succeed();
    }

    @GameTest
    public void aStackBeingUsedWaitsForTheUseToEnd(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Inventory inventory = player.getInventory();
        ItemStack stack = legacy(MilkVariant.REGULAR, CleanseMode.NEGATIVE_ONLY);
        int slot = inventory.getSelectedSlot();
        inventory.setItem(slot, stack);
        player.startUsingItem(InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getUseItem() == stack, "the use did not start");

        inventory.tick();
        helper.assertTrue(inventory.getItem(slot) == stack, "a stack being used was replaced");

        player.stopUsingItem();
        inventory.tick();
        helper.assertTrue(inventory.getItem(slot).is(ModItems.get(MilkVariant.REGULAR, CleanseMode.NEGATIVE_ONLY)),
            "the slot holds " + inventory.getItem(slot) + " after the use ended");
        helper.succeed();
    }

    @GameTest
    public void anOffhandStackMigratesThroughTheEquipmentTick(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        Vec3 inside = helper.absoluteVec(Vec3.atCenterOf(new BlockPos(4, 1, 4)));
        player.setPos(inside.x, inside.y, inside.z);
        player.setItemInHand(InteractionHand.OFF_HAND, legacy(MilkVariant.LINGERING, CleanseMode.POSITIVE_ONLY));

        player.aiStep();

        ItemStack migrated = player.getOffhandItem();
        helper.assertTrue(migrated.is(ModItems.get(MilkVariant.LINGERING, CleanseMode.POSITIVE_ONLY)),
            "the offhand holds " + migrated);
        helper.assertFalse(migrated.has(ModComponents.CLEANSE_MODE), "the offhand stack still carries the legacy component");
        helper.succeed();
    }
}
