package com.fruityspikes.whaleborne_cannons.mixin;

import com.fruityspikes.whaleborne.client.menus.CannonMenu;
import com.fruityspikes.whaleborne.server.entities.CannonEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CannonMenu.class)
public interface ICannonMenuAccessor {
    @Accessor("cannon")
    CannonEntity getCannon();
}
