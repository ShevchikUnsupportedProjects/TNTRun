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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.messages.Messages;
import tntrun.utils.Stats;

public class GameCommands implements CommandExecutor {

	private TNTRun plugin;

	public GameCommands(TNTRun plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§7[§6TNTRun§7] §cYou must be player");
			return true;
		}
		Player player = (Player) sender;
		if (args.length < 1){
			sender.sendMessage("§7============[§6TNTRun§7]§7============");
			sender.sendMessage("§7[§6TNTRun§7] §cPlease use §6/tr help");
			return true;
		}
		// help command
		if (args[0].equalsIgnoreCase("help")) {
			sender.sendMessage("§7============[§6TNTRun§7]§7============");
			sender.sendMessage("§6/tr lobby §f- §cTeleport to lobby");
			sender.sendMessage("§6/tr list §f- §cList all arenas");
			sender.sendMessage("§6/tr join {arena} §f- §cJoin arena");
			sender.sendMessage("§6/tr leave §f- §cLeave current arena");
			sender.sendMessage("§6/tr vote §f- §cVote for current arena");
			sender.sendMessage("§6/tr cmds §f- §cView all commands");
			sender.sendMessage("§6/tr info §f- §cPlugin info");
			sender.sendMessage("§6/tr stats §f- §cStats");
			return true;
		} else if (args[0].equalsIgnoreCase("lobby")) {
			if (plugin.globallobby.isLobbyLocationSet()) {
				if (plugin.globallobby.isLobbyLocationWorldAvailable()) {
					player.teleport(plugin.globallobby.getLobbyLocation());
					Messages.sendMessage(player, Messages.teleporttolobby);
				} else {
					player.sendMessage("§7[§6TNTRun§7] §cLobby world is unloaded or doesn't exist");
				}
			} else {
				sender.sendMessage("§7[§6TNTRun§7] §cLobby isn't set");

			}
			return true;
		}
		// list arenas
		else if (args[0].equalsIgnoreCase("list")) {
			StringBuilder message = new StringBuilder(200);
			message.append(Messages.availablearenas);
			for (Arena arena : plugin.amanager.getArenas()) {
				if (arena.getStatusManager().isArenaEnabled()) {
					message.append("&a" + arena.getArenaName() + " ; ");
				} else {
					message.append("&c" + arena.getArenaName() + " ; ");
				}
			}
			Messages.sendMessage(player, message.toString());
			return true;
		}
		// join arena
		else if (args[0].equalsIgnoreCase("join")) {
			if (args.length != 2) {
				player.sendMessage("§7[§6TNTRun§7] §cInvalid number of arguments supplied");
				return false;
			}
			Arena arena = plugin.amanager.getArenaByName(args[1]);
			if (arena != null) {
				boolean canJoin = arena.getPlayerHandler().checkJoin(player);
				if (canJoin) {
					arena.getPlayerHandler().spawnPlayer(player, Messages.playerjoinedtoplayer, Messages.playerjoinedtoothers);
				}
				return true;
			} else {
				sender.sendMessage("§7[§6TNTRun§7] §cArena §6" + args[1] + "§c doesn't exist");
				return true;
			}
		}
		// tntrun info
		else if (args[0].equalsIgnoreCase("info")) {
			sender.sendMessage("§7============[§6TNTRun§7]§7============");
			sender.sendMessage("§cVersion of plugin> §6" + plugin.getDescription().getVersion());
			sender.sendMessage("§cWebsite> §6http://www.spigotmc.org/resources/tntrun.xxxx/");
			sender.sendMessage("§cOriginal Author> §6Shevchikden");
			sender.sendMessage("§cCurrent Author> §6steve4744");
			sender.sendMessage("§7============[§6TNTRun§7]§7============");
		}
		// player stats
		else if (args[0].equalsIgnoreCase("stats")) {
			player.sendMessage("§7============[§6TNTRun§7]§7============");
			if(!plugin.usestats){
				player.sendMessage("§cStats are disabled");
				return true;
			}
			player.sendMessage("§7Played games: §6" + Stats.getPlayedGames(player));
			player.sendMessage("§7Wins: §6" + Stats.getWins(player));
			player.sendMessage("§7Losses: §6" + Stats.getLooses(player));
		}
		// leave arena
		else if (args[0].equalsIgnoreCase("leave")) {
			Arena arena = plugin.amanager.getPlayerArena(player.getName());
			if (arena != null) {
				arena.getPlayerHandler().leavePlayer(player, Messages.playerlefttoplayer, Messages.playerlefttoothers);
				return true;
			} else {
				sender.sendMessage("§7[§6TNTRun§7] §cYou are not in an arena");
				return true;
			}
		}
		// all commands
		else if (args[0].equalsIgnoreCase("cmds")) {
			sender.sendMessage("§7============[§6TNTRun§7]============");
			sender.sendMessage("§6/trsetup setlobby §f- §cSet lobby at your current location");
			sender.sendMessage("§6/trsetup create {arena} §f- §cCreate new Arena");
			sender.sendMessage("§6/trsetup setarena {arena} §f- §cSet bounds for arena");
			sender.sendMessage("§6/trsetup setloselevel {arena} §f- §cSet looselevel bounds for arena");
			sender.sendMessage("§6/trsetup setspawn {arena} §f- §cSet spawn for players at your current location");
			sender.sendMessage("§6/trsetup setspectate {arena} §f- §cSet spectators spawn");
			sender.sendMessage("§6/trsetup finish {arena} §f- §cFinish arena and save it to config file");
			sender.sendMessage("§7============[§6Other commands§7]============");
			sender.sendMessage("§6/trsetup delspectate {arena} §f- §cDelete spectators spawn");
			sender.sendMessage("§6/trsetup setgameleveldestroydelay {arena} {ticks} §f- §cSet a delay for removing blocks when player steps on it");
			sender.sendMessage("§6/trsetup setmaxplayers {arena} {players} §f- §cSet a max players for arena");
			sender.sendMessage("§6/trsetup setminplayers {arena} {players} §f- §cSet a min players for arena");
			sender.sendMessage("§6/trsetup setvotepercent {arena} {0<votepercent<1} §f- §cSet a vote percentage for arena  (Default: 0.75)");
			sender.sendMessage("§6/trsetup settimelimit {arena} {seconds} §f- §cSet a limit for arena");
			sender.sendMessage("§6/trsetup setcountdown {arena} {seconds} §f- §cSet a countdown for arena");
			sender.sendMessage("§6/trsetup setitemsrewards {arena} §f- §cSet a everithing to reward (Item)");
			sender.sendMessage("§6/trsetup setmoneyrewards {arena} {money} §f- §cSet a money reward for winning player");
			sender.sendMessage("§6/trsetup setteleport {arena} {previous/lobby} §f- §cSet teleport when you lose or win in arena");
			sender.sendMessage("§6/trsetup setdamage {arena} {on/off/zero} §f- §cSet a pvp for arena");
			sender.sendMessage("§6/trsetup reloadbars §f- §cReload Bar messages");
			sender.sendMessage("§6/trsetup reloadtitles §f- §cReload Title messages");
			sender.sendMessage("§6/trsetup reloadmsg §f- §cReload arena messages");
			sender.sendMessage("§6/trsetup reloadconfig §f- §cReload config file");
			sender.sendMessage("§6/trsetup enable {arena} §f- §cEnable Arena");
			sender.sendMessage("§6/trsetup disable {arena} §f- §cDisable Arena");
			sender.sendMessage("§6/trsetup delete {arena} §f- §cDelete Arena");
		}
		// vote
		else if (args[0].equalsIgnoreCase("vote")) {
			Arena arena = plugin.amanager.getPlayerArena(player.getName());
			if (arena != null) {
				if (arena.getPlayerHandler().vote(player)) {
					Messages.sendMessage(player, Messages.playervotedforstart);
				} else {
					Messages.sendMessage(player, Messages.playeralreadyvotedforstart);
				}
				return true;
			} else {
				sender.sendMessage("§7[§6TNTRun§7] §cYou are not in arena");
				return true;
			}
		} 
		else {
			sender.sendMessage("§7[§6TNTRun§7] §cInvalid argument supplied, please use §6/tr help");
			return true;
		}	
		return false;
	}

}
