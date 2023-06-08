package de.kxmischesdomi.just_end_anchor.common.registry;

import de.kxmischesdomi.just_end_anchor.EndAnchorMod;
import de.kxmischesdomi.just_end_anchor.common.blocks.EndAnchorBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class ModBlocks {

	public static Block END_ANCHOR = register("end_anchor", new EndAnchorBlock(FabricBlockSettings.of().requiresCorrectToolForDrops().strength(50f, 1200.0F).lightLevel((state) -> EndAnchorBlock.getLightLevel(state, 15)).mapColor(MapColor.COLOR_BLACK)));

	private static <T extends Block> T register(String name, T block) {
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(EndAnchorMod.MOD_ID, name), block);
		return block;
	}

}
