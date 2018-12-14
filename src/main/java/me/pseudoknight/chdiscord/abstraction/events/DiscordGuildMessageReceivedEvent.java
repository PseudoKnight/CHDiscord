package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public interface DiscordGuildMessageReceivedEvent extends BindableEvent {
	MessageChannel getChannel();
	User getAuthor();
	Member getMember();
	Message getMessage();
}
