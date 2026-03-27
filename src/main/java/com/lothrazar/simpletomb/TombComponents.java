package com.lothrazar.simpletomb;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

	public record CurioSlot(String slotType, int slotIndex) {
		public static final Codec<CurioSlot> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.STRING.fieldOf("slotType").forGetter(CurioSlot::slotType),
						Codec.INT.fieldOf("slotIndex").forGetter(CurioSlot::slotIndex)
				).apply(instance, CurioSlot::new)
		);

		public static final StreamCodec<ByteBuf, CurioSlot> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, CurioSlot::slotType,
				ByteBufCodecs.VAR_INT, CurioSlot::slotIndex,
				CurioSlot::new
		);
	}

	public static final Supplier<DataComponentType<CurioSlot>> CURIO_SLOT = COMPONENT_TYPE.registerComponentType("curio_slot", builder ->
			builder
					.persistent(CurioSlot.CODEC)
					.networkSynchronized(CurioSlot.STREAM_CODEC)
	);
}
