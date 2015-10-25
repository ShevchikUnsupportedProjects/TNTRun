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
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.arena.structure.StructureManager.TeleportDestination;
import tntrun.utils.Bars;
import tntrun.utils.Stats;
import tntrun.utils.TitleMsg;
import tntrun.messages.Messages;

public class PlayerHandler {

	private TNTRun plugin;
	private Arena arena;

	public PlayerHandler(TNTRun plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
	}

	// check if player can join the arena
	public boolean checkJoin(Player player) {
		if (arena.getStructureManager().getWorld() == null) {
			Messages.sendMessage(player, Messages.arenawolrdna);
			return false;
		}
		if (!arena.getStatusManager().isArenaEnabled()) {
			Messages.sendMessage(player, Messages.arenadisabled);
			return false;
		}
		if (arena.getStatusManager().isArenaRunning()) {
			Messages.sendMessage(player, Messages.arenarunning);
			return false;
		}
		if (arena.getStatusManager().isArenaRegenerating()) {
			Messages.sendMessage(player, Messages.arenaregenerating);
			return false;
		}
		if (player.isInsideVehicle()) {
			Messages.sendMessage(player, Messages.arenavehicle);
			return false;
		}
		if (arena.getPlayersManager().getPlayersCount() == arena.getStructureManager().getMaxPlayers()) {
			Messages.sendMessage(player, Messages.limitreached);
			return false;
		}
		return true;
	}

	// spawn player on arena
	@SuppressWarnings("deprecation")
	public void spawnPlayer(final Player player, String msgtoplayer, String msgtoarenaplayers) {
		// teleport player to arena
		plugin.pdata.storePlayerLocation(player);
		player.teleport(arena.getStructureManager().getSpawnPoint());
		// set player visible to everyone
		for (Player aplayer : Bukkit.getOnlinePlayers()) {
			aplayer.showPlayer(player);
		}
		// change player status
		plugin.pdata.storePlayerGameMode(player);
		player.setFlying(false);
		player.setAllowFlight(false);
		plugin.pdata.storePlayerLevel(player);
		plugin.pdata.storePlayerInventory(player);
		plugin.pdata.storePlayerArmor(player);
		plugin.pdata.storePlayerPotionEffects(player);
		plugin.pdata.storePlayerHunger(player);
		// update inventory
		player.updateInventory();
		//set full countdown
		if(!arena.getStatusManager().isArenaStarting()){
			arena.getGameHandler().count = arena.getStructureManager().getCountdown();
		}
		// send message to player
		Messages.sendMessage(player, msgtoplayer);	
		// set player on arena data
		arena.getPlayersManager().add(player);
		// send message to other players
		for (Player oplayer : arena.getPlayersManager().getPlayers()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, msgtoarenaplayers);
			// send title for players
			try {
				TitleMsg.sendFullTitle(oplayer, TitleMsg.join.replace("{PLAYER}", player.getName()), TitleMsg.subjoin.replace("{PLAYER}", player.getName()), 10, 20, 20, plugin);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// start cooldown and add leave item
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
			public void run(){
				String[] ids = plugin.getConfig().getString("items.leave.ID").split(":");
				ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(ids[0])), 1, (byte) Byte.parseByte(ids[1]));
				ItemMeta im = item.getItemMeta();
				im.setDisplayName(plugin.getConfig().getString("items.leave.name").replace("&", "§"));
				item.setItemMeta(im);
				
				player.getInventory().setItem(8, item);
				
				if(plugin.getConfig().getBoolean("items.vote.use")){
					addVoteDiamond(player, 0);
				}
				if(plugin.getConfig().getBoolean("items.shop.use")){
					addShop(player, 2);
				}
				if(plugin.getConfig().getBoolean("items.info.use")){
					addInfo(player, 1);
				}
				if(plugin.getConfig().getBoolean("items.stats.use")){
					addStats(player, 3);
				}
			}
		}, 5L);
		// send message about arena player count
		String message = Messages.playerscountinarena;
		message = message.replace("{COUNT}", String.valueOf(arena.getPlayersManager().getPlayersCount()));
		Messages.sendMessage(player, message);
		// modify signs
		plugin.signEditor.modifySigns(arena.getArenaName());
		// create scoreboard
		arena.getGameHandler().createWaitingScoreBoard();
		// modify bars
		if (!arena.getStatusManager().isArenaStarting()) {
			for (Player oplayer : arena.getPlayersManager().getPlayers()) {
				Bars.setBar(oplayer, Bars.waiting, arena.getPlayersManager().getPlayersCount(), 0, arena.getPlayersManager().getPlayersCount() * 100 / arena.getStructureManager().getMinPlayers(), plugin);
				// play sound
				oplayer.playSound(oplayer.getLocation(), Sound.NOTE_PLING, 1, 1);
			}
		}
		// check for game start
		if (!arena.getStatusManager().isArenaStarting() && arena.getPlayersManager().getPlayersCount() == arena.getStructureManager().getMinPlayers()) {
		arena.getGameHandler().runArenaCountdown();
		}
	}

	// move to spectators
	public void spectatePlayer(final Player player, String msgtoplayer, String msgtoarenaplayers) {
		// remove form players
		arena.getPlayersManager().remove(player);
		// add to lostPlayers
		arena.getGameHandler().lostPlayers++;
		// remove scoreboard
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		// teleport to spectators spawn
		player.teleport(arena.getStructureManager().getSpectatorSpawn());
		// clear inventory
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);
		// allow flight
		player.setAllowFlight(true);
		player.setFlying(true);
		// hide from others
		for (Player oplayer : Bukkit.getOnlinePlayers()) {
			oplayer.hidePlayer(player);
		}
		// send message to player
		Messages.sendMessage(player, msgtoplayer);
		// modify signs
		plugin.signEditor.modifySigns(arena.getArenaName());
		// send message to other players and update bars
		for (Player oplayer : arena.getPlayersManager().getAllParticipantsCopy()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, msgtoarenaplayers);
		}
		// add to spectators
		arena.getPlayersManager().addSpectator(player);
		// start cooldown and add leave item
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
			public void run(){
				String[] ids = plugin.getConfig().getString("items.leave.ID").split(":");
				@SuppressWarnings("deprecation")
				ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(ids[0])), 1, (byte) Byte.parseByte(ids[1]));
				ItemMeta im = item.getItemMeta();
				im.setDisplayName(plugin.getConfig().getString("items.leave.name").replace("&", "§"));
				item.setItemMeta(im);
				
				player.getInventory().setItem(8, item);
				
				if(plugin.getConfig().getBoolean("items.info.use")){
					addInfo(player, 0);
				}
				if(plugin.getConfig().getBoolean("items.stats.use")){
					addInfo(player, 0);
				}
			}
		}, 5L);
	}

	// remove player from arena
	public void leavePlayer(Player player, String msgtoplayer, String msgtoarenaplayers) {
		// reset spectators
		boolean spectator = arena.getPlayersManager().isSpectator(player.getName());
		if (spectator) {
			arena.getPlayersManager().removeSpecator(player.getName());
			for (Player oplayer : Bukkit.getOnlinePlayers()) {
				oplayer.showPlayer(player);
			}
			player.setAllowFlight(false);
			player.setFlying(false);
		}
		// check if arena is running
		if(arena.getStatusManager().isArenaRunning()){
			// add to lostPlayers
			arena.getGameHandler().lostPlayers++;
			Stats.addLoses(player, 1);
		}
		// remove scoreboard
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		// remove player from arena and restore his state
		removePlayerFromArenaAndRestoreState(player, false);
		// should not send messages and other things when player is a spectator
		if (spectator) {
			return;
		}
		// send message to player
		Messages.sendMessage(player, msgtoplayer);
		// modify signs
		plugin.signEditor.modifySigns(arena.getArenaName());
		// create scoreboard
		if(!arena.getStatusManager().isArenaRunning()){
			arena.getGameHandler().createWaitingScoreBoard();
		}
		// send message to other players and update bars
		for (Player oplayer : arena.getPlayersManager().getAllParticipantsCopy()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, msgtoarenaplayers);
			if (!arena.getStatusManager().isArenaStarting() && !arena.getStatusManager().isArenaRunning()) {
				Bars.setBar(oplayer, Bars.waiting, arena.getPlayersManager().getPlayersCount(), 0, arena.getPlayersManager().getPlayersCount() * 100 / arena.getStructureManager().getMinPlayers(), plugin);
			}
		}
	}

	protected void leaveWinner(Player player, String msgtoplayer) {
		// remove player from arena and restore his state
		removePlayerFromArenaAndRestoreState(player, true);
		// remove scoreboard
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		// send message to player
		Messages.sendMessage(player, msgtoplayer);
		// modify signs
		plugin.signEditor.modifySigns(arena.getArenaName());
	}
	
	private void removePlayerFromArenaAndRestoreState(Player player, boolean winner) {
		// remove vote
		votes.remove(player.getName());
		// remove bar
		Bars.removeBar(player);
		// remove player on arena data
		arena.getPlayersManager().remove(player);
		// remove all potion effects
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		// restore player status
		plugin.pdata.restorePlayerHunger(player);
		plugin.pdata.restorePlayerPotionEffects(player);
		plugin.pdata.restorePlayerArmor(player);
		plugin.pdata.restorePlayerInventory(player);
		plugin.pdata.restorePlayerLevel(player);
		// add player damage resistance
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 80, true), true);
		// restore location ot teleport to lobby
		if (arena.getStructureManager().getTeleportDestination() == TeleportDestination.LOBBY && plugin.globallobby.isLobbyLocationWorldAvailable()) {
			player.teleport(plugin.globallobby.getLobbyLocation());
			plugin.pdata.clearPlayerLocation(player);
		} else {
			plugin.pdata.restorePlayerLocation(player);
		}
		// reward player before restoring gamemode if player is winner
		if (winner) {
			arena.getStructureManager().getRewards().rewardPlayer(player);
			// spawn firework
			Firework f = player.getWorld().spawn(player.getLocation(), Firework.class);
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
		plugin.pdata.restorePlayerGameMode(player);
		// update inventory
		player.updateInventory();
		// remove fly
		player.setAllowFlight(false);
		if(player.getGameMode() == GameMode.CREATIVE){
			player.setAllowFlight(true);
		}
		if(player.hasPermission("tntrun.fly.everywhere")){
			player.setAllowFlight(true);
			player.setFlying(true);
		}
		player.setFlying(false);
		// check is in arena 0 players
		if (arena.getPlayersManager().getPlayersCount() == 0) {
			arena.getGameHandler().stopArena();
		}
	}

	// vote for game start
	private HashSet<String> votes = new HashSet<String>();

	public boolean vote(Player player) {
		if (!votes.contains(player.getName())) {
			votes.add(player.getName());
			if (!arena.getStatusManager().isArenaStarting() && arena.getPlayersManager().getPlayersCount() > 1 && votes.size() >= arena.getPlayersManager().getPlayersCount() * arena.getStructureManager().getVotePercent()) {
				arena.getGameHandler().runArenaCountdown();
			}
			return true;
		}
		return false;
	}
	
	public void addInfo(Player p, int slot){
		String[] ids = plugin.getConfig().getString("items.info.ID").split(":");
		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(ids[0])), 1, (byte) Byte.parseByte(ids[1]));
	     
	     ItemMeta meta = item.getItemMeta();
	     meta.setDisplayName(plugin.getConfig().getString("items.info.name").replace("&", "§"));
	     item.setItemMeta(meta);
	    
	     p.getInventory().addItem(item);
	    
	}
	
	public void addVoteDiamond(Player p, int slot){
		String[] ids = plugin.getConfig().getString("items.vote.ID").split(":");
		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(ids[0])), 1, (byte) Byte.parseByte(ids[1]));
	     
	     ItemMeta meta = item.getItemMeta();
	     meta.setDisplayName(plugin.getConfig().getString("items.vote.name").replace("&", "§"));
	     item.setItemMeta(meta);
	    
	     p.getInventory().addItem(item);
	}
	
	public void addShop(Player p, int slot){
		String[] ids = plugin.getConfig().getString("items.shop.ID").split(":");
		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(ids[0])), 1, (byte) Byte.parseByte(ids[1]));
	     
	     ItemMeta meta = item.getItemMeta();
	     meta.setDisplayName(plugin.getConfig().getString("items.shop.name").replace("&", "§"));
	     item.setItemMeta(meta);
	    
	     p.getInventory().addItem(item);
	}
	
	public void addStats(Player p, int slot){
		String[] ids = plugin.getConfig().getString("items.stats.ID").split(":");
		@SuppressWarnings("deprecation")
		ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(ids[0])), 1, (byte) Byte.parseByte(ids[1]));
	     
	     ItemMeta meta = item.getItemMeta();
	     meta.setDisplayName(plugin.getConfig().getString("items.stats.name").replace("&", "§"));
	     item.setItemMeta(meta);
	    
	     p.getInventory().addItem(item);
	}

}
