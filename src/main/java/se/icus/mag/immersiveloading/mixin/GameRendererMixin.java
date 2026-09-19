/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "tryTakeScreenshotIfNeeded", at = @At("HEAD"))
    private void tryTakeScreenshotIfNeededStart(CallbackInfo ci) {
        ImmersiveLoadingMod.getImmersiveLoading().interceptRenderTakeScreenshot();
    }
}
