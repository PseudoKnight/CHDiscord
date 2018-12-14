package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public interface DiscordPrivateMessageReceivedEvent extends BindableEvent {
	User getAuthor();
	Message getMessage();
}
