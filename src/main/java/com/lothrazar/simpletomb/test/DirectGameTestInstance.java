package com.lothrazar.simpletomb.test;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class DirectGameTestInstance extends GameTestInstance {

  private static final MapCodec<DirectGameTestInstance> CODEC = MapCodec.unit(() -> {
    throw new UnsupportedOperationException("DirectGameTestInstance cannot be deserialized");
  });

  private final Consumer<GameTestHelper> testFunction;
  private final String name;

  @SuppressWarnings({"unchecked", "rawtypes"})
  public DirectGameTestInstance(String name, Consumer<GameTestHelper> testFunction, TestData info) {
    super(info);
    this.testFunction = testFunction;
    this.name = name;
  }

  @Override
  public void run(GameTestHelper helper) {
    testFunction.accept(helper);
  }

  @Override
  public MapCodec<? extends GameTestInstance> codec() {
    return CODEC;
  }

  @Override
  protected MutableComponent typeDescription() {
    return Component.literal(name);
  }
}
