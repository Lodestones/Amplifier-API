package gg.lode.amplifierapi.api.manager;

import gg.lode.amplifierapi.api.data.IVoicePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IVoiceManager {
    void cleanup();

    CompletableFuture<IVoicePlayer> fetchOrCreateVoicePlayer(Player player);

    IVoicePlayer getVoicePlayer(UUID uniqueId);

    IVoicePlayer getVoicePlayer(Player player);

    boolean isEnabled();

    void setEnabled(boolean enabled);
}
