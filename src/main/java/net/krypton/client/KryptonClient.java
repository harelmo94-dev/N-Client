package net.krypton.client;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KryptonClient implements ModInitializer {
    public static final String MOD_ID = "krypton-client";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Krypton Client initialized successfully!");
    }
}
