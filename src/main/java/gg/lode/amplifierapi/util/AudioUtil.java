package gg.lode.amplifierapi.util;

public class AudioUtil {

    public static short[] floatToShort(float[] in) {
        short[] out = new short[in.length];
        for (int i = 0; i < in.length; i++) {
            float v = Math.max(-1f, Math.min(1f, in[i]));     // clip
            out[i] = (short) Math.round(v * 32767f);          // scale
        }
        return out;
    }

    // short -> float  (-32768 … +32767 ➜  -1.0f … +1.0f)
    public static float[] shortToFloat(short[] in) {
        float[] out = new float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i] / 32768f;                          // note divisor
        }
        return out;
    }

}
