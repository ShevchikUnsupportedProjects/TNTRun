package tntrun.utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import tntrun.TNTRun;

public class TitleMsg {

	public static String join = "&7[&6TNTRun&7]";
	public static String subjoin = "&6{PLAYER} &7joined";
	public static String win = "&6You won";
	public static String subwin = "&7Congratulations";
	public static String starting = "&7[&6TNTRun&7]";
	public static String substarting = "&7Starting in &6{COUNT}";
	public static String start = "&7[&6TNTRun&7]";
	public static String substart = "&7The Game has started";

	public static void sendFullTitle(Player player, String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime, TNTRun plugin) throws IOException {
		if(plugin.getConfig().getBoolean("special.UseTitle") == false){
			return;
		}
		if(getVersion().equals("v1_7_R4.")){
			Title_1_7_R4 t = new Title_1_7_R4(title.replace("&", "§"), subtitle.replace("&", "§"), fadeInTime, stayTime, fadeOutTime);
			t.setTimingsToTicks();
			t.send(player);
			return;
		}
		Title t = new Title(title.replace("&", "§"), subtitle.replace("&", "§"), fadeInTime, stayTime, fadeOutTime, plugin);
		t.setTimingsToTicks();
		t.send(player);
	}

	public static void loadTitles(TNTRun plugin) {
		File messageconfig = new File(plugin.getDataFolder(), "configtitles.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(messageconfig);
		join = config.getString("join", join);
		subjoin = config.getString("subjoin", subjoin);
		win = config.getString("win", win);
		subwin = config.getString("subwin", subwin);
		starting = config.getString("starting", starting);
		substarting = config.getString("substarting", substarting);
		start = config.getString("start", start);
		substart = config.getString("substart", substart);
		saveTitles(messageconfig);
	}

	private static void saveTitles(File messageconfig) {
		FileConfiguration config = new YamlConfiguration();
		config.set("join", join);
		config.set("subjoin", subjoin);
		config.set("win", win);
		config.set("subwin", subwin);
		config.set("starting", starting);
		config.set("substarting", substarting);
		config.set("start", start);
		config.set("substart", substart);
		try {
			config.save(messageconfig);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getVersion() {
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String version = name.substring(name.lastIndexOf('.') + 1) + ".";
		return version;
	}
}
