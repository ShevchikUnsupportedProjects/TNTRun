package tntrun.events;

import org.bukkit.entity.Player;

public class PlayerLeaveArenaEvent extends TNTRunEvent {

	public PlayerLeaveArenaEvent(Player player, String arenaName) {
		super(player, arenaName);
	}

}
