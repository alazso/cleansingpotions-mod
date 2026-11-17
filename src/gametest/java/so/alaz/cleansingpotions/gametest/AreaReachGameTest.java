package so.alaz.cleansingpotions.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.core.CleanseMode;

public class AreaReachGameTest {

    private static final BlockPos PIG_POS = new BlockPos(2, 1, 2);

    private static LivingEntity pigWithSpeed(GameTestHelper helper) {
        helper.setBlock(PIG_POS.below(), Blocks.STONE);
        LivingEntity pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, PIG_POS);
        pig.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200));
        return pig;
    }

    @GameTest
    public void reachesAMobWhoseBoxIsInRangeAndFeetAreNot(GameTestHelper helper) {
        LivingEntity pig = pigWithSpeed(helper);
        // A pig is 0.9 tall, so this point is 1.0 from its feet and 0.1 from its box.
        CleansingPotions.cleanser().cleanseArea(helper.getLevel(),
            pig.getX(), pig.getY() + 1.0, pig.getZ(), 0.5, CleanseMode.ALL);
        helper.assertFalse(pig.hasEffect(MobEffects.SPEED), "the cleanse missed a mob whose box is within the radius");
        helper.succeed();
    }

    @GameTest
    public void missesAMobWhoseBoxIsOutOfRange(GameTestHelper helper) {
        LivingEntity pig = pigWithSpeed(helper);
        // A pig is 0.9 wide, so this point is 1.55 from its box on each of x and z, about 2.19 in all. The
        // box the entity query searches holds the pig, so only the radius check leaves it out.
        CleansingPotions.cleanser().cleanseArea(helper.getLevel(),
            pig.getX() + 2.0, pig.getY(), pig.getZ() + 2.0, 2.0, CleanseMode.ALL);
        helper.assertTrue(pig.hasEffect(MobEffects.SPEED), "the cleanse reached a mob wholly outside the radius");
        helper.succeed();
    }
}
