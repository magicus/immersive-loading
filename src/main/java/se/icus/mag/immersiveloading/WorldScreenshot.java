/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

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

        updateScreenshotTexture();
        saveScreenshot();
    }

    private void updateScreenshotTexture() {
        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        renderer.updateTexture(target);

        width = target.width;
        height = target.height;
    }

    private void saveScreenshot() {
        Screenshot.takeScreenshot(
                Minecraft.getInstance().gameRenderer.mainRenderTarget(),
                image -> Util.ioPool().execute(() -> {
                    try (image) {
                        image.writeToFile(screenshotPath);
                    } catch (IOException e) {
                        ImmersiveLoadingMod.LOGGER.error("Could not write background screenshot", e);
                    }
                }));
    }

    private void preloadScreenshot() {
        if (!Files.exists(screenshotPath)) {
            ImmersiveLoadingMod.LOGGER.warn("Background screenshot does not exist: {		}", screenshotPath);
            return;
        }

        try (InputStream inputStream = Files.newInputStream(screenshotPath)) {
            NativeImage nativeImage = NativeImage.read(inputStream);
            // Flip upside down to match GpuTextureSnapshot
            NativeImage textureImage =
                    new NativeImage(nativeImage.format(), nativeImage.getWidth(), nativeImage.getHeight(), false);
            nativeImage.copyRect(
                    textureImage, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, true);
            nativeImage.close();

            DynamicTexture image = new DynamicTexture(SCREENSHOT::toString, textureImage);
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
