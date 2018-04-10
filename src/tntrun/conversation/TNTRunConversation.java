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

public class TNTRunConversation implements ConversationAbandonedListener {
	private ConversationFactory conversationFactory;

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
                .thatExcludesNonPlayersWithMessage("This is only possible in-game, sorry.")
                .addConversationAbandonedListener(this)
                .withFirstPrompt(getEntryPrompt(conversationType, player));
    }

    private Prompt getEntryPrompt(ConversationType type, Player player) {
        player.sendMessage(ChatColor.GRAY + "Enter 'cancel' anytime to quit the conversation.");
        switch (type){
            case ARENAPRIZE:
                return new ArenaRewardConversation(arena);
            default:
                player.sendMessage("§7[§6TNTRun§7] §cUnexpected conversation type: " + type);
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

    public void begin() {
        Conversation convo = conversationFactory.buildConversation(player);
        convo.getContext().setSessionData("playerName", player.getName());
        convo.begin();
    }

}
