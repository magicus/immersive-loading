/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
    private void disconnectStart(Screen screen, boolean bl, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (mc.level != null) {
            ImmersiveLoadingMod.getImmersiveLoading().onDisconnectingWorld();
        }
    }
}
