package com.drmangotea.tfmg.content.electricity.lights;

import com.drmangotea.tfmg.base.annotation.NothingNullByDefault;
import com.drmangotea.tfmg.base.blocks.WallMountBlock;
import com.drmangotea.tfmg.content.electricity.base.IElectric;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;


@NothingNullByDefault
public class LightBulbBlock extends WallMountBlock implements IBE<LightBulbBlockEntity>, SimpleWaterloggedBlock, IWrenchable {
    public static final IntegerProperty LIGHT = BlockStateProperties.LEVEL;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public final BlockEntityEntry<? extends LightBulbBlockEntity> blockEntityType;
    public final VoxelShaper shape;

    public LightBulbBlock(Properties properties, BlockEntityEntry<? extends LightBulbBlockEntity> blockEntityType, VoxelShaper shape) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIGHT, 0).setValue(WATERLOGGED, false));
        this.blockEntityType = blockEntityType;
        this.shape = shape;
    }

    @Override
    public void onPlace(BlockState pState, Level level, BlockPos pos, BlockState pOldState, boolean pIsMoving) {
        withBlockEntityDo(level,pos, IElectric::onPlaced);
    }
	
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }
	
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape.get(pState.getValue(FACING));
    }
	
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult hitResult) {
        if (player.isShiftKeyDown())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        ItemStack heldItem = player.getItemInHand(pHand);
        LightBulbBlockEntity be = getBlockEntity(level, pos);
        DyeColor dye = DyeColor.getColor(heldItem);
        if (be != null && dye != null) {
			level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			be.setColor(dye);
			return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT, WATERLOGGED, FACING);
    }
	
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public Class<LightBulbBlockEntity> getBlockEntityClass() {
        return LightBulbBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends LightBulbBlockEntity> getBlockEntityType() {
        return blockEntityType.get();
    }
}
