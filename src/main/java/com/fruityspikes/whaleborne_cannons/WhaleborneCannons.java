package com.fruityspikes.whaleborne_cannons;

import com.fruityspikes.whaleborne_cannons.network.CannonPacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(WhaleborneCannons.MODID)
public class WhaleborneCannons {
    public static final String MODID = "whaleborne_yeet_cannonry";

    public WhaleborneCannons() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CannonPacketHandler.register();
    }
}
