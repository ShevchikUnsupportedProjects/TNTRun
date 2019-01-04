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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import tntrun.TNTRun;
import tntrun.messages.Messages;

public class Stats {
	
	public static TNTRun pl;
	public static File file;
	private static int position;
	
	private static HashMap<String, Integer> playedmap = new HashMap<String, Integer>();
	private static HashMap<String, Integer> winmap = new HashMap<String, Integer>();

	public Stats(TNTRun plugin) {
		pl = plugin;
		file = new File(pl.getDataFolder(), "stats.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void addPlayedGames(Player player, int value) {
		if (pl.isFile()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			if (Bukkit.getOnlineMode()) {
				if (config.get("stats." + player.getUniqueId().toString() + ".played") == null) {
					config.set("stats." + player.getUniqueId().toString() + ".played", value);
				} else {
					config.set("stats." + player.getUniqueId().toString() + ".played", config.getInt("stats." + player.getUniqueId().toString() + ".played") + value);
				}
			} else {
				if (config.get("stats." + player.getName() + ".played") == null) {
					config.set("stats." + player.getName() + ".played", value);
				} else {
					config.set("stats." + player.getName(), config.getInt("stats." + player.getName() + ".played") + value);
				}
			}
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			setValue("played",  player,  getStat("played", player) + 1);	
		}
	}
	
	public static void addWins(Player player, int value) {
		if (pl.isFile()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			if (Bukkit.getOnlineMode()) {
				if (config.get("stats." + player.getUniqueId().toString() + ".wins") == null) {
					config.set("stats." + player.getUniqueId().toString() + ".wins", value);
				} else {
					config.set("stats." + player.getUniqueId().toString() + ".wins", config.getInt("stats." + player.getUniqueId().toString() + ".wins") + value);
				}
			} else {
				if (config.get("stats." + player.getName() + ".wins") == null) {
					config.set("stats." + player.getName() + ".wins", value);
				} else {
					config.set("stats." + player.getName(), config.getInt("stats." + player.getName() + ".wins") + value);
				}
			}
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			setValue("wins",  player,  getStat("wins", player) + 1);	
		}
	}
	
	public static int getLosses(Player player) {
		return getPlayedGames(player) - getWins(player);
	}
	
	public static int getWins(OfflinePlayer offlinePlayer) {
		if (pl.isFile()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			if (Bukkit.getOnlineMode()) {
				if (config.get("stats." + offlinePlayer.getUniqueId().toString() + ".wins") == null) {
					return 0;
				} else {
					return config.getInt("stats." + offlinePlayer.getUniqueId().toString() + ".wins");
				}
			} else {
				if (config.get("stats." + offlinePlayer.getName() + ".wins") == null) {
					return 0;
				} else {
					return config.getInt("stats." + offlinePlayer.getName() + ".wins");
				}
			}
		}
		return getStat("wins", offlinePlayer);
	}
	
	public static int getPlayedGames(Player player) {
		if (pl.isFile()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			if (Bukkit.getOnlineMode()) {
				if (config.get("stats." + player.getUniqueId().toString() + ".played") == null) {
					return 0;
				} else {
					return config.getInt("stats." + player.getUniqueId().toString() + ".played");
				}
			} else {
				if (config.get("stats." + player.getName() + ".played") == null) {
					return 0;
				} else {
					return config.getInt("stats." + player.getName() + ".played");
				}
			}
		}
		return getStat("played", player);
	}
	
    private static int getStat(String statname, OfflinePlayer offlinePlayer) {
    	if (statname.equals("played")) {
    		if (playedmap.containsKey(offlinePlayer.getName())) {
    			return playedmap.get(offlinePlayer.getName());
    		}
    	}
    	if (statname.equals("wins")) {
    		if (winmap.containsKey(offlinePlayer.getName())) {
    			return winmap.get(offlinePlayer.getName());
    		}
    	}

        try {
            int stat = 0;
            ResultSet rs;
            
            if (Bukkit.getOnlineMode()) {
            	rs = pl.mysql.query("SELECT * FROM `stats` WHERE `username`='" + offlinePlayer.getUniqueId().toString() + "'").getResultSet();
            } else {
            	rs = pl.mysql.query("SELECT * FROM `stats` WHERE `username`='" + offlinePlayer.getName() + "'").getResultSet();
            }
            
            while (rs.next()) {
               	stat = rs.getInt(statname);
            }

            return stat;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 999;
	}

    public static void cacheDBStats(Player player) {
    	if (!playedmap.containsKey(player.getName())) {
    		playedmap.put(player.getName(), getStat("played", player));
    	}
    	if (!winmap.containsKey(player.getName())) {
    		winmap.put(player.getName(), getStat("wins", player));
    	}
    }
 
    private static void setValue(String statname, Player p, int value) {    
        if (!pl.useStats()) {
        	return;
        }
        if (statname.equals("played")) {
        	playedmap.put(p.getName(), value);
        }
        if (statname.equals("wins")) {
        	winmap.put(p.getName(), value);
        }
        new BukkitRunnable() {
        	@Override
        	public void run() {
        		if (Bukkit.getOnlineMode()) {
                    pl.mysql.query("UPDATE `stats` SET `" + statname
                            + "`='" + value + "' WHERE `username`='" + p.getUniqueId().toString() + "';");
                } else {
                    pl.mysql.query("UPDATE `stats` SET `" + statname
                            + "`='" + value + "' WHERE `username`='" + p.getName() + "';");
                }
        	}
        }.runTaskAsynchronously(pl);
    }
    
    public static void getLeaderboard(Player player, int entries) {
    	HashMap<String, Integer> statsMap = new HashMap<String, Integer>();
    	
    	if (pl.isFile()) {
    		statsMap = getStatsFromFile();
    	} else {
    		statsMap = getStatsFromDB(entries);
    	}
    	position = 0;
    	statsMap.entrySet().stream()
    		.sorted(Entry.comparingByValue(Comparator.reverseOrder()))
    		.limit(entries)
    		.forEach(e -> {position++;
    			   Messages.sendMessage(player, Messages.leaderboard
    					   .replace("{POSITION}", String.valueOf(position))
    					   .replace("{PLAYER}", e.getKey())
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
    
    public static HashMap<String, Integer> getStatsFromFile() {
    	FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		ConfigurationSection stats = config.getConfigurationSection("stats");
		
		final HashMap<String, Integer> statsMap = new HashMap<String, Integer>();

		if (stats != null) {
			if (Bukkit.getOnlineMode()) {
				for (String uuid : stats.getKeys(false)) {
					// validate UUID as file could contain player names if its been in offline mode
					if (!isValidUuid(uuid)) {
						continue;
					}
					
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
					if (offlinePlayer.getName() != null) {
						statsMap.put(offlinePlayer.getName(), getWins(offlinePlayer));
					} 				
				}
			} else {
				for (String playerName : stats.getKeys(false)) {
					if (Bukkit.getPlayer(playerName) != null) {
						statsMap.put(playerName, getWins(Bukkit.getPlayer(playerName)));
					}
				}
			}
		}
		return statsMap;
    }
    
    public static HashMap<String, Integer> getStatsFromDB(int limit) {
    	final HashMap<String, Integer> statsMap = new HashMap<String, Integer>();
    	
    	try {
            ResultSet rs;
            
            rs = pl.mysql.query("SELECT * FROM `stats` ORDER BY wins DESC LIMIT " + limit).getResultSet();
           
            while (rs.next()) {
            	String playerName = rs.getString("username");
            	
            	if (Bukkit.getOnlineMode()) {
            		if (isValidUuid(playerName)) {
            			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerName));
                		playerName = offlinePlayer.getName();
            		}
            	}
            	statsMap.put(playerName, rs.getInt("wins"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return statsMap;
    }
    
}
