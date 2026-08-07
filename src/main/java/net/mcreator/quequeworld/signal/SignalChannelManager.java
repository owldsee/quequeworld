package net.mcreator.quequeworld.signal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class SignalChannelManager {
    private static final Set<ISignalListener> LISTENERS = Collections.newSetFromMap(new WeakHashMap<>());

    public static void registerListener(ISignalListener listener) {
        LISTENERS.add(listener);
    }

    public static void unregisterListener(ISignalListener listener) {
        LISTENERS.remove(listener);
    }

    public static void emitSignal(Level level, String fullChannelSignal) {
        if (fullChannelSignal == null || !fullChannelSignal.contains(":")) return;
        String[] parts = fullChannelSignal.split(":", 2);
        emitSignal(level, parts[0].trim(), parts[1].trim());
    }

    public static void emitSignal(Level level, String channel, String signal) {
        if (level.isClientSide()) return;

        // Broadcast to registered listeners
        for (ISignalListener listener : LISTENERS) {
            if (listener.getLevel() == level) {
                listener.onSignalReceived(channel, signal);
            }
        }
    }

    public interface ISignalListener {
        Level getLevel();
        BlockPos getListenerPos();
        void onSignalReceived(String channel, String signal);
    }
}
