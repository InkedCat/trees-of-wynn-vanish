package dev.inkedcat.treesofwynnvanish.client.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientLevel.class, priority = 1500)
public class EnableNonAirBlockChangeDirtyingMixin {
	@TargetHandler(
			mixin = "me.cortex.voxy.client.mixin.minecraft.MixinClientLevel",
			name = "voxy$injectIngestOnStateChange",
			prefix = "handler"
	)
	@ModifyExpressionValue(
			method = "@MixinSquared:Handler",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z")
	)
	private boolean towv$allowNonAirBlock(boolean original) {
		return original;
	}
}