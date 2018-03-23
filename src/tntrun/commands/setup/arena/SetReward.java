package tntrun.commands.setup.arena;

import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.commands.setup.CommandHandlerInterface;
import tntrun.conversation.ConversationType;
import tntrun.conversation.TNTRunConversation;

public class SetReward implements CommandHandlerInterface {
	
	private TNTRun plugin;
	public SetReward(TNTRun plugin) {
		this.plugin = plugin;
	}
	@Override
	public boolean handleCommand(Player player, String[] args) {
		Arena arena = plugin.amanager.getArenaByName(args[0]);
		if (arena != null) {
			if (arena.getStatusManager().isArenaEnabled()) {
				player.sendMessage("§7[§6TNTRun§7] §cPlease disable arena §6/trsetup disable " + args[0]);
				return true;
			}
			// start prize conversation
			new TNTRunConversation(plugin, player, arena, ConversationType.ARENAPRIZE).begin();
			
			//arena.getStructureManager().getRewards().setItemsReward(player.getInventory().getContents());
			player.sendMessage("§7[§6TNTRun§7] §7Arena §6" + args[0] + "§7 set reward");
		} else {
			player.sendMessage("§7[§6TNTRun§7] §cArena §6" + args[0] + "§c doesn't exist");
		}
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 1;
	}

}
