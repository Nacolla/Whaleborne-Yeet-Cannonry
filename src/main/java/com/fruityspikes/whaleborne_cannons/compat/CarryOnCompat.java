package com.fruityspikes.whaleborne_cannons.compat;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public final class CarryOnCompat {

    private static final Logger LOG = LoggerFactory.getLogger(CarryOnCompat.class);

    private static final boolean AVAILABLE;
    private static final Method GET_CARRY_DATA;
    private static final Method SET_CARRY_DATA;
    private static final Method IS_CARRYING_PLAYER;
    private static final Method CLEAR;
    private static final Object TYPE_PLAYER;

    static {
        boolean ok = false;
        Method getData = null, setData = null, isCarryingPlayer = null, clear = null;
        Object typePlayer = null;
        try {
            ClassLoader cl = CarryOnCompat.class.getClassLoader();
            Class<?> manager = Class.forName("tschipp.carryon.common.carry.CarryOnDataManager", false, cl);
            Class<?> data = Class.forName("tschipp.carryon.common.carry.CarryOnData", false, cl);
            Class<?> carryType = Class.forName("tschipp.carryon.common.carry.CarryOnData$CarryType", false, cl);
            getData = manager.getMethod("getCarryData", Player.class);
            setData = manager.getMethod("setCarryData", Player.class, data);
            isCarryingPlayer = data.getMethod("isCarrying", carryType);
            clear = data.getMethod("clear");
            typePlayer = carryType.getField("PLAYER").get(null);
            ok = true;
        } catch (Throwable ignored) {
        }
        AVAILABLE = ok;
        GET_CARRY_DATA = getData;
        SET_CARRY_DATA = setData;
        IS_CARRYING_PLAYER = isCarryingPlayer;
        CLEAR = clear;
        TYPE_PLAYER = typePlayer;
        LOG.info("Carry On compatibility {}.", ok ? "enabled" : "disabled (mod not present)");
    }

    private CarryOnCompat() {}

    public static boolean isLoaded() {
        return AVAILABLE;
    }

    public static LivingEntity getCarriedPlayer(Player carrier) {
        if (!AVAILABLE)
            return null;
        Entity passenger = carrier.getFirstPassenger();
        if (!(passenger instanceof Player))
            return null;
        try {
            Object carry = GET_CARRY_DATA.invoke(null, carrier);
            if (carry == null || !((Boolean) IS_CARRYING_PLAYER.invoke(carry, TYPE_PLAYER)))
                return null;
        } catch (Throwable t) {
            return null;
        }
        return (LivingEntity) passenger;
    }

    public static boolean stopCarrying(ServerPlayer carrier) {
        if (!AVAILABLE)
            return false;
        try {
            Object carry = GET_CARRY_DATA.invoke(null, carrier);
            if (carry == null)
                return false;
            CLEAR.invoke(carry);
            SET_CARRY_DATA.invoke(null, carrier, carry);
            carrier.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            return true;
        } catch (Throwable t) {
            LOG.warn("Failed to clear Carry On carry state, skipping.", t);
            return false;
        }
    }
}
