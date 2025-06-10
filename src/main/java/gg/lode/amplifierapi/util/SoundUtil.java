package gg.lode.amplifierapi.util;

import de.maxhenkel.sonic.Sonic;

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

    public static short[] pitchAudio(short[] input, float pitch) {
        // Map pitch from -10..10 to 0.5x..2.0x for more dramatic effect
        pitch = Math.max(-10f, Math.min(10f, pitch));
        float factor = (float) Math.pow(2.0, pitch / 10.0);
        
        // Ensure factor is within reasonable bounds
        factor = Math.max(MIN_PITCH, Math.min(MAX_PITCH, factor));
        
        // Create a new Sonic instance for this operation
        Sonic stream = new Sonic(SAMPLE_RATE, 1);
        stream.setQuality(1); // 1 for high quality
        stream.setPitch(factor);
        
        // Process the audio
        stream.writeShortToStream(input, input.length);
        stream.flushStream();
        int numSamples = stream.samplesAvailable();
        short[] output = new short[input.length];
        int readSamples = stream.readShortFromStream(output, Math.min(numSamples, input.length));
        
        // Apply crossfading if we have fewer samples than input
        if (readSamples < input.length) {
            // Calculate crossfade region
            int crossfadeStart = Math.max(0, readSamples - CROSSFADE_SAMPLES);

            // Apply crossfade
            for (int i = crossfadeStart; i < readSamples; i++) {
                float fadeOut = (float)(readSamples - i) / CROSSFADE_SAMPLES;
                float fadeIn = 1.0f - fadeOut;
                
                // Mix the end of the processed audio with the start of the next chunk
                output[i] = (short) (output[i] * fadeOut + input[i] * fadeIn);
            }
            
            // Copy remaining samples from input
            System.arraycopy(input, readSamples, output, readSamples, input.length - readSamples);
        }
        
        return output;
    }

}
