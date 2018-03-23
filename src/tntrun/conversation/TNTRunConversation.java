package tntrun.conversation;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import tntrun.TNTRun;
import tntrun.arena.Arena;
import tntrun.commands.setup.arena.ArenaRewardConversation;

public class TNTRunConversation implements ConversationAbandonedListener {
	private ConversationFactory conversationFactory;

    //private String courseName;
    private Player player;
    private TNTRun plugin;
    private Arena arena;

    public TNTRunConversation(TNTRun plugin, Player player, Arena arena, ConversationType conversationType){
        this.player = player;
        this.plugin = plugin;
        this.arena = arena;

        conversationFactory = new ConversationFactory(this.plugin)
                .withEscapeSequence("cancel")
                .withTimeout(30)
                .thatExcludesNonPlayersWithMessage("This is only possible in game, sorry.")
                .addConversationAbandonedListener(this)
                .withFirstPrompt(getEntryPrompt(conversationType, player));
    }

    private Prompt getEntryPrompt(ConversationType type, Player player) {
        player.sendMessage(ChatColor.GRAY + "Note: Enter 'cancel' to quit the conversation.");
        switch (type){
            case ARENAPRIZE:
                return new ArenaRewardConversation(player, arena);
            default:
                player.sendMessage(ChatColor.RED + "Something went wrong.");
                return null;
        }
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent event) {
        if (!event.gracefulExit())
            event.getContext().getForWhom().sendRawMessage("§7[§6TNTRun§7] §cConversation aborted...");
    }

    public static void sendErrorMessage(ConversationContext context, String message) {
        context.getForWhom().sendRawMessage("§7[§6TNTRun§7] §c" + message + ". Please try again...");
    }

    /*
    public ParkourConversation withCourseName(String courseName) {
        this.courseName = courseName;
        return this;
    }*/

    public void begin() {
        Conversation convo = conversationFactory.buildConversation(player);
        convo.getContext().setSessionData("playerName", player.getName());
        /*if (courseName != null)
            convo.getContext().setSessionData("courseName", courseName);*/

        convo.begin();
    }

}
