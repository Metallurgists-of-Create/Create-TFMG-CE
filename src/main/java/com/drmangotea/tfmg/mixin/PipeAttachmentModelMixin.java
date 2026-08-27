package com.drmangotea.tfmg.mixin;

import com.drmangotea.tfmg.content.decoration.pipes.rendering.TFMGPipeModelData;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.content.fluids.PipeAttachmentModel;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.model.BakedModelWrapperWithData;
import net.createmod.catnip.data.Iterate;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION;

@MethodsReturnNonnullByDefault
@Mixin(PipeAttachmentModel.class)
public abstract class PipeAttachmentModelMixin extends BakedModelWrapperWithData {
    @Unique
    private static final ModelProperty<TFMGPipeModelData> PIPE_PROPERTY = new ModelProperty<>();

    public PipeAttachmentModelMixin(BakedModel template) {
        super(template);
    }

    @Inject(method = "gatherModelData", at = @At("HEAD"), cancellable = true, remap = false)
    private void tfmg$gatherModelData(ModelData.Builder builder, BlockAndTintGetter world, BlockPos pos,
                                      BlockState state, ModelData blockEntityData,
                                      CallbackInfoReturnable<ModelData.Builder> cir) {
        TFMGPipeModelData data = new TFMGPipeModelData();
        FluidTransportBehaviour transport = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
        BracketedBlockEntityBehaviour bracket = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);

        if (transport != null)
            for (Direction d : Iterate.directions) {
                boolean shouldConnect = true;
                if (world.getBlockState(pos.relative(d)).getBlock() instanceof FluidPipeBlock) {
                    if (d.getAxis().isHorizontal())
                        shouldConnect = world.getBlockState(pos.relative(d)).getValue(PROPERTY_BY_DIRECTION.get(d.getOpposite()));
                }

                data.putAttachment(d, transport.getRenderedRimAttachment(world, pos, state, d));

                if (!shouldConnect)
                    if (state.getBlock() instanceof FluidPipeBlock)
                        if (state.getValue(PROPERTY_BY_DIRECTION.get(d)))
                            data.putAttachment(d, AttachmentTypes.RIM);
            }
        if (bracket != null)
            data.putBracket(bracket.getBracket());
        data.setEncased(FluidPipeBlock.shouldDrawCasing(world, pos, state));

        cir.setReturnValue(builder.with(PIPE_PROPERTY, data));
    }

    @SuppressWarnings("deprecation")
    @Override
    public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        ChunkRenderTypeSet set = super.getRenderTypes(state, rand, data);
        if (set.isEmpty()) {
            return ItemBlockRenderTypes.getRenderLayers(state);
        }
        return set;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource rand, @NotNull ModelData data, RenderType renderType) {
        List<BakedQuad> quads = super.getQuads(state, side, rand, data, renderType);
        if (data.has(PIPE_PROPERTY)) {
            TFMGPipeModelData pipeData = data.get(PIPE_PROPERTY);
            quads = new ArrayList<>(quads);
            if (pipeData != null) {
                tfmg$addQuads(quads, state, side, rand, data, pipeData, renderType);
            }
        }
        return quads;
    }

    @Unique
    private void tfmg$addQuads(List<BakedQuad> quads, BlockState state, Direction side, RandomSource rand, ModelData data, TFMGPipeModelData pipeData, RenderType renderType) {
        BakedModel bracket = pipeData.getBracket();
        if (bracket != null)
            quads.addAll(bracket.getQuads(state, side, rand, data, renderType));
        for (Direction d : Iterate.directions) {
            AttachmentTypes type = pipeData.getAttachment(d);
            for (ComponentPartials partial : type.partials) {
                quads.addAll(AllPartialModels.PIPE_ATTACHMENTS.get(partial)
                        .get(d)
                        .get()
                        .getQuads(state, side, rand, data, renderType));
            }
        }
        if (pipeData.isEncased())
            quads.addAll(AllPartialModels.FLUID_PIPE_CASING.get()
                    .getQuads(state, side, rand, data, renderType));
    }
}