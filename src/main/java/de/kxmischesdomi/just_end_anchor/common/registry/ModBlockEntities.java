package de.kxmischesdomi.just_end_anchor.common.registry;

import de.kxmischesdomi.just_end_anchor.EndAnchorMod;
import de.kxmischesdomi.just_end_anchor.common.entities.EndAnchorBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class ModBlockEntities {

	public static BlockEntityType<EndAnchorBlockEntity> END_ANCHOR = BlockEntityType.Builder.create(EndAnchorBlockEntity::new, ModBlocks.END_ANCHOR).build(null);

	public static void init() {
		END_ANCHOR = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(EndAnchorMod.MOD_ID, "end_anchor"), END_ANCHOR);
	}

}
