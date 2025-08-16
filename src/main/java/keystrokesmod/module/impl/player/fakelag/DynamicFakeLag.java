package keystrokesmod.module.impl.player.fakelag;

import keystrokesmod.module.impl.player.FakeLag;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.backtrack.TimedPacket;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.network.Packet;
import keystrokesmod.event.network.AttackEntityEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.event.render.Render2DEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static keystrokesmod.module.ModuleManager.blink;

public class DynamicFakeLag extends SubMode<FakeLag> {
    private final SliderSetting delay;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting stopOnHurt;
    private final SliderSetting stopOnHurtTime;
    private final SliderSetting startRange;
    private final SliderSetting stopRange;
    private final SliderSetting maxTargetRange;
    private final ButtonSetting debug;
    private final Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private AbstractClientPlayer target = null;
    private long lastDisableTime = -1;
    private boolean lastHurt = false;
    private long lastStartBlinkTime = -1;

    public DynamicFakeLag(String name, @NotNull FakeLag parent) {
        super(name, parent);
        this.registerSetting(delay = new SliderSetting("Delay", 200, 25, 1000, 5, "ms"));
        this.registerSetting(debug = new ButtonSetting("Debug", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Dynamic Ignore teammates", true));
        this.registerSetting(stopOnHurt = new ButtonSetting("Dynamic Stop on hurt", true));
        this.registerSetting(stopOnHurtTime = new SliderSetting("Dynamic Stop on hurt time", 500, 0, 1000, 5, "ms"));
        this.registerSetting(startRange = new SliderSetting("Dynamic Start range", 6.0, 3.0, 10.0, 0.1, "blocks"));
        this.registerSetting(stopRange = new SliderSetting("Dynamic Stop range", 3.5, 1.0, 6.0, 0.1, "blocks"));
        this.registerSetting(maxTargetRange = new SliderSetting("Dynamic Max target range", 15.0, 6.0, 20.0, 0.5, "blocks"));
    }

    @Override
    public void onEnable() {
        lastDisableTime = -1;
        lastHurt = false;
        lastStartBlinkTime = -1;
        packetQueue.clear();
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(stopRange, startRange);
        Utils.correctValue(startRange, maxTargetRange);
    }

    @EventListener(priority = 2)
    public void onRenderTick(Render2DEvent ev) {
        if (!Utils.nullCheck()) {
            sendPacket(false);
            lastDisableTime = System.currentTimeMillis();
            lastStartBlinkTime = -1;
            return;
        }
        if (System.currentTimeMillis() - lastDisableTime <= stopOnHurtTime.getInput()) {
            blink.disable();
        }

        if (blink.isEnabled()) {
            if (System.currentTimeMillis() - lastStartBlinkTime > delay.getInput()) {
                if (debug.isToggled()) Utils.sendModuleMessage(parent, "stop lag: time out.");
                lastStartBlinkTime = System.currentTimeMillis();
                blink.disable();
            } else if (!lastHurt && mc.thePlayer.hurtTime > 0 && stopOnHurt.isToggled()) {
                if (debug.isToggled()) Utils.sendModuleMessage(parent, "stop lag: hurt.");
                lastDisableTime = System.currentTimeMillis();
                blink.disable();
            }
        }

        if (target != null) {
            double distance = new Vec3(mc.thePlayer).distanceTo(target);
            if (blink.isEnabled() && distance < stopRange.getInput()) {
                if (debug.isToggled()) Utils.sendModuleMessage(parent, "stop lag: too low range.");
                blink.disable();
            } else if (!blink.isEnabled() && distance > stopRange.getInput()
                    && new Vec3(mc.thePlayer).distanceTo(target) < startRange.getInput()) {
                if (debug.isToggled()) Utils.sendModuleMessage(parent, "start lag: in range.");
                lastStartBlinkTime = System.currentTimeMillis();
                blink.enable();
            } else if (blink.isEnabled() && distance > startRange.getInput()) {
                if (debug.isToggled()) Utils.sendModuleMessage(parent, "stop lag: out of range.");
                blink.disable();
            } else if (distance > maxTargetRange.getInput()) {
                if (debug.isToggled())
                    Utils.sendModuleMessage(parent, String.format("release target: %s", target.getName()));
                target = null;
                blink.disable();
            }
        } else blink.disable();

        lastHurt = mc.thePlayer.hurtTime > 0;
    }

    @EventListener
    public void onAttack(@NotNull AttackEntityEvent e) {
        if (e.getTarget() instanceof AbstractClientPlayer) {
            if (ignoreTeammates.isToggled() && Utils.isTeamMate(e.getTarget())) return;
            target = (AbstractClientPlayer) e.getTarget();
        }
    }

    public void sendPacket(boolean delay) {
        try {
            while (!packetQueue.isEmpty()) {
                if (!delay || packetQueue.element().getCold().getCum((long) this.delay.getInput())) {
                    Packet<?> packet = packetQueue.remove().getPacket();
                    if (packet == null) continue;

                    PacketUtils.sendPacketNoEvent(packet);
                } else {
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}