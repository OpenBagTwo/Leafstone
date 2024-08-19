package io.github.openbagtwo.leafstone.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.BigDripleafBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Tilt;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BigDripleafBlock.class)
public abstract class LeafstoneMixin {

  private boolean locked = false;

  @Accessor("NEXT_TILT_DELAYS")
  public static final Object2IntMap<Tilt> getTiltDelayMap() {
    throw new AssertionError();
  }

  @Accessor("TILT")
  public static final EnumProperty<Tilt> getTiltEnum() {
    throw new AssertionError();
  }

  @Inject(
      method="scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V",
      at=@At("HEAD"),
      cancellable = true
  )
  public void lockState(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
    if (world.isReceivingRedstonePower(pos)) {
      this.locked = true;
      ci.cancel();
    }
  }

  /**
   * @author OpenBagTwo
   * @reason If redstone signal is removed, schedule the next tilt update
   */
  @Overwrite
  protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
    if (world.isReceivingRedstonePower(pos)){
      this.locked = true;
    } else if (this.locked) {
      this.locked = false;
      int delay = getTiltDelayMap().getInt(state.get(getTiltEnum()));
      if (delay >= 0) {
        world.scheduleBlockTick(pos, (BigDripleafBlock) (Object) this, delay);
      }
    }
  }
}

