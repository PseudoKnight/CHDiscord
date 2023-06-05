package me.pseudoknight.chdiscord.abstraction.events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public interface DiscordMemberLeaveEvent extends DiscordGuildEvent {
	User getUser();
	Member getMember();
}
