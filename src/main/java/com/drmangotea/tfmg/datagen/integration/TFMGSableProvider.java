package com.drmangotea.tfmg.datagen.integration;

import com.drmangotea.tfmg.TFMG;
import com.drmangotea.tfmg.base.annotation.NothingNullByDefault;
import com.drmangotea.tfmg.datagen.integration.sable.BlockPhysicsProvider;
import com.drmangotea.tfmg.registry.TFMGBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

@NothingNullByDefault
public class TFMGSableProvider extends BlockPhysicsProvider {
	public TFMGSableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(TFMG.MOD_ID, output, registries);
	}
	
	@Override
	protected void generate() {
		add(TFMGBlocks.ALUMINUM_BLOCK.getDelegate(), b -> b.mass(0.5d));
	}
}
