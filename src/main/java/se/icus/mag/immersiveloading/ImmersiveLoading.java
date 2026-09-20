/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading;

import java.nio.file.Path;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelStorageSource;

public class ImmersiveLoading {
    private static final String SCREENSHOT_FILENAME = "screenshot.png";

    private final Path serverBasePath;

    private WorldScreenshot worldScreenshot;
    private boolean shouldCapture;

    public ImmersiveLoading() {
        this.serverBasePath = Minecraft.getInstance()
                .gameDirectory
                .toPath()
                .resolve("serverdata")
                .resolve(ImmersiveLoadingMod.MOD_ID);
    }

    public void onNoSelectedWorld() {
        worldScreenshot = null;
    }

    public void onJoinLocalWorld(String levelId) {
        Path levelPath;
        try (LevelStorageSource.LevelStorageAccess access =
                Minecraft.getInstance().getLevelSource().createAccess(levelId)) {
            levelPath = access.getLevelDirectory().path();
        } catch (Exception e) {
            ImmersiveLoadingMod.LOGGER.error("Failed to access level directory for world: " + levelId, e);
            return;
        }
        Path screenshotPath =
                levelPath.resolve("data").resolve(ImmersiveLoadingMod.MOD_ID).resolve(SCREENSHOT_FILENAME);

        worldScreenshot = new WorldScreenshot(screenshotPath);
    }

    public void onJoinServer(String serverIp) {
        String serverId = serverIp.replaceAll("[^A-Za-z0-9._-]", "_");
        Path screenshotPath = serverBasePath.resolve(serverId).resolve(SCREENSHOT_FILENAME);

        worldScreenshot = new WorldScreenshot(screenshotPath);
    }

    public void onDisconnectingWorld() {
        // knownWorldScreenshotPath can be null since disconnect can be called even when not connected
        if (worldScreenshot == null) return;

        shouldCapture = true;
        // Trigger a final render to be able to capture the screenshot
        Minecraft.getInstance().gameRenderer.render(DeltaTracker.ZERO, true);
    }

    public void interceptRenderTakeScreenshot() {
        if (shouldCapture) {
            worldScreenshot.takeScreenshot();
            shouldCapture = false;
        }
    }

    public boolean interceptRenderPanorama() {
        if (worldScreenshot == null) return false;

        worldScreenshot.renderScreenshot();
        return true;
    }
}
