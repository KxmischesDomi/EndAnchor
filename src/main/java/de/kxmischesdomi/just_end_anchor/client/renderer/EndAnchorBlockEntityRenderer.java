package de.kxmischesdomi.just_end_anchor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.kxmischesdomi.just_end_anchor.common.blocks.EndAnchorBlock;
import de.kxmischesdomi.just_end_anchor.common.entities.EndAnchorBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 1.0
 */
public class EndAnchorBlockEntityRenderer<T extends EndAnchorBlockEntity> extends TheEndPortalRenderer<T> {

	public EndAnchorBlockEntityRenderer(Context ctx) {
		super(ctx);
	}

	public void render(T endPortalBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
		if (endPortalBlockEntity.getBlockState().getValue(EndAnchorBlock.CHARGES) > 0) {
			Matrix4f matrix4f = matrixStack.last().pose();
			this.renderSide(endPortalBlockEntity, matrix4f, vertexConsumerProvider.getBuffer(this.renderType()), 0.1238F, 0.8762F, 1, 1, 0.8762F, 0.8762F, 0.1238F, 0.1238F, Direction.UP);
		}

	}

	private void renderSide(T entity, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, Direction direction) {
		if (entity.shouldRenderFace(direction)) {
			vertex(vertices, model, x1, y1, z1).endVertex();
			vertex(vertices, model, x2, y1, z2).endVertex();
			vertex(vertices, model, x2, y2, z3).endVertex();
			vertex(vertices, model, x1, y2, z4).endVertex();
		}

	}

	VertexConsumer vertex(VertexConsumer vertexConsumer, Matrix4f matrix, float x, float y, float z) {
		Vector4f vector4f = matrix.transform(new Vector4f(x, y, z, 1.0f));
		return vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z());
	}


}
