package com.yourname.myfirstmod.csgn.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

public final class PipeHeatHandler {
    private PipeHeatHandler() {}

    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) {
            return;
        }

        int interval = BridgeConfig.UPDATE_INTERVAL.get();
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.getGameTime() % interval != 0L) {
                continue;
            }

            List<BlockPos> stale = new ArrayList<>();
            for (BlockPos pipe : PositionCache.pipes(level)) {
                if (!level.isLoaded(pipe)) {
                    continue;
                }

                if (!PositionCache.matches(level.getBlockState(pipe), BridgeConfig.PIPE_BLOCK_IDS.get())) {
                    stale.add(pipe);
                    continue;
                }

                if (!SteamFluidHelper.containsConfiguredSteam(level, pipe)) {
                    continue;
                }

                applyHeatInRadius(level, pipe, BridgeConfig.PIPE_RADIUS.get(),
                    BridgeConfig.STEAM_HEAT_MULTIPLIER.get());
            }
            PositionCache.pipes(level).removeAll(stale);
        }
    }

    private static void applyHeatInRadius(ServerLevel level, BlockPos source, double radius, double strength) {
        int r = (int) Math.ceil(radius);
        double radiusSquared = radius * radius;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if ((double) x * x + (double) y * y + (double) z * z > radiusSquared) {
                        continue;
                    }

                    cursor.set(source.getX() + x, source.getY() + y, source.getZ() + z);
                    if (!level.isLoaded(cursor)) {
                        continue;
                    }
                    ColdSweatAdapter.applyHeat(level, source, cursor.immutable(), strength);
                }
            }
        }
    }
}
