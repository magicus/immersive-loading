package se.icus.mag.immersiveloading;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;

public final class ImmersiveLoadingMod implements ClientModInitializer {
    public static final String MOD_ID = "immersive-loading";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final ImmersiveLoading IMMERSIVE_LOADING = new ImmersiveLoading();

    @Override
    public void onInitializeClient() {}

    public static ImmersiveLoading getImmersiveLoading() {
        return IMMERSIVE_LOADING;
    }
}
