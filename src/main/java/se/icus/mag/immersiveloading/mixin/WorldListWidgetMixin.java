package se.icus.mag.immersiveloading.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.icus.mag.immersiveloading.ImmersiveLoadingMod;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListWidgetMixin {
    @Shadow
    @Final
    private LevelSummary summary;

    @Inject(method = "joinWorld", at = @At("HEAD"))
    private void joinWorldStart(CallbackInfo ci) {
        ImmersiveLoadingMod.getImmersiveLoading().onJoinLocalWorld(summary.getLevelId());
    }
}
