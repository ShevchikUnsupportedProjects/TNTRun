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

import org.bukkit.plugin.java.JavaPlugin;

import tntrun.arena.Arena;
import tntrun.bars.Bars;
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

	private static TNTRun instance;
	public static TNTRun getInstance() {
		return instance;
	}

	private Logger log;

	@Override
	public void onEnable() {
		instance = this;
		log = getLogger();
		Messages.loadMessages();
		Bars.loadBars();
		ArenasManager.getInstance();
		PlayerDataStore.getInstance();
		SignEditor.getInstance();
		GlobalLobby.getInstance();
		getCommand("tntrunsetup").setExecutor(new SetupCommandsHandler());
		getCommand("tntrun").setExecutor(new GameCommands());
		getCommand("tntrunconsole").setExecutor(new ConsoleCommands());
		getServer().getPluginManager().registerEvents(new PlayerStatusHandler(), this);
		getServer().getPluginManager().registerEvents(new RestrictionHandler(), this);
		getServer().getPluginManager().registerEvents(new PlayerLeaveArenaChecker(), this);
		getServer().getPluginManager().registerEvents(new SignHandler(), this);
		// load arenas
		final File arenasfolder = new File(getDataFolder() + File.separator + "arenas");
		arenasfolder.mkdirs();
		getServer().getScheduler().scheduleSyncDelayedTask(
			this,
			new Runnable() {
				@Override
				public void run() {
					// load globallobyy
					GlobalLobby.getInstance().loadFromConfig();
					// load arenas
					for (String file : arenasfolder.list()) {
						Arena arena = new Arena(file.substring(0, file.length() - 4));
						arena.getStructureManager().loadFromConfig();
						arena.getStatusManager().enableArena();
						ArenasManager.getInstance().registerArena(arena);
					}
					// load signs
					SignEditor.getInstance().loadConfiguration();
				}
			},
			20
		);
	}

	@Override
	public void onDisable() {
		// save arenas
		for (Arena arena : ArenasManager.getInstance().getArenas()) {
			arena.getStatusManager().disableArena();
			arena.getStructureManager().saveToConfig();
		}
		// save lobby
		GlobalLobby.getInstance().saveToConfig();
		// save signs
		SignEditor.getInstance().saveConfiguration();
		// set instance to null
		instance = null;
	}

	public void logSevere(String message) {
		log.severe(message);
	}

}
