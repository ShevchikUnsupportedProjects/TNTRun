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

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tntrun.commands.setup.arena.AddCommandsRewards;
import tntrun.commands.setup.arena.AddKit;
import tntrun.commands.setup.arena.ClearCommandsRewards;
import tntrun.commands.setup.arena.CreateArena;
import tntrun.commands.setup.arena.DeleteArena;
import tntrun.commands.setup.arena.DeleteKit;
import tntrun.commands.setup.arena.DeleteSpectatorSpawn;
import tntrun.commands.setup.arena.DisableArena;
import tntrun.commands.setup.arena.EnableArena;
import tntrun.commands.setup.arena.FinishArena;
import tntrun.commands.setup.arena.SetArena;
import tntrun.commands.setup.arena.SetCountdown;
import tntrun.commands.setup.arena.SetDamage;
import tntrun.commands.setup.arena.SetGameLevelDestroyDelay;
import tntrun.commands.setup.arena.SetItemsRewards;
import tntrun.commands.setup.arena.SetLoseLevel;
import tntrun.commands.setup.arena.SetMaxPlayers;
import tntrun.commands.setup.arena.SetMinPlayers;
import tntrun.commands.setup.arena.SetMoneyRewards;
import tntrun.commands.setup.arena.SetSpawn;
import tntrun.commands.setup.arena.SetSpectatorSpawn;
import tntrun.commands.setup.arena.SetTeleport;
import tntrun.commands.setup.arena.SetTimeLimit;
import tntrun.commands.setup.arena.SetVotePercent;
import tntrun.commands.setup.lobby.DeleteLobby;
import tntrun.commands.setup.lobby.SetLobby;
import tntrun.commands.setup.reload.ReloadBars;
import tntrun.commands.setup.reload.ReloadMSG;
import tntrun.commands.setup.selection.Clear;
import tntrun.commands.setup.selection.SetP1;
import tntrun.commands.setup.selection.SetP2;
import tntrun.messages.Messages;
import tntrun.selectionget.PlayerSelection;

public class SetupCommandsHandler implements CommandExecutor {

	private final PlayerSelection plselection = new PlayerSelection();

	private final HashMap<String, CommandHandlerInterface> commandHandlers = new HashMap<String, CommandHandlerInterface>();

	public SetupCommandsHandler() {
		commandHandlers.put("setp1", new SetP1(plselection));
		commandHandlers.put("setp2", new SetP2(plselection));
		commandHandlers.put("clear", new Clear(plselection));
		commandHandlers.put("setlobby", new SetLobby());
		commandHandlers.put("deletelobby", new DeleteLobby());
		commandHandlers.put("reloadmsg", new ReloadMSG());
		commandHandlers.put("reloadbars", new ReloadBars());
		commandHandlers.put("create", new CreateArena());
		commandHandlers.put("delete", new DeleteArena());
		commandHandlers.put("setarena", new SetArena(plselection));
		commandHandlers.put("setgameleveldestroydelay", new SetGameLevelDestroyDelay());
		commandHandlers.put("setloselevel", new SetLoseLevel(plselection));
		commandHandlers.put("setspawn", new SetSpawn());
		commandHandlers.put("setspectate", new SetSpectatorSpawn());
		commandHandlers.put("delspectate", new DeleteSpectatorSpawn());
		commandHandlers.put("setmaxplayers", new SetMaxPlayers());
		commandHandlers.put("setminplayers", new SetMinPlayers());
		commandHandlers.put("setvotepercent", new SetVotePercent());
		commandHandlers.put("setcountdown", new SetCountdown());
		commandHandlers.put("setitemsrewards", new SetItemsRewards());
		commandHandlers.put("setmoneyrewards", new SetMoneyRewards());
		commandHandlers.put("addcommandrewards", new AddCommandsRewards());
		commandHandlers.put("clearcommandrewards", new ClearCommandsRewards());
		commandHandlers.put("addkit", new AddKit());
		commandHandlers.put("deleteKit", new DeleteKit());
		commandHandlers.put("settimelimit", new SetTimeLimit());
		commandHandlers.put("setteleport", new SetTeleport());
		commandHandlers.put("setdamage", new SetDamage());
		commandHandlers.put("finish", new FinishArena());
		commandHandlers.put("disable", new DisableArena());
		commandHandlers.put("enable", new EnableArena());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Player is expected");
			return true;
		}
		Player player = (Player) sender;
		// check permissions
		if (!player.hasPermission("tntrun.setup")) {
			Messages.sendMessage(player, Messages.nopermission);
			return true;
		}
		// get command
		if (args.length > 0 && commandHandlers.containsKey(args[0])) {
			CommandHandlerInterface commandh = commandHandlers.get(args[0]);
			//check args length
			if (args.length - 1 < commandh.getMinArgsLength()) {
				Messages.sendMessage(player, ChatColor.RED+"Not enough args");
				return false;
			}
			//execute command
			boolean result = commandh.handleCommand(player, Arrays.copyOfRange(args, 1, args.length));
			return result;
		}
		return false;
	}

}
