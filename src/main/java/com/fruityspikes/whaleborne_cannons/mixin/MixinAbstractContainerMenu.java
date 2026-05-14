package com.fruityspikes.whaleborne_cannons.mixin;

import com.fruityspikes.whaleborne.client.menus.CannonMenu;
import com.fruityspikes.whaleborne_cannons.server.entities.ICannonRider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Blocks direct interaction with the cannon's ammo slot while a barrel rider is mounted. */
@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenu {

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true, remap = false)
    public void onClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (!(menu instanceof CannonMenu)) return;

        ICannonMenuAccessor accessor = (ICannonMenuAccessor) menu;

        if (slotId >= 0 && slotId < menu.slots.size()) {
            Slot slot = menu.slots.get(slotId);

            if (slot.container == accessor.getCannon().inventory && slot.getSlotIndex() == 0) {
                if (accessor.getCannon() instanceof ICannonRider cannonRider && cannonRider.getBarrelRider() != null) {
                    if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE ||
                        clickType == ClickType.SWAP || clickType == ClickType.THROW ||
                        clickType == ClickType.CLONE) {
                        // Clearing slot 0 destroys the head and ejects the rider via containerChanged.
                        slot.set(ItemStack.EMPTY);
                        ci.cancel();
                    }
                }
            }
        }
    }
}
