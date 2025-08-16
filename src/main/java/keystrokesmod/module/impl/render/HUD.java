package keystrokesmod.module.impl.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keystrokesmod.Client;
import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.event.render.Render2DEvent;
import keystrokesmod.event.world.WorldChangeEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.CommandLine;
import keystrokesmod.module.impl.player.ChestStealer;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.interact.moveable.Moveable;
import keystrokesmod.utility.interact.moveable.MoveableManager;
import keystrokesmod.utility.movement.MoveCorrect;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.RenderUtils;
import lombok.Getter;
import keystrokesmod.eventbus.annotations.EventListener;
import net.minecraft.client.renderer.entity.Render;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class HUD extends Module implements Moveable {
    private static final ButtonSetting combat = new ButtonSetting("Combat", true);
    private static final ButtonSetting movement = new ButtonSetting("Movement", true);
    private static final ButtonSetting player = new ButtonSetting("Player", true);
    private static final ButtonSetting world = new ButtonSetting("World", true);
    private static final ButtonSetting render = new ButtonSetting("Render", true);
    private static final ButtonSetting minigames = new ButtonSetting("Minigames", true);
    private static final ButtonSetting fun = new ButtonSetting("Fun", true);
    private static final ButtonSetting other = new ButtonSetting("Other", true);
    private static final ButtonSetting client = new ButtonSetting("Client", true);
    private static final ButtonSetting scripts = new ButtonSetting("Scripts", true);
    private static final ButtonSetting exploit = new ButtonSetting("Exploit", true);
    private static final ButtonSetting experimental = new ButtonSetting("Experimental", true);
    public static ModeSetting theme;
    private final ModeSetting font;
    private final ButtonSetting dropShadow;
    private final ButtonSetting alphabeticalSort;
    private final ButtonSetting lowercase;
    private final ButtonSetting showInfo;
    private final ButtonSetting alignRight;
    private final ButtonSetting background;
    private final ModeValue mode;

    private final List<ModuleRender> moduleRenders = new ObjectArrayList<>(8);
    public static int posX = 10;
    public static int posY = 10;
    @Getter
    public int minX;
    @Getter
    public int minY;
    @Getter
    public int maxX;
    @Getter
    public int maxY;

    public HUD() {
        super("HUD", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(theme = new ModeSetting("Theme", Theme.themes, 0));
        this.registerSetting(font = new ModeSetting("Font", new String[]{"Minecraft", "Product Sans", "Regular", "Tenacity"}, 0));
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new LiteralSubMode("None", this))
                .add(new LiteralSubMode("Fullbar", this))
                .add(new LiteralSubMode("Sidebar", this))
        );
        this.registerSetting(alignRight = new ButtonSetting("Align right", false));
        this.registerSetting(alphabeticalSort = new ButtonSetting("Alphabetical sort", false));
        this.registerSetting(dropShadow = new ButtonSetting("Drop shadow", true));
        this.registerSetting(background = new ButtonSetting("Background", false));

        this.registerSetting(lowercase = new ButtonSetting("Lowercase", false));
        this.registerSetting(showInfo = new ButtonSetting("Show info", true));

        this.registerSetting(new DescriptionSetting("Categories"));
        this.registerSetting(combat, movement, player, world, render, minigames, fun, other, client, scripts, exploit, experimental);
    }

    private IFont getFont() {
        switch ((int) font.getInput()) {
            default:
            case 0:
                return FontManager.getMinecraft();
            case 1:
                return FontManager.productSans20;
            case 2:
                return FontManager.regular22;
            case 3:
                return FontManager.tenacity20;
        }
    }

    private void initHUD() {
        if (!Utils.nullCheck()) return;  // INSANE BUG

        moduleRenders.clear();
        Client.getModuleManager().getModules()
                .forEach(module -> moduleRenders.add(new ModuleRender(module)));
        sortHUD();
    }

    private void sortHUD() {
        Client.getExecutor().execute(() -> {
            if (alphabeticalSort.isToggled()) {
                moduleRenders.sort(Comparator.comparing(render -> render.module.getPrettyName()));
            } else {
                moduleRenders.sort((c1, c2) -> Double.compare(c2.getWidth(), c1.getWidth()));
            }
        });
    }

    @Override
    public void onEnable() throws Throwable {
        initHUD();
        MoveableManager.register(this);
    }

    @Override
    public void onDisable() throws Throwable {
        MoveableManager.unregister(this);
        moduleRenders.clear();
    }

    @EventListener
    public void onPreUpdate(PreUpdateEvent event) {
        sortHUD();
    }

    @EventListener
    public void onWorldChange(WorldChangeEvent event) {
        initHUD();
    }

    @EventListener
    public void onRender2D(Render2DEvent event) {
        if ((mc.currentScreen != null || mc.gameSettings.showDebugInfo) && !(ChestStealer.noChestRender())) {
            return;
        }
        render();
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
    public void render() {
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;

        final int height = (int) Math.round(getFont().height() + 2);

        int targetY = posY;
        minY = targetY;
        for (ModuleRender moduleRender : moduleRenders) {
            final double width = moduleRender.getWidth();
            if (alignRight.isToggled()) {
                minX = Math.min(minX, (int) Math.round(posX - width));
                maxX = (maxX >= posX) ? maxX : posX;
            } else {
                minX = Math.min(minX, posX);
                int b = (int) Math.round(posX + width);
                maxX = (maxX >= b) ? maxX : b;
            }

            int targetX = (int) Math.round(alignRight.isToggled() ? posX - width : posX);
            boolean ignored = moduleRender.isIgnored();
            if (ignored) {
                targetX = alignRight.isToggled() ?
                        (int) Math.ceil(mc.displayWidth + width * 2) :
                        (int) Math.floor(-width * 2);
            }

            moduleRender.animationX.run(targetX);
            moduleRender.animationY.run(targetY);
            final double x = moduleRender.animationX.getValue();
            final double y = moduleRender.animationY.getValue();
            final int color = Theme.getGradient((int) theme.getInput(), targetY);

            // render
            if (background.isToggled()) {
                RenderUtils.drawRect(
                        x - 1, y - 1, x + width + 1, y + height,
                        new Color(0, 0, 0, 100).getRGB());
            }

            switch (mode.getSelected().getPrettyName()) {
                case "None":
                    break;
                case "Fullbar":
                    RenderUtils.drawRect(x - 1, y - 2, x + width + 2, y - 1, color);
                    RenderUtils.drawRect(x - 1, y + height - 1, x + width + 2, y + height, color);
                    RenderUtils.drawRect(x - 2, y - 2, x - 1, y + height, color);
                    RenderUtils.drawRect(x + width + 1, y - 1, x + width + 2, y + height - 1, color);
                    break;
                case "Sidebar":
                    RenderUtils.drawRect(alignRight.isToggled() ? x + width : x - 2, y - 1,
                            alignRight.isToggled() ? x + width + 1 : x - 1, y + height - 1, color);
                    break;
            }

            getFont().drawString(moduleRender.getText(), x, y, color, dropShadow.isToggled());

            if (!ignored)
                targetY += height;
        }
        maxY = targetY + height;
    }

    private final class ModuleRender {
        public final Module module;
        public final Animation animationX = new Animation(Easing.EASE_OUT_CIRC, 200);
        public final Animation animationY = new Animation(Easing.EASE_OUT_CIRC, 200);

        public ModuleRender(final Module module) {
            this.module = module;
            animationX.setValue(0);
            animationY.setValue(0);
        }

        public double getWidth() {
            String text = module.getPrettyName()
                    + ((showInfo.isToggled() && !module.getPrettyInfo().isEmpty()) ? " " + module.getPrettyInfo() : "");
            return getFont().width(lowercase.isToggled() ? text.toLowerCase() : text);
        }

        public String getText() {
            String text = module.getPrettyName();
            if (showInfo.isToggled() && !module.getPrettyInfo().isEmpty()) {
                text += " §7" + module.getPrettyInfo();
            }
            if (lowercase.isToggled()) {
                text = text.toLowerCase();
            }
            return text;
        }

        public boolean isIgnored() {
            if (module instanceof HUD || module instanceof CommandLine || module instanceof SubMode)
                return true;
            if (!module.isEnabled())
                return true;

            if (module.moduleCategory() == category.combat && !combat.isToggled()) return true;
            if (module.moduleCategory() == category.movement && !movement.isToggled()) return true;
            if (module.moduleCategory() == category.player && !player.isToggled()) return true;
            if (module.moduleCategory() == category.world && !world.isToggled()) return true;
            if (module.moduleCategory() == category.render && !render.isToggled()) return true;
            if (module.moduleCategory() == category.minigames && !minigames.isToggled()) return true;
            if (module.moduleCategory() == category.fun && !fun.isToggled()) return true;
            if (module.moduleCategory() == category.other && !other.isToggled()) return true;
            if (module.moduleCategory() == category.client && !client.isToggled()) return true;
            if (module.moduleCategory() == category.scripts && !scripts.isToggled()) return true;
            if (module.moduleCategory() == category.exploit && !exploit.isToggled()) return true;
            if (module.moduleCategory() == category.experimental && !experimental.isToggled()) return true;

            if (module.isHidden()) {
                return true;
            }
            return module == ModuleManager.commandLine;
        }
    }
}
