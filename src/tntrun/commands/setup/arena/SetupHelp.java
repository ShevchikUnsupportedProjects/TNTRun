package tntrun.commands.setup.arena;

import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.commands.setup.CommandHandlerInterface;
import tntrun.messages.Messages;

public class SetupHelp implements CommandHandlerInterface {

	@SuppressWarnings("unused")
	private TNTRun plugin;
	public SetupHelp(TNTRun plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean handleCommand(Player player, String[] args) {
		
		player.sendMessage("§7============[§6TNTRun§7]============");
		Messages.sendMessage(player, Messages.setuphelp);
		Messages.sendMessage(player, "§6/trsetup setlobby §f- §c" + Messages.setuplobby);
		Messages.sendMessage(player, "§6/trsetup create {arena} §f- §c" + Messages.setupcreate);
		Messages.sendMessage(player, "§6/trsetup setarena {arena} §f- §c" + Messages.setupbounds);
		Messages.sendMessage(player, "§6/trsetup setloselevel {arena} §f- §c" + Messages.setuploselevel);
		Messages.sendMessage(player, "§6/trsetup setspawn {arena} §f- §c" + Messages.setupspawn);
		Messages.sendMessage(player, "§6/trsetup setspectate {arena} §f- §c" + Messages.setupspectate);
		Messages.sendMessage(player, "§6/trsetup finish {arena} §f- §c" + Messages.setupfinish);
		player.sendMessage("Create a join sign for the arena to complete the setup.");
		return true;		
	}

	@Override
	public int getMinArgsLength() {
		return 0;
	}
	
}
