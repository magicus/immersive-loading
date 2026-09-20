/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Inject(method = "disconnectFromWorld", at = @At("HEAD"))
    private void disconnectFromWorldStart(Component component, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.level != null) {
            ImmersiveLoadingMod.getImmersiveLoading().onDisconnectingWorld();
        }
    }
}
