package gg.lode.amplifierapi;

public class AmplifierAPI {

    private static IAmplifierAPI api;

    public static void setApi(IAmplifierAPI a) {
        api = a;
    }

    public static IAmplifierAPI getApi() {
        return api;
    }

}
