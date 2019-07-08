package tntrun.arena.handlers;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import tntrun.FormattingCodesParser;
import tntrun.TNTRun;
import tntrun.arena.Arena;

public class ScoreboardHandler {

	private final TNTRun plugin;
	private Scoreboard scoreboard;
	private HashMap<String, Scoreboard> scoreboardMap = new HashMap<String, Scoreboard>();
	private int playingtask;
	private Arena arena;

	public ScoreboardHandler(TNTRun plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
	}

	public Scoreboard buildScoreboard() {
		FileConfiguration config = plugin.getConfig();
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		if (config.getBoolean("special.UseScoreboard")) {
			Objective o = scoreboard.registerNewObjective("TNTRun", "waiting", "TNTRun");
			o.setDisplaySlot(DisplaySlot.SIDEBAR);
			
			String header = FormattingCodesParser.parseFormattingCodes(config.getString("scoreboard.header", ChatColor.GOLD.toString() + ChatColor.BOLD + "TNTRUN"));
			o.setDisplayName(header);
		}
		return scoreboard;
	} 

	public void createWaitingScoreBoard() {
		if(!plugin.getConfig().getBoolean("special.UseScoreboard")) {
			return;
		}
		for (Player player : arena.getPlayersManager().getPlayers()) {
			if (scoreboardMap.containsKey(player.getName())) {
				scoreboard = scoreboardMap.get(player.getName());
			} else {
				scoreboard = buildScoreboard();
				scoreboardMap.put(player.getName(), scoreboard);
			}
			resetScoreboard(player);
			Objective o = scoreboard.getObjective(DisplaySlot.SIDEBAR);
			try {
				int size = plugin.getConfig().getStringList("scoreboard.waiting").size();

				for(String s : plugin.getConfig().getStringList("scoreboard.waiting")) {
					s = FormattingCodesParser.parseFormattingCodes(s).replace("{ARENA}", arena.getArenaName());
					s = s.replace("{PS}", arena.getPlayersManager().getAllParticipantsCopy().size() + "");
					s = s.replace("{MPS}", arena.getStructureManager().getMaxPlayers() + "");
					s = s.replace("{COUNT}", arena.getGameHandler().count + "");
					s = s.replace("{VOTES}", getVotesRequired(arena) + "");
					o.getScore(s).setScore(size);
					size--;
				}
				player.setScoreboard(scoreboard);

			} catch (NullPointerException ex) {

			}
		}
	}

	private void resetScoreboard(Player player) {
		scoreboard = scoreboardMap.get(player.getName());
		for (String entry : new ArrayList<String>(scoreboard.getEntries())) {
			scoreboard.resetScores(entry);
		}
	}

	private Integer getVotesRequired(Arena arena) {
		int minPlayers = arena.getStructureManager().getMinPlayers();
		double votePercent = arena.getStructureManager().getVotePercent();
		int votesCast = arena.getPlayerHandler().getVotesCast();

		return (int) (Math.ceil(minPlayers * votePercent) - votesCast);
	}

	public void removeScoreboard(Player player) {
		scoreboardMap.remove(player.getName());
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public void createPlayingScoreBoard() {
		if(!plugin.getConfig().getBoolean("special.UseScoreboard")){
			return;	
		}
		playingtask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			public void run() {
				for (Player player : arena.getPlayersManager().getPlayers()) {
					resetScoreboard(player);
					Objective o = scoreboard.getObjective(DisplaySlot.SIDEBAR);

					int size = plugin.getConfig().getStringList("scoreboard.playing").size();
					for(String s : plugin.getConfig().getStringList("scoreboard.playing")) {
						s = FormattingCodesParser.parseFormattingCodes(s).replace("{ARENA}", arena.getArenaName());
						s = s.replace("{PS}", arena.getPlayersManager().getAllParticipantsCopy().size() + "");		
						s = s.replace("{MPS}", arena.getStructureManager().getMaxPlayers() + "");
						s = s.replace("{LOST}", arena.getGameHandler().lostPlayers + "");
						s = s.replace("{LIMIT}", arena.getGameHandler().getTimeLimit()/20 + "");
						o.getScore(s).setScore(size);
						size--;
					}
				}
			}
		}, 0, 20);
	}

	public int getPlayingTask() {
		return playingtask;
	}

}
