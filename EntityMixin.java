package pl.visuals.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.visuals.VisualsConfig;
import pl.visuals.VisualsMod;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void visualsmod$glow(CallbackInfoReturnable<Boolean> cir) {
        if (VisualsMod.target == (Object) this && VisualsConfig.Visual.GLOW.enabled) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void visualsmod$glowColor(CallbackInfoReturnable<Integer> cir) {
        if (VisualsMod.target == (Object) this && VisualsConfig.Visual.GLOW.enabled) {
            cir.setReturnValue(VisualsConfig.Visual.GLOW.color() & 0xFFFFFF);
        }
    }
}
