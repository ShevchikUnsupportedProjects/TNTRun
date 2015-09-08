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

package tntrun;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import tntrun.arena.Arena;
import tntrun.utils.Bars;
import tntrun.utils.Shop;
import tntrun.utils.Stats;
import tntrun.utils.TitleMsg;
import tntrun.commands.ConsoleCommands;
import tntrun.commands.GameCommands;
import tntrun.commands.setup.SetupCommandsHandler;
import tntrun.datahandler.ArenasManager;
import tntrun.datahandler.PlayerDataStore;
import tntrun.eventhandler.PlayerLeaveArenaChecker;
import tntrun.eventhandler.PlayerStatusHandler;
import tntrun.eventhandler.RestrictionHandler;
import tntrun.lobby.GlobalLobby;
import tntrun.messages.Messages;
import tntrun.signs.SignHandler;
import tntrun.signs.editor.SignEditor;

public class TNTRun extends JavaPlugin {

	private Logger log;

	public PlayerDataStore pdata;
	public ArenasManager amanager;
	public GlobalLobby globallobby;
	public SignEditor signEditor;
	public boolean file = false;
	public boolean usestats = false;

	@Override
	public void onEnable() {
		log = getLogger();
		signEditor = new SignEditor(this);
		globallobby = new GlobalLobby(this);
		Messages.loadMessages(this);
		Bars.loadBars(this);
		TitleMsg.loadTitles(this);
		pdata = new PlayerDataStore();
		amanager = new ArenasManager();
		getCommand("tntrunsetup").setExecutor(new SetupCommandsHandler(this));
		getCommand("tntrun").setExecutor(new GameCommands(this));
		getCommand("tntrunconsole").setExecutor(new ConsoleCommands(this));
		getServer().getPluginManager().registerEvents(new PlayerStatusHandler(this), this);
		getServer().getPluginManager().registerEvents(new RestrictionHandler(this), this);
		getServer().getPluginManager().registerEvents(new PlayerLeaveArenaChecker(this), this);
		getServer().getPluginManager().registerEvents(new SignHandler(this), this);
		getServer().getPluginManager().registerEvents(new Shop(this), this);
	    // config
	    saveDefaultConfig();
	    getConfig().options().copyDefaults(true);
	    saveConfig();
		// load arenas
		final File arenasfolder = new File(getDataFolder() + File.separator + "arenas");
		arenasfolder.mkdirs();
		final TNTRun instance = this;
		getServer().getScheduler().scheduleSyncDelayedTask(
			this,
			new Runnable() {
				@Override
				public void run() {
					// load globallobyy
					globallobby.loadFromConfig();
					// load arenas
					for (String file : arenasfolder.list()) {
						Arena arena = new Arena(file.substring(0, file.length() - 4), instance);
						arena.getStructureManager().loadFromConfig();
						arena.getStatusManager().enableArena();
						amanager.registerArena(arena);
					}
					// load signs
					signEditor.loadConfiguration();
				}
			},
			20
		);
		
	     try {
	    	 Bukkit.getLogger().info("[TNTRun] Starting Metrics...");
	         Metrics metrics = new Metrics(this);
	         metrics.start();
	         Bukkit.getLogger().info("[TNTRun] Metrics started!");
	     } catch (IOException e) {
	    	 e.printStackTrace();
	        Bukkit.getLogger().info("[TNTRun] Error, can't start metrics, please report this! http://www.spigotmc.org/resources/tntrun.7320/");
	     }
	     
	     if(this.getConfig().getString("database").equals("file")){
	    	 file = true;
	    	 usestats = true;
	     }else if(this.getConfig().getString("database").equals("sql")){
	    	 this.connectToMySQL();
	    	 usestats = true;
	    	 file = false;
	     }else{
	    	 Bukkit.getLogger().info("[TNTRun] This database is not supported, supported database: sql, file");
	    	 usestats = false;
	    	 file = false;
	    	 Bukkit.getLogger().info("[TNTRun] Disabling stats...");
	     }
	     new Stats(this);
	}

	@Override
	public void onDisable() {
		//Close mysql connection
		if(!file){
			mysql.close();
		}
		// save arenas
		for (Arena arena : amanager.getArenas()) {
			arena.getStatusManager().disableArena();
			arena.getStructureManager().saveToConfig();
		}
		// save lobby
		globallobby.saveToConfig();
		globallobby = null;
		// save signs
		signEditor.saveConfiguration();
		signEditor = null;
		// unload other things
		pdata = null;
		amanager = null;
		log = null;
	}

	public void logSevere(String message) {
		log.severe(message);
	}
	
	public MySQL mysql;
	
	public void connectToMySQL(){
		Bukkit.getLogger().info("[TNTRun] Connecting to MySQL database...");
		String host = this.getConfig().getString("MySQL.host");
        Integer port = this.getConfig().getInt("MySQL.port");
        String name = this.getConfig().getString("MySQL.name");
        String user = this.getConfig().getString("MySQL.user");
        String pass = this.getConfig().getString("MySQL.pass");
        mysql = new MySQL(host, port, name, user, pass, this);

        mysql.query("CREATE TABLE IF NOT EXISTS `stats` ( `username` varchar(50) NOT NULL, "
                + "`looses` int(16) NOT NULL, `wins` int(16) NOT NULL, "
                + "`played` int(16) NOT NULL, "
                + "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        
        Bukkit.getLogger().info("[TNTRun] Connected to MySQL database!");
	}

}
