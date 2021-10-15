package de.kxmischesdomi.just_end_anchor.common.entities;

import de.kxmischesdomi.just_end_anchor.common.registry.ModBlockEntities;

import net.minecraft.block.entity.EndPortalBlockEntity;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class EndAnchorBlockEntity extends EndPortalBlockEntity {
	public EndAnchorBlockEntity() {
		super(ModBlockEntities.END_ANCHOR);
	}
}
