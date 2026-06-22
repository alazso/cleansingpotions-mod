package so.alaz.cleansingpotions.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import so.alaz.cleansingpotions.brew.CleansingBrewing;

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
        }
    }
}
