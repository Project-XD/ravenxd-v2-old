package keystrokesmod.mixins.impl.world;

import keystrokesmod.module.impl.render.Optimize;
import keystrokesmod.performance.FastLightUpdater;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(World.class)
public abstract class MixinWorld implements IBlockAccess {

    @Unique
    private final FastLightUpdater raven_XD$lightUpdater = new FastLightUpdater((World) (Object) this);

    @Shadow public abstract boolean isAreaLoaded(BlockPos blockPos, int anInt, boolean anBoolean);

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    public void onUpdateLight(EnumSkyBlock skyBlock, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        if (!((World) (Object) this instanceof WorldClient)) return;
        if (!Optimize.isFastLight()) return;

        if (!this.isAreaLoaded(blockPos, 17, false)) {
            cir.setReturnValue(false);
            return;
        }

        if (Optimize.isFastLightMultiThread()) {
            raven_XD$lightUpdater.updateLightMultiThread(skyBlock, blockPos);
        } else if (Optimize.isFastLightDeSync()) {
            raven_XD$lightUpdater.updateLightDeSync(skyBlock, blockPos);
        } else {
            raven_XD$lightUpdater.updateLight(skyBlock, blockPos);
        }
        cir.setReturnValue(true);
    }
}
