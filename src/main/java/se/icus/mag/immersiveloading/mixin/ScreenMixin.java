/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "renderPanorama", at = @At("TAIL"), cancellable = true)
    private void renderPanoramaEnd(GuiGraphics context, float delta, CallbackInfo ci) {
        ImmersiveLoadingMod.getImmersiveLoading().interceptRenderPanorama(context, (Screen) (Object) this);
    }
}
