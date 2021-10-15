package de.kxmischesdomi.just_end_anchor.common.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import de.kxmischesdomi.just_end_anchor.common.entities.EndAnchorBlockEntity;
import de.kxmischesdomi.just_end_anchor.common.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.Explosion.DestructionType;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class EndAnchorBlock extends Block implements BlockEntityProvider {

	public static final int NO_CHARGES = 0;
	public static final int MAX_CHARGES = 4;
	public static final IntProperty CHARGES;
	private static final ImmutableList<Vec3i> VALID_HORIZONTAL_SPAWN_OFFSETS;
	private static final ImmutableList<Vec3i> VALID_SPAWN_OFFSETS;

	public EndAnchorBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState((this.stateManager.getDefaultState()).with(CHARGES, 0));
	}

	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (hand == Hand.MAIN_HAND && !isChargeItem(itemStack) && isChargeItem(player.getStackInHand(Hand.OFF_HAND))) {
			return ActionResult.PASS;
		} else if (isChargeItem(itemStack) && canCharge(state)) {
			charge(world, pos, state);
			if (!player.abilities.creativeMode) {
				itemStack.decrement(1);
			}

			return ActionResult.success(world.isClient);
		} else if (state.get(CHARGES) == 0) {
			return ActionResult.PASS;
		} else if (!isEnd(world)) {
			if (!world.isClient) {
				this.explode(state, world, pos);
			}

			return ActionResult.success(world.isClient);
		} else {
			if (!world.isClient) {
				ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
				if (serverPlayerEntity.getSpawnPointDimension() != world.getRegistryKey() || !pos.equals(serverPlayerEntity.getSpawnPointPosition())) {
					serverPlayerEntity.setSpawnPoint(world.getRegistryKey(), pos, 0.0F, false, true);
					world.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
					return ActionResult.SUCCESS;
				}
			}

			return ActionResult.CONSUME;
		}
	}

	private static boolean isChargeItem(ItemStack stack) {
		return stack.getItem() == Items.ENDER_PEARL;
	}

	private static boolean canCharge(BlockState state) {
		return state.get(CHARGES) < 4;
	}

	private static boolean hasStillWater(BlockPos pos, World world) {
		FluidState fluidState = world.getFluidState(pos);
		if (!fluidState.isIn(FluidTags.WATER)) {
			return false;
		} else if (fluidState.isStill()) {
			return true;
		} else {
			float f = (float)fluidState.getLevel();
			if (f < 2.0F) {
				return false;
			} else {
				FluidState fluidState2 = world.getFluidState(pos.down());
				return !fluidState2.isIn(FluidTags.WATER);
			}
		}
	}

	private void explode(BlockState state, World world, final BlockPos explodedPos) {
		world.removeBlock(explodedPos, false);
		Stream<Direction> var10000 = Direction.Type.HORIZONTAL.stream();
		Objects.requireNonNull(explodedPos);
		boolean bl = var10000.map(explodedPos::offset).anyMatch((pos) -> hasStillWater(pos, world));
		final boolean bl2 = bl || world.getFluidState(explodedPos.up()).isIn(FluidTags.WATER);
		ExplosionBehavior explosionBehavior = new ExplosionBehavior() {
			public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
				return pos.equals(explodedPos) && bl2 ? Optional.of(Blocks.WATER.getBlastResistance()) : super.getBlastResistance(explosion, world, pos, blockState, fluidState);
			}
		};

//		Explosion explosion = new Explosion(world, null, DamageSource.badRespawnPoint(), explosionBehavior, explodedPos.getX() + 0.5D, explodedPos.getY() + 0.5D, (double) explodedPos.getZ() + 0.5D, 5.0F, false, DestructionType.DESTROY);
//		explosion.collectBlocksAndDamageEntities();

		world.createExplosion(null, DamageSource.badRespawnPoint(), explosionBehavior, (double) explodedPos.getX() + 0.5D, (double) explodedPos.getY() + 0.5D, (double) explodedPos.getZ() + 0.5D, 5.0F, false, DestructionType.DESTROY);

		List<EndermiteEntity> spawnedEndermites = Lists.newArrayList();

		if (!world.isClient()) {

			for (int i = 0; i < world.random.nextInt(12 - 6) + 6; i++) {
				EndermiteEntity endermiteEntity = new EndermiteEntity(EntityType.ENDERMITE, world);
				spawnedEndermites.add(endermiteEntity);
				endermiteEntity.refreshPositionAndAngles(explodedPos.getX() + 0.5, explodedPos.getY(), explodedPos.getZ() + 0.5, 0, 0);
				endermiteEntity.setVelocity(new Vec3d(world.random.nextDouble() * (world.random.nextBoolean() ? -1 : 1), world.random.nextDouble(), world.random.nextDouble() * (world.random.nextBoolean() ? -1 : 1)));
				world.spawnEntity(endermiteEntity);
			}

		}

	}

	public static boolean isEnd(World world) {
		return world.getRegistryKey().equals(World.END);
	}

	public static void charge(World world, BlockPos pos, BlockState state) {
		world.setBlockState(pos, state.with(CHARGES, state.get(CHARGES) + 1), 3);
		world.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
	}

	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(CHARGES) != 0) {
			if (random.nextInt(100) == 0) {
				world.playSound(null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
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

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(CHARGES);
	}

	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	public static int getLightLevel(BlockState state, int maxLevel) {
		return state.get(CHARGES) == 0 ? 0 : 7;
	}

	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		return MathHelper.floor((float)(state.get(CHARGES)) / 4.0F * (float)15);
	}

	public static Optional<Vec3d> findRespawnPosition(EntityType<?> entity, CollisionView world, BlockPos pos) {
		Optional<Vec3d> optional = findRespawnPosition(entity, world, pos, true);
		return optional.isPresent() ? optional : findRespawnPosition(entity, world, pos, false);
	}

	private static Optional<Vec3d> findRespawnPosition(EntityType<?> entity, CollisionView world, BlockPos pos, boolean bl) {
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		UnmodifiableIterator<Vec3i> var5 = VALID_SPAWN_OFFSETS.iterator();

		Vec3d vec3d;
		do {
			if (!var5.hasNext()) {
				return Optional.empty();
			}

			Vec3i vec3i = (Vec3i)var5.next();
			mutable.set(pos).move(vec3i);
			vec3d = Dismounting.method_30769(entity, world, mutable, bl);
		} while(vec3d == null);

		return Optional.of(vec3d);
	}

	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.BLOCK;
	}

	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new EndAnchorBlockEntity();
	}

	static {
		CHARGES = Properties.CHARGES;
		VALID_HORIZONTAL_SPAWN_OFFSETS = ImmutableList.of(new Vec3i(0, 0, -1), new Vec3i(-1, 0, 0), new Vec3i(0, 0, 1), new Vec3i(1, 0, 0), new Vec3i(-1, 0, -1), new Vec3i(1, 0, -1), new Vec3i(-1, 0, 1), new Vec3i(1, 0, 1));
		VALID_SPAWN_OFFSETS = (new Builder<Vec3i>()).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS.stream().map(Vec3i::down).iterator()).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS.stream().map(Vec3i::up).iterator()).add((new Vec3i(0, 1, 0))).build();

		DispenserBlock.registerBehavior(Items.ENDER_PEARL, new FallibleItemDispenserBehavior() {
			public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				Direction direction = pointer.getBlockState().get(DispenserBlock.FACING);
				BlockPos blockPos = pointer.getBlockPos().offset(direction);
				World world = pointer.getWorld();
				BlockState blockState = world.getBlockState(blockPos);
				this.setSuccess(true);
				if (blockState.isOf(ModBlocks.END_ANCHOR)) {
					if (blockState.get(EndAnchorBlock.CHARGES) != 4) {
						EndAnchorBlock.charge(world, blockPos, blockState);
						stack.decrement(1);
					} else {
						this.setSuccess(false);
					}

					return stack;
				} else {
					return super.dispenseSilently(pointer, stack);
				}
			}
		});

	}

}
