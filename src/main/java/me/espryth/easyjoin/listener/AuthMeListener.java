package me.espryth.easyjoin.listener;

import com.thewinterframework.service.annotation.Service;
import fr.xephi.authme.events.LoginEvent;
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
public class AuthMeListener implements Listener {

    private final Plugin plugin;
    private final FormatService formatService;

    public AuthMeListener(Plugin plugin, FormatService formatService) {
        this.plugin = plugin;
        this.formatService = formatService;
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (Bukkit.getPluginManager().getPlugin("AuthMe") == null) return;
        Player player = event.getPlayer();
        Optional<Format> formatOpt = formatService.getFormatForPlayer(player, false);
        if (formatOpt.isEmpty()) formatOpt = formatService.getDefaultFormat(false);

        formatOpt.ifPresent(format -> {
            new ActionQueue(format.authActions(), plugin).executeAll(player);
        });
    }
}
