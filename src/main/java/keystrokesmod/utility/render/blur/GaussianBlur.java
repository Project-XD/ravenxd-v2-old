package keystrokesmod.utility.render.blur;

import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.ColorUtils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.jetbrains.annotations.Range;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static keystrokesmod.Client.mc;
import static keystrokesmod.utility.render.blur.StencilUtil.checkSetupFBO;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUniform1;

/**
 * @author cedo
 * @since 05/13/2022
 */
public class GaussianBlur {
    private static boolean unsupported = false;
    private static ShaderUtil gaussianBlur = null;

    private static Framebuffer framebuffer = new Framebuffer(1, 1, false);

    private static void setupUniforms(float dir1, float dir2, @Range(from = 0, to = 64) int radius) {
        gaussianBlur.setUniformi("textureIn", 0);
        gaussianBlur.setUniformf("texelSize", 1.0F / (float) mc.displayWidth, 1.0F / (float) mc.displayHeight);
        gaussianBlur.setUniformf("direction", dir1, dir2);
        gaussianBlur.setUniformf("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(64);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(ColorUtils.calculateGaussianValue(i, radius / 2f));
        }

        weightBuffer.rewind();
        glUniform1(gaussianBlur.getUniform("weights"), weightBuffer);
    }

    public static boolean startBlur() {
        if (unsupported) return false;
        if (gaussianBlur == null) {
            try {
                gaussianBlur = new ShaderUtil("keystrokesmod:shaders/gaussian.frag");
            } catch (IllegalStateException e) {
                unsupported = true;
                Utils.handleException(e, "Initializing Blur, but device doesn't support!");
                return false;
            }
        }

        mc.mcProfiler.startSection("Pre-blur");
        mc.getFramebuffer().bindFramebuffer(false);
        checkSetupFBO(mc.getFramebuffer());
        glClear(GL_STENCIL_BUFFER_BIT);
        glEnable(GL_STENCIL_TEST);

        glStencilFunc(GL_ALWAYS, 1, 1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
        glColorMask(false, false, false, false);
        mc.mcProfiler.endSection();
        return true;
    }

    public static void endBlur(@Range(from = 0, to = 64) int radius, float compression) {
        if (unsupported) return;
        mc.mcProfiler.startSection("Post-blur");
        StencilUtil.readStencilBuffer(1);

        framebuffer = RenderUtils.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        gaussianBlur.init();
        setupUniforms(compression, 0, radius);

        RenderUtils.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtil.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.unload();

        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.init();
        setupUniforms(0, compression, radius);

        RenderUtils.bindTexture(framebuffer.framebufferTexture);
        ShaderUtil.drawQuads();
        gaussianBlur.unload();

        StencilUtil.uninitStencilBuffer();
        ColorUtils.resetColor();
        GlStateManager.bindTexture(0);
        mc.mcProfiler.endSection();
    }

}
