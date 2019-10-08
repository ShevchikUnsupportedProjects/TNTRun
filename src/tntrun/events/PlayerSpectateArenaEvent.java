package tntrun.events;

import org.bukkit.entity.Player;

public class PlayerSpectateArenaEvent extends TNTRunEvent {

	public PlayerSpectateArenaEvent(Player player, String arenaName) {
		super(player, arenaName);
	}
}
