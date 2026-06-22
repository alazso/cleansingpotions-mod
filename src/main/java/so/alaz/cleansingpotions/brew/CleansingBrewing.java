package so.alaz.cleansingpotions.brew;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.item.MilkItemFactory;
import so.alaz.cleansingpotions.item.MilkVariant;
import so.alaz.cleansingpotions.item.ModComponents;
import so.alaz.cleansingpotions.item.ModItems;

public final class CleansingBrewing {

    public static boolean isIngredient(ItemStack reagent) {
        return reagent.is(Items.MILK_BUCKET)
            || reagent.is(Items.HONEY_BOTTLE)
            || reagent.is(Items.FERMENTED_SPIDER_EYE)
            || reagent.is(Items.GUNPOWDER)
            || reagent.is(Items.DRAGON_BREATH);
    }

    public static ItemStack mix(ItemStack input, ItemStack reagent) {
        CleansingConfig cfg = CleansingConfig.get();
        if (!cfg.brewingEnabled) {
            return ItemStack.EMPTY;
        }
        if (reagent.is(Items.MILK_BUCKET) && isWaterBottle(input)) {
            return MilkItemFactory.create(MilkVariant.REGULAR, CleanseMode.ALL);
        }
        if (reagent.is(Items.HONEY_BOTTLE) && isPlainCleansing(input)) {
            return MilkItemFactory.create(MilkVariant.REGULAR, CleanseMode.NEGATIVE_ONLY);
        }
        if (reagent.is(Items.FERMENTED_SPIDER_EYE) && isPlainCleansing(input)) {
            return MilkItemFactory.create(MilkVariant.REGULAR, CleanseMode.POSITIVE_ONLY);
        }
        if (reagent.is(Items.GUNPOWDER) && input.is(ModItems.CLEANSING) && cfg.splashEnabled) {
            return MilkItemFactory.create(MilkVariant.SPLASH, modeOf(input));
        }
        if (reagent.is(Items.DRAGON_BREATH) && input.is(ModItems.SPLASH) && cfg.lingeringEnabled) {
            return MilkItemFactory.create(MilkVariant.LINGERING, modeOf(input));
        }
        return ItemStack.EMPTY;
    }

    private static CleanseMode modeOf(ItemStack stack) {
        return stack.getOrDefault(ModComponents.CLEANSE_MODE, CleanseMode.ALL);
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        // Compare by registry name rather than Potions.WATER: that field's type differs between
        // 26.1.2 and 26.2, so a direct reference crashes with NoSuchFieldError on the other patch.
        return contents != null && contents.potion()
            .map(Holder::getRegisteredName)
            .map("minecraft:water"::equals)
            .orElse(false);
    }

    private static boolean isPlainCleansing(ItemStack stack) {
        return stack.is(ModItems.CLEANSING)
            && stack.getOrDefault(ModComponents.CLEANSE_MODE, CleanseMode.ALL) == CleanseMode.ALL;
    }

    private CleansingBrewing() {
    }
}
