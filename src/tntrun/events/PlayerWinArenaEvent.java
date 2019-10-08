package tntrun.events;

import org.bukkit.entity.Player;

public class PlayerWinArenaEvent extends TNTRunEvent {

	public PlayerWinArenaEvent(Player player, String arenaName) {
		super(player, arenaName);
	}

}
