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

package tntrun.messages;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import tntrun.FormattingCodesParser;
import tntrun.TNTRun;

public class Messages {

	public static String nopermission = "&4You don't have permission to do this";

	public static String teleporttolobby = "&6Teleported to lobby";

	public static String availablearenas = "&6Available arenas:&r ";
	public static String arenawolrdna = "&6Arena world is not loaded";
	public static String arenadisabled = "&6Arena is disabled";
	public static String arenarunning = "&6Arena already running";
	public static String arenaregenerating = "&6Arena is regenerating";
	public static String arenavehicle = "&6You can't join the game while sitting inside vehicle";
	public static String arenadisabling = "&6Arena is disabling";

	public static String playerscountinarena = "&6Current number of players in arena: {COUNT}";
	public static String limitreached = "&6Slot limit reached.";
	public static String playerjoinedtoplayer = "&6You joined the arena";
	public static String playerjoinedtoothers = "&6Player {PLAYER} joined the arena";
	public static String playerlefttoplayer = "&6You left the arena";
	public static String playerlefttoothers = "&6Player {PLAYER} left the game";
	public static String playervotedforstart = "&6You voted for game start";
	public static String playeralreadyvotedforstart = "&6You already voted";
	public static String arenastarted = "&6Arena started. Time limit is {TIMELIMIT} seconds";
	public static String arenacountdown = "&6Arena starts in {COUNTDOWN} seconds";
	public static String arenatimeout = "&6Time is out.";
	public static String playerwontoplayer = "&6You won the game";
	public static String playerlosttoplayer = "&6You lost the game";
	public static String playerlosttoothers = "&6Player {PLAYER} lost the game";
	public static String playerwonbroadcast = "&9[TNTRun] &a{PLAYER}&r won the game on arena &c{ARENA}";
	public static String playerrewardmessage = "&6You have been rewarded: {REWARD}";

	public static void sendMessage(Player player, String message) {
		if (!message.equals("")) {
			player.sendMessage(FormattingCodesParser.parseFormattingCodes(message));
		}
	}

	public static void broadcastMessage(String message) {
		if (!message.equals("")) {
			Bukkit.broadcastMessage(FormattingCodesParser.parseFormattingCodes(message));
		}
	}

	public static void loadMessages(TNTRun plugin) {
		File messageconfig = new File(plugin.getDataFolder(), "configmsg.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(messageconfig);
		nopermission = config.getString("nopermission", nopermission);
		teleporttolobby = config.getString("teleporttolobby", teleporttolobby);
		availablearenas = config.getString("availablearenas", availablearenas);
		arenawolrdna = config.getString("arenawolrdna", arenawolrdna);
		arenadisabled = config.getString("arenadisabled", arenadisabled);
		arenarunning = config.getString("arenarunning", arenarunning);
		arenaregenerating = config.getString("arenaregenerating", arenaregenerating);
		arenavehicle = config.getString("arenavehicle", arenavehicle);
		arenadisabling = config.getString("arenadisabling", arenadisabling);
		playerscountinarena = config.getString("playerscountinarena", playerscountinarena);
		limitreached = config.getString("limitreached", limitreached);
		playerjoinedtoplayer = config.getString("playerjoinedtoplayer", playerjoinedtoplayer);
		playerjoinedtoothers = config.getString("playerjoinedtoothers", playerjoinedtoothers);
		playerlefttoplayer = config.getString("playerlefttoplayer", playerlefttoplayer);
		playerlefttoothers = config.getString("playerlefttoothers", playerlefttoothers);
		playervotedforstart = config.getString("playervotedforstart", playervotedforstart);
		playeralreadyvotedforstart = config.getString("playeralreadyvotedforstart", playeralreadyvotedforstart);
		arenastarted = config.getString("arenastarted", arenastarted);
		arenacountdown = config.getString("arenacountdown", arenacountdown);
		arenatimeout = config.getString("arenatimeout", arenatimeout);
		playerwontoplayer = config.getString("playerwontoplayer", playerwontoplayer);
		playerlosttoplayer = config.getString("playerlosttoplayer", playerlosttoplayer);
		playerlosttoothers = config.getString("playerlosttoothers", playerlosttoothers);
		playerwonbroadcast = config.getString("playerwonbroadcast", playerwonbroadcast);
		playerrewardmessage = config.getString("playerrewardmessage", playerrewardmessage);
		saveMessages(messageconfig);
	}

	private static void saveMessages(File messageconfig) {
		FileConfiguration config = new YamlConfiguration();
		config.set("nopermission", nopermission);
		config.set("teleporttolobby", teleporttolobby);
		config.set("availablearenas", availablearenas);
		config.set("arenawolrdna", arenawolrdna);
		config.set("arenadisabled", arenadisabled);
		config.set("arenarunning", arenarunning);
		config.set("arenaregenerating", arenaregenerating);
		config.set("arenavehicle", arenavehicle);
		config.set("arenadisabling", arenadisabling);
		config.set("playerscountinarena", playerscountinarena);
		config.set("limitreached", limitreached);
		config.set("playerjoinedtoplayer", playerjoinedtoplayer);
		config.set("playerjoinedtoothers", playerjoinedtoothers);
		config.set("playerlefttoplayer", playerlefttoplayer);
		config.set("playerlefttoothers", playerlefttoothers);
		config.set("playervotedforstart", playervotedforstart);
		config.set("playeralreadyvotedforstart", playeralreadyvotedforstart);
		config.set("arenastarted", arenastarted);
		config.set("arenacountdown", arenacountdown);
		config.set("arenatimeout", arenatimeout);
		config.set("playerwontoplayer", playerwontoplayer);
		config.set("playerlosttoplayer", playerlosttoplayer);
		config.set("playerlosttoothers", playerlosttoothers);
		config.set("playerwonbroadcast", playerwonbroadcast);
		config.set("playerrewardmessage", playerrewardmessage);
		try {
			config.save(messageconfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
