package xyz.alugor.tempfly.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.alugor.tempfly.TempFlyPlugin;
import xyz.alugor.tempfly.database.service.TempFlyService;

public class PlayerJoinListener implements Listener {
    public PlayerJoinListener(TempFlyPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        TempFlyService service = TempFlyPlugin.getService();

        //checking if the player has a database entry
        service.getTempFlyByUUID(player.getUniqueId()).ifPresent(tempFly -> {
            if (tempFly.getStatus()) {
                player.setAllowFlight(true);
            }
        });
    }
}