package me.pseudoknight.chdiscord.abstraction.events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

public interface DiscordVoiceMovedEvent extends DiscordGuildChannelEvent {
	Member getMember();
	AudioChannelUnion getChannelLeft();
}
