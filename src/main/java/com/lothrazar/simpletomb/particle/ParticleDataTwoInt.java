//package com.lothrazar.simpletomb.particle;
//
//import com.lothrazar.simpletomb.TombRegistry;
//import com.mojang.brigadier.StringReader;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.MapCodec;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import net.minecraft.core.particles.ParticleOptions;
//import net.minecraft.core.particles.ParticleType;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.codec.ByteBufCodecs;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraftforge.registries.ForgeRegistries;
//
//import java.util.Locale;
//
//public class ParticleDataTwoInt implements ParticleOptions {
//
//  public static final MapCodec<ParticleDataTwoInt> CODEC = RecordCodecBuilder.mapCodec(
//          instance -> instance.group(
//                          Codec.INT.fieldOf("oneInt").forGetter(data -> data.oneInt),
//                          Codec.INT.fieldOf("twoInt").forGetter(data -> data.twoInt)
//                  )
//                  .apply(instance, ParticleDataTwoInt::new)
//  );
//
//  public static final StreamCodec<RegistryFriendlyByteBuf, ParticleDataTwoInt> STREAM_CODEC = StreamCodec.composite(
//          ByteBufCodecs.INT, data -> data.oneInt, ByteBufCodecs.INT, data -> data.twoInt, ParticleDataTwoInt::new
//  );
//
//  @SuppressWarnings("deprecation")
//  public static final Deserializer<ParticleDataTwoInt> DESERIALIZER = new Deserializer<ParticleDataTwoInt>() {
//
//    @Override
//    public ParticleDataTwoInt fromCommand(ParticleType<ParticleDataTwoInt> particleType, StringReader reader) throws CommandSyntaxException {
//      if (reader.canRead()) {
//        reader.expect(' ');
//      }
//      int oneInt = 0xffffff, twoInt = 0xffffff;
//      if (reader.canRead()) {
//        oneInt = reader.readInt();
//      }
//      if (reader.canRead()) {
//        reader.expect(' ');
//      }
//      if (reader.canRead()) {
//        twoInt = reader.readInt();
//      }
//      return new ParticleDataTwoInt(particleType, oneInt, twoInt);
//    }
//
//    @Override
//    public ParticleDataTwoInt fromNetwork(ParticleType<ParticleDataTwoInt> particleType, FriendlyByteBuf buf) {
//      return new ParticleDataTwoInt(particleType, buf.readInt(), buf.readInt());
//    }
//  };
//  private final ParticleType<ParticleDataTwoInt> particleType;
//  public int oneInt, twoInt;
//
//  public ParticleDataTwoInt(ParticleType<ParticleDataTwoInt> particleType, int oneInt, int twoInt) {
//    this.particleType = particleType;
//    this.oneInt = oneInt;
//    this.twoInt = twoInt;
//  }
//
//  public ParticleDataTwoInt(int oneInt, int twoInt) {
//    this(TombRegistry.GRAVE_SMOKE.get(), oneInt, twoInt);
//  }
//
//  @Override
//  public ParticleType<?> getType() {
//    return this.particleType;
//  }
//}
