package gg.lode.amplifierapi;

import gg.lode.amplifierapi.api.manager.IVoiceManager;

public interface IAmplifierAPI {

    IVoiceManager getVoiceManager();
    float getDistance();
    void setDistance(float distance);

}
