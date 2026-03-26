package com.lothrazar.simpletomb.client;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.LayeringTransform;

public class LineRenderType {

  private static RenderType TOMB_LINES;

  public static RenderType tombLinesType() {
    if (TOMB_LINES == null) {
      TOMB_LINES = RenderType.create("tomb_lines",
          RenderSetup.builder(RenderPipelines.LINES)
              .bufferSize(256)
              .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
              .createRenderSetup());
    }
    return TOMB_LINES;
  }
}
