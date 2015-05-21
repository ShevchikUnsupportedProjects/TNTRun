package tntrun.commands.setup.lobby;

import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.commands.setup.CommandHandlerInterface;

public class DeleteLobby implements CommandHandlerInterface {

	private TNTRun plugin;
	public DeleteLobby(TNTRun plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean handleCommand(Player player, String[] args) {
		plugin.globallobby.setLobbyLocation(null);
		player.sendMessage("Lobby deleted");
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 0;
	}

}
