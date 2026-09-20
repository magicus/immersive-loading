/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(WorldSelectionList.class)
public abstract class WorldSelectionListMixin {
    @Inject(method = "returnToScreen", at = @At("HEAD"))
    private void returnToScreenStart(CallbackInfo ci) {
        ImmersiveLoadingMod.getImmersiveLoading().onNoSelectedWorld();
    }
}
