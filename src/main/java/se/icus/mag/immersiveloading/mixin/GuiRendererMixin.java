/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.CubeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(GuiRenderer.class)
public abstract class GuiRendererMixin {
    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/CubeMap;render(FF)V"))
    private void wrapCubeMapRender(CubeMap instance, float alpha, float spin, Operation<Void> original) {
        boolean didIntercept = ImmersiveLoadingMod.getImmersiveLoading().interceptRenderPanorama();

        if (!didIntercept) {
            original.call(instance, alpha, spin);
        }
    }
}
