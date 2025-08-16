package keystrokesmod.mixins.impl.world;

import keystrokesmod.Client;
import keystrokesmod.event.world.BlockAABBEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Block.class)
public abstract class MixinBlock {

    @Shadow public abstract AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state);

    /**
     * @author xia__mc
     * @reason for Hypixel Auto Phase (in module phase)
     *
     */
    @Inject(method = "addCollisionBoxesToList", at = @At("HEAD"), cancellable = true)
    public void onAddCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list,
                                        Entity collidingEntity, CallbackInfo ci) {
        if (collidingEntity == Client.mc.thePlayer) {
            ci.cancel();

            final AxisAlignedBB axisalignedbb = this.getCollisionBoundingBox(worldIn, pos, state);
            final BlockAABBEvent event = new BlockAABBEvent(worldIn, (Block)(Object) this, pos, axisalignedbb, mask);
            Client.EVENT_BUS.post(event);

            if (event.getBoundingBox() != null && event.getMaskBoundingBox().intersectsWith(event.getBoundingBox())) {
                list.add(event.getBoundingBox());
            }
        }
    }
}
