package keystrokesmod.module.impl.combat;

import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.event.network.AttackEntityEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.hitSelect;

public class HitSelect extends Module {
    private static final String[] MODES = new String[]{"Pause", "Active"};
    private static final String[] PREFERENCES = new String[]{"Move speed", "KB reduction", "Critical hits"};
    private static long attackTime = -1;
    private static boolean currentShouldAttack = false;
    private final ModeSetting preference;
    private final ModeSetting mode;
    private final SliderSetting delay;
    private final SliderSetting chance;

    public HitSelect() {
        super("HitSelect", category.combat, "Chooses the best time to hit.");
        this.registerSetting(mode = new ModeSetting("Mode", MODES, 0,
                "Pause: Legitimate pause clicking\n" +
                        "Active: Cancel attack but allow click"));
        this.registerSetting(preference = new ModeSetting("Preference", PREFERENCES, 0,
                "Move speed: Keep sprint but legitimate\n" +
                        "KB reduction: KnockBack reduction\n" +
                        "Critical hits: Critical hit frequency"));
        this.registerSetting(delay = new SliderSetting("Delay", 420, 300, 500, 1));
        this.registerSetting(chance = new SliderSetting("Chance", 80, 0, 100, 1));
    }

    public static boolean canAttack() {
        return canSwing();
    }

    public static boolean canSwing() {
        if (!hitSelect.isEnabled() || hitSelect.mode.getInput() == 1) return true;
        return currentShouldAttack;
    }

    @Override
    public String getInfo() {
        return MODES[(int) mode.getInput()];
    }

    @EventListener(priority = -2)
    public void onAttack(@NotNull AttackEntityEvent event) {
        if (mode.getInput() == 1 && !currentShouldAttack) {
            event.cancel();
            return;
        }

        if (canAttack())
            attackTime = System.currentTimeMillis();
    }

    @EventListener(priority = 2)
    public void onPreUpdate(PreUpdateEvent event) {
        currentShouldAttack = false;

        if (Math.random() * 100 > hitSelect.chance.getInput()) {
            currentShouldAttack = true;
        } else {
            switch ((int) preference.getInput()) {
                case 1:
                    currentShouldAttack = mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround && MoveUtil.isMoving();
                    break;
                case 2:
                    currentShouldAttack = !mc.thePlayer.onGround && mc.thePlayer.motionY < 0;
                    break;
            }

            if (!currentShouldAttack)
                currentShouldAttack = System.currentTimeMillis() - HitSelect.attackTime >= hitSelect.delay.getInput();
        }
    }
}
