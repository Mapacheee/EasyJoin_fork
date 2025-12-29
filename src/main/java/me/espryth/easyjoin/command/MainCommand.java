package me.espryth.easyjoin.command;

import com.google.inject.Inject;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.service.ReloadServiceManager;
import me.espryth.easyjoin.service.FormatService;
import me.espryth.easyjoin.util.MessageUtils;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.paper.util.sender.Source;

@CommandComponent
public class MainCommand {

    private final ReloadServiceManager reloadServiceManager;
    private final FormatService formatService;

    @Inject
    public MainCommand(ReloadServiceManager reloadServiceManager, FormatService formatService) {
        this.reloadServiceManager = reloadServiceManager;
        this.formatService = formatService;
    }

    @Command("easyjoin")
    public void mainHelp(Source source) {
        CommandSender sender = (CommandSender) source.source();
        sender.sendMessage(MessageUtils.colorize("&e&lEasy&6&lJoin &fV3"));
        if (sender.hasPermission("easyjoin.admin")) {
            sender.sendMessage(MessageUtils.colorize("&f- /ej reload"));
        }
    }

    @Command("easyjoin reload")
    @Permission("easyjoin.admin")
    public void reload(Source source) {
        CommandSender sender = (CommandSender) source.source();
        reloadServiceManager.reload();
        formatService.loadFormats();
        sender.sendMessage(MessageUtils.colorize("&aConfiguration reloaded!"));
    }
}
