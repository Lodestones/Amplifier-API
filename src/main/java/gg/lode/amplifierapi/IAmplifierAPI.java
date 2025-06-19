package gg.lode.amplifierapi;

import de.maxhenkel.voicechat.api.VoicechatServerApi;
import gg.lode.amplifierapi.api.manager.IVoiceManager;

public interface IAmplifierAPI {

    IVoiceManager getVoiceManager();

    float getVoiceDistance();

    void setVoiceDistance(float distance);

    VoicechatServerApi getVoicechatApi();


}
