package tntrun.commands.setup.arena;

import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.commands.setup.CommandHandlerInterface;
import tntrun.messages.Messages;

public class SetSpectatorSpawn implements CommandHandlerInterface {

	private TNTRun plugin;
	public SetSpectatorSpawn(TNTRun plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean handleCommand(Player player, String[] args) {
		Arena arena = plugin.amanager.getArenaByName(args[0]);
		if (arena != null) {
			if (arena.getStatusManager().isArenaEnabled()) {
				Messages.sendMessage(player, Messages.trprefix + Messages.arenanotdisabled.replace("{ARENA}", args[0]));
				return true;
			}
			if (arena.getStructureManager().setSpectatorsSpawn(player.getLocation())) {
				Messages.sendMessage(player, Messages.trprefix + "&7 Arena &6" + args[0] + "&7 SpectatorSpawn set to &6X: &7" + Math.round(player.getLocation().getX()) + " &6Y: &7" + Math.round(player.getLocation().getY()) + " &6Z: &7" + Math.round(player.getLocation().getZ()));
			} else {
				Messages.sendMessage(player, Messages.trprefix + "&c Arena &6" + args[0] + "&c SpectatorSpawn must be in arena bounds");
			}
		} else {
			Messages.sendMessage(player, Messages.trprefix + Messages.arenanotexist.replace("{ARENA}", args[0]));
		}
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 1;
	}

}
