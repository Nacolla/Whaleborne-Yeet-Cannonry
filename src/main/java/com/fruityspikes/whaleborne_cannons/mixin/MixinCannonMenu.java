package com.fruityspikes.whaleborne_cannons.mixin;

import com.fruityspikes.whaleborne.server.entities.CannonEntity;
import com.fruityspikes.whaleborne_cannons.server.entities.ICannonRider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(com.fruityspikes.whaleborne.client.menus.CannonMenu.class)
public class MixinCannonMenu {

    @Shadow(remap = false)
    private CannonEntity cannon;

    /** Prevents shift-clicking the phantom head out of slot 0 (would duplicate it). */
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    public void onQuickMoveStackReturn(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index == 0 && cannon instanceof ICannonRider cannonRider && cannonRider.getBarrelRider() != null) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}