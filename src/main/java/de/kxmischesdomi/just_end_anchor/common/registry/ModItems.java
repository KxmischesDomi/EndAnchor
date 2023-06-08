package de.kxmischesdomi.just_end_anchor.common.registry;

import de.kxmischesdomi.just_end_anchor.EndAnchorMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class ModItems {

	public static Item END_ANCHOR = register(ModBlocks.END_ANCHOR);

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
			entries.accept(END_ANCHOR);
		});
	}

	public static <T extends Item> T register(String name, T item) {
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(EndAnchorMod.MOD_ID, name), item);
		return item;
	}

	private static BlockItem register(Block block) {
		return register(BuiltInRegistries.BLOCK.getKey(block).getPath(), new BlockItem(block, (new Item.Properties())));
	}

}
    