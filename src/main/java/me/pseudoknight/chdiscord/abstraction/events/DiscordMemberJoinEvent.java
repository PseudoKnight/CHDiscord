package me.pseudoknight.chdiscord.abstraction.events;

import net.dv8tion.jda.api.entities.Member;

public interface DiscordMemberJoinEvent extends DiscordGuildEvent {
	Member getMember();
}
