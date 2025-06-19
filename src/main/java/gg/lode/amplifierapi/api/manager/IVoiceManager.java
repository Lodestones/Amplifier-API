package gg.lode.amplifierapi.api.manager;

import gg.lode.amplifierapi.api.data.IVoicePlayer;
import org.bukkit.Location;
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

    void playSound(Location location, byte[] data);

    void playSound(Location location, byte[] data, float volume);

    void playSound(Location location, byte[] data, float volume, float distance);

    void playSound(Location location, byte[] data, float volume, float pitch, float distance);

}
