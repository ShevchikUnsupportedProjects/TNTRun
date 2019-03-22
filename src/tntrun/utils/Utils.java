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

package tntrun.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.messages.Messages;

public class Utils {
	
	public static boolean isNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {}
        return false;
    }
	
	public static boolean isDouble(String text) {
		try {
			Double.parseDouble(text);
			return true;
		} catch (NumberFormatException e) {}
		return false;
	}
	
	public static int playerCount() {
		int pCount = 0;
		for (Arena arena : TNTRun.getInstance().amanager.getArenas()) {
			pCount += arena.getPlayersManager().getPlayersCount();			
		}
		return pCount;
	}
	
	public static void displayInfo(CommandSender sender) {
		Messages.sendMessage(sender, "&7============" + Messages.trprefix + "============");
		Messages.sendMessage(sender, "&bPlugin Version: &f" + TNTRun.getInstance().getDescription().getVersion());
		Messages.sendMessage(sender, "&bWebsite: &fhttps://www.spigotmc.org/resources/tntrun_reloaded.53359/");
		Messages.sendMessage(sender, "&bTNTRun_reloaded Author: &fsteve4744");
	}
	
	public static void displayUpdate(Player player) {
		if (player.hasPermission("tntrun.version.check")) {
			Messages.sendMessage(player, Messages.trprefix + "&6New update available!");
			Messages.sendMessage(player, Messages.trprefix + "Your version: &6" + TNTRun.getInstance().getDescription().getVersion());
			Messages.sendMessage(player, Messages.trprefix + "New version : &6" + TNTRun.getInstance().version[0]);
			Messages.sendMessage(player, Messages.trprefix + "New version available! Download now: &6https://www.spigotmc.org/resources/tntrun_reloaded.53359/");
		}
	}
	
	public static String getTitleCase(String input) {
		return input.substring(0,1).toUpperCase() + input.substring(1).toLowerCase();
	}

}
