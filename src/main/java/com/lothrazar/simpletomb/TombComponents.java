package com.lothrazar.simpletomb;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TombComponents {
	public static final DeferredRegister.DataComponents COMPONENT_TYPE = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ModTomb.MODID);

	public static final Supplier<DataComponentType<GlobalPos>> TOMB_POS = COMPONENT_TYPE.registerComponentType("tomb_pos", builder ->
			builder
					.persistent(GlobalPos.CODEC)
					.networkSynchronized(GlobalPos.STREAM_CODEC)
					.cacheEncoding()
	);
}
