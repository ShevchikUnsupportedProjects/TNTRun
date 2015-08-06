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

package tntrun.arena.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.arena.structure.Kits;
import tntrun.utils.ActionBar;
import tntrun.utils.Bars;
import tntrun.utils.Shop;
import tntrun.utils.TitleMsg;
import tntrun.messages.Messages;

public class GameHandler {

	private TNTRun plugin;
	private Arena arena;
	public int lostPlayers = 0;

	public GameHandler(TNTRun plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		count = arena.getStructureManager().getCountdown();
	}

	private Scoreboard scoreboard = buildScoreboard();

	// arena leave handler
	private int leavetaskid;

	public void startArenaAntiLeaveHandler() {
		leavetaskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					for (Player player : arena.getPlayersManager().getPlayersCopy()) {
						if (!arena.getStructureManager().isInArenaBounds(player.getLocation())) {
							arena.getPlayerHandler().leavePlayer(player, Messages.playerlefttoplayer, Messages.playerlefttoothers);
						}
					}
					for (Player player : arena.getPlayersManager().getSpectatorsCopy()) {
						if (!arena.getStructureManager().isInArenaBounds(player.getLocation())) {
							arena.getPlayerHandler().leavePlayer(player, "", "");
						}
					}
				}
			},
			0, 1
		);
	}

	public void stopArenaAntiLeaveHandler() {
		Bukkit.getScheduler().cancelTask(leavetaskid);
	}

	// arena start handler (running status updater)
	int runtaskid;
	int count;

	public void runArenaCountdown() {
		count = arena.getStructureManager().getCountdown();
		arena.getStatusManager().setStarting(true);
		runtaskid = Bukkit.getScheduler().scheduleSyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					// check if countdown should be stopped for some various reasons
					if (arena.getPlayersManager().getPlayersCount() < arena.getStructureManager().getMinPlayers()) {
						for (Player player : arena.getPlayersManager().getPlayers()) {
							Bars.setBar(player, Bars.waiting, arena.getPlayersManager().getPlayersCount(), 0, arena.getPlayersManager().getPlayersCount() * 100 / arena.getStructureManager().getMinPlayers(), plugin);
							createWaitingScoreBoard();
						}
						stopArenaCountdown();
					} else
					// start arena if countdown is 0
					if (count == 0) {
						stopArenaCountdown();
						startArena();
					} else if(count == 5) {
						String message = Messages.arenacountdown;
						message = message.replace("{COUNTDOWN}", String.valueOf(count));
						for (Player player : arena.getPlayersManager().getPlayers()) {
							player.teleport(arena.getStructureManager().getSpawnPoint());
							player.playSound(player.getLocation(), Sound.CLICK, 1, 5);
							Messages.sendMessage(player, message);
							try {
								TitleMsg.sendFullTitle(player, TitleMsg.starting.replace("{COUNT}", count + ""), TitleMsg.substarting.replace("{COUNT}", count + ""), 0, 40, 20, plugin);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else if (count < 11) {
						String message = Messages.arenacountdown;
						message = message.replace("{COUNTDOWN}", String.valueOf(count));
						for (Player player : arena.getPlayersManager().getPlayers()) {
							Messages.sendMessage(player, message);
							player.playSound(player.getLocation(), Sound.CLICK, 1, 5);
							try {
								TitleMsg.sendFullTitle(player, TitleMsg.starting.replace("{COUNT}", count + ""), TitleMsg.substarting.replace("{COUNT}", count + ""), 0, 40, 20, plugin);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else if (count % 10 == 0) {
						String message = Messages.arenacountdown;
						message = message.replace("{COUNTDOWN}", String.valueOf(count));
				          for (Player all : Bukkit.getOnlinePlayers()) {
				        	  Messages.sendMessage(all, message);
								all.playSound(all.getLocation(), Sound.CLICK, 1, 5);
								try {
									TitleMsg.sendFullTitle(all, TitleMsg.starting.replace("{COUNT}", count + ""), TitleMsg.substarting.replace("{COUNT}", count + ""), 0, 40, 20, plugin);
								} catch (IOException e) {
									e.printStackTrace();
								}
				          }
				        }
					if(count == 5) {
						for (Player player : arena.getPlayersManager().getPlayers()) {
							player.teleport(arena.getStructureManager().getSpawnPoint());
							player.playSound(player.getLocation(), Sound.CLICK, 1, 5);
						}
					}
					// scoreboard
					createWaitingScoreBoard();
					// sending bars
					for (Player player : arena.getPlayersManager().getPlayers()) {
						player.setLevel(count);
						Bars.setBar(player, Bars.starting, 0, count, count * 100 / arena.getStructureManager().getCountdown(), plugin);
				    }
					count--;
				}
			},
			0, 20
		);
	}

	public void stopArenaCountdown() {
		arena.getStatusManager().setStarting(false);
		count = arena.getStructureManager().getCountdown();
		Bukkit.getScheduler().cancelTask(runtaskid);
	}

	// main arena handler
	private int timelimit;
	private int arenahandler;
	private int playingtask;

	Random rnd = new Random();
	@SuppressWarnings("deprecation")
	public void startArena() {
		arena.getStatusManager().setRunning(true);
		String message = Messages.arenastarted;
		message = message.replace("{TIMELIMIT}", String.valueOf(arena.getStructureManager().getTimeLimit()));
		for (Player player : arena.getPlayersManager().getPlayers()) {
			player.setAllowFlight(true);
			Messages.sendMessage(player, message);
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
			String[] ids1 = plugin.getConfig().getString("items.shop.ID").split(":");
			String[] ids2 = plugin.getConfig().getString("items.vote.ID").split(":");
			String[] ids3 = plugin.getConfig().getString("items.info.ID").split(":");
			
			player.getInventory().remove(Integer.parseInt(ids1[0]));
			player.getInventory().remove(Integer.parseInt(ids2[0]));
			player.getInventory().remove(Integer.parseInt(ids3[0]));
			
            if (Shop.pitems.containsKey(player)) {
            	ArrayList<ItemStack> items = Shop.pitems.get(player);
                Shop.pitems.remove(player);
                Shop.bought.remove(player);
 
                if(items != null){
                    for (ItemStack item : items) {
                        player.getInventory().addItem(item);
                    }	
                }
                player.updateInventory();
            }
			try {
				TitleMsg.sendFullTitle(player, TitleMsg.start, TitleMsg.substart, 20, 20, 20, plugin);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		plugin.signEditor.modifySigns(arena.getArenaName());
		Kits kits = arena.getStructureManager().getKits();
		if (kits.getKits().size() > 0) {
			String[] kitnames = kits.getKits().toArray(new String[kits.getKits().size()]);
			for (Player player : arena.getPlayersManager().getPlayers()) {
				kits.giveKit(kitnames[rnd.nextInt(kitnames.length)], player);
			}
		}
		resetScoreboard();
		createPlayingScoreBoard();
		timelimit = arena.getStructureManager().getTimeLimit() * 20; // timelimit is in ticks
		arenahandler = Bukkit.getScheduler().scheduleSyncRepeatingTask(
			plugin,
			new Runnable() {
				@Override
				public void run() {
					// stop arena if player count is 0
					if (arena.getPlayersManager().getPlayersCount() == 0) {
						// stop arena
						stopArena();
						return;
					}
					// kick all players if time is out
					if (timelimit < 0) {
						for (Player player : arena.getPlayersManager().getPlayersCopy()) {
							arena.getPlayerHandler().leavePlayer(player,Messages.arenatimeout, "");
						}
						return;
					}
					// handle players
					for (Player player : arena.getPlayersManager().getPlayersCopy()) {
						// Xp level
						player.setLevel(timelimit/20);
						// update bar
						Bars.setBar(player, Bars.playing, arena.getPlayersManager().getPlayersCount(), timelimit / 20, timelimit * 5 / arena.getStructureManager().getTimeLimit(), plugin);
						// handle player
						handlePlayer(player);
					}
					// update bars for spectators too
					for (Player player : arena.getPlayersManager().getSpectators()) {
						Bars.setBar(player, Bars.playing, arena.getPlayersManager().getPlayersCount(), timelimit / 20, timelimit * 5 / arena.getStructureManager().getTimeLimit(), plugin);
					}
					// decrease timelimit
					timelimit--;
				}
			},
			0, 1
		);
	}

	public void stopArena() {
		resetScoreboard();
		for (Player player : arena.getPlayersManager().getAllParticipantsCopy()) {
			arena.getPlayerHandler().leavePlayer(player, "", "");
		}
		lostPlayers = 0;
		arena.getStatusManager().setRunning(false);
		Bukkit.getScheduler().cancelTask(arenahandler);
		Bukkit.getScheduler().cancelTask(playingtask);
		plugin.signEditor.modifySigns(arena.getArenaName());
		if (arena.getStatusManager().isArenaEnabled()) {
			startArenaRegen();
		}
	}

	// player handlers
	public void handlePlayer(final Player player) {
		Location plloc = player.getLocation();
		Location plufloc = plloc.clone().add(0, -1, 0);
		// remove block under player feet
		arena.getStructureManager().getGameZone().destroyBlock(plufloc, arena);
		// check for win
		if (arena.getPlayersManager().getPlayersCount() == 1) {
			// last player won
			startEnding(player);
			return;
		}
		// check for lose
		if (arena.getStructureManager().getLoseLevel().isLooseLocation(plloc)) {
			// if we have the spectate spawn than we will move player to spectators, otherwise we will remove him from arena
			if (arena.getStructureManager().getSpectatorSpawnVector() != null) {
				arena.getPlayerHandler().spectatePlayer(player, Messages.playerlosttoplayer, Messages.playerlosttoothers);
			} else {
				arena.getPlayerHandler().leavePlayer(player, Messages.playerlosttoplayer, Messages.playerlosttoothers);
			}
			return;
		}
	}

	public Scoreboard buildScoreboard() {
		
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		Objective o = scoreboard.registerNewObjective("TNTRun", "waiting");
		o.setDisplaySlot(DisplaySlot.SIDEBAR);
		o.setDisplayName("§6§lTNTRUN");
		return scoreboard;
	}
	
	public void createWaitingScoreBoard() {
		if(!plugin.getConfig().getBoolean("special.UseScoreboard")){
			return;
		}
		resetScoreboard();
		Objective o = scoreboard.getObjective(DisplaySlot.SIDEBAR);
		int size = plugin.getConfig().getStringList("scoreboard.waiting").size();
		for(String s : plugin.getConfig().getStringList("scoreboard.waiting")){
			s = s.replace("&", "§");
			s = s.replace("{ARENA}", arena.getArenaName());
			s = s.replace("{PS}", arena.getPlayersManager().getAllParticipantsCopy().size() + "");
			s = s.replace("{MPS}", arena.getStructureManager().getMaxPlayers() + "");
			s = s.replace("{COUNT}", count + "");
			o.getScore(s).setScore(size);
			size--;
		}
		for (Player p : arena.getPlayersManager().getPlayers()) {
			p.setScoreboard(scoreboard);
		}
	}

	public void resetScoreboard() {
		for (String entry : new ArrayList<String>(scoreboard.getEntries())) {
			scoreboard.resetScores(entry);
		}
	}

	public void createPlayingScoreBoard() {
		if(!plugin.getConfig().getBoolean("special.UseScoreboard")){
			return;
		}
		playingtask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				resetScoreboard();
				Objective o = scoreboard.getObjective(DisplaySlot.SIDEBAR);
				
				int size = plugin.getConfig().getStringList("scoreboard.playing").size();
				for(String s : plugin.getConfig().getStringList("scoreboard.playing")){
					s = s.replace("&", "§");
					s = s.replace("{ARENA}", arena.getArenaName());
					s = s.replace("{PS}", arena.getPlayersManager().getAllParticipantsCopy().size() + "");
					s = s.replace("{MPS}", arena.getStructureManager().getMaxPlayers() + "");
					s = s.replace("{LOST}", lostPlayers + "");
					s = s.replace("{LIMIT}", timelimit/20 + "");
					o.getScore(s).setScore(size);
					size--;
				}
				for(Player p : arena.getPlayersManager().getPlayers()){
					ActionBar bar = new ActionBar();
					bar.sendActionBar(p, Messages.getdoublejumpsaction.replace("&", "§").replace("{DB}", plugin.getConfig().getInt("doublejumps." + p.getName()) + ""));
				}
			}
		}, 0, 20);
	}

	private void startArenaRegen() {
		// set arena is regenerating status
		arena.getStatusManager().setRegenerating(true);
		// modify signs
		plugin.signEditor.modifySigns(arena.getArenaName());
		// schedule gamezone regen
		int delay = arena.getStructureManager().getGameZone().regen(arena.plugin);
		// regen finished
		Bukkit.getScheduler().scheduleSyncDelayedTask(
			arena.plugin,
			new Runnable() {
				@Override
				public void run() {
					// set not regenerating status
					arena.getStatusManager().setRegenerating(false);
					// modify signs
					plugin.signEditor.modifySigns(arena.getArenaName());
				}
			},
			delay
		);
	}
	
		public void startEnding(final Player player){
			for(Player all : Bukkit.getOnlinePlayers()){
				all.playSound(arena.getStructureManager().getSpawnPoint(), Sound.ENDERDRAGON_DEATH, 1, 20F);
				try {
					TitleMsg.sendFullTitle(player, TitleMsg.win, TitleMsg.subwin, 20, 60, 20, plugin);
				} catch (IOException e) {
					e.printStackTrace();
				}
				String message = Messages.playerwonbroadcast;
				message = message.replace("{PLAYER}", player.getName());
				message = message.replace("{ARENA}", arena.getArenaName());
				all.sendMessage(message.replace("&", "§"));
			}
				for(Player p : arena.getPlayersManager().getAllParticipantsCopy()){
					p.playSound(p.getLocation(), Sound.EXPLODE, 1, 1);
					p.setAllowFlight(true);
					p.setFlying(true);
					p.teleport(arena.getStructureManager().getSpawnPoint());
					p.getInventory().clear();
				}
				
				Bukkit.getScheduler().cancelTask(arenahandler);
				Bukkit.getScheduler().cancelTask(playingtask);
				
				final int endtask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable(){
					@Override
					public void run() {
						Firework f = player.getWorld().spawn(arena.getStructureManager().getSpawnPoint(), Firework.class);
						FireworkMeta fm = f.getFireworkMeta();
						fm.addEffect(FireworkEffect.builder()
								.withColor(Color.GREEN).withColor(Color.RED)
								.withColor(Color.PURPLE)
								.with(Type.BALL_LARGE)
								.withFlicker()
								.build());
						fm.setPower(1);
						f.setFireworkMeta(fm);
					}
					
				}, 0, 10);
				
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
					public void run(){
						try{
						Bukkit.getScheduler().cancelTask(endtask);
						arena.getPlayerHandler().leaveWinner(player, Messages.playerwontoplayer);
						stopArena();
						
						final ConsoleCommandSender console = Bukkit.getConsoleSender();
						
						if(plugin.getConfig().getStringList("commandsonwin") == null){
							return;
						}
						for(String commands : plugin.getConfig().getStringList("commandsonwin")){
							Bukkit.dispatchCommand(console, commands.replace("{PLAYER}", player.getName()));
						}
						}catch (NullPointerException ex){
							
						}
					}
				}, 160);
			}

}
