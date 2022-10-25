package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

public interface DiscordGuildMessageReceivedEvent extends BindableEvent {
	MessageChannel getChannel();
	User getAuthor();
	Member getMember();
	Message getMessage();
}
