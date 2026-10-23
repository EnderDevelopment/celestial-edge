package com.enderdevelopment.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 10000; // 10 seconds

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        createLegendaryItemRecipe();
    }

    private void createLegendaryItemRecipe() {
        ItemStack legendaryItem = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = legendaryItem.getItemMeta();
        meta.setDisplayName("§6Legendary Sword");
        meta.setUnbreakable(true);
        legendaryItem.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, "legendary_sword");
        ShapedRecipe recipe = new ShapedRecipe(key, legendaryItem);
        recipe.shape(" D ", " D ", " S ");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('S', Material.STICK);
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.sendMessage("§6Welcome! Use /givelegendary to get the Legendary Sword.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == Material.NETHERITE_SWORD && item.getItemMeta().getDisplayName().equals("§6Legendary Sword")) {
            if (event.getAction().toString().contains("RIGHT_CLICK") && player.isSneaking()) {
                if (isOnCooldown(player)) {
                    player.sendMessage("§cAbility is on cooldown!");
                    return;
                }

                activateSkyLaser(player);
                setCooldown(player);
            }
        }
    }

    private void activateSkyLaser(Player player) {
        player.sendMessage("§6Activating Sky Laser!");
        player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation(), 100, 0.5, 0.5, 0.5, 0.1);

        new BukkitRunnable() {
            int duration = 0;

            @Override
            public void run() {
                if (duration >= 80) { // 4 seconds (20 ticks per second)
                    cancel();
                    return;
                }

                Vector direction = player.getLocation().getDirection().normalize();
                for (double i = 0; i <= 10; i += 0.5) {
                    player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation().add(direction.clone().multiply(i)), 1, 0, 0, 0, 0);
                }

                for (Entity entity : player.getNearbyEntities(10, 10, 10)) {
                    if (entity instanceof Player) {
                        Player target = (Player) entity;
                        target.damage(1.0); // 1 heart per second
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 1));
                    }
                }

                duration++;
            }
        }.runTaskTimer(this, 0L, 1L); // Run every tick
    }

    private boolean isOnCooldown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            long lastUse = cooldowns.get(player.getUniqueId());
            return System.currentTimeMillis() - lastUse < COOLDOWN_TIME;
        }
        return false;
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.NETHERITE_SWORD && item.getItemMeta().getDisplayName().equals("§6Legendary Sword")) {
            event.setCancelled(true); // Prevent durability loss
        }
    }
}