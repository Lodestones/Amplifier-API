package gg.lode.amplifierapi.api.event;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import gg.lode.bookshelfapi.api.event.BaseEvent;
import org.bukkit.entity.Player;

public class PlayerMicrophoneEvent extends BaseEvent {
    private final Player player;
    private final VoicechatConnection connection;
    private final MicrophonePacketEvent event;

    public PlayerMicrophoneEvent(Player player, VoicechatConnection connection, MicrophonePacketEvent event) {
        super(true);
        this.player = player;
        this.connection = connection;
        this.event = event;
    }

    public Player player() {
        return player;
    }

    public VoicechatConnection connection() {
        return connection;
    }

    public MicrophonePacketEvent event() {
        return event;
    }
}
