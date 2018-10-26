/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

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
		
		if (!sender.hasPermission("tntrun.setup")) {
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
		complex.add("enablekits");
		complex.add("disablekits");
		complex.add("setdamage");
		
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
			} else if (args[0].equalsIgnoreCase("setdamage")) {
				list.add("yes");
				list.add("no");
				list.add("zero");
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
