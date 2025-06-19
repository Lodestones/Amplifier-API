package gg.lode.amplifierapi.util;

import com.tianscar.soundtouch.SoundTouch;
import de.maxhenkel.sonic.Sonic;
import it.unimi.dsi.fastutil.floats.FloatArrayList;

import java.io.IOException;

public class SoundUtil {
    private static final float MIN_PITCH = 0.5f;
    private static final float MAX_PITCH = 2.0f;
    public static final int SAMPLE_RATE = 48000;
    private static final int CROSSFADE_SAMPLES = 100; // Number of samples to crossfade
    
    public static short[] applyVolume(short[] input, float volume) {
        short[] output = new short[input.length];
        for (int i = 0; i < input.length; i++) {
            float scaled = input[i] * volume;
            output[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, scaled));
        }
        return output;
    }

    public static short[] setSpeed(short[] audio, double speed) {
        if (speed >= 0.99D && speed <= 1.01D) {
            return audio;
        }

        Sonic stream = new Sonic(SAMPLE_RATE, 1);
        stream.setSpeed((float) speed);
        stream.setPitch((float) speed);
        stream.writeShortToStream(audio, audio.length);
        stream.flushStream();
        int numSamples = stream.samplesAvailable();
        short[] outSamples = new short[audio.length];
        int readSamples = stream.readShortFromStream(outSamples, Math.min(numSamples, audio.length));
        
        // Apply crossfading if we have fewer samples than input
        if (readSamples < audio.length) {
            // Calculate crossfade region
            int crossfadeStart = Math.max(0, readSamples - CROSSFADE_SAMPLES);
            int crossfadeEnd = readSamples;
            
            // Apply crossfade
            for (int i = crossfadeStart; i < crossfadeEnd; i++) {
                float fadeOut = (float)(crossfadeEnd - i) / CROSSFADE_SAMPLES;
                float fadeIn = 1.0f - fadeOut;
                
                // Mix the end of the processed audio with the start of the next chunk
                if (i < audio.length) {
                    outSamples[i] = (short)(outSamples[i] * fadeOut + audio[i] * fadeIn);
                }
            }

            // Copy remaining samples from input
            System.arraycopy(audio, crossfadeEnd, outSamples, crossfadeEnd, audio.length - crossfadeEnd);
        }

        return outSamples;
    }

    /**
     * Pitch-shifts a mono 16-bit PCM buffer with SoundTouch and keeps
     * edges smooth so no “static” clicks are left in the stream.
     *
     * @param samples       raw 16-bit PCM (little-endian) audio
     * @param semitoneShift -12 - +12 semitones
     * @return processed audio sized exactly to the data SoundTouch generated
     */
    public static float[] pitchAudio(float[] samples, float semitoneShift) {
        // ── 1. Clamp shift to ±12 semitones
        semitoneShift = Math.max(-12f, Math.min(12f, semitoneShift));

        // ── 2. Configure processor
        try (SoundTouch st = new SoundTouch()) {
            st.setSampleRate(SAMPLE_RATE);
            st.setChannels(1);
            st.setPitchSemiTones(semitoneShift);
            st.setSetting(SoundTouch.SETTING_USE_AA_FILTER, 1);
            st.setSetting(SoundTouch.SETTING_AA_FILTER_LENGTH, 32);

            // ── 3. Feed data in fixed-size chunks with explicit offset/length
            final int CHUNK = 2048;         // 42 ms at 48 kHz mono
            for (int offset = 0; offset < samples.length; offset += CHUNK) {
                int len = Math.min(CHUNK, samples.length - offset);
                st.putSamples(samples, offset, len);
            }
            st.flush(); // tell SoundTouch no more input is coming

            // ── 4. Pull everything out, again in chunks with offset
            FloatArrayList out = new FloatArrayList(samples.length * 2);
            float[] buf = new float[CHUNK];
            int got;
            while ((got = st.receiveSamples(buf, 0, buf.length)) > 0) {
                out.addElements(out.size(), buf, 0, got);
            }

            // ── 5. Short tail-fade to erase zipper noise
            int fade = Math.min(64, out.size());
            for (int i = 0; i < fade; i++) {
                float w = (fade - i) / (float) fade;
                int idx = out.size() - fade + i;
                out.set(idx, out.getFloat(idx) * w);
            }
            return out.toFloatArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to process audio with SoundTouch", e);
        }
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
