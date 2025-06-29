package gg.lode.amplifierapi.util;

public class SoundUtil {
    
    public static short[] applyVolume(short[] input, float volume) {
        short[] output = new short[input.length];
        for (int i = 0; i < input.length; i++) {
            float scaled = input[i] * volume;
            output[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, scaled));
        }
        return output;
    }

    /**
     * Applies a simple Schroeder reverb (Freeverb-style) to a mono float[]
     *
     * @param in       mono 32-bit PCM, range ±1.0f
     * @param roomSize 0 … 1 ⇢ decay length (0.7 ≈ medium hall)
     * @param wetMix   0 … 1 ⇢ how much reverb to blend in (0.3 = subtle)
     * @return processed buffer, same length as input
     */
    public static float[] applyReverb(float[] in, float roomSize, float wetMix) {
        final int[] combDelays = {1116, 1188, 1277, 1356};   // @48 kHz ≈ 23-28 ms
        final int[] allPassDelays = {556, 441};                // 11 ms & 9 ms

        float feedback = 0.28f + 0.72f * roomSize;             // 0.28 → 1.0
        float damp = 0.2f;                                 // low-pass inside combs

        // internal delay lines
        float[][] combBuf = new float[combDelays.length][];
        int[] combIdx = new int[combDelays.length];
        float[] combFilter = new float[combDelays.length];     // one-pole low-pass state

        for (int i = 0; i < combDelays.length; i++)
            combBuf[i] = new float[combDelays[i]];

        float[][] apBuf = new float[allPassDelays.length][];
        int[] apIdx = new int[allPassDelays.length];
        for (int i = 0; i < allPassDelays.length; i++)
            apBuf[i] = new float[allPassDelays[i]];

        float[] out = new float[in.length];

        for (int n = 0; n < in.length; n++) {
            float sample = in[n];

            // --- comb group
            float combSum = 0;
            for (int i = 0; i < combBuf.length; i++) {
                float delayed = combBuf[i][combIdx[i]];
                combFilter[i] = delayed + (combFilter[i] - delayed) * damp;
                float y = combFilter[i];
                combBuf[i][combIdx[i]] = sample + y * feedback;
                if (++combIdx[i] == combBuf[i].length) combIdx[i] = 0;
                combSum += y;
            }
            float rev = combSum * (1f / combBuf.length);

            // --- two all-pass diffusers
            for (int i = 0; i < apBuf.length; i++) {
                float bufOut = apBuf[i][apIdx[i]];
                float bufIn = rev + bufOut * 0.5f;
                apBuf[i][apIdx[i]] = bufIn;
                if (++apIdx[i] == apBuf[i].length) apIdx[i] = 0;
                rev = bufOut - rev * 0.5f;
            }

            // --- wet / dry mix
            out[n] = sample * (1f - wetMix) + rev * wetMix;
        }
        return out;
    }

}
