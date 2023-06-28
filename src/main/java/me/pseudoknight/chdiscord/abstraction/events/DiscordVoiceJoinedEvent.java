package me.pseudoknight.chdiscord.abstraction.events;

import net.dv8tion.jda.api.entities.Member;

public interface DiscordVoiceJoinedEvent extends DiscordGuildChannelEvent {
	Member getMember();
}
