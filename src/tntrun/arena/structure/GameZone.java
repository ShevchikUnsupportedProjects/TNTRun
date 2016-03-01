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

package tntrun.arena.structure;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import me.tade.trgrenade.Main;
import me.tade.trgrenade.ParticleEffect;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;

import tntrun.TNTRun;
import tntrun.arena.Arena;

public class GameZone {

	private HashSet<Block> blockstodestroy = new HashSet<Block>();
	
	public Arena arena;
	
	public GameZone(Arena arena){
		this.arena = arena;
	}
	
	private final int SCAN_DEPTH = 2;
	public void destroyBlock(Location loc) {
		int y = loc.getBlockY() + 1;
		Block block = null;
		for (int i = 0; i <= SCAN_DEPTH; i++) {
			block = getBlockUnderPlayer(y, loc);
			y--;
			if (block != null) {
				break;
			}
		}
		if (block != null) {
			final Block fblock = block;
			if (!blockstodestroy.contains(fblock)) {
				blockstodestroy.add(fblock);
				Bukkit.getScheduler().scheduleSyncDelayedTask(
					TNTRun.getInstance(),
					new Runnable() {
						@Override
						public void run() {
							if (arena.getStatusManager().isArenaRunning()) {
								blockstodestroy.remove(fblock);
								if(TNTRun.getInstance().getConfig().getBoolean("special.FancyBlockBreak")){
									fblock.getWorld().playEffect(fblock.getLocation(), Effect.STEP_SOUND, fblock.getTypeId());
								}
								removeGLBlocks(fblock);
							}
						}
					}, arena.getStructureManager().getGameLevelDestroyDelay()
				);
			}
		}
	}

	public void regenNow() {
		final Iterator<String> bsit = B.iterator();
		while (bsit.hasNext()) {
			String bl = bsit.next();
			String[] bd = bl.split(":");
			
			int id = Integer.parseInt(bd[0]);
			byte data = Byte.parseByte(bd[1]);
			World world = Bukkit.getWorld(bd[2]);
			int x = Integer.parseInt(bd[3]);
			int y = Integer.parseInt(bd[4]);
			int z = Integer.parseInt(bd[5]);
			
			world.getBlockAt(x, y, z).setTypeId(id);
			world.getBlockAt(x, y, z).setData(data);
		}
	}
	
	private void removeGLBlocks(Block block) {
		saveBlock(block);
		block = block.getRelative(BlockFace.DOWN);
		saveBlock(block);
	}
	
	public void removeBlocksGrenade(Location loc) {
		for(Block b : Main.getBlockInRadius(loc, 2.5D, 999.9D).keySet()){
			if (!blockstodestroy.contains(b)) {
				if(b.getLocation().add(0, 1, 0).getBlock().getType() == Material.AIR){
					if(Main.getInstance().mat.contains(b.getType())){
						removeGLBlocks(b);
						if(Main.getInstance().getConfig().getBoolean("useparticles")){
							ParticleEffect.FIREWORKS_SPARK.display(0.5F, 0.5F, 0.5F, 0.05F, 10, b.getLocation(), 50);
						}
					}
				}
			}
		}
	}
	
	private static double PLAYER_BOUNDINGBOX_ADD = 0.3;
	
	private Block getBlockUnderPlayer(int y, Location location) {
		PlayerPosition loc = new PlayerPosition(location.getX(), y, location.getZ());
		Block b11 = loc.getBlock(location.getWorld(), +PLAYER_BOUNDINGBOX_ADD, -PLAYER_BOUNDINGBOX_ADD);
		if (b11.getType() != Material.AIR) {
			return b11;
		}
		Block b12 = loc.getBlock(location.getWorld(), -PLAYER_BOUNDINGBOX_ADD, +PLAYER_BOUNDINGBOX_ADD);
		if (b12.getType() != Material.AIR) {
			return b12;
		}
		Block b21 = loc.getBlock(location.getWorld(), +PLAYER_BOUNDINGBOX_ADD, +PLAYER_BOUNDINGBOX_ADD);
		if (b21.getType() != Material.AIR) {
			return b21;
		}
		Block b22 = loc.getBlock(location.getWorld(), -PLAYER_BOUNDINGBOX_ADD, -PLAYER_BOUNDINGBOX_ADD);
		if (b22.getType() != Material.AIR) {
			return b22;
		}
		return null;
	}

	private final int MAX_BLOCKS_PER_TICK = 20;
	
	private static List<String> B = new LinkedList<>();
	
	public void saveBlock(Block b){
		String block = b.getTypeId() + ":" + b.getData() + ":" + b.getWorld().getName() + 
				":" + b.getX() + ":" + b.getY() + ":" + b.getZ();
		B.add(block);
		b.setType(Material.AIR);
	}
	
	public int regen(){
		final Iterator<String> bsit = B.iterator();
		final BukkitTask ta = new BukkitRunnable() {
            @Override
            public void run() {
            	for(int i = MAX_BLOCKS_PER_TICK; i >= 0;i--){
                	while(bsit.hasNext()) {
    					String bl = bsit.next();
    					String[] bd = bl.split(":");
    					
    					int id = Integer.parseInt(bd[0]);
    					byte data = Byte.parseByte(bd[1]);
    					World world = Bukkit.getWorld(bd[2]);
    					int x = Integer.parseInt(bd[3]);
    					int y = Integer.parseInt(bd[4]);
    					int z = Integer.parseInt(bd[5]);
    					
    					world.getBlockAt(x, y, z).setTypeId(id);
    					world.getBlockAt(x, y, z).setData(data);
    				}	
            	}
            }
        }.runTaskTimer(TNTRun.getInstance(), 0L, 1L);
		/*for (;ticks <= (B.size() / MAX_BLOCKS_PER_TICK) + 1; ticks++) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(TNTRun.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						try{
							while (bsit.hasNext()) {
								String bl = bsit.next();
								String[] bd = bl.split(":");
								
								int id = Integer.parseInt(bd[0]);
								byte data = Byte.parseByte(bd[1]);
								World world = Bukkit.getWorld(bd[2]);
								int x = Integer.parseInt(bd[3]);
								int y = Integer.parseInt(bd[4]);
								int z = Integer.parseInt(bd[5]);
								
								world.getBlockAt(x, y, z).setTypeId(id);
								world.getBlockAt(x, y, z).setData(data);
							}
						}catch(ConcurrentModificationException e){
							
						}
					}
				},
				ticks
			);
		}*/
        Bukkit.getScheduler().runTaskLater(TNTRun.getInstance(), new Runnable(){
        	public void run(){
        		ta.cancel();
        	}
        }, 50L);
		return 50;
	}
	
	private static class PlayerPosition {

		private double x;
		private int y;
		private double z;

		public PlayerPosition(double x, int y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Block getBlock(World world, double addx, double addz) {
			return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
		}

	}
}
