package keystrokesmod.mixins.impl.render;

import keystrokesmod.Client;
import keystrokesmod.event.render.Render2DEvent;
import keystrokesmod.event.render.Render3DEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.render.CustomFOV;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static keystrokesmod.Client.mc;

@SuppressWarnings("UnresolvedMixinReference")
@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Shadow
    private float thirdPersonDistance;
    @Shadow
    private float thirdPersonDistanceTemp;
    @Shadow
    private boolean cloudFog;

    @Shadow protected abstract void setupCameraTransform(float p_setupCameraTransform_1_, int p_setupCameraTransform_2_);

    /**
     * @author strangerrrs
     * @reason for noHurtCam module
     */
    @Overwrite
    private void hurtCameraEffect(float p_hurtCameraEffect_1_) {
        if (mc.getRenderViewEntity() instanceof EntityLivingBase) {
            EntityLivingBase entitylivingbase = (EntityLivingBase)mc.getRenderViewEntity();
            float f = (float)entitylivingbase.hurtTime - p_hurtCameraEffect_1_;
            float f2;
            if (entitylivingbase.getHealth() <= 0.0F) {
                f2 = (float)entitylivingbase.deathTime + p_hurtCameraEffect_1_;
                GlStateManager.rotate(40.0F - 8000.0F / (f2 + 200.0F), 0.0F, 0.0F, 1.0F);
            }

            if (f < 0.0F) {
                return;
            }

            f /= (float)entitylivingbase.maxHurtTime;
            f = MathHelper.sin(f * f * f * f * 3.1415927F);
            f2 = entitylivingbase.attackedAtYaw;
            GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-f * (ModuleManager.noHurtCam.isEnabled() ? (float) ModuleManager.noHurtCam.multiplier.getInput() : 14.0F), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
        }
    }

    @Inject(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Vec3;distanceTo(Lnet/minecraft/util/Vec3;)D"), cancellable = true)
    public void orientCamera(float partialTicks, CallbackInfo ci) {
        if (ModuleManager.noCameraClip.isEnabled()) {
            ci.cancel();
            Entity entity = mc.getRenderViewEntity();
            float f = entity.getEyeHeight();
            float f1;
            if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
                f = (float) ((double) f + 1.0D);
                GlStateManager.translate(0.0F, 0.3F, 0.0F);
                if (!mc.gameSettings.debugCamEnable) {
                    BlockPos blockpos = new BlockPos(entity);
                    IBlockState iblockstate = Blocks.air.getDefaultState();
                    ForgeHooksClient.orientBedCamera(mc.theWorld, blockpos, iblockstate, entity);
                    GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                    GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
                }
            } else if (mc.gameSettings.thirdPersonView > 0) {
                double d3 = this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * partialTicks;
                if (mc.gameSettings.debugCamEnable) {
                    GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                } else {
                    f1 = entity.rotationYaw;
                    float f2 = entity.rotationPitch;
                    if (mc.gameSettings.thirdPersonView == 2) {
                        f2 += 180.0F;
                    }

                    if (mc.gameSettings.thirdPersonView == 2) {
                        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    }

                    GlStateManager.rotate(entity.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(entity.rotationYaw - f1, 0.0F, 1.0F, 0.0F);
                    GlStateManager.translate(0.0F, 0.0F, (float) (-d3));
                    GlStateManager.rotate(f1 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(f2 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
                }
            } else {
                GlStateManager.translate(0.0F, 0.0F, -0.1F);
            }

            if (!mc.gameSettings.debugCamEnable) {
                float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F;
                float pitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
                f1 = 0.0F;
                if (entity instanceof EntityAnimal) {
                    EntityAnimal entityanimal = (EntityAnimal) entity;
                    yaw = entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F;
                }

                GlStateManager.rotate(f1, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.translate(0.0F, -f, 0.0F);
            double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
            double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f;
            double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
            this.cloudFog = mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks);
        }
    }

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    public void onRender2D(float partialTicks, long p_updateCameraAndRender_2_, CallbackInfo ci) {
        Client.EVENT_BUS.post(new Render2DEvent(partialTicks));
    }

    @Inject(method = "renderWorldPass", at = @At("RETURN"))
    public void onRender3D(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        setupCameraTransform(partialTicks, 0);
        Client.EVENT_BUS.post(new Render3DEvent(partialTicks));
    }

    /**
     * @author kefpull
     * @reason attempt 1 have an option for CustomFOV to do the same thing as optifine's "Dynamic FOV:off". Hopefully this does not break the whole entire client :).
     * I know we're supposed to use an event or something like that, so I will do that if I can.
     * <p>
     * Source for method: line 412 in EntityRenderer.class
     */

    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    public void onGetFOVModifier(@NotNull CallbackInfoReturnable<Float> cir) {
        if (ModuleManager.customFOV == null) return;
        if (ModuleManager.customFOV.isEnabled() && ModuleManager.customFOV.forceStaticFOV.isToggled()) {
            cir.setReturnValue(CustomFOV.getDesiredFOV());
        }
    }
}
