package so.alaz.cleansingpotions.mixin;

// Before 26.3 vanilla hardcodes the items a bottle slot accepts. NeoForge routes this check through
// PotionBrewing.isInput instead, so this mixin is Fabric only.
//? if fabric && <26.3 {
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import so.alaz.cleansingpotions.brew.CleansingBrewing;

// The target class is private, so it is named by string.
@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class BrewingStandPotionSlotMixin {

    @Inject(method = "mayPlaceItem(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void cleansingpotions$mayPlaceItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (CleansingBrewing.isBrewingInput(stack)) {
            cir.setReturnValue(true);
        }
    }
}
//?}
