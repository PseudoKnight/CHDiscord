package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public interface DiscordPrivateMessageReceivedEvent extends BindableEvent {
	User getAuthor();
	Message getMessage();
}
