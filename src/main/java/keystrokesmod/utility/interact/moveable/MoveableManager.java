package keystrokesmod.utility.interact.moveable;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import keystrokesmod.Client;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import net.minecraft.client.gui.GuiChat;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.event.render.Render2DEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;

import java.util.Objects;
import java.util.Set;

import static keystrokesmod.Client.mc;

public final class MoveableManager {
    private static final Set<MoveableRender> moveObjs = new ObjectOpenHashSet<>();
    private static int lastX;
    private static int lastY;
    private static boolean isDragging = false;
    private static @Nullable MoveableRender draggingObj = null;
    private static final CoolDown coolDown = new CoolDown(0);

    public static void init() {
        Client.EVENT_BUS.register(MoveableManager.class);
    }

    public static void register(Moveable moveable) {
        moveObjs.add(new MoveableRender(moveable));
    }

    public static void unregister(Moveable moveable) {
        moveObjs.remove(new MoveableRender(moveable));  // we override equals and hashcode
    }

    @EventListener
    public static void onRender(Render2DEvent event) {
        if (!(mc.currentScreen instanceof GuiChat)) {
            draggingObj = null;
            isDragging = false;
            coolDown.start();
            return;
        }

        final int x = Mouse.getEventX()
                * mc.currentScreen.width / mc.currentScreen.mc.displayWidth;
        final int y = mc.currentScreen.height - Mouse.getEventY()
                * mc.currentScreen.height / mc.currentScreen.mc.displayHeight - 1;

        boolean disabled = false;
        if (!coolDown.finished(100)) {
            lastX = x;
            lastY = y;
            disabled = true;
        }

        if (Mouse.isButtonDown(0) && !disabled) {
            if (!isDragging) {
                // 开始拖拽
                for (MoveableRender obj : moveObjs) {
                    if (isHover(obj.moveable, x, y)) {
                        draggingObj = obj;
                        isDragging = true;
                        break;
                    }
                }
            } else if (draggingObj != null) {
                // 拖拽中
                draggingObj.moveX(x - lastX);
                draggingObj.moveY(y - lastY);
            }
        } else {
            // 鼠标左键松开，结束拖拽
            draggingObj = null;
            isDragging = false;
        }

        // 更新所有对象
        for (MoveableRender obj : moveObjs) {
            obj.update();
        }

        lastX = x;
        lastY = y;
    }

    private static boolean isHover(@NotNull Moveable obj, int mouseX, int mouseY) {
        return mouseX >= obj.getMinX() - 2 && mouseX <= obj.getMaxX() + 2
                && mouseY >= obj.getMinY() - 2 && mouseY <= obj.getMaxY() + 2;
    }

    private static final class MoveableRender {
        public final Moveable moveable;
        private final Animation animationX = new Animation(Easing.EASE_OUT_CIRC, 100);
        private final Animation animationY = new Animation(Easing.EASE_OUT_CIRC, 100);

        private int targetX;
        private int targetY;
        private int lastX;
        private int lastY;

        public MoveableRender(@NotNull Moveable moveable) {
            this.moveable = moveable;
            targetX = lastX = moveable.getMinX();
            targetY = lastY = moveable.getMinY();
        }

        public void update() {
            if (targetX != lastX) {
                animationX.run(targetX);
                double curX = animationX.getValue();
                int motionX = (int) Math.round(curX - lastX);
                lastX += motionX;
                moveable.moveX(motionX);
            }

            if (targetY != lastY) {
                animationY.run(targetY);
                double curY = animationY.getValue();
                int motionY = (int) Math.round(curY - lastY);
                lastY += motionY;
                moveable.moveY(motionY);
            }

            moveable.render();
        }

        public void moveX(int amount) {
            targetX += amount;
        }

        public void moveY(int amount) {
            targetY += amount;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            MoveableRender that = (MoveableRender) object;
            return Objects.equals(moveable, that.moveable);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(moveable);
        }
    }
}
