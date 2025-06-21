package gg.lode.amplifierapi.api.data;

import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface IVoicePlayer {

    @Nullable Set<UUID> getWhoCanHear();
    void resetWhoCanHear();
    void addWhoCanHear(UUID uuid);
    void removeWhoCanHear(UUID uuid);

    void setWhoCanHear(@Nullable Set<UUID> whoCanHear);

    UUID getUniqueId();

    void setPitch(float pitch);

    void setVolume(float volume);

    float getVolume();

    float getPitch();

    float getDistance();

    void setDistance(float distance);

    void setBroadcasting(boolean isBroadcasting);

    boolean isBroadcasting();

    boolean shouldReverb();

    void setShouldReverb(boolean shouldReverb);

    boolean isDeafened();

    void setDeafened(boolean deafened);
}
