package me.espryth.easyjoin.service;

import com.thewinterframework.service.annotation.Service;
import me.espryth.easyjoin.action.Action;
import me.espryth.easyjoin.util.AvatarUtils;
import me.espryth.easyjoin.util.MessageUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;

@Service
public class ActionService {

    private final Map<String, ActionFactory> actionFactories = new HashMap<>();

    public ActionService() {
        registerDefaultActions();
    }

    public void registerAction(String identifier, ActionFactory factory) {
        actionFactories.put(identifier.toUpperCase(), factory);
    }

    public Optional<Action> parseAction(String line) {
        String[] parts = line.split(" ", 2);
        String identifier = parts[0].toUpperCase();
        String data = parts.length > 1 ? parts[1] : "";

        ActionFactory factory = actionFactories.get(identifier);
        if (factory != null) {
            return Optional.of(factory.create(data));
        }
        return Optional.empty();
    }

    private void registerDefaultActions() {
        registerAction("[MESSAGE]", data -> (player, queue) -> {
            Component msg = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, data));
            if (data.startsWith("<c>")) {
                // Centering with Components is harder, let's keep it legacy for centering or try to adapt
                String legacy = MessageUtils.formatString(player, data);
                player.sendMessage(MessageUtils.getCenteredMessage(legacy.replace("<c>", "")));
            } else {
                player.sendMessage(msg);
            }
        });

        registerAction("[BROADCAST]", data -> (player, queue) -> {
            Component msg = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, data));
            if (data.startsWith("<c>")) {
                String legacy = MessageUtils.formatString(player, data);
                Bukkit.broadcastMessage(MessageUtils.getCenteredMessage(legacy.replace("<c>", "")));
            } else {
                Bukkit.broadcast(msg);
            }
        });

        registerAction("[JSON_MESSAGE]", data -> (player, queue) -> {
            String json = MessageUtils.formatString(player, data);
            player.sendMessage(GsonComponentSerializer.gson().deserialize(json));
        });

        registerAction("[JSON_BROADCAST]", data -> (player, queue) -> {
            String json = MessageUtils.formatString(player, data);
            Bukkit.broadcast(GsonComponentSerializer.gson().deserialize(json));
        });

        registerAction("[ACTIONBAR]", data -> (player, queue) -> {
            String[] parts = data.split(";");
            Component msg = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, parts[0]));
            player.sendActionBar(msg);
        });

        registerAction("[BROADCAST_ACTIONBAR]", data -> (player, queue) -> {
            String[] parts = data.split(";");
            Component msg = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, parts[0]));
            Bukkit.broadcast(msg);
        });

        registerAction("[TITLE]", data -> (player, queue) -> {
            String[] parts = data.split(";");
            String titleStr = parts.length > 0 ? parts[0] : "";
            String subtitleStr = parts.length > 1 ? parts[1] : "";
            int fadeIn = parts.length > 2 ? Integer.parseInt(parts[2]) : 10;
            int stay = parts.length > 3 ? Integer.parseInt(parts[3]) : 70;
            int fadeOut = parts.length > 4 ? Integer.parseInt(parts[4]) : 20;

            Component title = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, titleStr));
            Component subtitle = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, subtitleStr));

            player.showTitle(net.kyori.adventure.title.Title.title(title, subtitle, net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(fadeIn * 50L),
                    java.time.Duration.ofMillis(stay * 50L),
                    java.time.Duration.ofMillis(fadeOut * 50L)
            )));
        });

        registerAction("[BROADCAST_TITLE]", data -> (player, queue) -> {
            String[] parts = data.split(";");
            String titleStr = parts.length > 0 ? parts[0] : "";
            String subtitleStr = parts.length > 1 ? parts[1] : "";
            int fadeIn = parts.length > 2 ? Integer.parseInt(parts[2]) : 10;
            int stay = parts.length > 3 ? Integer.parseInt(parts[3]) : 70;
            int fadeOut = parts.length > 4 ? Integer.parseInt(parts[4]) : 20;

            Component title = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, titleStr));
            Component subtitle = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, subtitleStr));

            Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(net.kyori.adventure.title.Title.title(title, subtitle, net.kyori.adventure.title.Title.Times.times(
                    java.time.Duration.ofMillis(fadeIn * 50L),
                    java.time.Duration.ofMillis(stay * 50L),
                    java.time.Duration.ofMillis(fadeOut * 50L)
            ))));
        });

        registerAction("[AVATAR_MESSAGE]", data -> (player, queue) -> {
            List<Component> avatar = AvatarUtils.getAvatar(player);
            String[] lines = data.split("<nl>");
            for (int i = 0; i < 8; i++) {
                Component avatarLine = avatar.size() > i ? avatar.get(i) : Component.text("        ");
                String messageLine = lines.length > i ? lines[i] : "";
                Component formattedMessageLine = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, messageLine));
                player.sendMessage(avatarLine.append(Component.text(" ")).append(formattedMessageLine));
            }
        });

        registerAction("[AVATAR_BROADCAST]", data -> (player, queue) -> {
            List<Component> avatar = AvatarUtils.getAvatar(player);
            String[] lines = data.split("<nl>");
            for (int i = 0; i < 8; i++) {
                Component avatarLine = avatar.size() > i ? avatar.get(i) : Component.text("        ");
                String messageLine = lines.length > i ? lines[i] : "";
                Component formattedMessageLine = MessageUtils.colorizeToComponent(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, messageLine));
                Bukkit.broadcast(avatarLine.append(Component.text(" ")).append(formattedMessageLine));
            }
        });

        registerAction("[CLEARCHAT]", data -> (player, queue) -> {
            int lines = Integer.parseInt(data);
            for (int i = 0; i < lines; i++) player.sendMessage(" ");
        });

        registerAction("[FIREWORK]", data -> (player, queue) -> {
            String[] parts = data.split(";");
            if (parts.length < 3) return;
            try {
                FireworkEffect.Type type = FireworkEffect.Type.valueOf(parts[0].toUpperCase());
                int amount = Integer.parseInt(parts[1]);
                int power = Integer.parseInt(parts[2]);
                List<Color> colors = new ArrayList<>();
                List<Color> fades = new ArrayList<>();
                for (int i = 3; i < parts.length; i++) {
                    String[] rgb = parts[i].split(",");
                    Color color = Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
                    if (i % 2 == 0) fades.add(color); else colors.add(color);
                }
                Firework firework = (Firework) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
                FireworkMeta meta = firework.getFireworkMeta();
                meta.setPower(power);
                meta.addEffect(FireworkEffect.builder().with(type).withColor(colors).withFade(fades).trail(true).build());
                firework.setFireworkMeta(meta);
                for (int i = 1; i < amount; i++) {
                    Firework f = (Firework) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
                    f.setFireworkMeta(meta);
                }
            } catch (Exception ignored) {}
        });

        registerAction("[CONSOLE]", data -> (player, queue) -> {
            String cmd = MessageUtils.formatString(player, data);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        });

        registerAction("[PLAYER]", data -> (player, queue) -> {
            String cmd = MessageUtils.formatString(player, data);
            player.performCommand(cmd);
        });

        registerAction("[SOUND]", data -> (player, queue) -> {
            String[] parts = data.split(";");
            try {
                Sound sound = Sound.valueOf(parts[0].toUpperCase());
                float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (Exception ignored) {}
        });

        registerAction("[BROADCAST_SOUND]", data -> (player, queue) -> {
            String[] parts = data.split(";");
            try {
                Sound sound = Sound.valueOf(parts[0].toUpperCase());
                float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1.0f;
                float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1.0f;
                Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), sound, volume, pitch));
            } catch (Exception ignored) {}
        });

        registerAction("[DELAY]", data -> (player, queue) -> {
            try {
                int delay = Integer.parseInt(data);
                queue.setPaused(true);
                Bukkit.getScheduler().runTaskLater(queue.getPlugin(), () -> {
                    queue.setPaused(false);
                    queue.executeAll(player);
                }, delay * 20L);
            } catch (NumberFormatException ignored) {}
        });
    }

    @FunctionalInterface
    public interface ActionFactory {
        Action create(String data);
    }
}
