package keystrokesmod.module.impl.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import keystrokesmod.Client;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.ChestStealer;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.interact.moveable.Moveable;
import keystrokesmod.utility.interact.moveable.MoveableManager;
import keystrokesmod.utility.render.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.event.render.Render2DEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Watermark extends Module implements Moveable {
    public static final Map<String, ResourceLocation> WATERMARK = new Object2ObjectOpenHashMap<>();

    public static String customName = "CustomClient";

    public static int posX = 5;
    public static int posY = 5;
    @Getter
    private int minX = 0;
    @Getter
    private int maxX = 0;
    @Getter
    private int minY = 0;
    @Getter
    private int maxY = 0;
    private final ModeSetting mode;
    private final ModeSetting watermarkText;
    private final ModeSetting watermarkPhoto;
    private final ModeSetting font;
    private final ButtonSetting showVersion;
    private final ButtonSetting lowercase;
    private final ButtonSetting shadow;

    public Watermark() {
        super("Watermark", category.render);
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Text", "Photo"}, 0));
        final ModeOnly textMode = new ModeOnly(mode, 0);
        final ModeOnly photoMode = new ModeOnly(mode, 1);
        this.registerSetting(watermarkText = new ModeSetting("Watermark text", new String[]{"Default", "Custom", "Sense"}, 0, textMode));
        this.registerSetting(watermarkPhoto = new ModeSetting("Watermark photo", new String[]{"Default", "Enders"}, 0, photoMode));
        this.registerSetting(font = new ModeSetting("Font", new String[]{"Minecraft", "Product Sans"}, 0, textMode));
        this.registerSetting(new ModeSetting("Theme", Theme.themes, 0, textMode.extend(new ModeOnly(watermarkText, 2))));
        this.registerSetting(showVersion = new ButtonSetting("Show version", true, textMode));
        this.registerSetting(lowercase = new ButtonSetting("Lowercase", false, textMode));
        this.registerSetting(shadow = new ButtonSetting("Shadow", true, textMode));

        for (String s : Arrays.asList("default", "enders")) {
            try (InputStream stream = Objects.requireNonNull(Client.class.getResourceAsStream("/assets/keystrokesmod/textures/watermarks/" + s + ".png"))) {
                BufferedImage image = ImageIO.read(stream);
                WATERMARK.put(s, Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(s, new DynamicTexture(image)));
            } catch (NullPointerException | IOException ignored) {
            }
        }
    }

    @EventListener
    public void onRenderTick(Render2DEvent event) {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChest && ChestStealer.noChestRender()) && !(mc.currentScreen instanceof GuiChat) || mc.gameSettings.showDebugInfo)
            return;
        render();
    }

    @Override
    public void render() {
        switch ((int) mode.getInput()) {
            case 0:
                renderTextWatermark();
                break;
            case 1:
                switch ((int) watermarkPhoto.getInput()) {
                    case 0:
                        RenderUtils.drawImage(WATERMARK.get("default"), posX, posY, 50, 50);
                        break;
                    case 1:
                        RenderUtils.drawImage(WATERMARK.get("enders"), posX, posY, 150, 45);
                        break;
                }
                break;
        }
    }

    private void renderTextWatermark() {
        IFont font;
        switch ((int) this.font.getInput()) {
            default:
            case 0:
                font = FontManager.getMinecraft();
                break;
            case 1:
                font = FontManager.productSans20;
        }

        String clientName = watermarkText.getInput() == 1 ? customName : "Raven XD";
        String version = showVersion.isToggled() ? "v" + Client.VERSION : "";
        String fpsValue = String.valueOf(Minecraft.getDebugFPS());
        String pingValue = "0";

        if (mc.getNetHandler() != null && mc.thePlayer != null) {
            NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            if (playerInfo != null) {
                pingValue = String.valueOf(playerInfo.getResponseTime());
            }
        }

        if (lowercase.isToggled()) {
            clientName = clientName.toLowerCase();
            version = version.toLowerCase();
            fpsValue = fpsValue.toLowerCase();
            pingValue = pingValue.toLowerCase();
        }

        String[] segments = {
                clientName + " " + version,
                "FPS: " + fpsValue,
                "Ping: " + pingValue
        };

        int padding = 5;
        int segmentSpacing = 8;
        int dividerWidth = 1;

        int[] segmentWidths = new int[3];
        int totalTextWidth = 0;
        for (int i = 0; i < 3; i++) {
            segmentWidths[i] = (int) font.width(segments[i]);
            totalTextWidth += segmentWidths[i];
        }

        int dividersWidth = 2 * dividerWidth;
        int spacingWidth = 4 * segmentSpacing;
        int totalWidth = padding * 2 + totalTextWidth + dividersWidth + spacingWidth;
        int height = (int) (font.height() + (padding * 2));

        int backgroundColor = new Color(20, 20, 20, 180).getRGB();
        RenderUtils.drawRoundedRectangle(posX, posY, posX + totalWidth, posY + height,
                height / 2f, backgroundColor);

        int textY = posY + padding + (font == FontManager.getMinecraft() ? 1 : 0);
        int currentX = posX + padding;

        int dividerColor = new Color(255, 255, 255, 80).getRGB();

        for (int i = 0; i < 3; i++) {
            font.drawString(segments[i], currentX, textY, Color.WHITE.getRGB(), shadow.isToggled());
            currentX += segmentWidths[i];

            if (i < 2) {
                currentX += segmentSpacing;
                RenderUtils.drawRect(currentX, posY + padding, currentX + dividerWidth,
                        posY + height - padding, dividerColor);
                currentX += dividerWidth + segmentSpacing;
            }
        }

        minX = posX;
        maxX = posX + totalWidth;
        minY = posY;
        maxY = posY + height;
    }

    private int calculateTextWatermarkWidth() {
        IFont font;
        switch ((int) this.font.getInput()) {
            default:
            case 0:
                font = FontManager.getMinecraft();
                break;
            case 1:
                font = FontManager.productSans20;
        }

        String clientName = watermarkText.getInput() == 1 ? customName : "Raven XD";
        String version = showVersion.isToggled() ? "v" + Client.VERSION : "";
        String fpsValue = "0";
        String pingValue = "0";

        if (lowercase.isToggled()) {
            clientName = clientName.toLowerCase();
            version = version.toLowerCase();
            fpsValue = fpsValue.toLowerCase();
            pingValue = pingValue.toLowerCase();
        }

        String[] segments = {
                clientName + " " + version,
                "FPS: " + fpsValue,
                "Ping: " + pingValue
        };

        int padding = 5;
        int segmentSpacing = 8;
        int dividerWidth = 1;

        int[] segmentWidths = new int[3];
        int totalTextWidth = 0;
        for (int i = 0; i < 3; i++) {
            segmentWidths[i] = (int) font.width(segments[i]);
            totalTextWidth += segmentWidths[i];
        }

        int dividersWidth = 2 * dividerWidth;
        int spacingWidth = 4 * segmentSpacing;
        return padding * 2 + totalTextWidth + dividersWidth + spacingWidth;
    }

    @Override
    public void moveX(int amount) {
        posX += amount;
    }

    @Override
    public void moveY(int amount) {
        posY += amount;
    }

    @Override
    public void onEnable() throws Throwable {
        MoveableManager.register(this);
    }

    @Override
    public void onDisable() throws Throwable {
        MoveableManager.unregister(this);
    }
}
