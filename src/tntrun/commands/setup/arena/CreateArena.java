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

package tntrun.commands.setup.arena;

import org.bukkit.entity.Player;

import tntrun.arena.Arena;
import tntrun.commands.setup.CommandHandlerInterface;
import tntrun.datahandler.ArenasManager;

public class CreateArena implements CommandHandlerInterface {

	@Override
	public boolean handleCommand(Player player, String[] args) {
		Arena arenac = ArenasManager.getInstance().getArenaByName(args[0]);
		if (arenac != null) {
			player.sendMessage("Arena already exists");
			return true;
		}
		Arena arena = new Arena(args[0]);
		ArenasManager.getInstance().registerArena(arena);
		player.sendMessage("Arena created");
		return true;
	}

	@Override
	public int getMinArgsLength() {
		return 1;
	}

}