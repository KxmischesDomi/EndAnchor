package de.kxmischesdomi.just_end_anchor.mixin;

import de.kxmischesdomi.just_end_anchor.EndAnchorMod;
import de.kxmischesdomi.just_end_anchor.common.blocks.EndAnchorBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Inject(method = "findRespawnPosition", at = @At(value = "HEAD"), cancellable = true)
	private static void findRespawn(ServerWorld world, BlockPos pos, float f, boolean bl, boolean bl2, CallbackInfoReturnable<Optional<Vec3d>> cir) {
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();

		if (EndAnchorMod.respawnAfterCredits) {
			EndAnchorMod.respawnAfterCredits = false;
			return;
		}

		if (block instanceof EndAnchorBlock && blockState.get(EndAnchorBlock.CHARGES) > 0 && EndAnchorBlock.isEnd(world)) {
			Optional<Vec3d> optional = EndAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, pos);
			if (!bl2 && optional.isPresent()) {
				world.setBlockState(pos, blockState.with(EndAnchorBlock.CHARGES, blockState.get(EndAnchorBlock.CHARGES) - 1), 3);
			}

			cir.setReturnValue(optional);
		}
	}

}
