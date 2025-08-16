package keystrokesmod.utility.render;

import keystrokesmod.Client;
import keystrokesmod.Const;
import keystrokesmod.mixins.impl.gui.GuiScreenAccessor;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.render.blur.GaussianBlur;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;

import static keystrokesmod.Client.mc;

public final class MainMenu {
    private static boolean initialized = false;

    private static final String COPYRIGHT_MSG = String.format("Copyright Mojang Studios & %s Group.", Client.NAME);
    private static final String CLIENT_MSG = String.format("%s %s", Client.NAME, Client.VERSION);
    private static final FontRenderer LOGO_FONT = FontManager.getFont(FontManager.Fonts.MAPLESTORY, 80);
    private static final FontRenderer GENERAL_FONT = FontManager.tenacity20;
    private static final double GENERAL_FONT_HEIGHT = GENERAL_FONT.getHeight();
    private static final FontRenderer CHANGELOG_FONT = FontManager.getFont(FontManager.Fonts.PRODUCT_SANS_REGULAR, 18);
    private static final double CHANGELOG_FONT_HEIGHT = CHANGELOG_FONT.getHeight();
    private static final int LOGO_COLOR = new Color(255, 255, 255, 200).getRGB();
    private static final Color BUTTON_BOX_COLOR = new Color(0, 0, 0, 50);
    private static final Color BUTTON_BOX_OUTLINE_COLOR = new Color(0, 0, 0, 100);

    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_SPILT = 4;
    private static final int BOX_SPILT = BUTTON_SPILT * 2;

    private static final double CHANGELOG_BOX_WIDTH = Const.CHANGELOG.stream()
            .map(CHANGELOG_FONT::getStringWidth)
            .max(Double::compareTo)
            .orElse(0.0) + BOX_SPILT * 2;
    private static final double CHANGELOG_BOX_HEIGHT = (CHANGELOG_FONT_HEIGHT + BUTTON_SPILT) * Const.CHANGELOG.size()
            + BOX_SPILT * 2;

    private int buttonStartX;
    private int buttonStartY;

    private final GuiMainMenu screen;
    private final Animation backgroundAlphaAnimation = new Animation(Easing.EASE_IN_SINE, 3000);
    private final Animation generalAlphaAnimation = new Animation(Easing.EASE_IN_SINE, 1000);
    private final Animation clientMsgAnimation = new Animation(Easing.EASE_OUT_SINE, 300);

    public MainMenu(@NotNull GuiMainMenu screen) {
        this.screen = screen;
        if (!initialized) {
            backgroundAlphaAnimation.setValue(0);
            generalAlphaAnimation.setValue(0);
            initialized = true;
        } else {
            backgroundAlphaAnimation.setValue(255);
            generalAlphaAnimation.setValue(255);
        }
    }

    public void init() {
        clientMsgAnimation.setValue(screen.height - GENERAL_FONT_HEIGHT - 4);
        buttonStartX = screen.width / 2 - BUTTON_WIDTH / 2;
        buttonStartY = screen.height / 4 + 62;

        final List<GuiButton> buttonList = ((GuiScreenAccessor) screen).getButtonList();

        buttonList.add(new FixedGuiButton(1,
                buttonStartX,
                buttonStartY,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "SinglePlayer"
        ));
        buttonList.add(new FixedGuiButton(2,
                buttonStartX,
                buttonStartY + (BUTTON_HEIGHT + BUTTON_SPILT),
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "MultiPlayer"
        ));
        buttonList.add(new FixedGuiButton(6,
                buttonStartX,
                buttonStartY + (BUTTON_HEIGHT + BUTTON_SPILT) * 2,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                "Mods"
        ));
        buttonList.add(new FixedGuiButton(0,
                buttonStartX,
                buttonStartY + (BUTTON_HEIGHT + BUTTON_SPILT) * 3,
                BUTTON_WIDTH / 2 - BUTTON_SPILT / 2, BUTTON_HEIGHT,
                "Options"
        ));
        buttonList.add(new FixedGuiButton(4,
                buttonStartX + BUTTON_WIDTH / 2 + BUTTON_SPILT / 2,
                buttonStartY + (BUTTON_HEIGHT + BUTTON_SPILT) * 3,
                BUTTON_WIDTH / 2 - BUTTON_SPILT / 2, BUTTON_HEIGHT,
                "Quit"
        ));

        mc.setConnectedToRealms(false);
    }

    private void update() {
        buttonStartX = screen.width / 2 - BUTTON_WIDTH / 2;
        buttonStartY = screen.height / 4 + 62;
        backgroundAlphaAnimation.run(255);
        generalAlphaAnimation.run(255);
    }

    public void render() {
        update();

        final int generalAlpha = (int) Math.round(this.generalAlphaAnimation.getValue());
        final int generalColor = new Color(255, 255, 255, generalAlpha).getRGB();

        BackgroundUtils.updateShadow((int) Math.round(255 - backgroundAlphaAnimation.getValue()));
        BackgroundUtils.renderBackground(screen.width, screen.height);

        LOGO_FONT.drawCenteredString(Client.NAME, screen.width / 2.0, screen.height * 0.2 + 20, LOGO_COLOR);

        if (GaussianBlur.startBlur()) {
            RenderUtils.drawBloomShadow(
                    buttonStartX - BOX_SPILT + 1, buttonStartY - BOX_SPILT + 1,
                    BUTTON_WIDTH + BOX_SPILT * 2 - 2, BUTTON_HEIGHT * 4 + BUTTON_SPILT * 3 + BOX_SPILT * 2 - 2,
                    0, BOX_SPILT, BUTTON_BOX_COLOR.getRGB(), false
            );
            GaussianBlur.endBlur(10, 1);
        }
        RRectUtils.drawRoundOutline(
                buttonStartX - BOX_SPILT, buttonStartY - BOX_SPILT,
                BUTTON_WIDTH + BOX_SPILT * 2, BUTTON_HEIGHT * 4 + BUTTON_SPILT * 3 + BOX_SPILT * 2,
                BOX_SPILT, 0.002f, BUTTON_BOX_COLOR, BUTTON_BOX_OUTLINE_COLOR
        );

        final String moduleMsg = Client.moduleCounter + " modules and " + Client.settingCounter + " settings loaded!";


        double clientMsgY = clientMsgAnimation.getValue();
        boolean clientMsgHover = isClientMsgHover(clientMsgY);
        if (clientMsgHover) {
            clientMsgAnimation.run(screen.height - GENERAL_FONT_HEIGHT - 8 - CHANGELOG_BOX_HEIGHT);
        } else {
            clientMsgAnimation.run(screen.height - GENERAL_FONT_HEIGHT - 4);
        }
        clientMsgY = clientMsgAnimation.getValue();

        final Color changeLogBoxColor = ColorUtils.reAlpha(BUTTON_BOX_COLOR,
                getAlphaClientMsg(BUTTON_BOX_COLOR));
        final Color changeLogBoxOutlineColor = ColorUtils.reAlpha(BUTTON_BOX_OUTLINE_COLOR,
                getAlphaClientMsg(BUTTON_BOX_OUTLINE_COLOR));

        RenderUtils.drawBloomShadow(
                4F, (float) (clientMsgY + GENERAL_FONT_HEIGHT + 4),
                (float) CHANGELOG_BOX_WIDTH, (float) CHANGELOG_BOX_HEIGHT,
                0, BOX_SPILT, changeLogBoxColor.getRGB(),
                false
        );
        RRectUtils.drawRoundOutline(
                4F, (float) (clientMsgY + GENERAL_FONT_HEIGHT + 4),
                (float) CHANGELOG_BOX_WIDTH, (float) CHANGELOG_BOX_HEIGHT,
                BOX_SPILT, 0.002f,
                changeLogBoxColor, changeLogBoxOutlineColor
        );

        for (int i = 0; i < Const.CHANGELOG.size(); i++) {
            String string = Const.CHANGELOG.get(i);
            CHANGELOG_FONT.drawString(string,
                    4 + BOX_SPILT, clientMsgY + GENERAL_FONT_HEIGHT + 4 + BOX_SPILT
                            + (CHANGELOG_FONT_HEIGHT + BUTTON_SPILT) * i,
                    Color.WHITE.getRGB()
            );
        }
        GENERAL_FONT.drawString(CLIENT_MSG,
                4, clientMsgY,
                generalColor);

        GENERAL_FONT.drawRightString(COPYRIGHT_MSG,
                screen.width - 4, screen.height - GENERAL_FONT_HEIGHT - 4,
                generalColor);
        GENERAL_FONT.drawRightString(moduleMsg,
                screen.width - 4, 4,
                generalColor);
    }

    private boolean isClientMsgHover(double clientMsgY) {
        final int x = Mouse.getEventX()
                * mc.currentScreen.width / mc.currentScreen.mc.displayWidth;
        final int y = mc.currentScreen.height - Mouse.getEventY()
                * mc.currentScreen.height / mc.currentScreen.mc.displayHeight - 1;
        return x >= 4 && x <= GENERAL_FONT.width(CLIENT_MSG)
                && y >= clientMsgY && y <= screen.height - 4;
    }

    @Range(from = 0, to = 1)
    private float getAlphaClientMsg(final @NotNull Color color) {
        return Utils.limit(
                (float) ((double) color.getAlpha() / 255 *
                        Utils.limit(clientMsgAnimation.getProgress(), 0, 1))
                , 0, 1);
    }
}
