package com.fruityspikes.whaleborne_cannons.mixin;

import com.fruityspikes.whaleborne.server.entities.CannonEntity;
import com.fruityspikes.whaleborne.server.entities.RideableWhaleWidgetEntity;
import com.fruityspikes.whaleborne_cannons.compat.CarryOnCompat;
import com.fruityspikes.whaleborne_cannons.server.entities.CannonPartEntity;
import com.fruityspikes.whaleborne_cannons.server.entities.ICannonMultipart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/** Adds human-cannonball mechanics and a multipart barrel hitbox to CannonEntity. */
@Mixin(CannonEntity.class)
public abstract class MixinCannonEntity extends RideableWhaleWidgetEntity implements ICannonMultipart, com.fruityspikes.whaleborne_cannons.server.entities.ICannonRider {

    @Shadow(remap = false) public SimpleContainer inventory;
    @Shadow(remap = false) public abstract float getCannonXRot();
    @Shadow(remap = false) public abstract void setCannonXRot(float xRot);

    @Unique
    private static EntityDataSerializer<Optional<UUID>> whaleborne_cannons$getOptionalUuidSerializer() {
        try {
            return EntityDataSerializers.OPTIONAL_UUID;
        } catch (NoSuchFieldError | NoClassDefFoundError e) {
            try {
                java.lang.reflect.Field field = EntityDataSerializers.class.getDeclaredField("f_135041_");
                return (EntityDataSerializer<Optional<UUID>>) field.get(null);
            } catch (Exception ex) {
                throw new RuntimeException("Could not find OPTIONAL_UUID serializer", ex);
            }
        }
    }

    @Unique
    private static EntityDataSerializer<Integer> whaleborne_cannons$getIntSerializer() {
        try {
            return EntityDataSerializers.INT;
        } catch (NoSuchFieldError | NoClassDefFoundError e) {
            try {
                java.lang.reflect.Field field = EntityDataSerializers.class.getDeclaredField("f_135028_");
                return (EntityDataSerializer<Integer>) field.get(null);
            } catch (Exception ex) {
                throw new RuntimeException("Could not find INT serializer", ex);
            }
        }
    }

    @Unique private CannonPartEntity whaleborne_cannons$barrel;
    @Unique private CannonPartEntity[] whaleborne_cannons$parts;

    @Unique private static final EntityDataAccessor<Optional<UUID>> DATA_BARREL_RIDER = SynchedEntityData.defineId(CannonEntity.class, whaleborne_cannons$getOptionalUuidSerializer());
    @Unique private static final EntityDataAccessor<Integer> DATA_BARREL_ID =
        SynchedEntityData.defineId(CannonEntity.class, whaleborne_cannons$getIntSerializer());

    @Unique private float whaleborne_cannons$prevCannonXRot;
    @Unique private int whaleborne_cannons$launchTimer = 0;
    @Unique private Entity whaleborne_cannons$entityToLaunch = null;
    @Unique private Vec3 whaleborne_cannons$launchVelocity = null;
    @Unique private boolean whaleborne_cannons$expectingRemount = false;

    public MixinCannonEntity(EntityType<?> entityType, Level level, Item dropItem) {
        super(entityType, level, dropItem);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void onConstruct(EntityType<?> entityType, Level level, CallbackInfo ci) {
        this.whaleborne_cannons$barrel = new CannonPartEntity((CannonEntity)(Object)this, "barrel", 1.0F, 1.0F);
        this.whaleborne_cannons$parts = new CannonPartEntity[]{this.whaleborne_cannons$barrel};

        if (!level.isClientSide) {
            this.entityData.set(DATA_BARREL_ID, this.whaleborne_cannons$barrel.getId());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BARREL_RIDER, Optional.empty());
        builder.define(DATA_BARREL_ID, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isVehicle()) {
            // rotatePassengers skips ridden cannon, so prevWidget fields must be updated here
            // for smooth client-side render interpolation.
            this.prevWidgetYRot = this.getYRot();
            this.prevWidgetXRot = this.getXRot();

            // Gunner controls aim when present, otherwise the barrel rider falls through.
            Entity controller = null;
            UUID barrelRiderId = whaleborne_cannons$getBarrelRider();

            for (Entity p : getPassengers()) {
                if (barrelRiderId != null && p.getUUID().equals(barrelRiderId)) {
                    continue;
                }
                controller = p;
                break;
            }

            if (controller == null && !getPassengers().isEmpty()) {
                 controller = getPassengers().get(0);
            }

            this.whaleborne_cannons$prevCannonXRot = this.getCannonXRot();
            if (controller != null) {
                this.setCannonXRot(Mth.rotLerp(0.5f, this.getCannonXRot(), controller.getXRot()));
                this.setYRot(Mth.rotLerp(0.5f, this.getYRot(), controller.getYRot()));
            }
        }

        if (!this.level().isClientSide) {
            UUID barrelRider = whaleborne_cannons$getBarrelRider();
            if (barrelRider != null) {
                boolean riding = false;
                for (Entity p : getPassengers()) {
                    if (p.getUUID().equals(barrelRider)) {
                        riding = true;
                        break;
                    }
                }
                if (riding) {
                    this.whaleborne_cannons$expectingRemount = false;
                } else {
                    Player rider = this.level().getPlayerByUUID(barrelRider);
                    if (rider == null) {
                        // Rider offline: hold the seat and head until they return.
                        this.whaleborne_cannons$expectingRemount = true;
                    } else if (this.whaleborne_cannons$expectingRemount) {
                        // Rider reconnected (or cannon just reloaded): remount at the muzzle.
                        if (!rider.isPassenger()) {
                            rider.startRiding(this, true);
                        }
                        if (rider.getVehicle() == this) {
                            this.whaleborne_cannons$expectingRemount = false;
                        }
                    } else {
                        // Rider online but dismounted on purpose (sneak/eject): release the seat.
                        whaleborne_cannons$setBarrelRider(null);
                    }
                }
            }
        }

        if (this.level().isClientSide && this.whaleborne_cannons$barrel != null) {
            int partId = this.entityData.get(DATA_BARREL_ID);
            if (partId != 0 && this.whaleborne_cannons$barrel.getId() != partId) {
                this.whaleborne_cannons$barrel.setId(partId);
            }
        }

        if (this.whaleborne_cannons$barrel != null) {
            double barrelLength = 2.5;
            double heightOffset = 0.5;

            double xRotRad = Math.toRadians(this.getCannonXRot());
            double yRotRad = Math.toRadians(-this.getYRot());

            double hDist = barrelLength * Math.cos(xRotRad);
            double yDist = barrelLength * Math.sin(-xRotRad);

            double xOff = hDist * Math.sin(yRotRad);
            double zOff = hDist * Math.cos(yRotRad);

            this.whaleborne_cannons$barrel.setPos(
                this.getX() + xOff,
                this.getY() + yDist + heightOffset,
                this.getZ() + zOff
            );
            this.whaleborne_cannons$barrel.tick();
        }

        if (this.whaleborne_cannons$launchTimer > 0) {
            this.whaleborne_cannons$launchTimer--;

            if (this.whaleborne_cannons$launchTimer == 0 &&
                this.whaleborne_cannons$entityToLaunch != null &&
                this.whaleborne_cannons$launchVelocity != null) {

                Entity p = this.whaleborne_cannons$entityToLaunch;
                if (p.isAlive()) {
                    p.setDeltaMovement(this.whaleborne_cannons$launchVelocity);
                    p.hurtMarked = true;
                    p.hasImpulse = true;
                    p.setOnGround(false);

                    if (p instanceof ServerPlayer sp) {
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(
                            p.getId(), this.whaleborne_cannons$launchVelocity));
                    }
                }

                this.whaleborne_cannons$entityToLaunch = null;
                this.whaleborne_cannons$launchVelocity = null;
            }
        }
    }

    /** Ejects the barrel rider if the phantom head is removed from slot 0. */
    @Inject(method = "containerChanged", at = @At("HEAD"), remap = false)
    private void onContainerChanged(net.minecraft.world.Container container, CallbackInfo ci) {
        if (!this.level().isClientSide) {
            UUID barrelRider = whaleborne_cannons$getBarrelRider();
            if (barrelRider != null && this.inventory.getItem(0).isEmpty()) {
                for (Entity p : getPassengers()) {
                    if (p.getUUID().equals(barrelRider)) {
                        p.stopRiding();
                        break;
                    }
                }
                // Head gone: drop the reservation so an offline rider stays put instead of snapping back.
                whaleborne_cannons$setBarrelRider(null);
            }
        }
    }

    /** Persists the barrel rider UUID across save/load so the rider remounts at the muzzle on world reload. */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void saveBarrelRider(CompoundTag tag, CallbackInfo ci) {
        UUID barrelRider = whaleborne_cannons$getBarrelRider();
        if (barrelRider != null) {
            tag.putUUID("WhaleborneCannonsBarrelRider", barrelRider);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void loadBarrelRider(CompoundTag tag, CallbackInfo ci) {
        if (tag.hasUUID("WhaleborneCannonsBarrelRider")) {
            this.entityData.set(DATA_BARREL_RIDER, Optional.of(tag.getUUID("WhaleborneCannonsBarrelRider")));
            this.whaleborne_cannons$expectingRemount = true;
        }
    }

    /** Strips the phantom head from inventory before destroy so it isn't dropped. */
    @Inject(method = "remove", at = @At("HEAD"), remap = false)
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            if (whaleborne_cannons$getBarrelRider() != null) {
                ItemStack stack = this.inventory.getItem(0);
                if (!stack.isEmpty() && stack.is(Items.PLAYER_HEAD)) {
                    this.inventory.setItem(0, ItemStack.EMPTY);
                }
            }
        }
        if (reason.shouldDestroy()) {
            whaleborne_cannons$setBarrelRider(null);
        }
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        UUID barrelRider = whaleborne_cannons$getBarrelRider();
        if (barrelRider != null && passenger.getUUID().equals(barrelRider)) {
             double barrelLength = 3.0;
             double heightOffset = -0.2;

             double xRotRad = Math.toRadians(this.getCannonXRot());
             double yRotRad = Math.toRadians(-this.getYRot());

             double hDist = barrelLength * Math.cos(xRotRad);
             double yDist = barrelLength * Math.sin(-xRotRad);

             double xOff = hDist * Math.sin(yRotRad);
             double zOff = hDist * Math.cos(yRotRad);

             callback.accept(passenger, this.getX() + xOff, this.getY() + yDist + heightOffset, this.getZ() + zOff);
             return;
        }

        super.positionRider(passenger, callback);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity passenger) {
        UUID barrelRider = whaleborne_cannons$getBarrelRider();
        if (barrelRider != null && passenger.getUUID().equals(barrelRider)) {
            double barrelLength = 3.8;
            Vec3 direction = Vec3.directionFromRotation(this.getCannonXRot(), this.getYRot());
            return this.position().add(0, 1, 0).add(direction.scale(barrelLength));
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    @Nullable
    public net.minecraft.world.entity.LivingEntity getControllingPassenger() {
        UUID barrelRiderId = whaleborne_cannons$getBarrelRider();
        for (Entity p : getPassengers()) {
            if (p instanceof net.minecraft.world.entity.LivingEntity le && (barrelRiderId == null || !p.getUUID().equals(barrelRiderId))) {
                return le;
            }
        }
        if (!getPassengers().isEmpty() && getPassengers().get(0) instanceof net.minecraft.world.entity.LivingEntity le) {
             return le;
        }
        return null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 2 && passenger instanceof net.minecraft.world.entity.LivingEntity;
    }

    @Override
    public void ejectPassengers() {
        if (!this.level().isClientSide && whaleborne_cannons$getBarrelRider() != null) {
            whaleborne_cannons$setBarrelRider(null);
        }
        super.ejectPassengers();
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.whaleborne_cannons$parts;
    }

    @Override
    public InteractionResult interactPart(CannonPartEntity part, Player player, Vec3 vec, InteractionHand hand) {
        if (part == this.whaleborne_cannons$barrel) {
            if (CarryOnCompat.isLoaded() && player.isShiftKeyDown()) {
                net.minecraft.world.entity.LivingEntity carried = CarryOnCompat.getCarriedPlayer(player);
                if (carried != null) {
                    if (!this.level().isClientSide) {
                        if (whaleborne_cannons$getBarrelRider() != null || !this.inventory.getItem(0).isEmpty()) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.whaleborne.cannon_full"), true);
                        } else if (CarryOnCompat.stopCarrying((ServerPlayer) player)) {
                            whaleborne_cannons$setBarrelRider(carried.getUUID());
                            if (!(carried.startRiding(this, true) && carried.getVehicle() == this)) {
                                carried.stopRiding();
                                whaleborne_cannons$setBarrelRider(null);
                            }
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            if (whaleborne_cannons$getBarrelRider() == null) {
                if (!this.level().isClientSide) {
                    if (!this.inventory.getItem(0).isEmpty()) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.whaleborne.cannon_full"), true);
                        return InteractionResult.FAIL;
                    }
                    whaleborne_cannons$setBarrelRider(player.getUUID());
                    player.startRiding(this);
                }
                return InteractionResult.SUCCESS;
            } else {
                if (!this.level().isClientSide) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.whaleborne.cannon_full"), true);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        if (CarryOnCompat.isLoaded() && player.isShiftKeyDown()) {
            net.minecraft.world.entity.LivingEntity carried = CarryOnCompat.getCarriedPlayer(player);
            if (carried != null) {
                if (!this.level().isClientSide && this.getPassengers().size() < 2
                        && CarryOnCompat.stopCarrying((ServerPlayer) player)) {
                    Entity rider = null;
                    UUID barrelRiderId = whaleborne_cannons$getBarrelRider();
                    for (Entity p : getPassengers()) {
                        if (barrelRiderId != null && p.getUUID().equals(barrelRiderId)) rider = p;
                    }
                    if (rider != null) rider.stopRiding();
                    carried.startRiding(this, true);
                    if (rider != null) rider.startRiding(this, true);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.interactAt(player, vec, hand);
    }

    /** Mounts a second player as gunner, re-seating them into passenger 0 because vanilla
     *  input routing uses getFirstPassenger and the barrel rider can't occupy that slot. */
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true, remap = false)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.getItemInHand(hand).isEmpty()) {
            UUID barrelRiderId = whaleborne_cannons$getBarrelRider();
            if (barrelRiderId != null && !getPassengers().isEmpty()) {
                Entity barrelRiderEntity = null;
                boolean hasGunner = false;
                for (Entity p : getPassengers()) {
                    if (p.getUUID().equals(barrelRiderId)) {
                        barrelRiderEntity = p;
                    } else {
                        hasGunner = true;
                    }
                }
                if (!hasGunner && barrelRiderEntity != null) {
                    if (!this.level().isClientSide) {
                        barrelRiderEntity.stopRiding();
                        player.startRiding(this);
                        barrelRiderEntity.startRiding(this);
                    }
                    cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
                }
            }
        }
    }

    /** Launches the loaded barrel rider instead of firing an item. */
    @Inject(method = "fireCannon", at = @At("HEAD"), cancellable = true, remap = false)
    private void onFireCannon(int power, CallbackInfo ci) {
        Entity barrelPassenger = null;
        UUID barrelRider = whaleborne_cannons$getBarrelRider();

        if (barrelRider != null) {
            for (Entity p : getPassengers()) {
                if (p.getUUID().equals(barrelRider)) {
                    barrelPassenger = p;
                    break;
                }
            }
        }

        if (barrelRider != null && barrelPassenger == null) {
            // Don't fire a seat reserved for an offline rider.
            ci.cancel();
            return;
        }

        if (barrelPassenger != null && barrelPassenger instanceof net.minecraft.world.entity.LivingEntity livingPassenger) {
            ItemStack gunpowder = inventory.getItem(1);
            if (gunpowder.isEmpty()) {
                level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                ci.cancel();
                return;
            }
            gunpowder.shrink(1);

            float launchPower = Math.max(power, 60f);
            double speed = (double) launchPower / 32.0;

            Vec3 lookAngle = Vec3.directionFromRotation(this.getCannonXRot(), this.getYRot());

            double barrelLength = 3.8;
            Vec3 direction = Vec3.directionFromRotation(this.getCannonXRot(), this.getYRot());
            Vec3 dismountPos = this.position().add(0, 1, 0).add(direction.scale(barrelLength));

            barrelPassenger.stopRiding();
            whaleborne_cannons$setBarrelRider(null);

            barrelPassenger.moveTo(dismountPos.x, dismountPos.y, dismountPos.z, this.getYRot(), this.getCannonXRot());
            if (barrelPassenger instanceof ServerPlayer sp) {
                sp.connection.teleport(dismountPos.x, dismountPos.y, dismountPos.z, this.getYRot(), this.getCannonXRot());
            }

            this.whaleborne_cannons$entityToLaunch = barrelPassenger;
            this.whaleborne_cannons$launchVelocity = lookAngle.scale(speed);
            this.whaleborne_cannons$launchTimer = 2;

            level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F,
                launchPower / 100 + (this.random.nextFloat() * 0.4F));

            ci.cancel();
        }
    }

    @Unique
    private void whaleborne_cannons$setBarrelRider(@Nullable UUID uuid) {
        this.entityData.set(DATA_BARREL_RIDER, Optional.ofNullable(uuid));
        this.whaleborne_cannons$expectingRemount = false;

        if (!this.level().isClientSide) {
            if (uuid != null) {
                ItemStack head = new ItemStack(Items.PLAYER_HEAD);
                Player p = this.level().getPlayerByUUID(uuid);
                if (p != null) {
                    head.set(net.minecraft.core.component.DataComponents.PROFILE,
                        new net.minecraft.world.item.component.ResolvableProfile(p.getGameProfile()));
                }
                inventory.setItem(0, head);
            } else {
                if (inventory.getItem(0).is(Items.PLAYER_HEAD)) {
                    inventory.setItem(0, ItemStack.EMPTY);
                }
            }
        }
    }

    @Unique
    private @Nullable UUID whaleborne_cannons$getBarrelRider() {
        return this.entityData.get(DATA_BARREL_RIDER).orElse(null);
    }

    @Override
    public @Nullable UUID getBarrelRider() {
        return whaleborne_cannons$getBarrelRider();
    }

    @Override
    public void setBarrelRider(@Nullable UUID uuid) {
        whaleborne_cannons$setBarrelRider(uuid);
    }

    @Override
    public float getPrevCannonXRot() {
        return this.whaleborne_cannons$prevCannonXRot;
    }

    public CannonPartEntity getCannonBarrel() {
        return this.whaleborne_cannons$barrel;
    }
}
