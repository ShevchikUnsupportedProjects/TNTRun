package tntrun.commands.setup.arena;

import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.commands.setup.CommandHandlerInterface;
import tntrun.messages.Messages;

public class DisableKits implements CommandHandlerInterface {
	
	private TNTRun plugin;
	public DisableKits(TNTRun plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean handleCommand(Player player, String[] args) {
		Arena arena = plugin.amanager.getArenaByName(args[0]);
		if (arena != null) {
			if (!arena.getStructureManager().isKitsEnabled()) {
				player.sendMessage("§7[§6TNTRun§7] §cKits are already disabled for arena §6" + args[0]);
				return true;
			}
			if (arena.getStatusManager().isArenaEnabled()) {
				player.sendMessage("§7[§6TNTRun§7] §cPlease disable arena §6/trsetup disable " + args[0]);
				return true;
			}
			arena.getStructureManager().enableKits(false);
			player.sendMessage("§7[§6TNTRun§7] §7Kits have been §6disabled §7for arena §6" + args[0]);
			
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
