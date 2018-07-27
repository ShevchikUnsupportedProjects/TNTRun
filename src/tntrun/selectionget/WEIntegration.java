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

package tntrun.selectionget;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.world.World;


public class WEIntegration {

	private WorldEditPlugin weplugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
	private WorldEdit we = WorldEdit.getInstance();

	protected Location[] getLocations(Player player) {
		try {
			return getPlayerSelection(player);
		} catch (Exception e) {
		}
		return null;
	}

	private Location[] getPlayerSelection(Player player) {
		Location[] locs = new Location[2];
		//Selection psel = we.getSelection(player);
		//locs[0] = psel.getMinimumPoint();
		//locs[1] = psel.getMaximumPoint();
		
		BukkitPlayer bplayer = new BukkitPlayer(weplugin, player);
		SessionOwner so = (SessionOwner) bplayer;
		
		LocalSession session = we.getSessionManager().get(so);
		World world = session.getSelectionWorld();
		Region rg = null;
		try {
			rg = session.getSelection(world);
		} catch (IncompleteRegionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Vector v1 = rg.getMinimumPoint();
		Vector v2 = rg.getMaximumPoint();
		
		locs[0] = new Location(player.getWorld(), v1.getX(), v1.getY(), v1.getZ());
		locs[1] = new Location(player.getWorld(), v2.getX(), v2.getY(), v2.getZ());

		return locs;
	}

}
