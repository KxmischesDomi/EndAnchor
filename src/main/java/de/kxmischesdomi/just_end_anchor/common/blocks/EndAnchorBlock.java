package de.kxmischesdomi.just_end_anchor.common.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.UnmodifiableIterator;
import de.kxmischesdomi.just_end_anchor.common.entities.EndAnchorBlockEntity;
import de.kxmischesdomi.just_end_anchor.common.registry.ModBlockEntities;
import de.kxmischesdomi.just_end_anchor.common.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class EndAnchorBlock extends Block implements EntityBlock {

	public static final int NO_CHARGES = 0;
	public static final int MAX_CHARGES = 4;
	public static final IntegerProperty CHARGES;
	private static final ImmutableList<Vec3i> VALID_HORIZONTAL_SPAWN_OFFSETS;
	private static final ImmutableList<Vec3i> VALID_SPAWN_OFFSETS;

	public EndAnchorBlock(BlockBehaviour.Properties settings) {
		super(settings);
		this.registerDefaultState((this.stateDefinition.any()).setValue(CHARGES, 0));
	}

	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		ItemStack itemStack = player.getItemInHand(hand);
		if (hand == InteractionHand.MAIN_HAND && !isChargeItem(itemStack) && isChargeItem(player.getItemInHand(InteractionHand.OFF_HAND))) {
			return InteractionResult.PASS;
		} else if (isChargeItem(itemStack) && canCharge(state)) {
			charge(world, pos, state);
			if (!player.getAbilities().instabuild) {
				itemStack.shrink(1);
			}

			return InteractionResult.sidedSuccess(world.isClientSide);
		} else if (state.getValue(CHARGES) == 0) {
			return InteractionResult.PASS;
		} else if (!isEnd(world)) {
			if (!world.isClientSide) {
				this.explode(state, world, pos);
			}

			return InteractionResult.sidedSuccess(world.isClientSide);
		} else {
			if (!world.isClientSide) {
				ServerPlayer serverPlayerEntity = (ServerPlayer)player;
				if (serverPlayerEntity.getRespawnDimension() != world.dimension() || !pos.equals(serverPlayerEntity.getRespawnPosition())) {
					serverPlayerEntity.setRespawnPosition(world.dimension(), pos, 0.0F, false, true);
					world.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
					return InteractionResult.SUCCESS;
				}
			}

			return InteractionResult.CONSUME;
		}
	}

	private static boolean isChargeItem(ItemStack stack) {
		return stack.is(Items.ENDER_PEARL);
	}

	private static boolean canCharge(BlockState state) {
		return state.getValue(CHARGES) < 4;
	}

	private static boolean hasStillWater(BlockPos pos, Level world) {
		FluidState fluidState = world.getFluidState(pos);
		if (!fluidState.is(FluidTags.WATER)) {
			return false;
		} else if (fluidState.isSource()) {
			return true;
		} else {
			float f = (float)fluidState.getAmount();
			if (f < 2.0F) {
				return false;
			} else {
				FluidState fluidState2 = world.getFluidState(pos.below());
				return !fluidState2.is(FluidTags.WATER);
			}
		}
	}

	private void explode(BlockState state, Level world, final BlockPos explodedPos) {
		world.removeBlock(explodedPos, false);
		Stream<Direction> var10000 = Direction.Plane.HORIZONTAL.stream();
		Objects.requireNonNull(explodedPos);
		boolean bl = var10000.map(explodedPos::relative).anyMatch((pos) -> hasStillWater(pos, world));
		final boolean bl2 = bl || world.getFluidState(explodedPos.above()).is(FluidTags.WATER);
		ExplosionDamageCalculator explosionBehavior = new ExplosionDamageCalculator() {
			public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter world, BlockPos pos, BlockState blockState, FluidState fluidState) {
				return pos.equals(explodedPos) && bl2 ? Optional.of(Blocks.WATER.getExplosionResistance()) : super.getBlockExplosionResistance(explosion, world, pos, blockState, fluidState);
			}
		};

		world.explode(null, world.damageSources().badRespawnPointExplosion(explodedPos.getCenter()), explosionBehavior, (double) explodedPos.getX() + 0.5D, (double) explodedPos.getY() + 0.5D, (double) explodedPos.getZ() + 0.5D, 5.0F, false, Level.ExplosionInteraction.BLOCK);

		if (!world.isClientSide() && world.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {

			for (int i = 0; i < world.random.nextInt(12 - 6) + 6; i++) {
				Endermite endermiteEntity = new Endermite(EntityType.ENDERMITE, world);
				endermiteEntity.setPosRaw(explodedPos.getX() + 0.5, explodedPos.getY(), explodedPos.getZ() + 0.5);
				endermiteEntity.setYRot(0);
				endermiteEntity.setXRot(0);
				endermiteEntity.setDeltaMovement(new Vec3(world.random.nextDouble() * (world.random.nextBoolean() ? -1 : 1), world.random.nextDouble(), world.random.nextDouble() * (world.random.nextBoolean() ? -1 : 1)));
				world.addFreshEntity(endermiteEntity);
			}

		}

	}

	public static boolean isEnd(Level world) {
		return world.dimension().equals(Level.END);
	}

	public static void charge(Level world, BlockPos pos, BlockState state) {
		world.setBlock(pos, state.setValue(CHARGES, state.getValue(CHARGES) + 1), Block.UPDATE_ALL);
		world.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
		if (state.getValue(CHARGES) != 0) {
			if (random.nextInt(100) == 0) {
				world.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
			}

			double startPos = 0.1238F;
			double endPos = 0.8762F;
			double difference = endPos - startPos;

			double x = random.nextDouble() * difference + startPos;
			double y = random.nextDouble() * difference + startPos;

			double d = (double)pos.getX() + x;
			double e = (double)pos.getY() + 1.0D;
			double f = (double)pos.getZ() + y;
			double g = (double)random.nextFloat() * 0.04D;
			world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0D, g, 0.0D);

		}
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(CHARGES);
	}

	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	public static int getLightLevel(BlockState state, int maxLevel) {
		return state.getValue(CHARGES) == 0 ? 0 : 7;
	}

	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		return Mth.floor((float)(state.getValue(CHARGES)) / 4.0F * (float)15);
	}

	public static Optional<Vec3> findRespawnPosition(EntityType<?> entity, CollisionGetter world, BlockPos pos) {
		Optional<Vec3> optional = findRespawnPosition(entity, world, pos, true);
		return optional.isPresent() ? optional : findRespawnPosition(entity, world, pos, false);
	}

	private static Optional<Vec3> findRespawnPosition(EntityType<?> entity, CollisionGetter world, BlockPos pos, boolean bl) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		UnmodifiableIterator var5 = VALID_SPAWN_OFFSETS.iterator();

		Vec3 vec3d;
		do {
			if (!var5.hasNext()) {
				return Optional.empty();
			}

			Vec3i vec3i = (Vec3i)var5.next();
			mutable.set(pos).move(vec3i);
			vec3d = DismountHelper.findSafeDismountLocation(entity, world, mutable, bl);
		} while(vec3d == null);

		return Optional.of(vec3d);
	}

	public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new EndAnchorBlockEntity(ModBlockEntities.END_ANCHOR, pos, state);
	}

	static {
		CHARGES = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
		VALID_HORIZONTAL_SPAWN_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
		VALID_SPAWN_OFFSETS = (new Builder()).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS.stream().map(Vec3i::below).iterator()).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS.stream().map(Vec3i::above).iterator()).add((new Vec3i(0, 1, 0))).build();

		DispenserBlock.registerBehavior(Items.ENDER_PEARL, new OptionalDispenseItemBehavior() {
			public ItemStack execute(BlockSource pointer, ItemStack stack) {
				Direction direction = pointer.getBlockState().getValue(DispenserBlock.FACING);
				BlockPos blockPos = pointer.getPos().relative(direction);
				Level world = pointer.getLevel();
				BlockState blockState = world.getBlockState(blockPos);
				this.setSuccess(true);
				if (blockState.is(ModBlocks.END_ANCHOR)) {
					if (blockState.getValue(EndAnchorBlock.CHARGES) != 4) {
						EndAnchorBlock.charge(world, blockPos, blockState);
						stack.shrink(1);
					} else {
						this.setSuccess(false);
					}

					return stack;
				} else {
					return super.execute(pointer, stack);
				}
			}
		});

	}

}
