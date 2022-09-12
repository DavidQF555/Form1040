package io.github.davidqf555.minecraft.f1040.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.davidqf555.minecraft.f1040.common.Form1040;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;

public class MixedVillagerModel<T extends Mob> extends VillagerModel<T> implements ArmedModel {

    public static final ModelLayerLocation MIXED_VILLAGER = new ModelLayerLocation(new ResourceLocation(Form1040.MOD_ID, "mixed_villager"), "main");
    public static final ModelLayerLocation EXTENDED_VILLAGER = new ModelLayerLocation(new ResourceLocation(Form1040.MOD_ID, "extended_villager"), "main");
    private final ModelPart leftArm, rightArm, arms;

    public MixedVillagerModel(ModelPart part) {
        super(part);
        arms = part.getChild("arms");
        rightArm = part.getChild("right_arm");
        leftArm = part.getChild("left_arm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = createBodyModel();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(64, 0)
                .addBox(-3, -2, -2, 4, 12, 4), PartPose.offset(-5, 2, 0)
        );
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(64, 0)
                .mirror()
                .addBox(-1, -2, -2, 4, 12, 4), PartPose.offset(5, 2, 0)
        );
        return LayerDefinition.create(meshdefinition, 80, 64);
    }

    public static LayerDefinition createExtendedVillager() {
        return LayerDefinition.create(createBodyModel(), 80, 64);
    }

    @Override
    public void setupAnim(T entity, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
        super.setupAnim(entity, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
        boolean aggressive = entity.isAggressive();
        if (entity.getMainHandItem().isEmpty()) {
            AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, p_225597_4_);
        } else {
            AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, entity, this.attackTime, p_225597_4_);
        }
        this.arms.visible = !aggressive;
        this.leftArm.visible = aggressive;
        this.rightArm.visible = aggressive;
    }

    @Override
    public void translateToHand(HumanoidArm hand, PoseStack matrix) {
        (hand == HumanoidArm.LEFT ? leftArm : rightArm).translateAndRotate(matrix);
    }
}
