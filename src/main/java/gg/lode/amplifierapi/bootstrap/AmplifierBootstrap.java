package gg.lode.amplifierapi.bootstrap;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Lifecycle contract that the runtime-loaded Amplifier implementation
 * fulfils. The public {@code Amplifier-Loader} jar is a JavaPlugin shim
 * that loads an implementation (via cloud or local jar), instantiates the
 * entry class, and forwards Bukkit lifecycle calls to it.
 *
 * <p>Implementations MUST have a public no-arg constructor so the loader
 * can instantiate them via reflection.
 */
public interface AmplifierBootstrap {
    void onLoad(JavaPlugin host);
    void onEnable(JavaPlugin host);
    void onDisable(JavaPlugin host);
}
