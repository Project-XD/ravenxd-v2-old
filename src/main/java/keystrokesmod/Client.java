package keystrokesmod;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import keystrokesmod.event.client.PreTickEvent;
import keystrokesmod.event.render.Render2DEvent;
import keystrokesmod.eventbus.EventBus;
import keystrokesmod.eventbus.EventDispatcher;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.keystroke.KeySrokeRenderer;
import keystrokesmod.keystroke.KeyStrokeConfigGui;
import keystrokesmod.keystroke.keystrokeCommand;
import keystrokesmod.module.Module;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.script.ScriptManager;
import keystrokesmod.utility.*;
import keystrokesmod.utility.clicks.CPSCalculator;
import keystrokesmod.utility.i18n.I18nManager;
import keystrokesmod.utility.interact.moveable.MoveableManager;
import keystrokesmod.utility.profile.Profile;
import keystrokesmod.utility.profile.ProfileManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
        modid = "keystrokes",
        name = "KeystrokesMod",
        version = "KMV5",
        acceptedMinecraftVersions = "[1.8.9]"
)
public final class Client {
    public static final String NAME = Const.NAME;
    public static final String VERSION = Const.VERSION;

    public static boolean debugger = false;
    public static Minecraft mc = Minecraft.getMinecraft();
    private static KeySrokeRenderer keySrokeRenderer;
    private static boolean isKeyStrokeConfigGuiToggled;
    @Getter
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(8);
    @Getter
    public static ModuleManager moduleManager = new ModuleManager();
    public static final EventBus EVENT_BUS = new EventBus();
    public static ClickGui clickGui;
    public static ProfileManager profileManager;
    public static ScriptManager scriptManager;
    public static Profile currentProfile;
    public static BadPacketsHandler badPacketsHandler;

    public static int moduleCounter;
    public static int settingCounter;

    @EventHandler
    public void init(FMLInitializationEvent ignored) {
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
        ClientCommandHandler.instance.registerCommand(new keystrokeCommand());
        Client.EVENT_BUS.register(this);
        Client.EVENT_BUS.register(new DebugInfoRenderer());
        Client.EVENT_BUS.register(new CPSCalculator());
        Client.EVENT_BUS.register(new KeySrokeRenderer());
        Client.EVENT_BUS.register(new Ping());
        Client.EVENT_BUS.register(badPacketsHandler = new BadPacketsHandler());
        Reflection.getFields();
        Reflection.getMethods();
        moduleManager.register();
        scriptManager = new ScriptManager();
        keySrokeRenderer = new KeySrokeRenderer();
        clickGui = new ClickGui();
        profileManager = new ProfileManager();
        profileManager.loadProfiles();
        profileManager.loadProfile();
        Reflection.setKeyBindings();
        scriptManager.loadScripts();
        Client.EVENT_BUS.register(ModuleManager.tower);
        Client.EVENT_BUS.register(ModuleManager.rotationHandler);
        Client.EVENT_BUS.register(ModuleManager.slotHandler);
        Client.EVENT_BUS.register(profileManager);

        I18nManager.init();
        AutoUpdate.init();
        EventDispatcher.init();
        MoveableManager.init();
    }

    @EventListener
    public void onTick(PreTickEvent event) {
        if (Utils.nullCheck()) {
            if (Reflection.sendMessage) {
                Utils.sendMessageAnyWay("&cThere was an error, relaunch the game.");
                Reflection.sendMessage = false;
            }
            Module lastModule = null;
            try {
                for (Module module : getModuleManager().getModules()) {
                    lastModule = module;
                    if (mc.currentScreen instanceof ClickGui) {
                        module.guiUpdate();
                    }

                    if (module.isEnabled()) {
                        module.onUpdate();
                    }
                }
            } catch (Throwable e) {
                if (lastModule != null) {
                    Utils.handleException(e, String.format("update module '%s'", lastModule.getName()), "client global");
                } else {
                    Utils.handleException(e);
                }
            }
        }

        if (isKeyStrokeConfigGuiToggled) {
            isKeyStrokeConfigGuiToggled = false;
            mc.displayGuiScreen(new KeyStrokeConfigGui());
        }
    }

    @EventListener
    public void onRender2D(Render2DEvent event) {
        try {
            if (Utils.nullCheck()) {
                for (Module module : getModuleManager().getModules()) {
                    if (mc.currentScreen == null && module.canBeEnabled()) {
                        module.keybind();
                    }
                }
                synchronized (Client.profileManager.profiles) {
                    for (Profile profile : Client.profileManager.profiles) {
                        if (mc.currentScreen == null) {
                            profile.getModule().keybind();
                        }
                    }
                }
                for (Module module : Client.scriptManager.scripts.values()) {
                    if (mc.currentScreen == null) {
                        module.keybind();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static KeySrokeRenderer getKeyStrokeRenderer() {
        return keySrokeRenderer;
    }

    public static void toggleKeyStrokeConfigGui() {
        isKeyStrokeConfigGuiToggled = true;
    }
}
