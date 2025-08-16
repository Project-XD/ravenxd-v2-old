package keystrokesmod.module.impl.render;

import keystrokesmod.event.render.PostRenderPlayerEvent;
import keystrokesmod.event.render.PreRenderPlayerEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import net.minecraft.entity.Entity;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;

public class Chams extends Module {
    private final ButtonSetting ignoreBots;
    private final HashSet<Entity> bots = new HashSet<>();

    public Chams() {
        super("Chams", Module.category.render, 0);
        this.registerSetting(ignoreBots = new ButtonSetting("Ignore bots", false));
    }

    @EventListener
    public void r1(@NotNull PreRenderPlayerEvent e) {
        if (e.getEntity() == mc.thePlayer) {
            return;
        }
        if (ignoreBots.isToggled()) {
            if (AntiBot.isBot(e.getEntity())) {
                return;
            }
            this.bots.add(e.getEntity());
        }
        GL11.glEnable(32823);
        GL11.glPolygonOffset(1.0f, -1000000.0f);
    }

    @EventListener
    public void r2(@NotNull PostRenderPlayerEvent e) {
        if (e.getEntity() == mc.thePlayer) {
            return;
        }
        if (ignoreBots.isToggled()) {
            if (!this.bots.contains(e.getEntity())) {
                return;
            }
            this.bots.remove(e.getEntity());
        }
        GL11.glPolygonOffset(1.0f, 1000000.0f);
        GL11.glDisable(32823);
    }

    public void onDisable() {
        this.bots.clear();
    }
}
