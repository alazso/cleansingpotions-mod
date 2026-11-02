package so.alaz.cleansingpotions.mixin;

// Before 26.3 vanilla hardcodes the items a hopper can put in a bottle slot. NeoForge routes this check
// through PotionBrewing.isInput, so this mixin is Fabric only.
//? if fabric && <26.3 {
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import so.alaz.cleansingpotions.brew.CleansingBrewing;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {

    // Slots 0 to 2 hold the bottles.
    @Inject(method = "canPlaceItem(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void cleansingpotions$canPlaceItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot < 3 && CleansingBrewing.isBrewingInput(stack)) {
            cir.setReturnValue(((BrewingStandBlockEntity) (Object) this).getItem(slot).isEmpty());
        }
    }
}
//?}
