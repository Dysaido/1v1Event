package xyz.dysaido.onevsonegame.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import xyz.dysaido.onevsonegame.OneVSOneGame;
import xyz.dysaido.onevsonegame.match.model.MatchPlayer;
import xyz.dysaido.onevsonegame.match.model.PlayerState;

public class MatchListener implements Listener {

    private final OneVSOneGame plugin;

    public MatchListener(OneVSOneGame plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getMatchManager().getMatch().ifPresent(match -> match.leave(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        plugin.getMatchManager().getMatch().ifPresent(match -> match.leave(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        plugin.getMatchManager().getMatch().ifPresent(match -> {
            MatchPlayer matchPlayer = match.getQueue().findByPlayer(victim);
            if (matchPlayer != null) {
                matchPlayer.setLose(true);
                matchPlayer.reset(match.getRing().getLobby());
            }
        });
    }

    @EventHandler
    public void onPlayerInventory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        plugin.getMatchManager().getMatch().ifPresent(match -> {
            MatchPlayer matchPlayer = match.getQueue().findByPlayer(player);
            if (matchPlayer != null && (matchPlayer.getState() == PlayerState.QUEUE || matchPlayer.getState() == PlayerState.SPECTATOR)) {
                event.setCancelled(true);
            }
        });
    }

}
