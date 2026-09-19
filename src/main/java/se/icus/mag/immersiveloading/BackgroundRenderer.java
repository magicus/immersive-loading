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
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class BackgroundRenderer {
    private static final CachedOrthoProjectionMatrixBuffer PROJECTION_MATRIX_BUFFER =
            new CachedOrthoProjectionMatrixBuffer("immersive_loading_screenshot", 1000.0F, 11000.0F, true);

    public void render(int x, int y, int renderWidth, int renderHeight, int screenWidth, int screenHeight) {
        Minecraft client = Minecraft.getInstance();

        GpuBufferSlice projectionMatrix = PROJECTION_MATRIX_BUFFER.getBuffer(screenWidth, screenHeight);
        RenderSystem.setProjectionMatrix(projectionMatrix, ProjectionType.ORTHOGRAPHIC);

        RenderTarget target = client.getMainRenderTarget();

        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        modelView.translate(0.0F, 0.0F, -11000.0F);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        new Matrix4f(modelView),
                        new Vector4f(1.0F, 1.0F, 1.0F, 1.0F),
                        new Vector3f(),
                        new Matrix4f(),
                        0.0F);
        modelView.popMatrix();

        try (RenderPass pass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> "Immersive Loading screenshot",
                        target.getColorTextureView(),
                        OptionalInt.empty(),
                        target.getDepthTextureView(),
                        OptionalDouble.empty())) {
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);

            // Black background in case scale does not match
            drawBlackBackground(pass, screenWidth, screenHeight);

            drawScreenshotQuad(pass, x, y, renderWidth, renderHeight);
        }
    }

    private void drawBlackBackground(RenderPass pass, int screenWidth, int screenHeight) {
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

            pass.setPipeline(pipeline);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());
            pass.drawIndexed(0, 0, indexCount, 1);

            vertexBuffer.close();
        }
    }

    private void drawScreenshotQuad(RenderPass pass, int x, int y, int renderWidth, int renderHeight) {
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

            pass.setPipeline(pipeline);
            pass.setVertexBuffer(0, vertexBuffer);
            pass.setIndexBuffer(indices.getBuffer(indexCount), indices.type());

            pass.bindSampler(
                    "Sampler0",
                    client.getTextureManager()
                            .getTexture(WorldScreenshot.SCREENSHOT)
                            .getTextureView());

            pass.drawIndexed(0, 0, indexCount, 1);

            vertexBuffer.close();
        }
    }
}
