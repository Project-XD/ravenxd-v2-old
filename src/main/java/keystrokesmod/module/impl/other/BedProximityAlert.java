package keystrokesmod.module.impl.other;

import keystrokesmod.event.player.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.minigames.BedWars;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import keystrokesmod.eventbus.annotations.EventListener;

import java.util.HashMap;
import java.util.Map;

public class BedProximityAlert extends Module {
    private final Map<String, Boolean> playerAlertStatus;
    private final SliderSetting Distance;
    private final ButtonSetting shouldPing;
    private final ButtonSetting tellTheteam;
    private final ButtonSetting ignoreTeammates;

    public BedProximityAlert() {
        super("BedProximityAlert", category.other);
        this.registerSetting(Distance = new SliderSetting("Distance", 40, 10, 120, 1));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
        this.registerSetting(tellTheteam = new ButtonSetting("Tell the team", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
        playerAlertStatus = new HashMap<>();
    }

    @EventListener
    public void onPlayerTick(PreUpdateEvent event) {
        BlockPos spawnPos = BedWars.getSpawnPos();
        if (BedWars.whitelistOwnBed.isToggled() && spawnPos != null) {
            for (EntityPlayer otherPlayer : mc.thePlayer.worldObj.playerEntities) {
                if (otherPlayer == mc.thePlayer) {
                    continue;
                }
                if (ignoreTeammates.isToggled()) {
                    if (Utils.isTeamMate(otherPlayer)) {
                        return;
                    }
                }
                double distance = otherPlayer.getDistance(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                String playerName = otherPlayer.getDisplayName().getFormattedText();

                if (distance <= Distance.getInput()) {
                    if (!playerAlertStatus.getOrDefault(playerName, false)) {
                        Notifications.sendNotification(Notifications.NotificationTypes.WARN, playerName + " is " + (int) distance + " blocks away from the bed!");
                        informTeam(playerName, (int) distance);
                        ping();
                        playerAlertStatus.put(playerName, true);
                    }
                } else {
                    playerAlertStatus.put(playerName, false);
                }
            }
        }
    }

    private void ping() {
        if (shouldPing.isToggled()) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
        }
    }

    public void informTeam(String playerName, int distance) {
        if (tellTheteam.isToggled()) {
            mc.thePlayer.sendChatMessage(Utils.getUnformatedString(playerName + " is " + distance + " blocks away from the bed!"));
        }
    }
}
