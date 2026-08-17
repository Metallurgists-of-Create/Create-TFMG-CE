package com.drmangotea.tfmg.content.machinery.vat;

import com.drmangotea.tfmg.content.machinery.vat.base.IVatMachine;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface MultiUseAttachment<BE extends IVatMachine> {

    ResourceLocation getOperation();

    default String getOperationId() {
        return isValid() ? getOperation().toString() : "";
    }

    default boolean isValid() {
        return getOperation() != null;
    }

    void renderInVat(BE be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, @Nullable ItemRenderer itemRenderer);

    void tick(VatBlockEntity controllerVat, Level level, BlockPos pos, boolean active, boolean clientTick);
}
