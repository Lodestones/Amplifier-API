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

    float getReverbIntensity();

    void setReverbIntensity(float intensity);

    float getReverbRoomSize();

    void setReverbRoomSize(float roomSize);

    /**
     * @deprecated Use {@link #getReverbIntensity()} > 0f instead.
     */
    @Deprecated
    boolean shouldReverb();

    /**
     * @deprecated Use {@link #setReverbIntensity(float)} instead.
     */
    @Deprecated
    void setShouldReverb(boolean shouldReverb);

    boolean isDeafened();

    void setDeafened(boolean deafened);

    /**
     * Whether this player is muted. A muted player's outgoing voice packets are
     * dropped before any audio processing or routing occurs. Unlike setting
     * {@link #setVolume(float)} to {@code 0f} or clearing {@link #setWhoCanHear(Set)},
     * mute is an explicit first-class flag intended for moderation / cross-plugin
     * integrations that need to query "is this player currently muted?".
     */
    boolean isMuted();

    void setMuted(boolean muted);

    /**
     * Whether this player's voice is routed to the real-time (priority) Cipher
     * stream for low-latency transcription/TTS (e.g. the Verity scenario). This
     * is an explicit first-class flag rather than a Bukkit permission so the
     * async voice path can read it without a main-thread permission race.
     */
    boolean isPriority();

    void setPriority(boolean priority);

    /**
     * Whether this player is exempt from voice moderation. Their speech is still
     * transcribed and logged, but the moderation service takes no action against
     * them. Like {@link #setPriority(boolean)}, this is a first-class data flag
     * (not a Bukkit permission) so the async voice path can read it race-free.
     */
    boolean isModerationBypass();

    void setModerationBypass(boolean moderationBypass);

    void reset();
}
