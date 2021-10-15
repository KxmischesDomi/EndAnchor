package de.kxmischesdomi.just_end_anchor.client.renderer;

import de.kxmischesdomi.just_end_anchor.common.blocks.EndAnchorBlock;
import de.kxmischesdomi.just_end_anchor.common.entities.EndAnchorBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.EndPortalBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;

import java.util.Random;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class EndAnchorBlockEntityRenderer<T extends EndAnchorBlockEntity> extends EndPortalBlockEntityRenderer<T> {
	private static final Random RANDOM = new Random(31100L);

	public EndAnchorBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}

	public void render(T endPortalBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
		if (endPortalBlockEntity.getCachedState().get(EndAnchorBlock.CHARGES) > 0) {
			RANDOM.setSeed(31100L);

			double squaredDistance = endPortalBlockEntity.getPos().getSquaredDistance(this.dispatcher.camera.getPos(), true);
			int layers = this.method_3592(squaredDistance);

			Matrix4f matrix4f = matrixStack.peek().getModel();

			this.renderSides(endPortalBlockEntity, 1.0F, 0.15F, matrix4f, vertexConsumerProvider.getBuffer(RenderLayer.getEndPortal(1)));
			for(int layer = 1; layer < layers; ++layer) {
				this.renderSides(endPortalBlockEntity, 1.0F, 2.0F / (float)(18 - layer), matrix4f, vertexConsumerProvider.getBuffer(RenderLayer.getEndPortal(1 + layer)));
			}
		}
	}

	private void renderSides(T endPortalBlockEntity, float blockHeight, float lightness, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
		float r = (RANDOM.nextFloat() * 0.5F + 0.1F) * lightness;
		float g = (RANDOM.nextFloat() * 0.5F + 0.4F) * lightness;
		float b = (RANDOM.nextFloat() * 0.5F + 0.5F) * lightness;
		this.renderSide(endPortalBlockEntity, matrix4f, vertexConsumer, 0.1238F, 0.8762F, blockHeight, blockHeight, 0.8762F, 0.8762F, 0.1238F, 0.1238F, r, g, b, Direction.UP);
	}

	private void renderSide(T endPortalBlockEntity, Matrix4f matrix4f, VertexConsumer vertexConsumer, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, float r, float g, float b, Direction direction) {
		if (endPortalBlockEntity.shouldDrawSide(direction)) {
			vertexConsumer.vertex(matrix4f, x1, y1, z1).color(r, g, b, 1.0F).next();
			vertexConsumer.vertex(matrix4f, x2, y1, z2).color(r, g, b, 1.0F).next();
			vertexConsumer.vertex(matrix4f, x2, y2, z3).color(r, g, b, 1.0F).next();
			vertexConsumer.vertex(matrix4f, x1, y2, z4).color(r, g, b, 1.0F).next();
		}
	}
}
