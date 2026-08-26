package com.drmangotea.tfmg.base;

import com.drmangotea.tfmg.TFMG;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TFMGBlockConnectivityHandler {
	public static <T extends BlockEntity & IMultiBlockEntityContainer> void formMulti(T be) {
		SearchCache<T> cache = new SearchCache<>();
		List<T> frontier = new ArrayList<>();
		frontier.add(be);
		BlockEntityType<?> type = be.getType();
		BlockGetter level = be.getLevel();
		BlockPos pos = be.getBlockPos();
		PriorityQueue<Pair<Integer, T>> creationQueue = new PriorityQueue<>((one, two) -> two.getKey() - one.getKey());
		Set<BlockPos> visited = new HashSet<>();
		Direction.Axis mainAxis = be.getMainConnectionAxis();
		
		// essentially, if it's a vertical multi then the search won't be restricted by Y
		// alternately, a horizontal multi search shouldn't be restricted by X or Z
		int minX = (mainAxis == Direction.Axis.Y ? pos.getX() - be.getMaxWidth() : Integer.MIN_VALUE);
		int minY = (mainAxis != Direction.Axis.Y ? pos.getY() - be.getMaxWidth() : Integer.MIN_VALUE);
		int minZ = (mainAxis == Direction.Axis.Y ? pos.getZ() - be.getMaxWidth() : Integer.MIN_VALUE);
		
		while (!frontier.isEmpty()) {
			T part = frontier.removeFirst();
			BlockPos partPos = part.getBlockPos();
			if (visited.contains(partPos))
				continue;
			
			visited.add(partPos);
			
			int amount = simFormNewMulti(part, cache);
			if (amount > 1) {
				creationQueue.add(Pair.of(amount, part));
			}
			
			for (Direction.Axis axis : Iterate.axes) {
				Direction dir = Direction.get(Direction.AxisDirection.NEGATIVE, axis);
				BlockPos next = partPos.relative(dir);
				
				if (next.getX() <= minX || next.getY() <= minY || next.getZ() <= minZ)
					continue;
				if (visited.contains(next))
					continue;
				T nextBe = partAt(type, level, next);
				if (nextBe == null)
					continue;
				if (nextBe.isRemoved())
					continue;
				frontier.add(nextBe);
			}
		}
		visited.clear();
		
		while (!creationQueue.isEmpty()) {
			T toCreate = creationQueue.poll().getValue();
			if (visited.contains(toCreate.getBlockPos()))
				continue;
			
			visited.add(toCreate.getBlockPos());
			tryToFormNewMulti(toCreate, cache);
		}
		cache.printHits();
	}
	
	private static <T extends BlockEntity & IMultiBlockEntityContainer> void tryToFormNewMulti(
		T be, SearchCache<T> cache
	) {
		if (!be.isController())
			return;
		Level level = be.getLevel();
		if (level == null)
			return;
		BlockEntityType<?> type = be.getType();
		BlockPos origin = be.getBlockPos();
		// optional fluid handling
		IFluidTank beTank;
		FluidStack fluid = FluidStack.EMPTY;
		if (be instanceof IMultiBlockEntityContainer.Fluid ifluid && ifluid.hasTank()) {
			beTank = ifluid.getTank(0);
			fluid = beTank.getFluid();
		} else {
			beTank = null;
		}
		Direction.Axis axis = be.getMainConnectionAxis();
		
		int bestWidth = 1;
		int bestHeight = 1;
		int bestAmount = -1;
		int radius = be.getMaxWidth();
		for (int w = 1; w <= radius; w++) {
			int[] search = multiSearch(be, type, origin, cache, axis, level, w, fluid);
			int amount = search[0];
			if (amount < bestAmount)
				continue;
			bestWidth = w;
			bestHeight = search[1];
			bestAmount = amount;
		}
		
		int beWidth = be.getWidth();
		if (beWidth == bestWidth && beWidth * beWidth * be.getHeight() == bestAmount)
			return;
		
		splitMultiAndInvalidate(be, cache);
		if (be instanceof IMultiBlockEntityContainer.Fluid ifluid && ifluid.hasTank())
			ifluid.setTankSize(0, bestAmount);
		
		
		AtomicReference<Object> extraData = new AtomicReference<>(be.getExtraData());
		
		int height = bestHeight;
		int width = bestWidth;
		forEachPosition(origin, axis, height, width, (pos) -> {
			T part = partAt(type, level, pos);
			if (part == null || part == be)
				return;
			
			extraData.set(be.modifyExtraData(extraData));
			
			if (part instanceof IMultiBlockEntityContainer.Fluid ifluidPart && ifluidPart.hasTank()) {
				IFluidTank tankAt = ifluidPart.getTank(0);
				FluidStack fluidAt = tankAt.getFluid();
				if (!fluidAt.isEmpty()) {
					if (be instanceof IMultiBlockEntityContainer.Fluid ifluidBE && ifluidBE.hasTank()
						&& beTank != null) {
						beTank.fill(fluidAt, IFluidHandler.FluidAction.EXECUTE);
					}
				}
				tankAt.drain(tankAt.getCapacity(), IFluidHandler.FluidAction.EXECUTE);
			}
			
			splitMultiAndInvalidate(part, cache);
			part.setController(origin);
			part.preventConnectivityUpdate();
			cache.put(pos, be);
			part.setHeight(height);
			part.setWidth(width);
			part.notifyMultiUpdated();
		});
		be.setExtraData(extraData);
		be.preventConnectivityUpdate();
		be.setWidth(bestWidth);
		be.setHeight(bestAmount / bestWidth / bestWidth);
		be.notifyMultiUpdated();
	}
	
	private static <T extends BlockEntity & IMultiBlockEntityContainer> int simFormNewMulti(
		T be, SearchCache<T> cache
	) {
		if (!be.isController())
			return 0;
		Level level = be.getLevel();
		if (level == null)
			return 0;
		BlockEntityType<?> type = be.getType();
		BlockPos origin = be.getBlockPos();
		
		// optional fluid handling
		FluidStack fluid = FluidStack.EMPTY;
		if (be instanceof IMultiBlockEntityContainer.Fluid ifluid && ifluid.hasTank()) {
			fluid = ifluid.getTank(0).getFluid();
		}
		Direction.Axis axis = be.getMainConnectionAxis();
		
		int bestAmount = -1;
		int radius = be.getMaxWidth();
		for (int w = 1; w <= radius; w++) {
			int amount = multiSearch(be, type, origin, cache, axis, level, w, fluid)[0];
			if (amount < bestAmount)
				continue;
			bestAmount = amount;
		}
		
		return bestAmount;
	}
	
	private static <T extends BlockEntity & IMultiBlockEntityContainer> int[] multiSearch(
		T be, BlockEntityType<?> type, BlockPos origin, SearchCache<T> cache, Direction.Axis axis, BlockGetter level, int width, FluidStack fluid
	) {
		int amount = 0;
		int height = 0;
		
		Block b = level.getBlockState(origin).getBlock();
		
		Search:
		for (int Y = 0; Y < be.getMaxLength(axis, width); Y++) {
			for (int X = 0; X < width; X++) { for (int Z = 0; Z < width; Z++) {
				BlockPos pos = switch (axis) {
					case X -> origin.offset(Y, X, Z);
					case Y -> origin.offset(X, Y, Z);
					case Z -> origin.offset(X, Z, Y);
				};
				Optional<T> part = cache.getOrCache(type, level, pos);
				if (part.isEmpty())
					break Search;
				
				Block otherBlock = level.getBlockState(pos).getBlock();
				if (!b.equals(otherBlock))
					break Search;
				
				T controller = part.get();
				int otherWidth = controller.getWidth();
				if (otherWidth > width)
					break Search;
				if (otherWidth == width && controller.getHeight() == be.getMaxLength(axis, width))
					break Search;
				
				Direction.Axis conAxis = controller.getMainConnectionAxis();
				if (axis != conAxis)
					break Search;
				
				BlockPos conPos = controller.getBlockPos();
				if (!conPos.equals(origin)) {
					if (axis == Direction.Axis.Y) { // vertical multi, like a FluidTank
						if (conPos.getX() < origin.getX())
							break Search;
						if (conPos.getZ() < origin.getZ())
							break Search;
						if (conPos.getX() + otherWidth > origin.getX() + width)
							break Search;
						if (conPos.getZ() + otherWidth > origin.getZ() + width)
							break Search;
					} else { // horizontal multi, like an ItemVault
						if (axis == Direction.Axis.Z && conPos.getX() < origin.getX())
							break Search;
						if (conPos.getY() < origin.getY())
							break Search;
						if (axis == Direction.Axis.X && conPos.getZ() < origin.getZ())
							break Search;
						if (axis == Direction.Axis.Z && conPos.getX() + otherWidth > origin.getX() + width)
							break Search;
						if (conPos.getY() + otherWidth > origin.getY() + width)
							break Search;
						if (axis == Direction.Axis.X && conPos.getZ() + otherWidth > origin.getZ() + width)
							break Search;
					}
				}
				if (controller instanceof IMultiBlockEntityContainer.Fluid ifluidCon && ifluidCon.hasTank()) {
					FluidStack otherFluid = ifluidCon.getFluid(0);
					if (!fluid.isEmpty() && !otherFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(fluid, otherFluid))
						break Search;
				}
			} }
			amount += width * width;
			height++;
		}
		return new int[]{amount, height};
	}
	
	public static <T extends BlockEntity & IMultiBlockEntityContainer> void splitMulti(T be) {
		splitMultiAndInvalidate(be, null);
	}
	
	private static <T extends BlockEntity & IMultiBlockEntityContainer> void splitMultiAndInvalidate(
		T be, @Nullable SearchCache<T> cache
	) {
		T controller = be.getControllerBE();
		if (controller == null)
			return;
		
		Level level = controller.getLevel();
		if (level == null)
			return;
		
		int height = controller.getHeight();
		int width = controller.getWidth();
		if (width == 1 && height == 1)
			return;
		
		BlockPos origin = controller.getBlockPos();
		Direction.Axis axis = controller.getMainConnectionAxis();
		
		// fluid handling, if present
		FluidStack toDistribute;
		int maxCapacity;
		if (controller instanceof IMultiBlockEntityContainer.Fluid ifluidBE && ifluidBE.hasTank()) {
			toDistribute = ifluidBE.getFluid(0);
			maxCapacity = ifluidBE.getTankSize(0);
			if (!toDistribute.isEmpty() && !be.isRemoved())
				toDistribute.shrink(maxCapacity);
			ifluidBE.setTankSize(0, 1);
		} else {
			maxCapacity = 0;
			toDistribute = FluidStack.EMPTY;
		}
		BlockEntityType<?> type = controller.getType();
		forEachPosition(origin, axis, height, width, (pos) -> {
			T partAt = partAt(type, level, pos);
			if (partAt == null)
				return;
			if (!partAt.getController().equals(origin))
				return;
					
			T controllerBE = partAt.getControllerBE();
			partAt.setExtraData((controllerBE == null ? null : controllerBE.getExtraData()));
			partAt.removeController(true);
					
			if (!toDistribute.isEmpty() && partAt != controller) {
				FluidStack copy = toDistribute.copy();
				IFluidTank tank =
					(partAt instanceof IMultiBlockEntityContainer.Fluid ifluidPart ? ifluidPart.getTank(0) : null);
					
				int split = Math.min(maxCapacity, toDistribute.getAmount());
				copy.setAmount(split);
				toDistribute.shrink(split);
				if (tank != null)
					tank.fill(copy, IFluidHandler.FluidAction.EXECUTE);
			}
			if (cache != null) cache.put(pos, partAt);
		});
		
		if ((controller instanceof IMultiBlockEntityContainer.Inventory inv && inv.hasInventory())||
			(controller instanceof IMultiBlockEntityContainer.Fluid fluid && fluid.hasTank()))
			level.invalidateCapabilities(controller.getBlockPos());
	}
	
	@Nullable
	public static <T extends BlockEntity & IMultiBlockEntityContainer> T partAt(
		BlockEntityType<?> type, BlockGetter level, BlockPos pos
	) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be != null && be.getType() == type && !be.isRemoved())
			return checked(be);
		return null;
	}
	
	public static <T extends BlockEntity & IMultiBlockEntityContainer> boolean isConnected(
		BlockGetter level, BlockPos pos, BlockPos other
	) {
		T one = checked(level.getBlockEntity(pos));
		T two = checked(level.getBlockEntity(other));
		if (one == null || two == null)
			return false;
		return one.getController().equals(two.getController());
	}
	
	@Nullable @SuppressWarnings("unchecked")
	private static <T extends BlockEntity & IMultiBlockEntityContainer> T checked(BlockEntity be) {
		if (be instanceof IMultiBlockEntityContainer)
			return (T) be;
		return null;
	}
	
	private static void forEachPosition (BlockPos origin, Direction.Axis axis, int height, int width, Consumer<BlockPos> func) {
		for (int Y = 0; Y < height; Y++) { for (int X = 0; X < width; X++) { for (int Z = 0; Z < width; Z++) {
			BlockPos pos = switch (axis) {
				case X -> origin.offset(Y, X, Z);
				case Y -> origin.offset(X, Y, Z);
				case Z -> origin.offset(X, Z, Y);
			};
			
			func.accept(pos);
		} } }
	}
	
	private static class SearchCache<T extends BlockEntity & IMultiBlockEntityContainer> {
		private int CacheAdds = 0, CacheHits = 0, CacheCalls = 0;
		Map<BlockPos, Optional<T>> controllerMap;
		
		public SearchCache() {
			controllerMap = new HashMap<>();
		}
		
		void put(BlockPos pos, T target) {
			CacheAdds++;
			controllerMap.put(pos, Optional.of(target));
		}
		
		void putEmpty(BlockPos pos) {
			CacheAdds++;
			controllerMap.put(pos, Optional.empty());
		}
		
		void printHits () {
			TFMG.LOGGER.info("Connectivity Cache:\n  Calls: {}\n  Adds: {}\n  Hits: {}", CacheCalls, CacheAdds, CacheHits);
		}
		
		Optional<T> getOrCache(BlockEntityType<?> type, BlockGetter level, BlockPos pos) {
			CacheCalls++;
			if (controllerMap.containsKey(pos)) {
				CacheHits++;
				return controllerMap.get(pos);
			}
			
			T partAt = partAt(type, level, pos);
			if (partAt == null) {
				putEmpty(pos);
				return Optional.empty();
			}
			T controller = checked(level.getBlockEntity(partAt.getController()));
			if (controller == null) {
				putEmpty(pos);
				return Optional.empty();
			}
			put(pos, controller);
			return Optional.of(controller);
		}
	}
}