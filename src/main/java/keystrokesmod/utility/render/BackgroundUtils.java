package keystrokesmod.utility.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

import static keystrokesmod.Client.mc;

public class BackgroundUtils {
    private static final List<ResourceLocation> BACKGROUNDS = new ObjectArrayList<>();
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final double ASPECT_RATIO = (double) WIDTH / HEIGHT;
    private static final int MAX_INDEX;

    private static long lastRenderTime = -1;
    private static ResourceLocation lastBackground;
    private static int shadow = 0;

    static {
        for (int i = 1; i <= 5; i++) {
            BACKGROUNDS.add(new ResourceLocation(String.format("keystrokesmod:textures/backgrounds/%d.png", i)));
        }
        MAX_INDEX = BACKGROUNDS.size() - 1;

        lastBackground = BACKGROUNDS.get(Utils.randomizeInt(0, MAX_INDEX));
    }

    public static void renderBackground(@NotNull GuiScreen screen) {
        updateShadow(0);
        renderBackground(screen.width, screen.height);
    }

    public static void renderBackground(@NotNull GuiSlot slot) {
        updateShadow(200);
        renderBackground(slot.width, slot.height);
    }

    protected static void renderBackground(final int width, final int height) {
        if (Utils.nullCheck()) return;

        final long time = System.currentTimeMillis();
        if (time - lastRenderTime > 3000) {
            lastBackground = BACKGROUNDS.get(Utils.randomizeInt(0, MAX_INDEX));
        }
        lastRenderTime = time;

        float renderWidth = width;
        float renderHeight = height;
        float x = 0;
        float y = 0;

        if (width != WIDTH || height != HEIGHT) {
            double screenAspectRatio = (double) width / height;
            if (screenAspectRatio != ASPECT_RATIO) {
                if (screenAspectRatio > ASPECT_RATIO) {
                    renderWidth = width;
                    renderHeight = (float) (width / ASPECT_RATIO);
                    y = (height - renderHeight) / 2;
                } else {
                    renderHeight = height;
                    renderWidth = (float) (height * ASPECT_RATIO);
                    x = (width - renderWidth) / 2;
                }
            }
        }

        RenderUtils.drawImage(lastBackground, x, y, renderWidth, renderHeight);

        if (shadow != 0) {
            RenderUtils.drawBloomShadow(-16, -16, mc.displayWidth + 16, mc.displayHeight + 16, 4,
                    new Color(0, 0, 0, shadow), false
            );
        }
    }

    protected static void updateShadow(final int shadowTarget) {
        if (shadowTarget > shadow) {
            shadow = (int) Math.min(shadow + 4.0 * 300 / Minecraft.getDebugFPS(), shadowTarget);
        } else if (shadowTarget < shadow) {
            shadow = (int) Math.max(shadow - 4.0 * 300 / Minecraft.getDebugFPS(), shadowTarget);
        }
    }
}
