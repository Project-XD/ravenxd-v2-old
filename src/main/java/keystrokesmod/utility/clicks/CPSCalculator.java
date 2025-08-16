package keystrokesmod.utility.clicks;

import keystrokesmod.Client;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import keystrokesmod.event.client.MouseEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CPSCalculator {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final List<Long> a = new ArrayList<>();
    private static final List<Long> b = new ArrayList<>();
    public static long LL = 0L;
    public static long LR = 0L;

    @EventListener
    public void onMouseUpdate(@NotNull MouseEvent d) {
        if (d.isButtonstate()) {
            if (d.getButton() == 0) {
                aL();
                if (Client.debugger && mc.objectMouseOver != null) {
                    Entity en = mc.objectMouseOver.entityHit;
                    if (en == null) {
                        return;
                    }

                    Utils.sendMessage("&7&m-------------------------");
                    Utils.sendMessage("n: " + en.getName());
                    Utils.sendMessage("rn: " + en.getName().replace("§", "%"));
                    Utils.sendMessage("d: " + en.getDisplayName().getUnformattedText());
                    Utils.sendMessage("rd: " + en.getDisplayName().getUnformattedText().replace("§", "%"));
                    Utils.sendMessage("b?: " + AntiBot.isBot(en));
                }
            } else if (d.getButton() == 1) {
                aR();
            }
        }
    }

    public static void aL() {
        a.add(LL = System.currentTimeMillis());
    }

    public static void aR() {
        b.add(LR = System.currentTimeMillis());
    }

    public static int f() {
        a.removeIf(o -> o < System.currentTimeMillis() - 1000L);
        return a.size();
    }

    public static int i() {
        b.removeIf(o -> o < System.currentTimeMillis() - 1000L);
        return b.size();
    }
}
