package com.itemsadder_topminions.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class SkullUtil {

    public static ItemStack getCustomSkull(String base64Texture, String displayName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta == null) return skull;

        try {
            // Decode the base64 texture to get the URL
            String decoded = new String(Base64.getDecoder().decode(base64Texture));
            String urlPart = decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length());
            URL skinUrl = new URI(urlPart).toURL();

            // Create profile and set the skin
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(skinUrl);
            profile.setTextures(textures);

            skullMeta.setOwnerProfile(profile);
        } catch (Exception e) {
            Bukkit.getLogger().warning("[SkullUtil] Failed to apply skin: " + e.getMessage());
        }

        skullMeta.setDisplayName(displayName);
        skull.setItemMeta(skullMeta);
        return skull;
    }
}
