package tntrun.utils;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sounds_1_9 extends Sounds {

	@Override
	public void NOTE_PLING(Player p, float volume, float pitch) {
		p.playSound(p.getLocation(), Sound.valueOf("BLOCK_NOTE_PLING"), volume, pitch);
	}

	@Override
	public void ENDER_DRAGON(Player p, float volume, float pitch) {
		p.playSound(p.getLocation(), Sound.valueOf("ENTITY_ENDERDRAGON_GROWL"), volume, pitch);
	}

	@Override
	public void WITHER_HURT(Player p, float volume, float pitch) {
		p.playSound(p.getLocation(), Sound.valueOf("ENTITY_WITHER_HURT"), volume, pitch);
	}	
}
