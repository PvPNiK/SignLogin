package me.pvpnik.signLogin;

import com.google.common.collect.Lists;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SignLogin extends JavaPlugin implements Listener {

    private static SignLogin INSTANCE;
    public static SignLogin getInstance() {
        return INSTANCE;
    }

    private AuthMeApi authmeApi;
    private SignMenuFactory signMenuFactory;
    public String[] signLines;
    public Material signMaterial;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("AuthMe") == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        INSTANCE = this;
        authmeApi = AuthMeApi.getInstance();
        signMenuFactory = new SignMenuFactory(this);

        getConfig();
        saveDefaultConfig();

        signLines = new String[]{"", "", "", ""};
        loadSignLines();

        signMaterial = Material.OAK_SIGN;
        loadSignMaterial();

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onCommandExecute(PlayerCommandPreprocessEvent e) {
        if (!e.getMessage().equalsIgnoreCase("/signlogin"))
            return;

        if (!e.getPlayer().isOp() && !e.getPlayer().hasPermission("signlogin.reload")) {
            e.getPlayer().sendMessage("Not enough permissions");
            return;
        }

        loadSignMaterial();
        loadSignLines();
        e.getPlayer().sendMessage("Reloaded SignLogin Config! (material and lines)");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!authmeApi.isRegistered(e.getPlayer().getName()))
            return;

        loginSign(e.getPlayer());
    }

    public void loginSign(Player player) {
        signMenuFactory.newMenu(player, Lists.newArrayList(signLines)).setSignMaterial(signMaterial)
                .response((p, strings) -> {

                    boolean checkPass = false;

                    for (int i = 0; i <= 3; i++) {
                        if (authmeApi.checkPassword(p.getName(), strings[i])) {
                            checkPass = true;
                        }
                    }

                    if (checkPass) {
                        authmeApi.forceLogin(p);
                    } else {
                        loginSign(player);
                    }

                    return true;
                }).open();
    }

    public void loadSignLines() {
        reloadConfig();
        for (int i = 1; i <= 4; i++) {
            if (getConfig().contains("sign.line" + i)) {
                signLines[i-1] = ChatColor.translateAlternateColorCodes('&', getConfig().getString("sign.line" + i));
            }
        }
    }

    public void loadSignMaterial() {
        reloadConfig();
        if (getConfig().contains("sign.material")) {
            try {
                signMaterial = Material.getMaterial(getConfig().getString("sign.material"));
            } catch (Exception e) {
                signMaterial = Material.OAK_SIGN;
            }
        }
    }

}
