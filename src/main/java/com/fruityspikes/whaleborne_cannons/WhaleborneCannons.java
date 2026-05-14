package com.fruityspikes.whaleborne_cannons;

import com.fruityspikes.whaleborne_cannons.network.CannonFirePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(WhaleborneCannons.MODID)
public class WhaleborneCannons {
    public static final String MODID = "whaleborne_yeet_cannonry";

    public WhaleborneCannons(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                CannonFirePayload.TYPE,
                CannonFirePayload.STREAM_CODEC,
                CannonFirePayload::handle
        );
    }
}
