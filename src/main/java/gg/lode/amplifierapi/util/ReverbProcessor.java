package gg.lode.amplifierapi.util;

/**
 * Stateful Schroeder/Freeverb reverb processor that persists delay line state
 * between audio frames, producing smooth reverb tails across packet boundaries.
 * <p>
 * Each player should have their own instance. Not thread-safe by design —
 * VoiceManager guarantees single-threaded access per player.
 */
public class ReverbProcessor {

    private static final int[] COMB_DELAYS = {1116, 1188, 1277, 1356};   // @48 kHz ~ 23-28 ms
    private static final int[] ALL_PASS_DELAYS = {556, 441};              // 11 ms & 9 ms
    private static final float DAMP = 0.2f;

    private final float[][] combBuf;
    private final int[] combIdx;
    private final float[] combFilter;
    private final float[][] apBuf;
    private final int[] apIdx;

    public ReverbProcessor() {
        combBuf = new float[COMB_DELAYS.length][];
        combIdx = new int[COMB_DELAYS.length];
        combFilter = new float[COMB_DELAYS.length];
        for (int i = 0; i < COMB_DELAYS.length; i++) {
            combBuf[i] = new float[COMB_DELAYS[i]];
        }

        apBuf = new float[ALL_PASS_DELAYS.length][];
        apIdx = new int[ALL_PASS_DELAYS.length];
        for (int i = 0; i < ALL_PASS_DELAYS.length; i++) {
            apBuf[i] = new float[ALL_PASS_DELAYS[i]];
        }
    }

    /**
     * Processes a mono float audio frame with reverb. State carries across calls
     * so reverb tails decay smoothly between packets.
     *
     * @param in       mono 32-bit PCM, range +/-1.0f
     * @param roomSize 0-1, decay length (0.7 = medium hall)
     * @param wetMix   0-1, how much reverb to blend in
     * @return processed buffer, same length as input
     */
    public float[] process(float[] in, float roomSize, float wetMix) {
        float feedback = 0.28f + 0.72f * roomSize;
        float[] out = new float[in.length];

        for (int n = 0; n < in.length; n++) {
            float sample = in[n];

            // --- comb filter group
            float combSum = 0;
            for (int i = 0; i < combBuf.length; i++) {
                float delayed = combBuf[i][combIdx[i]];
                combFilter[i] = delayed + (combFilter[i] - delayed) * DAMP;
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

    /**
     * Zeros all delay line state. Call on player reset/cleanup.
     */
    public void reset() {
        for (int i = 0; i < combBuf.length; i++) {
            java.util.Arrays.fill(combBuf[i], 0f);
            combIdx[i] = 0;
            combFilter[i] = 0f;
        }
        for (int i = 0; i < apBuf.length; i++) {
            java.util.Arrays.fill(apBuf[i], 0f);
            apIdx[i] = 0;
        }
    }
}
