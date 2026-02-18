package me.espryth.easyjoin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AvatarUtils {

    public static List<Component> getAvatar(Player player) {
        return getAvatar(player.getUniqueId().toString().replace("-", ""), player.getName());
    }

    public static List<Component> getAvatar(String uuid, String playerName) {
        BufferedImage image = fetchSkinFromMojang(uuid, 64);
        if (image == null) image = fetchAshcon(playerName, 64);
        if (image == null) image = fetchMcHeads(playerName, 8);
        if (image == null) return generatePlaceholder(playerName, 8, 8);

        if (image.getWidth() >= 32) {
            BufferedImage head = cropHeadFromSkin(image);
            BufferedImage small = resize(head, 8, 8);
            return imageToComponents(small);
        }

        return imageToComponents(image);
    }

    private static BufferedImage fetchSkinFromMojang(String uuid, int targetSize) {
        try {
            URL profileUrl = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).toURL();
            byte[] profileBytes = profileUrl.openStream().readAllBytes();
            String profileJson = new String(profileBytes, StandardCharsets.UTF_8);

            String textureBase64 = extractBase64Texture(profileJson);
            if (textureBase64 == null) return null;

            String textureJson = new String(Base64.getDecoder().decode(textureBase64), StandardCharsets.UTF_8);
            String skinUrl = extractSkinUrlFromTextureJson(textureJson);
            if (skinUrl == null) return null;

            URL skinUrlObj = URI.create(skinUrl).toURL();
            BufferedImage skin = ImageIO.read(skinUrlObj);
            return skin;
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage fetchAshcon(String playerName, int size) {
        try {
            URL url = URI.create("https://api.ashcon.app/mojang/v2/user/" + playerName).toURL();
            byte[] data = url.openStream().readAllBytes();
            String json = new String(data, StandardCharsets.UTF_8);
            int texturesIdx = json.indexOf("\"textures\"");
            if (texturesIdx < 0) return null;
            int skinIdx = json.indexOf("\"skin\"", texturesIdx);
            if (skinIdx < 0) return null;
            int urlIdx = json.indexOf("\"url\"", skinIdx);
            if (urlIdx < 0) return null;
            int colon = json.indexOf(':', urlIdx);
            if (colon < 0) return null;
            int firstQuote = json.indexOf('"', colon);
            if (firstQuote < 0) return null;
            int secondQuote = json.indexOf('"', firstQuote + 1);
            if (secondQuote < 0) return null;
            String skinUrl = json.substring(firstQuote + 1, secondQuote);
            if (skinUrl.isBlank()) return null;
            return ImageIO.read(URI.create(skinUrl).toURL());
        } catch (IOException e) {
            return null;
        }
    }

    private static BufferedImage fetchMcHeads(String playerName, int size) {
        try {
            URL url = URI.create("https://mc-heads.net/avatar/" + playerName + "/" + size).toURL();
            return ImageIO.read(url);
        } catch (IOException e) {
            return null;
        }
    }

    private static String extractBase64Texture(String profileJson) {
        int props = profileJson.indexOf("\"properties\"");
        if (props < 0) return null;
        int valueIndex = profileJson.indexOf("\"value\"", props);
        if (valueIndex < 0) return null;
        int colon = profileJson.indexOf(':', valueIndex);
        if (colon < 0) return null;
        int firstQuote = profileJson.indexOf('"', colon);
        if (firstQuote < 0) return null;
        int secondQuote = profileJson.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return null;
        String base64 = profileJson.substring(firstQuote + 1, secondQuote);
        if (base64.isBlank()) return null;
        return base64;
    }

    private static String extractSkinUrlFromTextureJson(String textureJson) {
        int skinIdx = textureJson.indexOf("\"SKIN\"");
        if (skinIdx < 0) return null;
        int urlIdx = textureJson.indexOf("\"url\"", skinIdx);
        if (urlIdx < 0) return null;
        int colon = textureJson.indexOf(':', urlIdx);
        if (colon < 0) return null;
        int firstQuote = textureJson.indexOf('"', colon);
        if (firstQuote < 0) return null;
        int secondQuote = textureJson.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return null;
        String url = textureJson.substring(firstQuote + 1, secondQuote);
        return url;
    }

    private static BufferedImage cropHeadFromSkin(BufferedImage skin) {
        int width = skin.getWidth();
        int scale = Math.max(1, width / 64);
        int headX = 8 * scale;
        int headY = 8 * scale;
        int headSize = 8 * scale;
        try {
            BufferedImage head = skin.getSubimage(headX, headY, headSize, headSize);
            return head;
        } catch (Exception e) {
            return skin;
        }
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, newW, newH, null);
        g2.dispose();
        return resized;
    }

    private static List<Component> imageToComponents(BufferedImage image) {
        List<Component> lines = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y++) {
            Component line = Component.empty();
            for (int x = 0; x < image.getWidth(); x++) {
                int rgba = image.getRGB(x, y);
                int alpha = (rgba >> 24) & 0xFF;
                if (alpha < 64) {
                    line = line.append(Component.text(" "));
                    continue;
                }
                int rgb = rgba & 0xFFFFFF;
                TextColor color = TextColor.color(rgb);
                line = line.append(Component.text("█").color(color));
            }
            lines.add(line);
        }
        return lines;
    }

    private static List<Component> generatePlaceholder(String seed, int width, int height) {
        List<Component> lines = new ArrayList<>();
        int hash = seed.hashCode();
        int baseR = (hash >> 16) & 0xFF;
        int baseG = (hash >> 8) & 0xFF;
        int baseB = hash & 0xFF;

        for (int y = 0; y < height; y++) {
            Component line = Component.empty();
            for (int x = 0; x < width; x++) {
                int r = (baseR + x * 10 + y * 5) & 0xFF;
                int g = (baseG + x * 7 + y * 3) & 0xFF;
                int b = (baseB + x * 4 + y * 9) & 0xFF;
                int rgb = (r << 16) | (g << 8) | b;
                TextColor color = TextColor.color(rgb);
                line = line.append(Component.text("█").color(color));
            }
            lines.add(line);
        }
        return lines;
    }
}
