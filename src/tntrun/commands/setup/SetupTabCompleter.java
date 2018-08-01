package tntrun.commands.setup;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.arena.Arena;

public class SetupTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		
		List<String> list = new ArrayList<String>();
		List<String> auto = new ArrayList<String>();
		List<String> complex = new ArrayList<String>();
		
		complex.add("setarena");
		complex.add("setloselevel");
		complex.add("setspawn");
		complex.add("setspectate");
		complex.add("finish");
		complex.add("delspectate");
		complex.add("setgameleveldestroydelay");
		complex.add("setmaxplayers");
		complex.add("setminplayers");
		complex.add("setvotepercent");
		complex.add("settimelimit");
		complex.add("setcountdown");
		complex.add("setmoneyreward");
		complex.add("setteleport");
		complex.add("enable");
		complex.add("disable");
		complex.add("delete");
		complex.add("setreward");
		
		if (args.length == 1) {
			list.add("help");
			list.add("create");  //because it doesn't take an existing arena name
			list.add("setlobby");
			list.add("reloadbars");
			list.add("reloadtitles");
			list.add("reloadmsg");
			list.add("reloadconfig");
			
			list.addAll(complex);
			
		} else if (args.length == 2) {
			if (complex.contains(args[0])) {
				for (Arena arena : TNTRun.getInstance().amanager.getArenas()) {
					list.add(arena.getArenaName());
				}
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("setteleport")) {
				list.add("lobby");
				list.add("previous");
			}
		}
		for (String s : list) {
			if (s.startsWith(args[args.length - 1])) {
				auto.add(s);
			}
		}
		
		return auto.isEmpty() ? list : auto;
	}

}
