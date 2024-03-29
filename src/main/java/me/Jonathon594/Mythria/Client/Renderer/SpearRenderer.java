package me.Jonathon594.Mythria.Client.Renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.Jonathon594.Mythria.Client.Model.SpearModel;
import me.Jonathon594.Mythria.Entity.SpearEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class SpearRenderer extends EntityRenderer<SpearEntity> {
    private final SpearModel spearModel = new SpearModel();

    public SpearRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    public void render(SpearEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationYaw, entityIn.rotationYaw) - 90.0F));
        matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entityIn.prevRotationPitch, entityIn.rotationPitch) - 90.0F));
        IVertexBuilder ivertexbuilder = net.minecraft.client.renderer.ItemRenderer.getBuffer(bufferIn, this.spearModel.getRenderType(this.getEntityTexture(entityIn)), false, entityIn.func_226572_w_());
        this.spearModel.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStackIn.pop();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getEntityTexture(SpearEntity entity) {
        return entity.getTexture();
    }
}
