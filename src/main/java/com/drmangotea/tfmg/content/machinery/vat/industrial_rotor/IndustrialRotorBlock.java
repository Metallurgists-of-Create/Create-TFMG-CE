package com.drmangotea.tfmg.content.machinery.vat.industrial_rotor;

import com.drmangotea.tfmg.base.TFMGUtils;
import com.drmangotea.tfmg.content.machinery.vat.base.VatBlock;
import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.drmangotea.tfmg.registry.TFMGDataComponents;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicBoolean;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IndustrialRotorBlock extends HorizontalKineticBlock implements IBE<IndustrialRotorBlockEntity> {
    public IndustrialRotorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(HORIZONTAL_FACING);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            AtomicBoolean success = new AtomicBoolean(false);
            withBlockEntityDo(level, pos, rotor -> {
                if (!rotor.getRotorItem().isEmpty()) {
                    TFMGUtils.returnItemToInventory(rotor.inventory, 0, player, hand);
                    rotor.onInventoryChanged(0);
                    success.set(true);
                } else if (!rotor.getSecondaryItem().isEmpty()) {
                    TFMGUtils.returnItemToInventory(rotor.inventory, 1, player, hand);
                    rotor.onInventoryChanged(1);
                    success.set(true);
                }
            });
            return success.get() ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        AtomicBoolean success = new AtomicBoolean(false);
        withBlockEntityDo(level, pos, rotor -> {
            // Slot 0 holds the rotor attachment, slot 1 holds everything else.
            int slot = stack.has(TFMGDataComponents.ROTOR_MODE) ? 0 : 1;
            ItemStack current = slot == 0 ? rotor.getRotorItem() : rotor.getSecondaryItem();

            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(stack, current)) {
                success.set(true);
                return;
            }

            if (!current.isEmpty() && TFMGUtils.returnItemToInventory(rotor.inventory, slot, player, hand))
                rotor.onInventoryChanged(slot);

            if (rotor.inventory.insertItem(slot, stack.copyWithCount(1), false).isEmpty()) {
                rotor.onInventoryChanged(slot);
                stack.shrink(1);
                success.set(true);
            }
        });
        return success.get() ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState pNewState, boolean pIsMoving) {
        VatBlock.updateVatState(state, level, pos.relative(Direction.DOWN));
        super.onRemove(state, level, pos, pNewState, pIsMoving);
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        VatBlock.updateVatState(state, worldIn, pos.relative(Direction.DOWN));
        super.onPlace(state, worldIn, pos, oldState, isMoving);
    }

    @Override
    public Class<IndustrialRotorBlockEntity> getBlockEntityClass() {
        return IndustrialRotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends IndustrialRotorBlockEntity> getBlockEntityType() {
        return TFMGBlockEntities.INDUSTRIAL_ROTOR.get();
    }
}
