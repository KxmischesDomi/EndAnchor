package de.kxmischesdomi.just_end_anchor.common.registry;

import de.kxmischesdomi.just_end_anchor.EndAnchorMod;
import de.kxmischesdomi.just_end_anchor.common.entities.EndAnchorBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class ModBlockEntities {

	public static BlockEntityType<EndAnchorBlockEntity> END_ANCHOR;

	public static void init() {
		END_ANCHOR = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(EndAnchorMod.MOD_ID, "end_anchor"), FabricBlockEntityTypeBuilder.create(EndAnchorBlockEntity::new, ModBlocks.END_ANCHOR).build());
	}

}
