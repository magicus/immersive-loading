package se.icus.mag.immersiveloading.mixin;

import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin {
    @Inject(method = "init", at = @At("HEAD"))
    private void initStart(CallbackInfo ci) {
        ImmersiveLoadingMod.getImmersiveLoading().onNoSelectedWorld();
    }

    @Inject(method = "join", at = @At("HEAD"))
    private void joinStart(ServerData serverData, CallbackInfo ci) {
        ImmersiveLoadingMod.getImmersiveLoading().onJoinServer(serverData.ip);
    }
}
