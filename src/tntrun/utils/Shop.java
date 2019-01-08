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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import tntrun.FormattingCodesParser;
import tntrun.TNTRun;
import tntrun.messages.Messages;

public class Shop implements Listener{
	
	private static TNTRun pl;
	
	public Shop(TNTRun plugin){
		pl = plugin;
		ShopFiles shopFiles = new ShopFiles(pl);
		shopFiles.setShopItems();
		
		invsize = pl.getConfig().getInt("shop.size");
		invname = FormattingCodesParser.parseFormattingCodes(pl.getConfig().getString("shop.name"));
	}  
	
	public static HashMap<Integer, Integer> itemSlot = new HashMap<Integer, Integer>();
	public static HashMap<Player, ArrayList<ItemStack>> pitems = new HashMap<Player, ArrayList<ItemStack>>();
	public static List<Player> bought = new ArrayList<Player>();
	public static String invname;
	public static int invsize; 
	
	private static HashMap<Player, List<PotionEffect>> potionMap = new HashMap<Player, List<PotionEffect>>();
	
	private void giveItem(int slot, Player player, String title) {
		int kit = itemSlot.get(slot);		
		ArrayList<ItemStack> item = new ArrayList<ItemStack>();
		FileConfiguration cfg = ShopFiles.getShopConfiguration();
		List<PotionEffect> pelist = new ArrayList<PotionEffect>();
		
		for(String items : cfg.getConfigurationSection(kit + ".items").getKeys(false)) {
			try {				
				Material material = Material.getMaterial(cfg.getString(kit + ".items." + items + ".material"));
				int amount = Integer.valueOf(cfg.getInt(kit + ".items." + items + ".amount"));
				
				List<String> enchantments = cfg.getStringList(kit + ".items." + items + ".enchantments");
				
				if(!bought.contains(player)){
					bought.add(player);
				}
				// if the item is a potion, store the potion effect and skip to next item
				if (material.toString().equalsIgnoreCase("POTION")) {
					if (enchantments != null && !enchantments.isEmpty()) {
				    	for (String peffects : enchantments) {
				    		String[] array = peffects.split("#");
				    		String peffect = array[0].toUpperCase();
				    		
				    		// get duration of effect
				    		int duration = 30;
				    		if (array.length > 1) {
				    			duration = Integer.valueOf(array[1]).intValue();
				    		}
				    		
				    		PotionEffect effect = new PotionEffect(PotionEffectType.getByName(peffect), duration * 20, 1);
				    		if (effect != null) {
				    			pelist.add(effect);
				    		}
				    	}
					}
					player.updateInventory();
					player.closeInventory();
					continue;
				}
				
				String displayname = FormattingCodesParser.parseFormattingCodes(cfg.getString(kit + ".items." + items + ".displayname"));
				List<String> lore = cfg.getStringList(kit + ".items." + items + ".lore");
				
				if (material.toString().equalsIgnoreCase("SPLASH_POTION")) {
					item.add(getPotionItem(material, amount, displayname, lore, enchantments));
				} else {
					item.add(getItem(material, amount, displayname, lore, enchantments));
				}
				player.updateInventory();
				player.closeInventory();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		pitems.put(player, item);
		potionMap.put(player, pelist);
	}

	private ItemStack getItem(Material material, int amount, String displayname, List<String> lore, List<String> enchantments){
		
		ItemStack item = new ItemStack(material, amount);
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(displayname);
	    
	    if (lore != null && !lore.isEmpty()) {
	      meta.setLore(lore);
	    }
	    
	    if (enchantments != null && !enchantments.isEmpty()) {
	    	for (String enchs : enchantments) {
	    		String[] array = enchs.split("#");
	    		String ench = array[0].toUpperCase();
	    		
	    		// get the enchantment level
	    		int level = 1;
	    		if (array.length > 1) {
	    			level = Integer.valueOf(array[1]).intValue();
	    		}
	    		Enchantment realEnch = getEnchantmentFromString(ench);
	    		if (realEnch != null) {
	    			meta.addEnchant(realEnch, level, true);
	    		}
	    	}
	    }
	    item.setItemMeta(meta);
	    return item;
	}
	
	private ItemStack getPotionItem(Material material, int amount, String displayname, List<String> lore, List<String> enchantments) {
		ItemStack item = new ItemStack(material, amount);
		PotionMeta potionmeta = (PotionMeta) item.getItemMeta();
		
		potionmeta.setDisplayName(displayname);
		potionmeta.setColor(Color.RED);
	    
	    if (lore != null && !lore.isEmpty()) {
	      potionmeta.setLore(lore);
	    }
	    
	    if (enchantments != null && !enchantments.isEmpty()) {
	    	for (String peffects : enchantments) {
	    		String[] array = peffects.split("#");
	    		String peffect = array[0].toUpperCase();
	    		
	    		// get duration of effect
	    		int duration = 30;
	    		if (array.length > 1) {
	    			duration = Integer.valueOf(array[1]).intValue();
	    		}
	    		
	    		PotionEffect effect = new PotionEffect(PotionEffectType.getByName(peffect), duration * 20, 1);
	    		if (effect != null) {
	    			potionmeta.addCustomEffect(effect, true);
	    		}
	    	}
	    }
	    item.setItemMeta(potionmeta);
	    return item;
	}
	@EventHandler
	public void onClick(InventoryClickEvent e) {
	    Player p = (Player)e.getWhoClicked();
	    
	    if (e.getInventory().getName().equals(invname)) { 	
	    	e.setCancelled(true);
	    	if (e.getSlot() == e.getRawSlot() && e.getCurrentItem() != null) {
	    		ItemStack current = e.getCurrentItem();
	    		if (current.hasItemMeta() && current.getItemMeta().hasDisplayName()) {
	    			int kit = ((Integer)itemSlot.get(Integer.valueOf(e.getSlot()))).intValue();

	    			FileConfiguration cfg = ShopFiles.getShopConfiguration();
	    			String permission = cfg.getString(kit + ".permission");
	    			
	    			if (bought.contains(p)) {
	    				Messages.sendMessage(p, Messages.alreadyboughtitem);
	    				TNTRun.getInstance().sound.ITEM_SELECT(p);
	    				return;
	    			}
	    			if (p.hasPermission(permission) || p.hasPermission("tntrun.shop")) {
	    				String title = current.getItemMeta().getDisplayName();
	    				int cost = cfg.getInt(kit + ".cost");
	        	  
	    				if (Material.getMaterial(cfg.getString(kit + ".material").toUpperCase()) == Material.FEATHER) {
	    					if (pl.getConfig().getInt("shop.doublejump.maxdoublejumps") <= pl.getConfig().getInt("doublejumps." + p.getName())) {
	    						Messages.sendMessage(p, Messages.alreadyboughtitem);
	    						TNTRun.getInstance().sound.ITEM_SELECT(p);
	    						return;
	    					}
	    				}
	        	  
	    				if (hasMoney(cost, p)) {
	    					Messages.sendMessage(p, Messages.playerboughtitem.replace("{ITEM}", title).replace("{MONEY}", cost + ""));
	    					Messages.sendMessage(p, Messages.playerboughtwait);
	    					TNTRun.getInstance().sound.NOTE_PLING(p, 5, 10);
	    				} else {
	    					Messages.sendMessage(p, Messages.notenoughtmoney.replace("{MONEY}", cost + ""));
	    					TNTRun.getInstance().sound.ITEM_SELECT(p);
	    					return;
	    				}
	    				if (Material.getMaterial(cfg.getString(kit + ".material").toUpperCase()) == Material.FEATHER) {
	    					if(pl.getConfig().get("doublejumps." + p.getName()) == null){
	    						pl.getConfig().set("doublejumps." + p.getName(), 1);
	    					}else{
	    						pl.getConfig().set("doublejumps." + p.getName(), pl.getConfig().getInt("doublejumps." + p.getName()) + 1);
	    					}
	    					pl.saveConfig();
	    					return;
	    				}
	    				giveItem(e.getSlot(), p, current.getItemMeta().getDisplayName());  
	    			} else {
	    				p.closeInventory();
	    				Messages.sendMessage(p, Messages.nopermission);
	    				TNTRun.getInstance().sound.ITEM_SELECT(p);
	    			}
	    		}
	    	}
	    }
	}
	  
	private Object economy = null;
		
	private boolean hasMoney(int moneyneed, Player player) {
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
		}
			 
		if (economy != null) {
			OfflinePlayer offplayer = player.getPlayer();
			Economy econ = (Economy) economy;
			double pmoney = econ.getBalance(offplayer);
			if(pmoney >= moneyneed){
				econ.withdrawPlayer(offplayer, moneyneed);
				return true;
			}
		}
		return false;
	}
		
	public static void setItems(Inventory inventory){
		FileConfiguration cfg = ShopFiles.getShopConfiguration();
		int slot = 0;
		for (String kitCounter : cfg.getConfigurationSection("").getKeys(false)) {
		      String title = FormattingCodesParser.parseFormattingCodes(cfg.getString(kitCounter + ".name"));
		      List<String> lore = new ArrayList<String>();
		      for (String loreLines : cfg.getStringList(kitCounter + ".lore")) {
		          lore.add(FormattingCodesParser.parseFormattingCodes(loreLines));
		      }
		      Material material = Material.getMaterial(cfg.getString(kitCounter + ".material"));		      
		      int amount = cfg.getInt(kitCounter + ".amount");

		      if (material.toString().equalsIgnoreCase("POTION") || material.toString().equalsIgnoreCase("SPLASH_POTION")) {
		    	  inventory.setItem(slot, getShopPotionItem(material, title, lore, amount));
		      } else {
		    	  inventory.setItem(slot, getShopItem(material, title, lore, amount));
		      }
		      itemSlot.put(Integer.valueOf(slot), Integer.valueOf(kitCounter));
		      slot++;
		}
	}

	private static ItemStack getShopItem(Material material, String title, List<String> lore, int amount){
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		
		meta.setDisplayName(title);
		if ((lore != null) && (!lore.isEmpty())) {
			meta.setLore(lore);
		}
		item.setItemMeta(meta);
		return item;
	}
	
	private static ItemStack getShopPotionItem(Material material, String title, List<String> lore, int amount) {
		ItemStack item = new ItemStack(material, amount);
		PotionMeta potionmeta = (PotionMeta) item.getItemMeta();
		
		potionmeta.setDisplayName(title);
		potionmeta.setColor(Color.BLUE);
		if (material.toString().equalsIgnoreCase("SPLASH_POTION")) {
			potionmeta.setColor(Color.RED);
		}
		potionmeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		if ((lore != null) && (!lore.isEmpty())) {
			potionmeta.setLore(lore);
		}
		item.setItemMeta(potionmeta);
		return item;
	}
	
	private Enchantment getEnchantmentFromString(String enchantment) {		
	    return Enchantment.getByKey(NamespacedKey.minecraft(enchantment.toLowerCase()));
	}
	
	public static List<PotionEffect> getPotionEffects(Player player) {
		return potionMap.get(player);
	}
	
	public static void removePotionEffects(Player player) {
		potionMap.remove(player);
	}
}
