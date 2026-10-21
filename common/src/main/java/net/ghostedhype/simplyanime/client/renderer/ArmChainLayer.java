package net.ghostedhype.simplyanime.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ghostedhype.simplyanime.entity.HeavenChainVisualEntity;
import net.ghostedhype.simplyanime.item.InvertedSpearItem;
import net.ghostedhype.simplyanime.platform.ClientPlatform;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ArmChainLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final float TURNS = 2.5F;
    private static final float LINK = 1.3F;

    private final boolean slim;

    public ArmChainLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> context,
                                   boolean slim) {
        super(context);
        this.slim = slim;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int light,
                       AbstractClientPlayer player, float limbAngle, float limbDistance, float partialTick,
                       float animationProgress, float headYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        HeavenChainVisualEntity chain = HeavenChainVisualEntity.CLIENT_BY_OWNER.get(player.getId());
        if (chain != null && !chain.isRemoved() && chain.level() == player.level() && chain.isChainOut()) {
            return;
        }

        HumanoidArm arm;
        if (player.getMainHandItem().getItem() instanceof InvertedSpearItem) {
            arm = player.getMainArm();
        } else if (player.getOffhandItem().getItem() instanceof InvertedSpearItem) {
            arm = player.getMainArm().getOpposite();
        } else {
            return;
        }

        poseStack.pushPose();
        PlayerModel<AbstractClientPlayer> model = this.getParentModel();
        (arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm).translateAndRotate(poseStack);
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(AddonRenderTypes.WHITE));
        wrap(vertices, poseStack.last().pose(), poseStack.last().normal(), arm, light);
        poseStack.popPose();
    }

    private void wrap(VertexConsumer vertices, Matrix4f matrix, Matrix3f normal, HumanoidArm arm, int light) {
        // arm box in model pixels, see PlayerEntityModel
        float halfX = this.slim ? 1.5F : 2.0F;
        float centerX = (arm == HumanoidArm.RIGHT ? -1.0F : 1.0F) * (this.slim ? 0.5F : 1.0F);
        float radiusX = halfX + 0.55F;
        float radiusZ = 2.55F;
        float top = -0.5F;
        float bottom = 9.6F;

        int samples = 240;
        Vec3 previous = spiral(0, centerX, radiusX, radiusZ, top, bottom);
        float travelled = 0;
        int link = 0;
        Vec3 linkStart = previous;
        for (int i = 1; i <= samples; i++) {
            Vec3 point = spiral(i / (float) samples, centerX, radiusX, radiusZ, top, bottom);
            travelled += (float) point.distanceTo(previous);
            previous = point;
            if (travelled < LINK) {
                continue;
            }
            drawLink(vertices, matrix, normal, linkStart, point, centerX, link++, light);
            linkStart = point;
            travelled = 0;
        }
    }

    private static Vec3 spiral(float progress, float centerX, float radiusX, float radiusZ, float top, float bottom) {
        float angle = progress * TURNS * Mth.TWO_PI;
        return new Vec3(centerX + Mth.cos(angle) * radiusX, Mth.lerp(progress, top, bottom), Mth.sin(angle) * radiusZ);
    }

    private static void drawLink(VertexConsumer vertices, Matrix4f matrix, Matrix3f normal, Vec3 from, Vec3 to,
                                 float centerX, int index, int light) {
        Vec3 along = to.subtract(from);
        Vec3 mid = from.add(to).scale(0.5);
        Vec3 out = new Vec3(mid.x - centerX, 0, mid.z).normalize();
        Vec3 stretch = along.scale(0.6);
        // alternate links lie flat on the arm and stand up off it
        boolean flat = (index & 1) == 0;
        Vec3 across = flat ? along.cross(out).normalize().scale(0.38) : out.scale(0.42);
        Vec3 center = flat ? mid : mid.add(out.scale(0.3));
        int shade = flat ? 196 : 128;

        Vec3 cornerA = center.subtract(stretch).subtract(across);
        Vec3 cornerB = center.add(stretch).subtract(across);
        Vec3 cornerC = center.add(stretch).add(across);
        Vec3 cornerD = center.subtract(stretch).add(across);
        Vec3 facing = flat ? out : along.cross(out).normalize();
        vertex(vertices, matrix, normal, cornerA, facing, shade, light);
        vertex(vertices, matrix, normal, cornerB, facing, shade, light);
        vertex(vertices, matrix, normal, cornerC, facing, shade, light);
        vertex(vertices, matrix, normal, cornerD, facing, shade, light);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Matrix3f normal, Vec3 pixel, Vec3 facing,
                               int shade, int light) {
        ClientPlatform.entityVertex(vertices, matrix, normal, (float) pixel.x / 16.0F, (float) pixel.y / 16.0F, (float) pixel.z / 16.0F,
                shade, shade, shade + 10, 255, 0.5F, 0.5F, light, (float) facing.x, (float) facing.y, (float) facing.z);
    }
}
