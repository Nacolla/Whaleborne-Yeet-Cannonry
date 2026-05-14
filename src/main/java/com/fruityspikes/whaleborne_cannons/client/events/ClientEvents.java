package com.fruityspikes.whaleborne_cannons.client.events;

import com.fruityspikes.whaleborne.server.entities.CannonEntity;
import com.fruityspikes.whaleborne_cannons.WhaleborneCannons;
import com.fruityspikes.whaleborne_cannons.server.entities.ICannonRider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WhaleborneCannons.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();

        Entity vehicle = ((Entity) player).getVehicle();
        if (vehicle instanceof ICannonRider cannon) {
            if (player.getUUID().equals(cannon.getBarrelRider())) {
                event.setCanceled(true);
            }
        }
    }

    /** Locks the barrel rider's first-person camera to the cannon's aim when a gunner is in
     *  control. Third-person stays free. */
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;
        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (!(cameraEntity instanceof Player player)) return;
        Entity vehicle = ((Entity) player).getVehicle();
        if (!(vehicle instanceof ICannonRider cannonRider)) return;
        if (!player.getUUID().equals(cannonRider.getBarrelRider())) return;
        if (!(vehicle instanceof CannonEntity cannon)) return;

        boolean hasGunner = false;
        for (Entity p : cannon.getPassengers()) {
            if (!p.getUUID().equals(cannonRider.getBarrelRider())) {
                hasGunner = true;
                break;
            }
        }
        if (!hasGunner) return;

        float partialTick = (float) event.getPartialTick();
        event.setYaw(Mth.rotLerp(partialTick, cannon.prevWidgetYRot, cannon.getYRot()));
        event.setPitch(Mth.rotLerp(partialTick, cannonRider.getPrevCannonXRot(), cannon.getCannonXRot()));
    }

    /** In first person, overwrites xRot/yRot after mouse input so server packets carry the
     *  locked rotation. Third-person leaves input free. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!player.level().isClientSide) return;
        if (player != Minecraft.getInstance().player) return;
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

        Entity vehicle = ((Entity) player).getVehicle();
        if (!(vehicle instanceof ICannonRider cannonRider)) return;
        if (!player.getUUID().equals(cannonRider.getBarrelRider())) return;
        if (!(vehicle instanceof CannonEntity cannon)) return;

        boolean hasGunner = false;
        for (Entity p : cannon.getPassengers()) {
            if (!p.getUUID().equals(cannonRider.getBarrelRider())) {
                hasGunner = true;
                break;
            }
        }
        if (!hasGunner) return;

        player.setYRot(cannon.getYRot());
        player.setXRot(cannon.getCannonXRot());
        player.yRotO = cannon.getYRot();
        player.xRotO = cannon.getCannonXRot();
    }
}
