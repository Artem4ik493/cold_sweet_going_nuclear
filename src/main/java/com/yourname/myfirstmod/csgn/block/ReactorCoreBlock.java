package com.yourname.myfirstmod.csgn.block;

import com.yourname.myfirstmod.csgn.handler.HeatZoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Блок-сердцевина реактора.
 *
 * Его задача — сообщать «менеджеру тепла» (HeatZoneManager), что в этой точке
 * мира появилась тепловая зона, и убирать её, когда блок ломают.
 *
 * Ключевые методы жизненного цикла блока:
 *  - onPlace(...)   — вызывается, когда блок поставили;
 *  - neighborChanged — вызывается, когда соседний блок изменился (нам не нужен);
 *  - animateTick    — вызывается каждый тик для блоков вокруг игрока (тут можно
 *                    добавить частицы/звук жара — отличная задача для новичка!);
 *  - destroy/onRemove — момент удаления блока из мира.
 */
public class ReactorCoreBlock extends Block {

    public ReactorCoreBlock(Properties properties) {
        super(properties);
    }

    /** Блок поставили в мир — регистрируем тепловую зону. */
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // Работаем только на сервере (клиент блоки мира не меняет)
        if (!level.isClientSide()) {
            HeatZoneManager.get(level).addReactorZone(pos);
        }
    }

    /** Блок убрали из мира — снимаем тепловую зону. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        // Если заменяемый блок — НЕ наш (то есть нас сломали/заменили), убираем зону.
        // Проверка нужна, потому что Minecraft вызывает onRemove и при простой смене свойств.
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            HeatZoneManager.get(level).removeReactorZone(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /**
     * Вызывается каждый игровой тик (20 раз в секунду) для блоков в радиусе видимости игрока.
     * Идеальное место для визуальных эффектов: частицы дыма, жара, свечения.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos,
                            net.minecraft.RandomSources randomSources, long gameTime) {
        super.animateTick(state, level, pos, randomSources, gameTime);

        // =================================================================================
        // TODO: Artem's Task — Добавь сюда эффект жара!
        // Пример (частица огня над реактором раз в ~8 тиков):
        //
        // if (level.isClientSide() && gameTime % 8 == 0) {
        //     level.addParticle(ParticleTypes.FLAME,
        //             pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
        //             0.0, 0.05, 0.0);
        // }
        //
        // Подсказка: импорт — net.minecraft.core.particles.ParticleTypes
        // =================================================================================
    }
}
