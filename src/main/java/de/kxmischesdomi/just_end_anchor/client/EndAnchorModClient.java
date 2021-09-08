package de.kxmischesdomi.just_end_anchor.client;

import de.kxmischesdomi.just_end_anchor.client.renderer.EndAnchorBlockEntityRenderer;
import de.kxmischesdomi.just_end_anchor.common.registry.ModBlockEntities;
import de.kxmischesdomi.just_end_anchor.common.registry.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class EndAnchorModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ModBlocks.END_ANCHOR);
		BlockEntityRendererRegistry.INSTANCE.register(ModBlockEntities.END_ANCHOR, EndAnchorBlockEntityRenderer::new);
	}

}
