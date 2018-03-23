package tntrun.commands.setup.arena;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import tntrun.arena.Arena;
import tntrun.conversation.TNTRunConversation;


public class ArenaRewardConversation extends FixedSetPrompt {
	private Player player;
	private Arena arena;
	
	public ArenaRewardConversation(Player player, Arena arena) {
		super("material", "command", "xp");
	}
	
	@Override
	public String getPromptText(ConversationContext context) {
		return ChatColor.LIGHT_PURPLE + " What type of prize would you like to set?\n" 
				+ ChatColor.GREEN + formatFixedSet();
	}
	
	@Override
	protected Prompt acceptValidatedInput(ConversationContext context, String choice) {
		if (choice.equalsIgnoreCase("material"))
			return new ChooseMaterial();
	
		if (choice.equalsIgnoreCase("command"))
			return new ChooseCommand();

		if (choice.equalsIgnoreCase("xp"))
			return new ChooseXP();

		return null;
	}
	
	// Reward Material(s)
	private class ChooseMaterial extends StringPrompt {
		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.LIGHT_PURPLE + " What Material do you want to reward the player with?";
		}

		@Override
		public Prompt acceptInput(ConversationContext context, String message) {
			Material material = Material.getMaterial(message.toUpperCase());
			if (material == null){
				TNTRunConversation.sendErrorMessage(context, "This is not a valid material");
				return this;
			}

			context.setSessionData("material", message.toUpperCase());
			return new ChooseAmount();
		}		
	}

	private class ChooseAmount extends NumericPrompt {

		@Override
		public String getPromptText(ConversationContext context) {
			return ChatColor.LIGHT_PURPLE + " How many would you like to reward the player with?";
		}

		@Override
		protected boolean isNumberValid(ConversationContext context, Number input) {
			return input.intValue() > 0 && input.intValue() <= 255;
		}

		@Override
		protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
			return "Amount must be between 1 and 255.";
		}

		@Override
		protected Prompt acceptValidatedInput(ConversationContext context, Number amount) {
			context.setSessionData("amount", amount.intValue());

			return new MaterialProcessComplete();
		}

	}

	private class MaterialProcessComplete extends MessagePrompt {
		public String getPromptText(ConversationContext context) {
			arena.getStructureManager().getRewards().setItemsReward(
                    context.getSessionData("material").toString(),
                    context.getSessionData("amount").toString());

			return " The Material prize for " + ChatColor.DARK_AQUA + arena.toString() + ChatColor.WHITE + " was set to " + ChatColor.AQUA + context.getSessionData("amount") + " " + context.getSessionData("material");
		}

		@Override
		protected Prompt getNextPrompt(ConversationContext context) {
			return Prompt.END_OF_CONVERSATION;
		}
	}


}
