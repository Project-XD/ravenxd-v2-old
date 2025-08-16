package keystrokesmod.module.impl.world;

import keystrokesmod.event.player.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import keystrokesmod.eventbus.annotations.EventListener;
import org.lwjgl.input.Mouse;

public class AutoTool extends Module {
    private final SliderSetting weaponHoverDelay;
    private final ButtonSetting weaponSwap;
    private final ButtonSetting weaponRequireMouseDown;
    private final ButtonSetting ignoreTeammates;

    private final SliderSetting toolHoverDelay;
    private final ButtonSetting toolSwap;
    private final ButtonSetting toolRequireMouseDown;
    private final ButtonSetting rightDisable;
    private final ButtonSetting sneakRequire;

    // Weapon state
    private int weaponPreviousSlot = -1;
    private int weaponTicksHovered;
    private Entity weaponCurrentEntity;

    private int toolPreviousSlot = -1;
    private int toolTicksHovered;
    private BlockPos toolCurrentBlock;

    public AutoTool() {
        super("AutoTool", Module.category.world);

        this.registerSetting(new DescriptionSetting("Weapon Settings"));
        this.registerSetting(weaponHoverDelay = new SliderSetting("Hover delay", 0.0, 0.0, 20.0, 1.0));
        this.registerSetting(weaponSwap = new ButtonSetting("Swap back", true));
        this.registerSetting(weaponRequireMouseDown = new ButtonSetting("Require mouse down", true));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(new DescriptionSetting("Configure weapons in Settings tab"));

        this.registerSetting(new DescriptionSetting("Tool Settings"));
        this.registerSetting(toolHoverDelay = new SliderSetting("Hover delay", 0.0, 0.0, 20.0, 1.0));
        this.registerSetting(toolSwap = new ButtonSetting("Swap back", true));
        this.registerSetting(toolRequireMouseDown = new ButtonSetting("Require mouse down", true));
        this.registerSetting(rightDisable = new ButtonSetting("Disable on rmb", true));
        this.registerSetting(sneakRequire = new ButtonSetting("Require sneak", false));
    }

    @Override
    public void onDisable() {
        resetWeaponState();
        resetToolState();
    }

    private void setSlot(final int slot) {
        if (slot == -1) return;
        SlotHandler.setCurrentSlot(slot);
    }

    @EventListener
    public void onPreMotion(PreMotionEvent e) {
        if (!Utils.nullCheck() || !mc.inGameHasFocus || mc.currentScreen != null) {
            resetWeaponState();
            resetToolState();
            return;
        }

        handleWeaponLogic();
        handleToolLogic();
    }

    private void handleWeaponLogic() {
        if (weaponRequireMouseDown.isToggled() && !Mouse.isButtonDown(0)) {
            resetWeaponSlot();
            return;
        }

        Entity hoveredEntity = mc.objectMouseOver != null ? mc.objectMouseOver.entityHit : null;

        if (!(hoveredEntity instanceof EntityLivingBase)) {
            resetWeaponState();
            return;
        }

        if (hoveredEntity instanceof EntityPlayer) {
            if (AntiBot.isBot(hoveredEntity)) {
                resetWeaponState();
                return;
            }
            if (Utils.isFriended((EntityPlayer) hoveredEntity)) {
                resetWeaponState();
                return;
            }
            if (ignoreTeammates.isToggled() && Utils.isTeamMate(hoveredEntity)) {
                resetWeaponState();
                return;
            }
        }

        if (hoveredEntity.equals(weaponCurrentEntity)) {
            weaponTicksHovered++;
        } else {
            weaponTicksHovered = 0;
            weaponCurrentEntity = hoveredEntity;
        }

        if (weaponHoverDelay.getInput() == 0 || weaponTicksHovered >= weaponHoverDelay.getInput()) {
            int weaponSlot = Utils.getWeapon();
            if (weaponSlot != -1) {
                if (weaponPreviousSlot == -1) {
                    weaponPreviousSlot = SlotHandler.getCurrentSlot();
                }
                setSlot(weaponSlot);
            }
        }
    }

    private void handleToolLogic() {
        if (rightDisable.isToggled() && Mouse.isButtonDown(1)) {
            resetToolState();
            return;
        }

        if (toolRequireMouseDown.isToggled() && !Mouse.isButtonDown(0)) {
            resetToolSlot();
            return;
        }

        MovingObjectPosition over = mc.objectMouseOver;
        if (over == null || over.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            resetToolState();
            return;
        }

        if (sneakRequire.isToggled() && !mc.thePlayer.isSneaking()) {
            resetToolState();
            return;
        }

        BlockPos blockPos = over.getBlockPos();

        if (blockPos.equals(toolCurrentBlock)) {
            toolTicksHovered++;
        } else {
            toolTicksHovered = 0;
            toolCurrentBlock = blockPos;
        }

        if (toolHoverDelay.getInput() == 0 || toolTicksHovered >= toolHoverDelay.getInput()) {
            int toolSlot = Utils.getTool(BlockUtils.getBlock(blockPos));
            if (toolSlot != -1) {
                if (toolPreviousSlot == -1) {
                    toolPreviousSlot = SlotHandler.getCurrentSlot();
                }
                setSlot(toolSlot);
            }
        }
    }

    private void resetWeaponState() {
        weaponTicksHovered = 0;
        weaponCurrentEntity = null;
        resetWeaponSlot();
    }

    private void resetWeaponSlot() {
        if (weaponPreviousSlot != -1 && weaponSwap.isToggled()) {
            setSlot(weaponPreviousSlot);
            weaponPreviousSlot = -1;
        }
    }

    private void resetToolState() {
        toolTicksHovered = 0;
        toolCurrentBlock = null;
        resetToolSlot();
    }

    private void resetToolSlot() {
        if (toolPreviousSlot != -1 && toolSwap.isToggled()) {
            setSlot(toolPreviousSlot);
            toolPreviousSlot = -1;
        }
    }
}