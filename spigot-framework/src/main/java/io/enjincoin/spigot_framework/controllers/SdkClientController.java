package io.enjincoin.spigot_framework.controllers;

import com.google.gson.JsonObject;
import io.enjincoin.sdk.client.Client;
import io.enjincoin.sdk.client.Clients;
import io.enjincoin.sdk.client.config.Config;
import io.enjincoin.spigot_framework.BasePlugin;

import java.io.IOException;
import java.util.logging.Level;

/**
 * <p>Controller for the Enjin Coin SDK client.</p>
 *
 * @since 1.0
 */
public class SdkClientController {

    /**
     * Config key for the base url of the trusted platform.
     */
    public static final String PLATFORM_BASE_URL = "platformBaseUrl";

    /**
     * Config key for the app id to use with the trusted platform.
     */
    public static final String APP_ID = "appId";

    /**
     * <p>The spigot plugin.</p>
     */
    private final BasePlugin main;

    /**
     * <p>The cached bootstrap config.</p>
     */
    private final JsonObject config;

    /**
     * <p>The Enjin Coin SDK client.</p>
     */
    private Client client;

    /**
     * <p>Controller constructor.</p>
     *
     * @param main the Spigot plugin
     * @param config the bootstrap config
     */
    public SdkClientController(BasePlugin main, JsonObject config) {
        this.main = main;
        this.config = config;
    }

    /**
     * <p>Initialization mechanism for this controller.</p>
     *
     * @since 1.0
     */
    public void setUp() {
        if (!config.has(PLATFORM_BASE_URL))
            throw new IllegalStateException(String.format("The \"%s\" key does not exists in the config.", PLATFORM_BASE_URL));
        if (!config.has(APP_ID))
            throw new IllegalStateException(String.format("The \"%s\" key does not exists in the config.", APP_ID));
        this.client = Clients.createClient(this.config.get(PLATFORM_BASE_URL).getAsString(),
                this.config.get(APP_ID).getAsInt(), this.main.getBootstrap().isDebugEnabled());
    }

    /**
     * <p>Cleanup mechanism for this controller.</p>
     *
     * @since 1.0
     */
    public void tearDown() {
         try {
             this.client.close();
         } catch (IOException e) {
             this.main.getLogger().log(Level.WARNING, "An error occurred while shutting down the Enjin Coin client.", e);
         }
    }

    /**
     * <p>Returns the Enjin Coin SDK client.</p>
     *
     * @return the client or null if not initialized
     *
     * @since 1.0
     */
    public Client getClient() {
        return client;
    }
}