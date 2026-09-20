/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class WorldScreenshot {
    public static final Identifier SCREENSHOT =
            Identifier.fromNamespaceAndPath(ImmersiveLoadingMod.MOD_ID, "screenshot");

    private final Path screenshotPath;
    private final BackgroundRenderer renderer = new BackgroundRenderer();

    private boolean textureLoaded = false;
    private static int width;
    private static int height;

    public WorldScreenshot(Path screenshotPath) {
        this.screenshotPath = screenshotPath;
        preloadScreenshot();
    }

    public void takeScreenshot() {
        if (!Files.exists(screenshotPath.getParent())) {
            try {
                Files.createDirectories(screenshotPath.getParent());
            } catch (IOException e) {
                ImmersiveLoadingMod.LOGGER.error("Could not create screenshot directory", e);
                return;
            }
        }

        saveScreenshotAndReload();
    }

    private void saveScreenshotAndReload() {
        Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget(), image -> {
            try {
                image.writeToFile(screenshotPath);
            } catch (IOException e) {
                ImmersiveLoadingMod.LOGGER.error("Could not write background screenshot", e);
            } finally {
                image.close();
            }

            // Update to use new screenshot
            preloadScreenshot();
        });
    }

    private void preloadScreenshot() {
        if (!Files.exists(screenshotPath)) {
            ImmersiveLoadingMod.LOGGER.warn("Background screenshot does not exist: {		}", screenshotPath);
            return;
        }

        try (InputStream inputStream = Files.newInputStream(screenshotPath)) {
            NativeImage nativeImage = NativeImage.read(inputStream);
            DynamicTexture image = new DynamicTexture(SCREENSHOT::toString, nativeImage);
            Minecraft.getInstance().getTextureManager().register(SCREENSHOT, image);
            width = nativeImage.getWidth();
            height = nativeImage.getHeight();
            textureLoaded = true;
        } catch (IOException e) {
            ImmersiveLoadingMod.LOGGER.error("Could not read background screenshot", e);
        }
    }

    public void renderScreenshot() {
        if (!textureLoaded) return;

        renderer.render(width, height);
    }
}
