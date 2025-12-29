package me.espryth.easyjoin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AvatarUtils {

    public static List<Component> getAvatar(Player player) {
        return getAvatar(player.getName());
    }

    public static List<Component> getAvatar(String playerName) {
        try {
            URL url = new URL("https://mc-heads.net/avatar/" + playerName + "/8");
            BufferedImage image = ImageIO.read(url);
            List<Component> lines = new ArrayList<>();

            for (int y = 0; y < image.getHeight(); y++) {
                Component line = Component.empty();
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    TextColor color = TextColor.color(rgb);
                    line = line.append(Component.text("█").color(color));
                }
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
