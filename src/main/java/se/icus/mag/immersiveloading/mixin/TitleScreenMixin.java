package se.icus.mag.immersiveloading.mixin;

import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"))
    private void initStart(CallbackInfo ci) {
        ImmersiveLoadingMod.getImmersiveLoading().onNoSelectedWorld();
    }
}
