package keystrokesmod.module.impl.movement.noweb;

import keystrokesmod.event.world.BlockAABBEvent;
import keystrokesmod.eventbus.annotations.EventListener;
import keystrokesmod.module.impl.movement.NoWeb;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

public class VulcanNoWeb extends SubMode<NoWeb> {
    public VulcanNoWeb(String name, @NotNull NoWeb parent) {
        super(name, parent);
    }

    @EventListener
    public void onAABB(@NotNull BlockAABBEvent event) {
        if (event.getBlock() == Blocks.web) {
            BlockPos pos = event.getBlockPos();
            event.setBoundingBox(new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1));
        }
    }
}
