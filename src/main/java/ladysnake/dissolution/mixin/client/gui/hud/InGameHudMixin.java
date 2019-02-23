package ladysnake.dissolution.mixin.client.gui.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import ladysnake.dissolution.api.v1.DissolutionPlayer;
import ladysnake.dissolution.api.v1.event.client.HotbarRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow @Final private MinecraftClient client;

    @Shadow @Nullable protected abstract PlayerEntity getCameraPlayer();

    @Shadow protected abstract int method_1744(LivingEntity livingEntity_1);

    @Inject(
            method = "renderStatusBars",
            at = @At(value = "CONSTANT", args = "stringValue=health")
    )
    private void drawPossessionHud(CallbackInfo info) {
        if (((DissolutionPlayer)client.player).getRemnantState().isIncorporeal()) {
            // Make everything that follows *invisible*
            GlStateManager.color4f(1, 1, 1, 0);
        }
    }

    @Redirect(
            method = "renderStatusBars",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;method_1744(Lnet/minecraft/entity/LivingEntity;)I"
            )
    )
    private int preventFoodRender(InGameHud self, LivingEntity livingEntity_1) {
        int actual = this.method_1744(livingEntity_1);
        DissolutionPlayer cameraPlayer = (DissolutionPlayer) this.getCameraPlayer();
        if (actual == 0 && cameraPlayer != null && cameraPlayer.getRemnantState().isSoul()) {
            return -1;
        }
        return actual;
    }

    @Redirect(
            method = "renderStatusBars",
            slice = @Slice(from = @At(value = "CONSTANT", args="stringValue=air")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isInFluid(Lnet/minecraft/tag/Tag;)Z")
    )
    private boolean preventAirRender(PlayerEntity playerEntity, Tag<Fluid> fluid) {
        if (((DissolutionPlayer)playerEntity).getRemnantState().isSoul()) {
            Entity possessed = (Entity) ((DissolutionPlayer) playerEntity).getPossessionComponent().getPossessedEntity();
            if (possessed == null) {
                return false;
            } else if (possessed instanceof LivingEntity && ((LivingEntity) possessed).canBreatheInWater()) {
                return false;
            }
        }
        return playerEntity.isInFluid(fluid);
    }

    @ModifyVariable(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/SystemUtil;getMeasuringTimeMs()J"), ordinal = 0)
    private int substituteHealth(int health) {
        LivingEntity entity = (LivingEntity) ((DissolutionPlayer)client.player).getPossessionComponent().getPossessedEntity();
        if (entity != null) {
            return MathHelper.ceil(entity.getHealth());
        }
        return health;
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void fireHotBarRenderEvent(float tickDelta, CallbackInfo info) {
        if (HotbarRenderCallback.EVENT.invoker().onHotbarRendered(tickDelta) != ActionResult.PASS) {
            info.cancel();
        }
    }

    @Inject(
            method = "renderStatusBars",
            at = @At(value = "CONSTANT", args = "stringValue=air")
    )
    private void resumeDrawing(CallbackInfo info) {
        if (((DissolutionPlayer)client.player).getRemnantState().isSoul()) {
            GlStateManager.color4f(1, 1, 1, 1);
        }
    }

}