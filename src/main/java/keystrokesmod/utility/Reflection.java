package keystrokesmod.utility;

import keystrokesmod.event.client.MouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.*;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.ResourceLocation;
import keystrokesmod.Client;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Mouse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.*;

public class Reflection {
    public static Field rightClickDelayTimerField;
    public static Field curBlockDamageMP;
    public static Field blockHitDelay;
    public static Method clickMouse;
    public static Method rightClickMouse;
    public static Field shaderResourceLocations;
    public static Field useShader;
    public static Field shaderIndex;
    public static Method loadShader;
    public static Method getPlayerInfo;
    public static Field inGround;
    public static Field itemInUseCount;
    public static Field S08PacketPlayerPosLookYaw;
    public static Field S08PacketPlayerPosLookPitch;
    public static Field C02PacketUseEntityEntityId;
    public static Field C03PacketPlayerOnGround;
    public static Field S12PacketEntityVelocityXMotion;
    public static Field S12PacketEntityVelocityYMotion;
    public static Field S12PacketEntityVelocityZMotion;
    public static Field S27PacketExplosionXMotion;
    public static Field S27PacketExplosionYMotion;
    public static Field S27PacketExplosionZMotion;
    public static Field EntityFallDistance;
    public static Field bookContents;
    public static HashMap<Class, Field> containerInventoryPlayer = new HashMap<>();
    private static List<Class> containerClasses = new ArrayList<>();
    public static boolean sendMessage = false;
    public static Map<KeyBinding, String> keyBindings = new HashMap<>();

    public static void getFields() {
        try {
            containerClasses.add(GuiFurnace.class);
            containerClasses.add(GuiBrewingStand.class);
            containerClasses.add(GuiEnchantment.class);
            containerClasses.add(ContainerHopper.class);
            containerClasses.add(GuiDispenser.class);
            containerClasses.add(ContainerWorkbench.class);
            containerClasses.add(ContainerMerchant.class);
            containerClasses.add(ContainerHorseInventory.class);

            rightClickDelayTimerField = ReflectionHelper.findField(Minecraft.class, "field_71467_ac", "rightClickDelayTimer");

            if (rightClickDelayTimerField != null) {
                rightClickDelayTimerField.setAccessible(true);
            }

            curBlockDamageMP = ReflectionHelper.findField(PlayerControllerMP.class, "field_78770_f", "curBlockDamageMP"); // fastmine and mining related stuff
            if (curBlockDamageMP != null) {
                curBlockDamageMP.setAccessible(true);
            }

            blockHitDelay = ReflectionHelper.findField(PlayerControllerMP.class, "field_78781_i", "blockHitDelay");
            if (blockHitDelay != null) {
                blockHitDelay.setAccessible(true);
            }

            shaderResourceLocations = ReflectionHelper.findField(EntityRenderer.class, "shaderResourceLocations", "field_147712_ad");
            if (shaderResourceLocations != null) {
                shaderResourceLocations.setAccessible(true);
            }

            useShader = ReflectionHelper.findField(EntityRenderer.class, "useShader", "field_175083_ad");
            if (useShader != null) {
                useShader.setAccessible(true);
            }

            shaderIndex = ReflectionHelper.findField(EntityRenderer.class, "field_147713_ae", "shaderIndex"); // for shaders
            if (shaderIndex != null) {
                shaderIndex.setAccessible(true);
            }

            inGround = ReflectionHelper.findField(EntityArrow.class, "field_70254_i", "inGround"); // for indicators
            if (inGround != null) {
                inGround.setAccessible(true);
            }

            itemInUseCount = ReflectionHelper.findField(EntityPlayer.class, "field_71072_f", "itemInUseCount"); // for fake block
            if (itemInUseCount != null) {
                itemInUseCount.setAccessible(true);
            }

            S08PacketPlayerPosLookYaw = ReflectionHelper.findField(S08PacketPlayerPosLook.class, "field_148936_d", "yaw");
            if (S08PacketPlayerPosLookYaw != null) {
                S08PacketPlayerPosLookYaw.setAccessible(true);
            }

            S08PacketPlayerPosLookPitch = ReflectionHelper.findField(S08PacketPlayerPosLook.class, "field_148937_e", "pitch");
            if (S08PacketPlayerPosLookPitch != null) {
                S08PacketPlayerPosLookPitch.setAccessible(true);
            }

            C02PacketUseEntityEntityId = ReflectionHelper.findField(C02PacketUseEntity.class, "entityId", "field_149567_a");
            if (C02PacketUseEntityEntityId != null) {
                C02PacketUseEntityEntityId.setAccessible(true);
            }

            C03PacketPlayerOnGround = ReflectionHelper.findField(C03PacketPlayer.class, "onGround", "field_149474_g");
            if (C03PacketPlayerOnGround != null) {
                C03PacketPlayerOnGround.setAccessible(true);
            }

            S12PacketEntityVelocityXMotion = ReflectionHelper.findField(S12PacketEntityVelocity.class, "motionX", "field_149415_b");
            if (S12PacketEntityVelocityXMotion != null) {
                S12PacketEntityVelocityXMotion.setAccessible(true);
            }

            S12PacketEntityVelocityYMotion = ReflectionHelper.findField(S12PacketEntityVelocity.class, "motionY", "field_149416_c");
            if (S12PacketEntityVelocityYMotion != null) {
                S12PacketEntityVelocityYMotion.setAccessible(true);
            }

            S12PacketEntityVelocityZMotion = ReflectionHelper.findField(S12PacketEntityVelocity.class, "motionZ", "field_149414_d");
            if (S12PacketEntityVelocityZMotion != null) {
                S12PacketEntityVelocityZMotion.setAccessible(true);
            }

            S27PacketExplosionXMotion = ReflectionHelper.findField(S27PacketExplosion.class, "field_149152_f", "field_149152_f");
            if (S27PacketExplosionXMotion != null) {
                S27PacketExplosionXMotion.setAccessible(true);
            }

            S27PacketExplosionYMotion = ReflectionHelper.findField(S27PacketExplosion.class, "field_149153_g", "field_149153_g");
            if (S27PacketExplosionYMotion != null) {
                S27PacketExplosionYMotion.setAccessible(true);
            }

            S27PacketExplosionZMotion = ReflectionHelper.findField(S27PacketExplosion.class, "field_149159_h", "field_149159_h");
            if (S27PacketExplosionZMotion != null) {
                S27PacketExplosionZMotion.setAccessible(true);
            }

            EntityFallDistance = ReflectionHelper.findField(Entity.class, "fallDistance", "field_70143_R");
            if (EntityFallDistance != null) {
                EntityFallDistance.setAccessible(true);
            }

            bookContents = ReflectionHelper.findField(GuiScreenBook.class, "field_175386_A");
            if (bookContents != null) {
                bookContents.setAccessible(true);
            }

            for (Class clazz : containerClasses) {
                for (Field field : clazz.getDeclaredFields()) {
                    addToMap(clazz, field);
                }
            }

        } catch (Exception var2) {
            System.out.println("There was an error, relaunch the game.");
            var2.printStackTrace();
            sendMessage = true;
        }
    }

    public static void setKeyBindings() {
        for (KeyBinding keyBinding : Minecraft.getMinecraft().gameSettings.keyBindings) {
            keyBindings.put(keyBinding, keyBinding.getKeyDescription().substring(4));
        }
    }

    public static void getMethods() {
        try {
            try {
                rightClickMouse = Minecraft.getMinecraft().getClass().getDeclaredMethod("func_147121_ag");
            } catch (NoSuchMethodException var4) {
                try {
                    rightClickMouse = Minecraft.getMinecraft().getClass().getDeclaredMethod("rightClickMouse");
                } catch (NoSuchMethodException ignored) {
                }
            }

            if (rightClickMouse != null) {
                rightClickMouse.setAccessible(true);
            }

            loadShader = ReflectionHelper.findMethod(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, new String[]{"func_175069_a", "loadShader"}, ResourceLocation.class);

            if (loadShader != null) {
                loadShader.setAccessible(true);
            }

            try {
                clickMouse = Minecraft.getMinecraft().getClass().getDeclaredMethod("clickMouse");
            } catch (NoSuchMethodException var4) {
                try {
                    clickMouse = Minecraft.getMinecraft().getClass().getDeclaredMethod("func_147116_af");
                } catch (NoSuchMethodException ignored) {
                }
            }

            if (clickMouse != null) {
                clickMouse.setAccessible(true);
            }

            try {
                getPlayerInfo = AbstractClientPlayer.class.getDeclaredMethod("getPlayerInfo");
            } catch (NoSuchMethodException var4) {
                try {
                    getPlayerInfo = AbstractClientPlayer.class.getDeclaredMethod("func_175155_b");
                } catch (NoSuchMethodException ignored) {
                }
            }

            if (getPlayerInfo != null) {
                getPlayerInfo.setAccessible(true);
            }
        }
        catch (Exception e) {
            System.out.println("There was an error, relaunch the game.");
            sendMessage = true;
        }
    }

    public static void setButton(int t, boolean s) {
        MouseEvent m = new MouseEvent();
        m.setButton(t);
        m.setButtonstate(s);
        Client.EVENT_BUS.post(m);
        if (!m.isCancelled())
            ((ByteBuffer) ReflectionUtils.getDeclared(Mouse.class, "buttons")).put(t, (byte) (s ? 1 : 0));
    }

    private static void addToMap(Class<?> clazz, Field field) {
        if (field == null || field.getType() != IInventory.class) {
            return;
        }
        field = ReflectionHelper.findField(clazz, field.getName());
        if (field == null) {
            return;
        }
        field.setAccessible(true);
        containerInventoryPlayer.put(clazz, field);
    }

    public static void rightClick() {
        try {
            Reflection.rightClickMouse.invoke(Minecraft.getMinecraft());
        }
        catch (InvocationTargetException | IllegalAccessException ignored) {}
    }

    public static void clickMouse() {
        if (clickMouse != null) {
            try {
                clickMouse.invoke(Minecraft.getMinecraft());
            }
            catch (InvocationTargetException | IllegalAccessException ignored) {}
        }
    }

    public static boolean setBlocking(boolean blocking) {
        try {
            itemInUseCount.set(Minecraft.getMinecraft().thePlayer, blocking ? 1 : 0);
        } catch (Exception e) {
            Utils.sendMessage("§cFailed to set block state client-side.");
            return false;
        }
        return blocking;
    }
}
