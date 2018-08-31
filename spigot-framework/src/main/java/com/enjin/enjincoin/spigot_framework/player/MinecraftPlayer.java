package com.enjin.enjincoin.spigot_framework.player;

import com.enjin.enjincoin.sdk.client.Client;
import com.enjin.enjincoin.sdk.client.model.body.GraphQLResponse;
import com.enjin.enjincoin.sdk.client.service.identities.vo.Identity;
import com.enjin.enjincoin.sdk.client.service.users.vo.User;
import com.enjin.enjincoin.sdk.client.service.users.vo.data.UsersData;
import com.enjin.enjincoin.spigot_framework.BasePlugin;
import com.enjin.enjincoin.spigot_framework.trade.TradeView;
import com.enjin.enjincoin.spigot_framework.util.Scoreboards;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MinecraftPlayer {

    // Bukkit Fields
    private BasePlugin plugin;
    private Player bukkitPlayer;

    // Trusted Platform Data Fields
    private UserData userData;
    private Identity identity;
    private IdentityData identityData;
    private Wallet wallet;

    // State Fields
    private boolean userLoaded;
    private boolean identityLoaded;
    private boolean showScoreboard;

    // Helper Fields
    private User user;

    // Trade Fields
    private List<MinecraftPlayer> sentTradeInvites = new ArrayList<>();
    private List<MinecraftPlayer> receivedTradeInvites = new ArrayList<>();
    private TradeView activeTradeView;

    public MinecraftPlayer(BasePlugin plugin, Player player) {
        this.plugin = plugin;
        this.bukkitPlayer = player;
        this.showScoreboard = (player.getScoreboard() == null) ? false : true;
    }

    public Player getBukkitPlayer() {
        return this.bukkitPlayer;
    }

    public UserData getUserData() {
        return this.userData;
    }

    public IdentityData getIdentityData() { return this.identityData; }

    public Wallet getWallet() {
        return wallet;
    }

    public Identity getIdentity() { return this.identity; }

    public boolean isUserLoaded() {
        return this.userLoaded;
    }

    public void loadUser(User user) {
        if (user == null) {
            return;
        }

        this.user = user;
        this.userData = new UserData(user);
        this.userLoaded = true;

        Integer appId = this.plugin.getBootstrap().getAppId();
        Optional<Identity> optionalIdentity = user.getIdentities().stream()
                .filter(identity -> identity.getAppId() == appId)
                .findFirst();
        optionalIdentity.ifPresent(this::loadIdentity);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> setScoreboard(), 1);
    }

    public User getUser() { return this.user; }

    public void reloadUser() {
        Client client = this.plugin.getBootstrap().getSdkController().getClient();
        // Fetch the User for the Player in question
        try {
            Response<GraphQLResponse<UsersData>> networkResponse = client.getUsersService()
                    .getUsersSync(null, bukkitPlayer.getUniqueId().toString(), null);

            User user = null;
            // we likely need a legit reload function for the wallet to repopulate it.
            this.wallet = new Wallet(bukkitPlayer.getUniqueId());

            if (networkResponse.isSuccessful()) {
                GraphQLResponse<UsersData> response = networkResponse.body();
                if (!response.isEmpty()) {
                    UsersData data = response.getData();
                    if (!data.isEmpty()) {
                        user = data.getUsers().get(0);
                    }
                }
            }

            loadUser(user);
        } catch (Exception e) {
            System.out.println("Failed to reload user");
        }
    }

    public boolean isIdentityLoaded() {
        return this.identityLoaded;
    }

    public void loadIdentity(Identity identity) {
        if (identity == null) {
            return;
        }

        this.identity = identity;

        this.identityData = new IdentityData(identity);
        this.wallet = new Wallet(bukkitPlayer.getUniqueId());
        this.identityLoaded = true;

        this.wallet.populate(identity.getTokens());
    }

    public boolean isLoaded() {
        return isUserLoaded() && isIdentityLoaded();
    }

    protected void cleanUp() {
        PlayerInitializationTask.cleanUp(bukkitPlayer.getUniqueId());

        this.bukkitPlayer = null;
    }

    public List<MinecraftPlayer> getSentTradeInvites() {
        return sentTradeInvites;
    }

    public List<MinecraftPlayer> getReceivedTradeInvites() {
        return receivedTradeInvites;
    }

    public boolean showScoreboard() {
        return showScoreboard;
    }

    public void showScoreboard(boolean showScoreboard) {
        this.showScoreboard = showScoreboard;
    }

    public void setScoreboard() {
        if (showScoreboard) {
            this.plugin.getBootstrap().getScoreboardManager().setSidebar(this);
        }
    }

    public void refresh() {
        if (showScoreboard)
            this.plugin.getBootstrap().getScoreboardManager().setSidebar(this);
    }

    public TradeView getActiveTradeView() {
        return activeTradeView;
    }

    public void setActiveTradeView(TradeView activeTradeView) {
        this.activeTradeView = activeTradeView;
    }
}