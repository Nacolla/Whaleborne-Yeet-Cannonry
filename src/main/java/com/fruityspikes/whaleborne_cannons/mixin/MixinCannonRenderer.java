package com.fruityspikes.whaleborne_cannons.mixin;

import com.fruityspikes.whaleborne.client.models.CannonModel;
import com.fruityspikes.whaleborne.client.renderers.WhaleWidgetRenderer;
import com.fruityspikes.whaleborne.server.entities.CannonEntity;
import com.fruityspikes.whaleborne.server.entities.WhaleWidgetEntity;
import com.fruityspikes.whaleborne_cannons.server.entities.ICannonRider;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(WhaleWidgetRenderer.class)
public class MixinCannonRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.BEFORE))
    public void onRender(WhaleWidgetEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity instanceof CannonEntity cannon && entity instanceof ICannonRider cannonRider) {
            UUID riderId = cannonRider.getBarrelRider();
            if (riderId != null) {
                if (((WhaleWidgetRenderer) (Object) this).getModel() instanceof CannonModel cannonModel) {
                    ModelPart barrelBone = ((ICannonModelAccessor) cannonModel).getCannonPart();

                    ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);
                    net.minecraft.world.entity.player.Player player = cannon.level().getPlayerByUUID(riderId);

                    if (player != null) {
                        if (player == net.minecraft.client.Minecraft.getInstance().player &&
                                net.minecraft.client.Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                            return;
                        }

                        CompoundTag tag = headStack.getOrCreateTag();
                        CompoundTag ownerTag = new CompoundTag();
                        NbtUtils.writeGameProfile(ownerTag, player.getGameProfile());
                        tag.put("SkullOwner", ownerTag);

                        poseStack.pushPose();

                        // The injection point is just before popPose, so the model is still in
                        // the entity-local pose. Walk the barrel bone from there to the muzzle.
                        barrelBone.translateAndRotate(poseStack);
                        poseStack.translate(0.0, -36.0 / 18.0, 0.25);
                        poseStack.mulPose(Axis.XP.rotationDegrees(90));

                        net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(headStack,
                                ItemDisplayContext.HEAD,
                                packedLight,
                                OverlayTexture.NO_OVERLAY,
                                poseStack,
                                buffer,
                                entity.level(),
                                0);

                        poseStack.popPose();
                    }
                }
            }
        }
    }
}
