package com.fruityspikes.whaleborne_cannons.server.entities;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ICannonMultipart {
    InteractionResult interactPart(CannonPartEntity part, Player player, Vec3 vec, InteractionHand hand);
}
