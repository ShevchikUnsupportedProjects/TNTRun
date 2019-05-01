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

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;
import tntrun.TNTRun;

public class VaultHandler {

	private Economy economy;
	private final TNTRun plugin;

	public VaultHandler(TNTRun plugin) {
		this.plugin = plugin;
		setupVaultEconomy();
	}

	private void setupVaultEconomy() {
		Plugin Vault = plugin.getServer().getPluginManager().getPlugin("Vault");
		if (Vault != null) {
			plugin.getLogger().info("Successfully linked with Vault, version " + Vault.getDescription().getVersion());
		} else {
			plugin.getLogger().info("Vault plugin not found, economy disabled");
			economy = null;
			return;
		}
		
		final RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp != null) {
            plugin.getLogger().info("Vault economy enabled.");
            economy = rsp.getProvider();
        } else {
            plugin.getLogger().info("Vault economy not detected.");
            economy = null;
        }
	}

	public boolean isEnabled() {
		return economy != null;
	}

	public Economy getEconomy() {
		return economy;
	}
}
