package tntrun.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

import tntrun.TNTRun;
import tntrun.messages.Messages;

public class Shop implements Listener{
	
	private TNTRun pl;
	
	public Shop(TNTRun plugin){
		pl = plugin;
		ShopFiles.setShopItems();
		
		invsize = pl.getConfig().getInt("shop.size");
		invname = pl.getConfig().getString("shop.name").replace("&", "§");
	}  
	
	public static HashMap<Integer, Integer> itemSlot = new HashMap<Integer, Integer>();
	public static HashMap<Player, ArrayList<ItemStack>> pitems = new HashMap<Player, ArrayList<ItemStack>>();
	public static List<Player> bought = new ArrayList<Player>();
	public static String invname;
	public static int invsize; 
	
	private void giveItem(int slot, Player player, String title) {
		int kit = itemSlot.get(slot);
		ArrayList<ItemStack> item = new ArrayList<ItemStack>();
		FileConfiguration cfg = ShopFiles.getShopConfiguration();
		for(String items : cfg.getConfigurationSection(kit + ".items").getKeys(false)) {
			try {
				int ID = Integer.valueOf(cfg.getInt(kit + ".items." + items + ".ID"));
				int subID = Integer.valueOf(cfg.getInt(kit + ".items." + items + ".subID"));
				int amount = Integer.valueOf(cfg.getInt(kit + ".items." + items + ".amount"));
				String displayname = cfg.getString(kit + ".items." + items + ".displayname").replace("&", "§");
				List<String> lore = cfg.getStringList(kit + ".items." + items + ".lore");
				List<String> enchantments = cfg.getStringList(kit + ".items." + items + ".enchantments");
				
				if(!bought.contains(player)){
					bought.add(player);
				}
				
				item.add(getItem(ID, subID, amount, displayname, lore, enchantments));
				
				player.updateInventory();
				player.closeInventory();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		pitems.put(player, item);
	}

	  private ItemStack getItem(int ID, int subID, int amount, String displayname, List<String> lore, List<String> enchantments){
	    ItemStack item = new ItemStack(ID, amount, (short)subID);
	    ItemMeta meta = item.getItemMeta();
	    meta.setDisplayName(displayname);
	    if ((lore != null) && (!lore.isEmpty())) {
	      meta.setLore(lore);
	    }
	    if ((enchantments != null) && (!enchantments.isEmpty())) {
	      for (String enchs : enchantments) {
	        String[] array = enchs.split(":");
	        String ench = array[0];
	        int level = Integer.valueOf(array[1]).intValue();
	        Enchantment realEnch = Enchantment.getByName(ench);
	        meta.addEnchant(realEnch, level, true);
	      }
	    }
	    item.setItemMeta(meta);
	    return item;
	  }
	  @EventHandler
	  public void onClick(InventoryClickEvent e){
	    Player p = (Player)e.getWhoClicked();
	    if (e.getInventory().getName().equals(invname)) {
	      e.setCancelled(true);
	      if ((e.getSlot() == e.getRawSlot()) && 
	        (e.getCurrentItem() != null)) {
	        ItemStack current = e.getCurrentItem();
	        if ((current.hasItemMeta()) && (current.getItemMeta().hasDisplayName())) {
	          int kit = ((Integer)itemSlot.get(Integer.valueOf(e.getSlot()))).intValue();
	          FileConfiguration cfg = ShopFiles.getShopConfiguration();
	          String permission = cfg.getString(kit + ".permission");
	          if(bought.contains(p)){
	        	  p.sendMessage(Messages.alreadyboughtitem.replace("&", "§"));
	        	  TNTRun.getInstance().sound.WITHER_HURT(p, 5, 999);
	        	  return;
	          }
	          if (p.hasPermission(permission)) {
	        	  String title = current.getItemMeta().getDisplayName();
	        	  int cost = cfg.getInt(kit + ".cost");
	        	  
	        	  if(cfg.getInt(kit + ".ID") == 288){
						if((pl.getConfig().getInt("shop.doublejump.maxdoublejumps") <= pl.getConfig().getInt("doublejumps." + p.getName()))){
				        	  p.sendMessage(Messages.alreadyboughtitem.replace("&", "§"));
				        	  TNTRun.getInstance().sound.WITHER_HURT(p, 5, 999);
				        	  return;
						}
	        	  }
	        	  
	        	  if(hasMoney(cost, p)) {
	        		  p.sendMessage(Messages.playerboughtitem.replace("&", "§").replace("{ITEM}", title).replace("{MONEY}", cost + ""));
	        		  p.sendMessage(Messages.playerboughtwait.replace("&", "§"));
	  				  TNTRun.getInstance().sound.NOTE_PLING(p, 5, 10);
	        	  }else{
	        		  p.sendMessage(Messages.notenoughtmoney.replace("&", "§").replace("{MONEY}", cost + ""));
	        		  TNTRun.getInstance().sound.WITHER_HURT(p, 5, 999);
	        		  return;
	        	  }
		          if(cfg.getInt(kit + ".ID") == 288){
						if(pl.getConfig().get("doublejumps." + p.getName()) == null){
							pl.getConfig().set("doublejumps." + p.getName(), 1);
						}else{
							pl.getConfig().set("doublejumps." + p.getName(), pl.getConfig().getInt("doublejumps." + p.getName()) + 1);
						}
						pl.saveConfig();
						p.sendMessage(Messages.playerboughtitem.replace("&", "§").replace("{ITEM}", title).replace("{MONEY}", cost + ""));
						return;
		          }
	            giveItem(e.getSlot(), p, current.getItemMeta().getDisplayName());  
	          } else {
	            p.closeInventory();
	            p.sendMessage(Messages.nopermission.replace("&", "§"));
	            TNTRun.getInstance().sound.WITHER_HURT(p, 5, 999);
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
				Economy econ = (Economy) economy;
				double pmoney = econ.getBalance(player.getName());
				if(pmoney >= moneyneed){
					econ.withdrawPlayer(player.getName(), moneyneed);
					return true;
				}
			}
			return false;
		}
		
		  public static void setItems(Inventory inventory){
		    FileConfiguration cfg = ShopFiles.getShopConfiguration();
		    int slot = 0;
		    for (String kitCounter : cfg.getConfigurationSection("").getKeys(false)) {
		      String title = cfg.getString(kitCounter + ".name").replace("&", "§");
		      List<String> lore = new ArrayList<String>();
		      for (String loreLines : cfg.getStringList(kitCounter + ".lore")) {
		    	  loreLines = loreLines.replace("&", "§");
		        lore.add(loreLines);
		      }
		      int ID = cfg.getInt(kitCounter + ".ID");
		      int subID = cfg.getInt(kitCounter + ".subID");
		      int amount = cfg.getInt(kitCounter + ".amount");
		      inventory.setItem(slot, getItem(ID, subID, title, lore, amount));
		      itemSlot.put(Integer.valueOf(slot), Integer.valueOf(kitCounter));
		      slot++;
		    }
		  }

		  private static ItemStack getItem(int ID, int subID, String title, List<String> lore, int amount){
		    ItemStack item = new ItemStack(ID, amount, (short)subID);
		    ItemMeta meta = item.getItemMeta();
		    meta.setDisplayName(title);
		    if ((lore != null) && (!lore.isEmpty())) {
		      meta.setLore(lore);
		    }
		    item.setItemMeta(meta);
		    return item;
		  }
}
