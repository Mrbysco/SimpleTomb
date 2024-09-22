package com.lothrazar.simpletomb;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TombComponents {
	public static final DeferredRegister<DataComponentType<?>> COMPONENT_TYPE = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ModTomb.MODID);

	public static final Supplier<DataComponentType<GlobalPos>> TOMB_POS = COMPONENT_TYPE.register("tomb_pos", () ->
			DataComponentType.<GlobalPos>builder()
					.persistent(GlobalPos.CODEC)
					.networkSynchronized(GlobalPos.STREAM_CODEC)
					.build());
}
