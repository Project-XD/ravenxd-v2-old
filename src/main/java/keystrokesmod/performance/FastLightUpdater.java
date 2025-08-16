package keystrokesmod.performance;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.concurrent.*;

public final class FastLightUpdater {
    private static final ScheduledExecutorService EXECUTOR;

    private final World world;
    private final BlockingQueue<Runnable> deSyncQueue;

    static {
        EXECUTOR = Executors.newScheduledThreadPool(
                Math.max(Runtime.getRuntime().availableProcessors(), 2)
        );

        Runtime.getRuntime().addShutdownHook(new Thread(EXECUTOR::shutdown));
    }

    public FastLightUpdater(World world) {
        this.world = world;
        this.deSyncQueue = new LinkedBlockingQueue<>();

        EXECUTOR.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    deSyncQueue.take().run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public void updateLightDeSync(final EnumSkyBlock skyBlock, final BlockPos blockPos) {
        deSyncQueue.add(() -> doUpdateLight(skyBlock, blockPos));
    }

    public void updateLightMultiThread(final EnumSkyBlock skyBlock, final BlockPos blockPos) {
        EXECUTOR.execute(() -> doUpdateLight(skyBlock, blockPos));
    }

    public void updateLight(final EnumSkyBlock skyBlock, final BlockPos blockPos) {
        doUpdateLight(skyBlock, blockPos);
    }

    private void doUpdateLight(final EnumSkyBlock skyBlock, final BlockPos blockPos) {
        final int[] lightUpdateBlockList = new int[32768];
        final BlockPos.MutableBlockPos nextBlockPos = new BlockPos.MutableBlockPos();

        final int defaultLight = world.getLightFor(skyBlock, blockPos);
        final int rawLight = getRawLight(blockPos, skyBlock);
        final int x = blockPos.getX();
        final int y = blockPos.getY();
        final int z = blockPos.getZ();
        int i = 0;
        int j = 0;
        int l1;
        int j5;
        int k5;
        int l5;
        int j6;
        int j3;
        int k3;
        int l3;

        if (rawLight > defaultLight) {
            lightUpdateBlockList[j++] = 133152;
        } else if (rawLight < defaultLight) {
            lightUpdateBlockList[j++] = 133152 | defaultLight << 18;

            label90:
            while (true) {
                int l2;
                do {
                    do {
                        do {
                            if (i >= j) {
                                i = 0;
                                break label90;
                            }

                            l1 = lightUpdateBlockList[i++];
                            j5 = (l1 & 63) - 32 + x;
                            k5 = (l1 >> 6 & 63) - 32 + y;
                            l5 = (l1 >> 12 & 63) - 32 + z;
                            l2 = l1 >> 18 & 15;
                            nextBlockPos.set(j5, k5, l5);
                            j6 = world.getLightFor(skyBlock, nextBlockPos);
                        } while (j6 != l2);

                        world.setLightFor(skyBlock, nextBlockPos, 0);
                    } while (l2 == 0);

                    j3 = Math.abs(j5 - x);
                    k3 = Math.abs(k5 - y);
                    l3 = Math.abs(l5 - z);
                } while (j3 + k3 + l3 >= 17);

                for (EnumFacing enumfacing : EnumFacing.values()) {
                    final int i4 = j5 + enumfacing.getFrontOffsetX();
                    final int j4 = k5 + enumfacing.getFrontOffsetY();
                    final int k4 = l5 + enumfacing.getFrontOffsetZ();
                    nextBlockPos.set(i4, j4, k4);
                    final int l4 = Math.max(1, world.getBlockState(nextBlockPos).getBlock().getLightOpacity());
                    j6 = world.getLightFor(skyBlock, nextBlockPos);
                    if (j >= lightUpdateBlockList.length)
                        break;
                    if (j6 == l2 - l4) {
                        lightUpdateBlockList[j++] = i4 - x + 32 | j4 - y + 32 << 6 | k4 - z + 32 << 12 | l2 - l4 << 18;
                    }
                }
            }
        }

        while (i < j) {
            l1 = lightUpdateBlockList[i++];
            j5 = (l1 & 63) - 32 + x;
            k5 = (l1 >> 6 & 63) - 32 + y;
            l5 = (l1 >> 12 & 63) - 32 + z;
            nextBlockPos.set(j5, k5, l5);
            final int i6 = world.getLightFor(skyBlock, nextBlockPos);
            j6 = getRawLight(nextBlockPos, skyBlock);
            if (j6 != i6) {
                world.setLightFor(skyBlock, nextBlockPos, j6);
                if (j6 > i6) {
                    j3 = Math.abs(j5 - x);
                    k3 = Math.abs(k5 - y);
                    l3 = Math.abs(l5 - z);
                    final boolean flag = j < lightUpdateBlockList.length - 6;
                    if (j3 + k3 + l3 < 17 && flag) {
                        if (world.getLightFor(skyBlock, nextBlockPos.west()) < j6) {
                            lightUpdateBlockList[j++] = j5 - 1 - x + 32 + (k5 - y + 32 << 6) + (l5 - z + 32 << 12);
                        }

                        if (world.getLightFor(skyBlock, nextBlockPos.east()) < j6) {
                            lightUpdateBlockList[j++] = j5 + 1 - x + 32 + (k5 - y + 32 << 6) + (l5 - z + 32 << 12);
                        }

                        if (world.getLightFor(skyBlock, nextBlockPos.down()) < j6) {
                            lightUpdateBlockList[j++] = j5 - x + 32 + (k5 - 1 - y + 32 << 6) + (l5 - z + 32 << 12);
                        }

                        if (world.getLightFor(skyBlock, nextBlockPos.up()) < j6) {
                            lightUpdateBlockList[j++] = j5 - x + 32 + (k5 + 1 - y + 32 << 6) + (l5 - z + 32 << 12);
                        }

                        if (world.getLightFor(skyBlock, nextBlockPos.north()) < j6) {
                            lightUpdateBlockList[j++] = j5 - x + 32 + (k5 - y + 32 << 6) + (l5 - 1 - z + 32 << 12);
                        }

                        if (world.getLightFor(skyBlock, nextBlockPos.south()) < j6) {
                            lightUpdateBlockList[j++] = j5 - x + 32 + (k5 - y + 32 << 6) + (l5 + 1 - z + 32 << 12);
                        }
                    }
                }
            }
        }
    }

    private int getRawLight(final BlockPos blockPos, final EnumSkyBlock skyBlock) {
        if (skyBlock == EnumSkyBlock.SKY && world.canSeeSky(blockPos)) {
            return 15;
        } else {
            final Block block = world.getBlockState(blockPos).getBlock();
            final int blockLight = block.getLightValue(world, blockPos);

            int i = skyBlock == EnumSkyBlock.SKY ? 0 : blockLight;
            int j = block.getLightOpacity(world, blockPos);
            if (j >= 15) {
                if (blockLight > 0) {
                    j = 1;
                } else {
                    return 0;
                }
            } else if (j < 1) {
                j = 1;
            }

            if (i < 14) {
                for (EnumFacing enumfacing : EnumFacing.values()) {
                    BlockPos blockpos = blockPos.offset(enumfacing);
                    int k = world.getLightFor(skyBlock, blockpos) - j;
                    if (k > i) {
                        i = k;
                    }

                    if (i >= 14) {
                        return i;
                    }
                }
            }
            return i;
        }
    }
}
