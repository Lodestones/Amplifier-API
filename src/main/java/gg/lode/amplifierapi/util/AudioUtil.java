package gg.lode.amplifierapi.util;

public class AudioUtil {

    public static short[] floatToShort(float[] in) {
        short[] out = new short[in.length];
        floatToShort(in, out, in.length);
        return out;
    }

    /**
     * Convert {@code length} float samples (+/-1.0f) into 16-bit PCM written
     * into {@code out}. {@code out} must be at least {@code length} long. Use
     * this in hot paths to skip the {@code new short[]} allocation that
     * {@link #floatToShort(float[])} makes.
     */
    public static void floatToShort(float[] in, short[] out, int length) {
        for (int i = 0; i < length; i++) {
            float v = Math.max(-1f, Math.min(1f, in[i]));
            out[i] = (short) Math.round(v * 32767f);
        }
    }

    // short -> float  (-32768 ... +32767  ->  -1.0f ... +1.0f)
    public static float[] shortToFloat(short[] in) {
        float[] out = new float[in.length];
        shortToFloat(in, out, in.length);
        return out;
    }

    /**
     * Convert {@code length} 16-bit PCM samples into normalized floats
     * (+/-1.0f) written into {@code out}. {@code out} must be at least
     * {@code length} long. Use this in hot paths to skip the
     * {@code new float[]} allocation that {@link #shortToFloat(short[])} makes.
     */
    public static void shortToFloat(short[] in, float[] out, int length) {
        for (int i = 0; i < length; i++) {
            out[i] = in[i] / 32768f;
        }
    }

}
