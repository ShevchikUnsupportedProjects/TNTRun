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

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import tntrun.utils.Utils;

public class TNTRunPlaceholders extends PlaceholderExpansion {
	private final TNTRun plugin;

	public TNTRunPlaceholders(TNTRun plugin) {
		this.plugin = plugin;
	}
    @Override
    public boolean canRegister() {
        return true;
    }
    
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String getIdentifier() {
        return "tntrun";
    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {

        if (p == null) {
        	return "";
        }
    	if (identifier.equals("version")) {
        	return String.valueOf(plugin.getDescription().getVersion());
        	
    	} else if (identifier.equals("arena_count")) {
        	return String.valueOf(plugin.amanager.getArenas().size());
        	
    	} else if (identifier.equals("played")) {
        	return String.valueOf(plugin.stats.getPlayedGames(p));
        	
        } else if (identifier.equals("wins")) {
            return String.valueOf(plugin.stats.getWins(p));
            
        } else if (identifier.equals("losses")) {
            return String.valueOf(plugin.stats.getLosses(p));
        	
        } else if (identifier.equals("player_count")) {
        	return String.valueOf(Utils.playerCount());      	
        }
        return null;
    }

}
