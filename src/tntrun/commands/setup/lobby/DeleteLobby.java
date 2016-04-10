package tntrun.commands.setup.lobby;

import org.bukkit.entity.Player;

import tntrun.commands.setup.CommandHandlerInterface;
import tntrun.lobby.GlobalLobby;

public class DeleteLobby implements CommandHandlerInterface {

	@Override
	public boolean handleCommand(Player player, String[] args) {
		GlobalLobby.getInstance().setLobbyLocation(null);
		player.sendMessage("Lobby deleted");
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 0;
	}

}
