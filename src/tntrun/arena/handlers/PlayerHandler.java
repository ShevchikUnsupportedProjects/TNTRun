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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.nossr50.api.PartyAPI;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.arena.structure.StructureManager.DamageEnabled;
import tntrun.arena.structure.StructureManager.TeleportDestination;
import tntrun.utils.Bars;
import tntrun.utils.FormattingCodesParser;
import tntrun.utils.TitleMsg;
import tntrun.messages.Messages;

public class PlayerHandler {

	private TNTRun plugin;
	private Arena arena;
	private Map<String, Integer> doublejumps = new HashMap<String, Integer>();   // playername -> number_of_doublejumps
	private List<String> pparty = new ArrayList<String>();

	public PlayerHandler(TNTRun plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
	}

	public boolean checkJoin(Player player) {
		if (!arena.getStatusManager().isArenaEnabled()) {
			Messages.sendMessage(player, Messages.trprefix + Messages.arenadisabled);
			return false;
		}
		if (arena.getStructureManager().getWorld() == null) {
			Messages.sendMessage(player, Messages.trprefix + Messages.arenawolrdna);
			return false;
		}
		if (arena.getStatusManager().isArenaRunning()) {
			Messages.sendMessage(player, Messages.trprefix + Messages.arenarunning);
			return false;
		}
		if (arena.getStatusManager().isArenaRegenerating()) {
			Messages.sendMessage(player, Messages.trprefix + Messages.arenaregenerating);
			return false;
		}
		if (!player.hasPermission("tntrun.join")) {
			Messages.sendMessage(player, Messages.trprefix + Messages.nopermission);
			return false;
		}
		if (player.isInsideVehicle()) {
			Messages.sendMessage(player, Messages.trprefix + Messages.arenavehicle);
			return false;
		}
		if (arena.getPlayersManager().getPlayersCount() == arena.getStructureManager().getMaxPlayers()) {
			Messages.sendMessage(player, Messages.trprefix + Messages.limitreached);
			return false;
		}

		if (plugin.amanager.getPlayerArena(player.getName()) != null) {
			Messages.sendMessage(player, Messages.trprefix + Messages.arenajoined);
			return false;
		}

		if (arena.getStructureManager().hasFee()) {
			double fee = arena.getStructureManager().getFee();
			if (arena.getArenaEconomy().hasFunds(player, fee)) {
				StringBuilder cost = new StringBuilder();
				if (arena.getStructureManager().isCurrencyEnabled()) {
					cost.append((int) fee).append(" x ").append(arena.getStructureManager().getCurrency().toString());
				} else {
					cost.append(fee);
				}
				Messages.sendMessage(player, Messages.trprefix + Messages.arenafee.replace("{FEE}", cost));
			} else {
				Messages.sendMessage(player, Messages.trprefix + Messages.arenanofee);
				return false;
			}
		}
		return true;
	}

	// spawn player on arena
	public void spawnPlayer(final Player player, String msgtoplayer, String msgtoarenaplayers) {
		// teleport player to arena
		plugin.pdata.storePlayerLocation(player);
		player.teleport(arena.getStructureManager().getSpawnPoint());
		// set player visible to everyone
		for (Player aplayer : Bukkit.getOnlinePlayers()) {
			aplayer.showPlayer(plugin, player);
		}
		// change player status
		plugin.pdata.storePlayerGameMode(player);
		plugin.pdata.storePlayerFlight(player);
		player.setFlying(false);
		player.setAllowFlight(false);
		plugin.pdata.storePlayerLevel(player);
		plugin.pdata.storePlayerInventory(player);
		plugin.pdata.storePlayerArmor(player);
		plugin.pdata.storePlayerPotionEffects(player);
		plugin.pdata.storePlayerHunger(player);
		
		if (plugin.isMCMMO() && !arena.getStructureManager().getDamageEnabled().equals(DamageEnabled.NO)) {
			allowFriendlyFire(player);
		}

		player.updateInventory();

		//set full countdown
		if (!arena.getStatusManager().isArenaStarting()) {
			arena.getGameHandler().count = arena.getStructureManager().getCountdown();
		}

		if (!plugin.getConfig().getBoolean("special.UseTitle")) {
			Messages.sendMessage(player, Messages.trprefix + msgtoplayer);
		}	
		// set player on arena data
		arena.getPlayersManager().add(player);
		// send message to other players
		for (Player oplayer : arena.getPlayersManager().getPlayers()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, Messages.trprefix + msgtoarenaplayers);
			// send title for players
			TitleMsg.sendFullTitle(oplayer, TitleMsg.join.replace("{PLAYER}", player.getName()), TitleMsg.subjoin.replace("{PLAYER}", player.getName()), 10, 20, 20, plugin);
		}

		// start cooldown and add leave item
		new BukkitRunnable() {
			@Override
			public void run(){
				addLeaveItem(player);
				
				if (plugin.getConfig().getBoolean("items.vote.use")) {
					addVote(player);
				}
				if (plugin.getConfig().getBoolean("items.shop.use")) {
					addShop(player);
				}
				if (plugin.getConfig().getBoolean("items.info.use")) {
					addInfo(player);
				}
				if (plugin.getConfig().getBoolean("items.stats.use")) {
					addStats(player);
				}
				if (plugin.isHeadsPlus() && plugin.getConfig().getBoolean("items.heads.use")) {
					addHeads(player);
				}
			}
		}.runTaskLater(plugin, 5L);

		if (plugin.getConfig().getBoolean("freedoublejumps.enabled")) {
			int amount = plugin.getConfig().getInt("freedoublejumps.amount", 0);
			if (amount > 0) {
				doublejumps.put(player.getName(), amount);
			}
		} else {
			if (plugin.shop.hasDoubleJumps(player)) {
				doublejumps.put(player.getName(), plugin.getConfig().getInt("doublejumps." + player.getName()));
			}
		}

		if (plugin.getConfig().getBoolean("special.UseBossBar")) {
			Bars.addPlayerToBar(player, arena.getArenaName());
		} else {
			String message = Messages.playerscountinarena;
			message = message.replace("{COUNT}", String.valueOf(arena.getPlayersManager().getPlayersCount()));
			Messages.sendMessage(player, Messages.trprefix + message);
		}

		plugin.signEditor.modifySigns(arena.getArenaName());
		arena.getScoreboardHandler().createWaitingScoreBoard();

		// modify bars
		if (!arena.getStatusManager().isArenaStarting()) {
			double progress = (double) arena.getPlayersManager().getPlayersCount() / arena.getStructureManager().getMinPlayers(); 
			
			Bars.setBar(arena, Bars.waiting, arena.getPlayersManager().getPlayersCount(), 0, progress, plugin);
			for (Player oplayer : arena.getPlayersManager().getPlayers()) {
				plugin.sound.NOTE_PLING(oplayer, 5, 999);
			}
		}
		// check for game start
		if (!arena.getStatusManager().isArenaStarting() && arena.getPlayersManager().getPlayersCount() == arena.getStructureManager().getMinPlayers()) {
			arena.getGameHandler().runArenaCountdown();
		}
	} 

	public void spectatePlayer(final Player player, String msgtoplayer, String msgtoarenaplayers) {
		// if existing spectator leaves bounds, send back to spectator spawn
		if (arena.getPlayersManager().isSpectator(player.getName())) {
			player.teleport(arena.getStructureManager().getSpectatorSpawn());
			return;
		}
		// remove form players
		arena.getPlayersManager().remove(player);
		arena.getGameHandler().lostPlayers++;
		arena.getScoreboardHandler().removeScoreboard(player);
		player.teleport(arena.getStructureManager().getSpectatorSpawn());
		// clear inventory and potion effects
		player.getInventory().clear();
		player.getInventory().setArmorContents(new ItemStack[4]);
		clearPotionEffects(player);
		player.setAllowFlight(true);
		player.setFlying(true);

		for (Player oplayer : Bukkit.getOnlinePlayers()) {
			oplayer.hidePlayer(plugin, player);
		}

		Messages.sendMessage(player, Messages.trprefix + msgtoplayer);
		plugin.signEditor.modifySigns(arena.getArenaName());
		// send message to other players and update bars
		for (Player oplayer : arena.getPlayersManager().getAllParticipantsCopy()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, Messages.trprefix + msgtoarenaplayers);
		}
		arena.getPlayersManager().addSpectator(player);
		// start cooldown and add leave item
		new BukkitRunnable() {
			@Override
			public void run(){
				addLeaveItem(player);

				if (plugin.getConfig().getBoolean("items.info.use")) {
					addInfo(player);
				}
				if (plugin.getConfig().getBoolean("items.stats.use")) {
					addStats(player);
				}
			}
		}.runTaskLater(plugin, 5L);
	}
	/**
	 * If the winner attempts to leave, teleport to arena spawn.
	 * For other players, if we have a spectator spawn then we will move player to spectators, otherwise we will remove player from arena.
	 * @param player
	 */
	public void dispatchPlayer(Player player) {
		if (arena.getPlayersManager().getPlayersCount() == 1) {
			player.teleport(arena.getStructureManager().getSpawnPoint());
		} else if (arena.getStructureManager().getSpectatorSpawnVector() != null) {
			spectatePlayer(player, Messages.playerlosttoplayer, Messages.playerlosttoothers);
		} else {
			leavePlayer(player, Messages.playerlosttoplayer, Messages.playerlosttoothers);
		}
	}

	// remove player from arena
	public void leavePlayer(Player player, String msgtoplayer, String msgtoarenaplayers) {
		// reset spectators
		boolean spectator = arena.getPlayersManager().isSpectator(player.getName());
		if (spectator) {
			arena.getPlayersManager().removeSpecator(player.getName());
			for (Player oplayer : Bukkit.getOnlinePlayers()) {
				oplayer.showPlayer(plugin, player);
			}
		} else if (arena.getStatusManager().isArenaRunning()) {
			arena.getGameHandler().lostPlayers++;
		}
		// disable flight for winner as well as spectators
		player.setAllowFlight(false);
		player.setFlying(false);

		arena.getScoreboardHandler().removeScoreboard(player);
		removePlayerFromArenaAndRestoreState(player, false);
		// should not send messages and other things when player is a spectator
		if (spectator) {
			return;
		}
		Messages.sendMessage(player, Messages.trprefix + msgtoplayer);
		plugin.signEditor.modifySigns(arena.getArenaName());
		if (!arena.getStatusManager().isArenaRunning()) {
			arena.getScoreboardHandler().createWaitingScoreBoard();
		}

		Bars.removeBar(player, arena.getArenaName());

		for (Player oplayer : arena.getPlayersManager().getAllParticipantsCopy()) {
			msgtoarenaplayers = msgtoarenaplayers.replace("{PLAYER}", player.getName());
			Messages.sendMessage(oplayer, Messages.trprefix + msgtoarenaplayers);
			if (!arena.getStatusManager().isArenaStarting() && !arena.getStatusManager().isArenaRunning()) {
				double progress = (double) arena.getPlayersManager().getPlayersCount() / arena.getStructureManager().getMinPlayers();
				Bars.setBar(arena, Bars.waiting, arena.getPlayersManager().getPlayersCount(), 0, progress, plugin);
			}
		}
	}

	protected void leaveWinner(Player player, String msgtoplayer) {
		arena.getScoreboardHandler().removeScoreboard(player);
		player.setFlying(false);
		removePlayerFromArenaAndRestoreState(player, true);
		Messages.sendMessage(player, Messages.trprefix + msgtoplayer);
		plugin.signEditor.modifySigns(arena.getArenaName());
		plugin.signEditor.refreshLeaderBoards();
	}

	private void removePlayerFromArenaAndRestoreState(Player player, boolean winner) {
		votes.remove(player.getName());
		Bars.removeBar(player, arena.getArenaName());
		resetDoubleJumps(player);
		arena.getPlayersManager().remove(player);
		clearPotionEffects(player);

		plugin.pdata.restorePlayerHunger(player);
		plugin.pdata.restorePlayerPotionEffects(player);
		plugin.pdata.restorePlayerArmor(player);
		plugin.pdata.restorePlayerInventory(player);
		plugin.pdata.restorePlayerLevel(player);
		// add player damage resistance
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 80, true), true);
		// restore location or teleport to lobby
		if (arena.getStructureManager().getTeleportDestination() == TeleportDestination.LOBBY && plugin.globallobby.isLobbyLocationWorldAvailable()) {
			player.teleport(plugin.globallobby.getLobbyLocation());
			plugin.pdata.clearPlayerLocation(player);
		} else {
			plugin.pdata.restorePlayerLocation(player);
		}
		// reward player before restoring gamemode if player is winner
		if (winner) {
			arena.getStructureManager().getRewards().rewardPlayer(player);
		}
		plugin.pdata.restorePlayerGameMode(player);
		player.updateInventory();
		plugin.pdata.restorePlayerFlight(player);
		removeFriendlyFire(player);

		if (plugin.getConfig().getBoolean("shop.onleave.removepurchase")) {
			removePurchase(player);
		}

		if (player.getGameMode() == GameMode.CREATIVE) {
			player.setAllowFlight(true);
		}		

		if (arena.getStatusManager().isArenaRunning() && arena.getPlayersManager().getPlayersCount() == 0) {
			arena.getGameHandler().stopArena();
		}
	}

	// vote for game start
	private HashSet<String> votes = new HashSet<String>();

	public boolean vote(Player player) {
		if (!votes.contains(player.getName())) {
			votes.add(player.getName());

			arena.getScoreboardHandler().createWaitingScoreBoard();
			if (!arena.getStatusManager().isArenaStarting() && forceStart()) {
				arena.getGameHandler().runArenaCountdown();
			}
			return true;
		}
		return false;
	}

	public boolean forceStart() {
		if (arena.getPlayersManager().getPlayersCount() > 1 && votes.size() >= arena.getStructureManager().getMinPlayers() * arena.getStructureManager().getVotePercent()) {
			return true;
		}
		if (arena.getGameHandler().isForceStartByCommand()) {
			return true;
		}
		return false;
	}

	private void addInfo(Player p) {
		ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("items.info.material")));	     
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(FormattingCodesParser.parseFormattingCodes(plugin.getConfig().getString("items.info.name")));
	    item.setItemMeta(meta);

	    p.getInventory().addItem(item);
	}

	private void addVote(Player p) {
		ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("items.vote.material")));     
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(FormattingCodesParser.parseFormattingCodes(plugin.getConfig().getString("items.vote.name")));
	    item.setItemMeta(meta);

	    p.getInventory().addItem(item);
	}

	private void addShop(Player p) {
		ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("items.shop.material"))); 
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(FormattingCodesParser.parseFormattingCodes(plugin.getConfig().getString("items.shop.name")));
	    item.setItemMeta(meta);
	    
	    p.getInventory().addItem(item);
	}

	private void addStats(Player p) {
		ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("items.stats.material")));
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(FormattingCodesParser.parseFormattingCodes(plugin.getConfig().getString("items.stats.name")));
	    item.setItemMeta(meta);

	    p.getInventory().addItem(item);
	}

	private void addHeads(Player p) {
		ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("items.heads.material")));
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(FormattingCodesParser.parseFormattingCodes(plugin.getConfig().getString("items.heads.name")));
	    item.setItemMeta(meta);

	    p.getInventory().addItem(item);
	}

	private void addLeaveItem(Player p) {
		// Old config files will have BED as leave item which is no longer valid on 1.13. Update any invalid material to valid one.
		Material leaveItem = Material.getMaterial(plugin.getConfig().getString("items.leave.material"));
		if (leaveItem == null) {
			leaveItem = Material.getMaterial("GREEN_BED");
			plugin.getConfig().set("items.leave.material", leaveItem.toString());
			plugin.saveConfig();
		}
		ItemStack item = new ItemStack(leaveItem);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(FormattingCodesParser.parseFormattingCodes(plugin.getConfig().getString("items.leave.name")));
		item.setItemMeta(im);

		p.getInventory().setItem(8, item);
	}

	public int getVotesCast() {
		return votes.size();
	}

	public void clearPotionEffects(Player player) {
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
	}

	public void allocateKits() {
		Random rnd = new Random();
		HashSet<String> kits = plugin.kitmanager.getKits();
		if (kits.size() > 0) {
			String[] kitnames = kits.toArray(new String[kits.size()]);
			for (Player player : arena.getPlayersManager().getPlayers()) {
				plugin.kitmanager.giveKit(kitnames[rnd.nextInt(kitnames.length)], player);
				//kits will replace the GUI items, so give each player the leave item again
				addLeaveItem(player);
			}
		}
	}

	public boolean hasDoubleJumps(Player player) {
		return getDoubleJumps(player) > 0;
	}

	public int getDoubleJumps(Player player) {
		return doublejumps.get(player.getName()) != null ? doublejumps.get(player.getName()) : 0;
	}

	public void decrementDoubleJumps(Player player) {
		if (getDoubleJumps(player) > 0) {
			doublejumps.put(player.getName(), getDoubleJumps(player) - 1);
		}
	}

	public void incrementDoubleJumps(Player player, Integer amount) {
		doublejumps.put(player.getName(), getDoubleJumps(player) + amount);
	}

	private void resetDoubleJumps(Player player) {
		if (!plugin.getConfig().getBoolean("freedoublejumps.enabled")) {
			if (hasDoubleJumps(player)) {
				plugin.getConfig().set("doublejumps." + player.getName(), getDoubleJumps(player));
			} else {
				plugin.getConfig().set("doublejumps." + player.getName(), null);
			}
			plugin.saveConfig();
		}
		doublejumps.remove(player.getName());
	}

	/**
	 * Allow players in mcMMO parties to PVP.
	 * If vault has detected a permissions plugin, then give the player the mcMMO friendly fire permission.
	 * @param player
	 */
	private void allowFriendlyFire(Player player) {
		if (!plugin.getVaultHandler().isPermissions()) {
			return;
		}
		if (!PartyAPI.inParty(player)) {
			return;
		}
		if (!plugin.getVaultHandler().getPermissions().playerHas(player, "mcmmo.party.friendlyfire")) {
			plugin.getVaultHandler().getPermissions().playerAdd(player, "mcmmo.party.friendlyfire");
			if (!pparty.contains(player.getName())) {
				pparty.add(player.getName());
			}
		}
	}

	/**
	 * Restore the player's mcMMO friendly fire permission.
	 * @param player
	 */
	private void removeFriendlyFire(Player player) {
		if (pparty.contains(player.getName())) {
			pparty.remove(player.getName());
			plugin.getVaultHandler().getPermissions().playerRemove(player, "mcmmo.party.friendlyfire");
		}
	}

	/**
	 * Remove the cached purchase for the player. This can be when the game starts and the
	 * player receives the item, or if the player leaves the arena before the game starts.
	 * @param player
	 */
	public void removePurchase(Player player ) {
		if (plugin.shop.getPlayersItems().containsKey(player.getName())) {
			plugin.shop.getPlayersItems().remove(player.getName());
			plugin.shop.getBuyers().remove(player.getName());
		}
		if (plugin.shop.getPotionEffects(player) != null) {
			plugin.shop.removePotionEffects(player);
		}
	}
}
