package keystrokesmod.module.impl.combat.criticals;

import keystrokesmod.module.impl.combat.Criticals;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.entity.EntityLivingBase;
import keystrokesmod.event.network.AttackEntityEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import org.jetbrains.annotations.NotNull;

public class JumpCriticals extends SubMode<Criticals> {
    public JumpCriticals(String name, @NotNull Criticals parent) {
        super(name, parent);
    }

    @EventListener
    public void onAttack(@NotNull AttackEntityEvent event) {
        if (event.getTarget() instanceof EntityLivingBase && mc.thePlayer.onGround)
            MoveUtil.jump();
    }
}
