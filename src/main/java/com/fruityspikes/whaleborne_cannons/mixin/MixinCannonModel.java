package com.fruityspikes.whaleborne_cannons.mixin;

import com.fruityspikes.whaleborne.client.models.CannonModel;
import com.fruityspikes.whaleborne.server.entities.CannonEntity;
import com.fruityspikes.whaleborne_cannons.server.entities.ICannonRider;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Lerps the cannon barrel's pitch across ticks via prevCannonXRot. The base model writes
 *  the raw cannonXRot into cannon.xRot and would otherwise jitter at the tick boundary. */
@Mixin(CannonModel.class)
public class MixinCannonModel {

    @Shadow(remap = false)
    private ModelPart cannon;

    @Inject(method = "setupAnim(Lcom/fruityspikes/whaleborne/server/entities/CannonEntity;FFFFF)V", at = @At("TAIL"), remap = false)
    private void smoothCannonRotation(CannonEntity entity, float partialTick, float v1, float v2, float v3, float v4, CallbackInfo ci) {
        if (entity instanceof ICannonRider rider) {
            float lerped = Mth.rotLerp(partialTick, rider.getPrevCannonXRot(), entity.getCannonXRot());
            this.cannon.xRot = (lerped + 90) * Mth.DEG_TO_RAD;
        }
    }
}
