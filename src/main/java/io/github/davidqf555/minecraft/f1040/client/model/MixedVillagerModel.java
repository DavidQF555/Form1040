package io.github.davidqf555.minecraft.f1040.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.client.renderer.entity.model.VillagerModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.HandSide;

public class MixedVillagerModel<T extends MobEntity> extends VillagerModel<T> implements IHasArm {

    private final ModelRenderer leftArm, rightArm;

    public MixedVillagerModel(float offset) {
        this(offset, 80, 64);
    }

    public MixedVillagerModel(float offset, int texWidth, int texHeight) {
        super(offset, texWidth, texHeight);
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        rightArm = new ModelRenderer(this, 64, 0);
        rightArm.addBox(-3, -2, -2, 4, 12, 4, offset);
        rightArm.setPos(-5, 2, 0);
        leftArm = new ModelRenderer(this, 64, 0);
        leftArm.addBox(-1, -2, -2, 4, 12, 4, offset);
        leftArm.setPos(5, 2, 0);
        leftArm.mirror = true;
    }

    @Override
    public Iterable<ModelRenderer> parts() {
        return new ImmutableList.Builder<ModelRenderer>().addAll(super.parts()).add(leftArm, rightArm).build();
    }

    @Override
    public void setupAnim(T entity, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
        super.setupAnim(entity, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
        boolean aggressive = entity.isAggressive();
        if (entity.getMainHandItem().isEmpty()) {
            ModelHelper.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, p_225597_4_);
        } else {
            ModelHelper.swingWeaponDown(this.rightArm, this.leftArm, entity, this.attackTime, p_225597_4_);
        }
        this.arms.visible = !aggressive;
        this.leftArm.visible = aggressive;
        this.rightArm.visible = aggressive;
    }

    @Override
    public void translateToHand(HandSide hand, MatrixStack matrix) {
        (hand == HandSide.LEFT ? leftArm : rightArm).translateAndRotate(matrix);
    }
}
