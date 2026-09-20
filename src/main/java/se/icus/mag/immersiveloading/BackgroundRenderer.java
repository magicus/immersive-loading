/*
 * Copyright © Magnus Ihse Bursie 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package se.icus.mag.immersiveloading;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
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
        Minecraft client = Minecraft.getInstance();
        WindowRenderState windowState = client.gameRenderer.getGameRenderState().windowRenderState;
        float screenWidth = windowState.width / (float) windowState.guiScale;
        float screenHeight = windowState.height / (float) windowState.guiScale;
        float scale = screenHeight / screenshotHeight;
        float renderWidth = screenshotWidth * scale;
        float x = (screenWidth - renderWidth) / 2.0F;

        PROJECTION.setupOrtho(1000.0F, 11000.0F, screenWidth, screenHeight, true);
        GpuBufferSlice projectionMatrix = PROJECTION_MATRIX_BUFFER.getBuffer(PROJECTION);
        RenderSystem.setProjectionMatrix(projectionMatrix, ProjectionType.ORTHOGRAPHIC);

        RenderTarget target = client.getMainRenderTarget();

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
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, pipeline.getVertexFormat());
        builder.addVertex(0, 0, 0).setColor(0xFF000000);
        builder.addVertex(0, screenHeight, 0).setColor(0xFF000000);
        builder.addVertex(screenWidth, screenHeight, 0).setColor(0xFF000000);
        builder.addVertex(screenWidth, 0, 0).setColor(0xFF000000);

        try (MeshData mesh = builder.buildOrThrow()) {
            GpuBuffer vertexBuffer = RenderSystem.getDevice()
                    .createBuffer(
                            () -> "Immersive Loading screenshot background",
                            GpuBuffer.USAGE_VERTEX,
                            mesh.vertexBuffer());

            int indexCount = mesh.drawState().indexCount();
            RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);

            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(
                            () -> "Immersive Loading screenshot background",
                            target.getColorTextureView(),
                            OptionalInt.empty(),
                            target.getDepthTextureView(),
                            OptionalDouble.empty())) {
                pass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", dynamicTransforms);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
                pass.drawIndexed(0, 0, indexCount, 1);
            }

            vertexBuffer.close();
        }
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
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, pipeline.getVertexFormat());
        builder.addVertex(x, y, 0).setUv(0.0F, 0.0F).setColor(0xFFFFFFFF);
        builder.addVertex(x, y + renderHeight, 0).setUv(0.0F, 1.0F).setColor(0xFFFFFFFF);
        builder.addVertex(x + renderWidth, y + renderHeight, 0)
                .setUv(1.0F, 1.0F)
                .setColor(0xFFFFFFFF);
        builder.addVertex(x + renderWidth, y, 0).setUv(1.0F, 0.0F).setColor(0xFFFFFFFF);

        try (MeshData mesh = builder.buildOrThrow()) {
            GpuBuffer vertexBuffer = RenderSystem.getDevice()
                    .createBuffer(() -> "Immersive Loading screenshot", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());

            int indexCount = mesh.drawState().indexCount();
            RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);

            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(
                            () -> "Immersive Loading screenshot",
                            target.getColorTextureView(),
                            OptionalInt.empty(),
                            target.getDepthTextureView(),
                            OptionalDouble.empty())) {
                pass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", dynamicTransforms);
                pass.setVertexBuffer(0, vertexBuffer);
                pass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());

                AbstractTexture screenshot = client.getTextureManager().getTexture(WorldScreenshot.SCREENSHOT);
                pass.bindTexture("Sampler0", screenshot.getTextureView(), screenshot.getSampler());

                pass.drawIndexed(0, 0, indexCount, 1);
            }

            vertexBuffer.close();
        }
    }
}
