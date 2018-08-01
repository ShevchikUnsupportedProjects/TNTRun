package tntrun.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.arena.Arena;

public class AutoTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (cmd.getName().equalsIgnoreCase("tntrun") || cmd.getName().equalsIgnoreCase("tr")) {
			if (!(sender instanceof Player)) {
				return null;
			}
			
			List<String> list = new ArrayList<String>();
			List<String> auto = new ArrayList<String>();
			
			if (args.length == 1) {
				list.add("help");
				list.add("lobby");
				list.add("list");
				list.add("join");
				list.add("leave");
				list.add("vote");
				list.add("cmds");
				list.add("info");
				list.add("stats");
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("list")) {
					for (Arena arena : TNTRun.getInstance().amanager.getArenas()) {
						list.add(arena.getArenaName());
					}
				}
			}
			for (String s : list) {
				if (s.startsWith(args[args.length - 1])) {
					auto.add(s);
				}
			}
			return auto.isEmpty() ? list : auto;
			
		}
		return null;
	}

}
