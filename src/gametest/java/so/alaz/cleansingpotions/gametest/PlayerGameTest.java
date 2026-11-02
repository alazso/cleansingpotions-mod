package so.alaz.cleansingpotions.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import so.alaz.cleansingpotions.CleansingPotions;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.entity.CleansingPotionEntity;
import so.alaz.cleansingpotions.item.MilkItemFactory;
import so.alaz.cleansingpotions.item.MilkVariant;

import java.util.List;

/** Uses mock server players, which the test removes from the player list before it ends. */
public class PlayerGameTest {

    private static final String ISOLATED = "cleansingpotions-gametest:isolated";

    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        Vec3 pos = helper.absoluteVec(new Vec3(1.5, 1.0, 1.5));
        player.setPos(pos.x, pos.y, pos.z);
        return player;
    }

    private static ItemStack splash() {
        return MilkItemFactory.create(MilkVariant.SPLASH, CleanseMode.ALL);
    }

    @GameTest
    public void aGivenPotionThatDoesNotFitIsForTheTargetOnly(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        ServerPlayer target = survivalPlayer(helper);
        ServerPlayer other = survivalPlayer(helper);
        try {
            Inventory inventory = target.getInventory();
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                inventory.setItem(slot, new ItemStack(Items.STONE, 64));
            }

            // Both mock players share a name, so the target runs the command, which gives to its source.
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withEntity(target),
                "cleanse give cleansing splash");

            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                target.getBoundingBox().inflate(4.0));
            helper.assertTrue(drops.size() == 1, "expected one dropped potion, found " + drops.size());
            ItemEntity drop = drops.getFirst();
            helper.assertFalse(drop.hasPickUpDelay(), "the dropped potion has a pickup delay");

            drop.playerTouch(other);
            helper.assertTrue(other.getInventory().isEmpty(), "another player took the dropped potion");

            inventory.setItem(0, ItemStack.EMPTY);
            drop.playerTouch(target);
            helper.assertTrue(inventory.getItem(0).is(splash().getItem()), "the target could not take the dropped potion");
        } finally {
            server.getPlayerList().remove(target);
            server.getPlayerList().remove(other);
        }
        helper.succeed();
    }

    /** Changes the shared config inside one test tick and restores it before returning. */
    @GameTest(environment = ISOLATED)
    public void aThrowCoolsForTheCurrentLength(GameTestHelper helper) {
        CleansingConfig cfg = CleansingConfig.get();
        boolean enabled = cfg.cooldownEnabled;
        long seconds = cfg.cooldownSeconds;
        ServerPlayer player = survivalPlayer(helper);
        try {
            cfg.cooldownEnabled = true;
            cfg.cooldownSeconds = 3600;
            helper.assertTrue(throwPotion(helper, player) != InteractionResult.FAIL, "the first throw was refused");
            helper.assertTrue(throwPotion(helper, player) == InteractionResult.FAIL, "a throw while cooling was not refused");

            cfg.cooldownSeconds = 0;
            helper.assertTrue(throwPotion(helper, player) != InteractionResult.FAIL, "a lowered length did not apply");
        } finally {
            cfg.cooldownEnabled = enabled;
            cfg.cooldownSeconds = seconds;
            CleansingPotions.cooldowns().clear();
            helper.getLevel().getEntitiesOfClass(CleansingPotionEntity.class, player.getBoundingBox().inflate(16.0))
                .forEach(Entity::discard);
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    private static InteractionResult throwPotion(GameTestHelper helper, ServerPlayer player) {
        ItemStack potion = splash();
        player.setItemInHand(InteractionHand.MAIN_HAND, potion);
        return potion.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
    }
}
