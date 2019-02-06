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
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import tntrun.arena.Arena;
import tntrun.arena.handlers.SoundHandler;
import tntrun.utils.Bars;
import tntrun.utils.Shop;
import tntrun.utils.Sounds;
import tntrun.utils.Stats;
import tntrun.utils.TitleMsg;
import tntrun.utils.Utils;
import tntrun.commands.AutoTabCompleter;
import tntrun.commands.ConsoleCommands;
import tntrun.commands.GameCommands;
import tntrun.commands.setup.SetupCommandsHandler;
import tntrun.commands.setup.SetupTabCompleter;
import tntrun.datahandler.ArenasManager;
import tntrun.datahandler.PlayerDataStore;
import tntrun.eventhandler.HeadsPlusHandler;
import tntrun.eventhandler.PlayerLeaveArenaChecker;
import tntrun.eventhandler.PlayerStatusHandler;
import tntrun.eventhandler.RestrictionHandler;
import tntrun.kits.Kits;
import tntrun.lobby.GlobalLobby;
import tntrun.messages.Messages;
import tntrun.signs.SignHandler;
import tntrun.signs.editor.SignEditor;

public class TNTRun extends JavaPlugin {

	private Logger log;
	private boolean headsplus = false;
	private boolean usestats = false;
	private boolean needupdate = false;
	private boolean file = false;

	public PlayerDataStore pdata;
	public ArenasManager amanager;
	public GlobalLobby globallobby;
	public SignEditor signEditor;
	public Kits kitmanager;
	public String[] version = {"Nothing", "Nothing"};
	public Sounds sound;
	public Stats stats;
	
	public static TNTRun instance;

	@Override
	public void onEnable() {
		instance = this;
		log = getLogger();
		signEditor = new SignEditor(this);
		globallobby = new GlobalLobby(this);
		kitmanager = new Kits();
		Messages.loadMessages(this);
		Bars.loadBars(this);
		TitleMsg.loadTitles(this);
		pdata = new PlayerDataStore();
		amanager = new ArenasManager();
		
		//register commands and events
		setupPlugin();

		// save config
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

		// load arenas
		loadArenas();
		
		//check for update
		checkUpdate();
		
		// enable sounds
		sound = new SoundHandler(this);
		
		// start metrics
		log.info("Starting Metrics...");
		new Metrics(this);
	     
		// set storage type
		setStorage();
		
		//enable stats
		if (usestats) {
			stats = new Stats(this);
		}
	}

	public static TNTRun getInstance() {
		return instance;
	}

	@Override
	public void onDisable() {
		//Close mysql connection
		if(!file){
			mysql.close();
		}
		// save arenas
		saveArenas();
		// save lobby
		globallobby.saveToConfig();
		globallobby = null;
		// save kits
		kitmanager.saveToConfig();
		kitmanager = null;
		// save signs
		signEditor.saveConfiguration();
		signEditor = null;
		// unload other things
		pdata = null;
		amanager = null;
		log = null;
		stats = null;
	}

	private void saveArenas() {
		for (Arena arena : amanager.getArenas()) {
			arena.getStructureManager().getGameZone().regenNow();
			arena.getStatusManager().disableArena();
			arena.getStructureManager().saveToConfig();
			Bars.removeAll(arena.getArenaName());
		}
	}

	public void logSevere(String message) {
		log.severe(message);
	}
	
	public boolean isHeadsPlus() {
		return headsplus;
	}

	public boolean useStats() {
		return usestats;
	}

	public boolean needUpdate() {
		return needupdate;
	}

	public boolean isFile() {
		return file;
	}

	private void checkUpdate() {
		if (!getConfig().getBoolean("special.CheckForNewVersion", true)) {
			return;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				log.info(" ");
				log.info("Checking plugin version...");
				new VersionChecker();
				version = VersionChecker.get().getVersion().split(";");
				if (version[0].equalsIgnoreCase("error")) {
					throw new NullPointerException("An error was occured while checking version! Please report this here: https://www.spigotmc.org/threads/tntrun_reloaded.303586/");
				} else if (version[0].equalsIgnoreCase(getDescription().getVersion())) {
					log.info("You are running the most recent version");
					needupdate = false;
				} else {
					log.info("Your version: " + getDescription().getVersion());
					log.info("New version : " + version[0]);
					log.info("New version available! Download now: https://www.spigotmc.org/resources/tntrun_reloaded.53359/");
					needupdate = true;
					for (Player p : Bukkit.getOnlinePlayers()) {
						Utils.displayUpdate(p);
					}
				}
				log.info(" ");
			}
		}.runTaskLaterAsynchronously(this, 30L);
	}

	public MySQL mysql;

	private void connectToMySQL() {
		log.info("Connecting to MySQL database...");
		String host = this.getConfig().getString("MySQL.host");
		Integer port = this.getConfig().getInt("MySQL.port");
		String name = this.getConfig().getString("MySQL.name");
		String user = this.getConfig().getString("MySQL.user");
		String pass = this.getConfig().getString("MySQL.pass");
		mysql = new MySQL(host, port, name, user, pass, this);
		
		new BukkitRunnable() {
			@Override
			public void run() {

				mysql.query("CREATE TABLE IF NOT EXISTS `stats` ( `username` varchar(50) NOT NULL, "
						+ "`looses` int(16) NOT NULL, `wins` int(16) NOT NULL, "
						+ "`played` int(16) NOT NULL, "
						+ "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");

				log.info("Connected to MySQL database!");
			}
		}.runTaskAsynchronously(this);
	}

	private void setupPlugin() {
		getCommand("tntrun").setExecutor(new GameCommands(this));
		getCommand("tntrunsetup").setExecutor(new SetupCommandsHandler(this));
		getCommand("tntrunconsole").setExecutor(new ConsoleCommands(this));
		getCommand("tntrun").setTabCompleter(new AutoTabCompleter());
		getCommand("tntrunsetup").setTabCompleter(new SetupTabCompleter());

		getServer().getPluginManager().registerEvents(new PlayerStatusHandler(this), this);
		getServer().getPluginManager().registerEvents(new RestrictionHandler(this), this);
		getServer().getPluginManager().registerEvents(new PlayerLeaveArenaChecker(this), this);
		getServer().getPluginManager().registerEvents(new SignHandler(this), this);
		getServer().getPluginManager().registerEvents(new Shop(this), this);

		Plugin HeadsPlus = getServer().getPluginManager().getPlugin("HeadsPlus");
		if (HeadsPlus != null && HeadsPlus.isEnabled()) {
			getServer().getPluginManager().registerEvents(new HeadsPlusHandler(this), this);
			headsplus = true;
			log.info("Successfully linked with HeadsPlus, version " + HeadsPlus.getDescription().getVersion());
		}
	}

	private void loadArenas() {
		final File arenasfolder = new File(getDataFolder() + File.separator + "arenas");
		arenasfolder.mkdirs();
		new BukkitRunnable() {
			@Override
			public void run() {
				// load global lobby
				globallobby.loadFromConfig();
				// load kits
				kitmanager.loadFromConfig();
				// load arenas
				for (String file : arenasfolder.list()) {
					Arena arena = new Arena(file.substring(0, file.length() - 4), instance);
					arena.getStructureManager().loadFromConfig();
					arena.getStatusManager().enableArena();
					amanager.registerArena(arena);
					Bars.createBar(arena.getArenaName());
				}
				// load signs
				signEditor.loadConfiguration();
			}
		}.runTaskLater(this, 20L);
	}
	
	private void setStorage() {
		if (this.getConfig().getString("database").equals("file")) {
			file = true;
			usestats = true;
		} else if (this.getConfig().getString("database").equals("sql")) {
			this.connectToMySQL();
			usestats = true;
			file = false;
		} else {
			log.info("This database is not supported, supported database: sql, file");
			usestats = false;
			file = false;
			log.info("Disabling stats...");
		}
	}
}
