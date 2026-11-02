package so.alaz.cleansingpotions.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.entity.CleansingCloudEntity;
import so.alaz.cleansingpotions.entity.ModEntities;

public class CloudGameTest {

    /**
     * Saves and reloads a cloud of the given mode, then checks the reloaded one removes {@code removed} and
     * leaves {@code kept}. A cloud that forgot its mode falls back to Cleansing and removes both, so the test
     * never reaches its success state and times out.
     */
    private static void assertModeSurvivesSaveAndLoad(GameTestHelper helper, CleanseMode mode,
                                                      Holder<MobEffect> removed, Holder<MobEffect> kept) {
        ServerLevel level = helper.getLevel();
        BlockPos middle = new BlockPos(4, 1, 4);
        // The default structure is all air, and the pig has to stay inside the radius of the cloud.
        helper.setBlock(middle.below(), Blocks.STONE);
        Vec3 center = helper.absoluteVec(Vec3.atCenterOf(middle));

        CleansingCloudEntity original = new CleansingCloudEntity(level, center.x, center.y, center.z);
        original.setMode(mode);
        original.configure(3.0F, mode.rgb());

        TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        original.saveWithoutId(saved);

        CleansingCloudEntity reloaded = new CleansingCloudEntity(ModEntities.CLEANSING_CLOUD, level);
        reloaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
        level.addFreshEntity(reloaded);

        LivingEntity pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, middle);
        pig.addEffect(new MobEffectInstance(removed, 1200));
        pig.addEffect(new MobEffectInstance(kept, 1200));

        helper.succeedWhen(() -> {
            helper.assertFalse(pig.hasEffect(removed), "the reloaded cloud has not removed " + removed.getRegisteredName());
            helper.assertTrue(pig.hasEffect(kept), "the reloaded cloud removed " + kept.getRegisteredName());
        });
    }

    @GameTest(maxTicks = 100)
    public void purgingCloudKeepsItsModeAcrossSaveAndLoad(GameTestHelper helper) {
        assertModeSurvivesSaveAndLoad(helper, CleanseMode.POSITIVE_ONLY, MobEffects.SPEED, MobEffects.POISON);
    }

    @GameTest(maxTicks = 100)
    public void soothingCloudKeepsItsModeAcrossSaveAndLoad(GameTestHelper helper) {
        assertModeSurvivesSaveAndLoad(helper, CleanseMode.NEGATIVE_ONLY, MobEffects.POISON, MobEffects.SPEED);
    }
}
