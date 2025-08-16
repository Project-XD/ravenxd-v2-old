package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.ContainerUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

public class SelfBowFly extends SubMode<Fly> {
    private int stage = 0;
    private int ticks = 0;
    private int bowSlot = -1;
    private boolean hasJumped = false;

    public SelfBowFly(String name, @NotNull Fly parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        stage = 0;
        ticks = 0;
        hasJumped = false;
        bowSlot = ContainerUtils.getSlot(ItemBow.class);

        // Hold forward and sprint continuously
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
    }

    @Override
    public void onDisable() {
        // Release all keys
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
    }

    @Override
    public void onUpdate() {
        // Always hold forward and sprint
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);

        switch (stage) {
            case 0: // Switch to bow and aim down
                if (bowSlot == -1) {
                    parent.disable();
                    return;
                }

                SlotHandler.setCurrentSlot(bowSlot);
                mc.thePlayer.rotationPitch = -7F; // Aim slightly backward/down
                stage = 1;
                break;

            case 1: // Start charging bow (minimum time)
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(
                        new BlockPos(-1, -1, -1), 255,
                        mc.thePlayer.getHeldItem(), 0.0f, 0.0f, 0.0f
                ));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                stage = 2;
                ticks = 0;
                break;


            case 2: // Hold charge for minimal time then jump
                if (ticks >= 2) { // Minimal charge time (2 ticks = 100ms)
                    // Jump at the last moment for maximum boost
                    if (!hasJumped) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
                        hasJumped = true;
                    }

                    if (ticks >= 3) { // Release arrow immediately after jump
                        // Release arrow
                        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                                BlockPos.ORIGIN, EnumFacing.DOWN
                        ));
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
                        stage = 3;
                        ticks = 0;
                    }
                }
                break;

            case 3: // Reset bow animation
                if (ticks++ >= 1) {
                    // Reset bow by simulating left click
                    mc.thePlayer.swingItem();
                    stage = 0;
                    hasJumped = false;
                }
                break;
        }

        ticks++;
    }
}