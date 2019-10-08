package tntrun.events;

import org.bukkit.entity.Player;

public class PlayerJoinArenaEvent extends TNTRunEvent {

	public PlayerJoinArenaEvent(Player player, String arenaName) {
		super(player, arenaName);
	}
}
