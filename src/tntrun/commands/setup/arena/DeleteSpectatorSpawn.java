package tntrun.commands.setup.arena;

import org.bukkit.entity.Player;

import tntrun.arena.Arena;
import tntrun.commands.setup.CommandHandlerInterface;
import tntrun.datahandler.ArenasManager;

public class DeleteSpectatorSpawn implements CommandHandlerInterface {

	@Override
	public boolean handleCommand(Player player, String[] args) {
		Arena arena = ArenasManager.getInstance().getArenaByName(args[0]);
		if (arena != null) {
			if (arena.getStatusManager().isArenaEnabled()) {
				player.sendMessage("Disable arena first");
				return true;
			}
			arena.getStructureManager().removeSpectatorsSpawn();
			player.sendMessage("Spectator spawn deleted");
		} else {
			player.sendMessage("Arena does not exist");
		}
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 1;
	}

}
