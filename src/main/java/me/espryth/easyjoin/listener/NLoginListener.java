package me.espryth.easyjoin.listener;

import com.nickuc.login.api.event.bukkit.auth.LoginEvent;
import com.nickuc.login.api.event.bukkit.auth.RegisterEvent;
import com.thewinterframework.service.annotation.Service;
import me.espryth.easyjoin.action.ActionQueue;
import me.espryth.easyjoin.format.Format;
import me.espryth.easyjoin.service.FormatService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Optional;

@Service
public class NLoginListener implements Listener {

    private final Plugin plugin;
    private final FormatService formatService;

    public NLoginListener(Plugin plugin, FormatService formatService) {
        this.plugin = plugin;
        this.formatService = formatService;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        executeActions(event.getPlayer());
    }

    @EventHandler
    public void onRegister(RegisterEvent event) {
        executeActions(event.getPlayer());
    }

    private void executeActions(Player player) {
        if (Bukkit.getPluginManager().getPlugin("nLogin") == null) return;
        Optional<Format> formatOpt = formatService.getFormatForPlayer(player, false);
        if (formatOpt.isEmpty()) formatOpt = formatService.getDefaultFormat(false);

        formatOpt.ifPresent(format -> {
            new ActionQueue(format.authActions(), plugin).executeAll(player);
        });
    }
}
