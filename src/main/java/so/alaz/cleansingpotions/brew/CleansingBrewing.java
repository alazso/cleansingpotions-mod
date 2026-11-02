package so.alaz.cleansingpotions.brew;

//? if <26.3 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import so.alaz.cleansingpotions.config.CleansingConfig;
import so.alaz.cleansingpotions.core.CleanseMode;
import so.alaz.cleansingpotions.item.CleansingItem;
import so.alaz.cleansingpotions.item.MilkItemFactory;
import so.alaz.cleansingpotions.item.MilkVariant;

public final class CleansingBrewing {

    // Vanilla mixes already make fermented spider eye, gunpowder, and dragon breath ingredients.
    public static boolean isIngredient(ItemStack reagent) {
        return CleansingConfig.get().brewingEnabled
            && (reagent.is(Items.MILK_BUCKET) || reagent.is(Items.HONEY_BOTTLE));
    }

    // Lingering is left out because no recipe takes it as an input.
    public static boolean isBrewingInput(ItemStack stack) {
        return stack.getItem() instanceof CleansingItem item && item.variant() != MilkVariant.LINGERING;
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
        if (reagent.is(Items.GUNPOWDER) && isVariant(input, MilkVariant.REGULAR) && cfg.splashEnabled) {
            return MilkItemFactory.create(MilkVariant.SPLASH, MilkItemFactory.modeOf(input));
        }
        if (reagent.is(Items.DRAGON_BREATH) && isVariant(input, MilkVariant.SPLASH) && cfg.lingeringEnabled) {
            return MilkItemFactory.create(MilkVariant.LINGERING, MilkItemFactory.modeOf(input));
        }
        return ItemStack.EMPTY;
    }

    private static boolean isVariant(ItemStack stack, MilkVariant variant) {
        return stack.getItem() instanceof CleansingItem item && item.variant() == variant;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }

    private static boolean isPlainCleansing(ItemStack stack) {
        return isVariant(stack, MilkVariant.REGULAR)
            && MilkItemFactory.modeOf(stack) == CleanseMode.ALL;
    }

    private CleansingBrewing() {
    }
}
//?}
