package com.fruityspikes.whaleborne_cannons.mixin;

import com.fruityspikes.whaleborne.client.models.CannonModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CannonModel.class)
public interface ICannonModelAccessor {
    @Accessor("cannon")
    ModelPart getCannonPart();
}
