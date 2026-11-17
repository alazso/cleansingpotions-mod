package so.alaz.cleansingpotions.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.entity.CleansingPotionEntity;
import so.alaz.cleansingpotions.entity.ModEntities;
import so.alaz.cleansingpotions.item.MilkVariant;
import so.alaz.cleansingpotions.item.ModItems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DispenserGameTest {

    private static final MilkVariant[] THROWN = {MilkVariant.SPLASH, MilkVariant.LINGERING};

    /** The dispensers face up, so every potion stays in the air inside the test area for its first ticks. */
    @GameTest(maxTicks = 40)
    public void poweredDispensersThrowThePotions(GameTestHelper helper) {
        List<DispenserBlockEntity> dispensers = new ArrayList<>();
        Set<Item> expected = new HashSet<>();
        int row = 0;
        for (MilkVariant variant : THROWN) {
            int column = 0;
            for (CleanseMode mode : CleanseMode.values()) {
                Item potion = ModItems.get(variant, mode);
                expected.add(potion);
                BlockPos pos = new BlockPos(1 + column * 2, 1, 1 + row * 2);
                helper.setBlock(pos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP));
                DispenserBlockEntity dispenser = helper.getBlockEntity(pos, DispenserBlockEntity.class);
                dispenser.setItem(0, new ItemStack(potion));
                dispensers.add(dispenser);
                helper.setBlock(pos.below(), Blocks.REDSTONE_BLOCK);
                column++;
            }
            row++;
        }

        helper.succeedWhen(() -> {
            Set<Item> thrown = new HashSet<>();
            for (CleansingPotionEntity projectile : helper.getEntities(ModEntities.CLEANSING_POTION)) {
                thrown.add(projectile.getItem().getItem());
            }
            helper.assertValueEqual(thrown, expected, "the potions in the air");
            for (DispenserBlockEntity dispenser : dispensers) {
                helper.assertTrue(dispenser.isEmpty(), "a dispenser still holds its potion");
            }
            helper.assertEntityNotPresent(EntityTypes.ITEM);
        });
    }
}
