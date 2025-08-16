package keystrokesmod.module.impl.fun;

import keystrokesmod.Client;
import keystrokesmod.anticrack.AntiCrack;
import keystrokesmod.module.Module;


public class Yeet extends Module {
    private int enableTicks = 0;

    public Yeet() {
        super("Yeet", category.fun, "Yeet!");
    }

    @Override
    public void onEnable() {
        enableTicks = 0;
        Client.mc.thePlayer.playSound("keystrokesmod:yeet", 1, 1);
    }

    @Override
    public void onUpdate() {
        enableTicks++;

        if (enableTicks == 20) {
            this.disable();
            AntiCrack.UNREACHABLE("Yeet!");
        }
    }
}
