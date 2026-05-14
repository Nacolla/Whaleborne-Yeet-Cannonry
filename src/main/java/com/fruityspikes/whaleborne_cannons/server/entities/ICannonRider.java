package com.fruityspikes.whaleborne_cannons.server.entities;

import java.util.UUID;
import javax.annotation.Nullable;

public interface ICannonRider {
    @Nullable UUID getBarrelRider();
    void setBarrelRider(@Nullable UUID uuid);

    /** Previous-tick value of cannonXRot, for client-side render/camera interpolation. */
    float getPrevCannonXRot();
}
