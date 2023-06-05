package me.pseudoknight.chdiscord.abstraction.events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public interface DiscordGuildMessageReceivedEvent extends DiscordGuildChannelEvent {
	User getAuthor();
	Member getMember();
	Message getMessage();
}
