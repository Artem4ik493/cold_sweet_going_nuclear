package com.yourname.myfirstmod.csgn.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

public final class ReactorHandler {
    private ReactorHandler() {}

    public static void onServerTick(ServerTickEvent.Post event) {
        if (!(event.getServer() != null)) {
            return;
        }

        int interval = BridgeConfig.UPDATE_INTERVAL.get();
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.getGameTime() % interval != 0L) {
                continue;
            }

            List<BlockPos> stale = new ArrayList<>();
            for (BlockPos reactor : PositionCache.reactors(level)) {
                if (!level.isLoaded(reactor)) {
                    continue;
                }

                if (!PositionCache.matches(level.getBlockState(reactor), BridgeConfig.REACTOR_BLOCK_IDS.get())) {
                    stale.add(reactor);
                    continue;
                }

                // TODO: Artem's Task
                // Replace this hook with the verified Going Nuclear reactor API/state.
                applyHeatInRadius(level, reactor, BridgeConfig.REACTOR_RADIUS.get(),
                    BridgeConfig.REACTOR_HEAT_MULTIPLIER.get());
            }
            PositionCache.reactors(level).removeAll(stale);
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
