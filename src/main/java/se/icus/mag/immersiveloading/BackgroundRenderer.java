/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BackgroundRenderer {
    private static final Projection PROJECTION = new Projection();
    private static final ProjectionMatrixBuffer PROJECTION_MATRIX_BUFFER =
            new ProjectionMatrixBuffer("immersive_loading_screenshot");

    public void render(int screenshotWidth, int screenshotHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        WindowRenderState windowState = minecraft.gameRenderer.gameRenderState().windowRenderState;
        float screenWidth = windowState.width / (float) windowState.guiScale;
        float screenHeight = windowState.height / (float) windowState.guiScale;
        float scale = screenHeight / screenshotHeight;
        float renderWidth = screenshotWidth * scale;
        float x = (screenWidth - renderWidth) / 2.0F;

        PROJECTION.setupOrtho(1000.0F, 11000.0F, screenWidth, screenHeight, true);
        GpuBufferSlice projectionMatrix = PROJECTION_MATRIX_BUFFER.getBuffer(PROJECTION);
        RenderSystem.setProjectionMatrix(projectionMatrix, ProjectionType.ORTHOGRAPHIC);

        RenderTarget target = minecraft.gameRenderer.mainRenderTarget();

        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        modelView.translate(0.0F, 0.0F, -11000.0F);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        new Matrix4f(modelView), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
        modelView.popMatrix();

        // Black background in case scale does not match
        drawBlackBackground(target, dynamicTransforms, screenWidth, screenHeight);
        drawScreenshotQuad(target, dynamicTransforms, x, 0.0F, renderWidth, screenHeight);
    }

    private void drawBlackBackground(
            RenderTarget target, GpuBufferSlice dynamicTransforms, float screenWidth, float screenHeight) {
        RenderPipeline pipeline = RenderPipelines.GUI;
        drawQuad(
                target,
                dynamicTransforms,
                pipeline,
                "Immersive Loading screenshot background",
                builder -> {
                    builder.addVertex(0, 0, 0).setColor(0xFF000000);
                    builder.addVertex(0, screenHeight, 0).setColor(0xFF000000);
                    builder.addVertex(screenWidth, screenHeight, 0).setColor(0xFF000000);
                    builder.addVertex(screenWidth, 0, 0).setColor(0xFF000000);
                },
                pass -> {});
    }

    private void drawScreenshotQuad(
            RenderTarget target,
            GpuBufferSlice dynamicTransforms,
            float x,
            float y,
            float renderWidth,
            float renderHeight) {
        Minecraft client = Minecraft.getInstance();
        RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
        AbstractTexture screenshot = client.getTextureManager().getTexture(WorldScreenshot.SCREENSHOT);
        drawQuad(
                target,
                dynamicTransforms,
                pipeline,
                "Immersive Loading screenshot",
                builder -> {
                    builder.addVertex(x, y, 0).setUv(0.0F, 1.0F).setColor(0xFFFFFFFF);
                    builder.addVertex(x, y + renderHeight, 0).setUv(0.0F, 0.0F).setColor(0xFFFFFFFF);
                    builder.addVertex(x + renderWidth, y + renderHeight, 0)
                            .setUv(1.0F, 0.0F)
                            .setColor(0xFFFFFFFF);
                    builder.addVertex(x + renderWidth, y, 0).setUv(1.0F, 1.0F).setColor(0xFFFFFFFF);
                },
                pass -> pass.setUniform("Sampler0", screenshot.getTextureView(), screenshot.getSampler()));
    }

    private void drawQuad(
            RenderTarget target,
            GpuBufferSlice dynamicTransforms,
            RenderPipeline pipeline,
            String label,
            Consumer<BufferBuilder> buildVertices,
            Consumer<RenderPass> configurePass) {
        PrimitiveTopology primitiveTopology = pipeline.getPrimitiveTopology();
        VertexFormat vertexFormat = Objects.requireNonNull(pipeline.getVertexFormatBinding(0));
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(4 * vertexFormat.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(byteBufferBuilder, primitiveTopology, vertexFormat);
            buildVertices.accept(builder);

            try (MeshData mesh = builder.buildOrThrow()) {
                try (GpuBuffer vertexBuffer = RenderSystem.getDevice()
                        .createBuffer(() -> label, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer())) {
                    int indexCount = mesh.drawState().indexCount();
                    RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(primitiveTopology);

                    try (RenderPass pass = RenderSystem.getDevice()
                            .createCommandEncoder()
                            .createRenderPass(
                                    () -> label,
                                    target.getColorTextureView(),
                                    Optional.empty(),
                                    target.getDepthTextureView(),
                                    OptionalDouble.empty())) {
                        pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
                        RenderSystem.bindDefaultUniforms(pass);
                        pass.setUniform("DynamicTransforms", dynamicTransforms);
                        pass.setVertexBuffer(0, vertexBuffer.slice());
                        pass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
                        configurePass.accept(pass);
                        pass.drawIndexed(indexCount, 1, 0, 0, 0);
                    }
                }
            }
        }
    }

    public void updateTexture(RenderTarget target) {
        GpuTexture source = target.getColorTexture();
        if (source == null) {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        }

        GpuTextureSnapshot screenshot = new GpuTextureSnapshot(source);
        RenderSystem.getDevice()
                .createCommandEncoder()
                .copyTextureToTexture(source, screenshot.getTexture(), 0, 0, 0, 0, 0, target.width, target.height);
        Minecraft.getInstance().getTextureManager().register(WorldScreenshot.SCREENSHOT, screenshot);
    }

    private static class GpuTextureSnapshot extends AbstractTexture {
        GpuTextureSnapshot(GpuTexture source) {
            GpuDevice device = RenderSystem.getDevice();
            texture = device.createTexture(
                    WorldScreenshot.SCREENSHOT::toString,
                    GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
                    source.getFormat(),
                    source.getWidth(0),
                    source.getHeight(0),
                    1,
                    1);
            textureView = device.createTextureView(texture);
            sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        }
    }
}
