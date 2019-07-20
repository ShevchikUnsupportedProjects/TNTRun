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

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tntrun.TNTRun;
import tntrun.messages.Messages;

public class Stats {

	private TNTRun plugin;
	private File file;
	private int position;

	private static HashMap<String, Integer> pmap = new HashMap<String, Integer>();
	private static HashMap<String, Integer> wmap = new HashMap<String, Integer>();

	public Stats(TNTRun plugin) {
		this.plugin = plugin;
		file = new File(plugin.getDataFolder(), "stats.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		loadStats();
	}

	private void loadStats() {
		if (plugin.isFile()) {
			getStatsFromFile();
			return;
		}
		getWinsFromDB();
		getPlayedFromDB();
	}

	public void addPlayedGames(Player player, int value) {
		String uuid = player.getUniqueId().toString();
		if (pmap.containsKey(uuid)) {
			pmap.put(uuid, pmap.get(uuid) + value);
			return;
		}
		pmap.put(uuid, value);
	}

	public void addWins(Player player, int value) {
		String uuid = player.getUniqueId().toString();
		if (wmap.containsKey(uuid)) {
			wmap.put(uuid, wmap.get(uuid) + value);
			return;
		}
		wmap.put(uuid, value);
	}

	public int getLosses(Player player) {
		return getPlayedGames(player) - getWins(player);
	}

	public int getPlayedGames(OfflinePlayer player) {
		String uuid = player.getUniqueId().toString();
		return pmap.containsKey(uuid) ? pmap.get(uuid) : 0;
	}

	public int getWins(OfflinePlayer player) {
		String uuid = player.getUniqueId().toString();
		return wmap.containsKey(uuid) ? wmap.get(uuid) : 0;
	}

    public void getLeaderboard(CommandSender sender, int entries) {
    	position = 0;
    	wmap.entrySet().stream()
    		.sorted(Entry.comparingByValue(Comparator.reverseOrder()))
    		.limit(entries)
    		.forEach(e -> {position++;
    			String player = Bukkit.getOfflinePlayer(UUID.fromString(e.getKey())).getName();
    			if (!Bukkit.getOnlineMode()) {
    				player = e.getKey();
    			}
    			Messages.sendMessage(sender, Messages.leaderboard
    					   .replace("{POSITION}", String.valueOf(position))
    					   .replace("{PLAYER}", player)
    					   .replace("{WINS}", String.valueOf(e.getValue())));
    		});	   
    	return; 
    }

    private static boolean isValidUuid(String uuid) {
    	try {
			UUID.fromString(uuid);
		} catch (IllegalArgumentException ex){
			return false;
		}
    	return true;
    }

    public void getStatsFromFile() {
    	FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		ConfigurationSection stats = config.getConfigurationSection("stats");

		if (stats != null) {
			if (Bukkit.getOnlineMode()) {
				for (String uuid : stats.getKeys(false)) {
					// validate UUID as file could contain player names if its been in offline mode
					if (!isValidUuid(uuid)) {
						continue;
					}
					wmap.put(uuid, config.getInt("stats." + uuid + ".wins", 0));
					pmap.put(uuid, config.getInt("stats." + uuid + ".played", 0));
				}
			} else {
				for (String playerName : stats.getKeys(false)) {
					if (isValidUuid(playerName)) {
						continue;
					}
					wmap.put(playerName, config.getInt("stats." + playerName + ".wins", 0));
					pmap.put(playerName, config.getInt("stats." + playerName + ".played", 0));
				}
			}
		}
    }

    public void getWinsFromDB() {
    	try {
            ResultSet rs;

            rs = plugin.mysql.query("SELECT * FROM `stats` ORDER BY wins DESC LIMIT ").getResultSet();

            while (rs.next()) {
            	String playerName = rs.getString("username");

            	// check if valid uuid
            	if (Bukkit.getOnlineMode()) {
            		if (!isValidUuid(playerName)) {
            			continue;
            		}
            	} else if (isValidUuid(playerName)) {
            		continue;
            	}
            	wmap.put(playerName, rs.getInt("wins"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void getPlayedFromDB() {
    	try {
            ResultSet rs;

            rs = plugin.mysql.query("SELECT * FROM `stats` ORDER BY played DESC LIMIT ").getResultSet();

            while (rs.next()) {
            	String playerName = rs.getString("username");

            	// check if valid uuid
            	if (Bukkit.getOnlineMode()) {
            		if (!isValidUuid(playerName)) {
            			continue;
            		}
            	} else if (isValidUuid(playerName)) {
            		continue;
            	}
            	pmap.put(playerName, rs.getInt("played"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public HashMap<String, Integer> getWinMap() {
    	return wmap;
    }

    public void saveStats() {
    	if (plugin.isFile()) {
			saveStatsToFile();
			return;
		}
		saveStatsToDB();
    }

    private void saveStatsToFile() {
    	FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    	for (String name : pmap.keySet()) {
    		config.set("stats." + name + ".played", pmap.get(name));
    	}
    	for (String name : wmap.keySet()) {
    		config.set("stats." + name + ".wins", pmap.get(name));
    	}	
    }

    private void saveStatsToDB() {
    	for (String player : pmap.keySet()) {
    		updateDB("played", player, pmap.get(player));
    	}
    	for (String player : wmap.keySet()) {
    		updateDB("wins", player, wmap.get(player));
    	}
    }

    private void updateDB(String statname, String player, Integer value) {
    	new BukkitRunnable() {
        	@Override
        	public void run() {
        		if (Bukkit.getOnlineMode()) {
                    plugin.mysql.query("UPDATE `stats` SET `" + statname
                            + "`='" + value + "' WHERE `username`='" + player + "';");
                } else {
                    plugin.mysql.query("UPDATE `stats` SET `" + statname
                            + "`='" + value + "' WHERE `username`='" + player + "';");
                }
        	}
        }.runTaskAsynchronously(plugin);
    }
}
