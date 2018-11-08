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
				list.add("listkits");
				list.add("leaderboard");
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("list")) {
					for (Arena arena : TNTRun.getInstance().amanager.getArenas()) {
						list.add(arena.getArenaName());
					}
				} else if (args[0].equalsIgnoreCase("listkits") || args[0].equalsIgnoreCase("listkit")) {
					list.addAll(TNTRun.getInstance().kitmanager.getKits());
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
