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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class WorldScreenshot {
    private static final ResourceLocation SCREENSHOT =
            ResourceLocation.fromNamespaceAndPath(ImmersiveLoadingMod.MOD_ID, "screenshot");

    private final Path screenshotPath;

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

    public void renderScreenshot(GuiGraphics graphics, Screen screen) {
        if (!textureLoaded) return;

        double scale = screen.height / (double) height;
        int renderWidth = (int) Math.round(width * scale);
        int renderHeight = screen.height;
        int x = (screen.width - renderWidth) / 2;

        // Black background in case scale does not match
        graphics.fill(0, 0, screen.width, screen.height, 0xFF000000);

        graphics.blit(
                RenderType::guiTextured,
                SCREENSHOT,
                x,
                0,
                0.0F,
                0.0F,
                renderWidth,
                renderHeight,
                width,
                height,
                width,
                height);

        graphics.flush();
    }
}
