package com.fruityspikes.whaleborne_cannons.network;

import com.fruityspikes.whaleborne_cannons.WhaleborneCannons;
import com.fruityspikes.whaleborne.server.entities.CannonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class CannonPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WhaleborneCannons.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, CannonFirePacket.class, CannonFirePacket::encode, CannonFirePacket::decode, CannonFirePacket::handle);
    }
}
