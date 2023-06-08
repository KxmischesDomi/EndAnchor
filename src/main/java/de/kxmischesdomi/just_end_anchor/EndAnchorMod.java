package de.kxmischesdomi.just_end_anchor;

import de.kxmischesdomi.just_end_anchor.common.registry.ModBlockEntities;
import de.kxmischesdomi.just_end_anchor.common.registry.ModItems;
import net.fabricmc.api.ModInitializer;

public class EndAnchorMod implements ModInitializer {

	public static final String MOD_ID = "just_end_anchor";
	public static boolean respawnAfterCredits = false;

	@Override
	public void onInitialize() {
		ModItems.init();
		ModBlockEntities.init();
	}

}
