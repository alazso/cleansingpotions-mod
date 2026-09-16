package so.alaz.cleansingpotions.mixin;

// 26.3 made brewing data driven and deleted PotionBrewing outright, so this whole mixin is
// 26.2-only. On 26.3 the same five recipes ship as JSON under data/cleansingpotions/recipe/brewing.
//? if <26.3 {
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import so.alaz.cleansingpotions.brew.CleansingBrewing;
import so.alaz.cleansingpotions.item.MilkItemFactory;
import so.alaz.cleansingpotions.metrics.CleansingMetrics;

@Mixin(PotionBrewing.class)
public class PotionBrewingMixin {

    @Inject(method = "isIngredient", at = @At("HEAD"), cancellable = true)
    private void cleansingpotions$isIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CleansingBrewing.isIngredient(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private void cleansingpotions$hasMix(ItemStack input, ItemStack reagent, CallbackInfoReturnable<Boolean> cir) {
        if (!CleansingBrewing.mix(input, reagent).isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    // Vanilla swaps argument order between hasMix(input, reagent) and mix(reagent, input).
    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private void cleansingpotions$mix(ItemStack reagent, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = CleansingBrewing.mix(input, reagent);
        if (!result.isEmpty()) {
            cir.setReturnValue(result);
            CleansingMetrics.potionBrewed(brewKey(result));
        }
    }

    private static String brewKey(ItemStack result) {
        return switch (MilkItemFactory.variantOf(result)) {
            case SPLASH -> "splash";
            case LINGERING -> "lingering";
            case REGULAR -> MilkItemFactory.modeOf(result).potionName();
        };
    }
}
//?}
