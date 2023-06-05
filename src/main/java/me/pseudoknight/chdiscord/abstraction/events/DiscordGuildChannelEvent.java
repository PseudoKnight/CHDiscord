package me.pseudoknight.chdiscord.abstraction.events;

import net.dv8tion.jda.api.entities.channel.Channel;

public interface DiscordGuildChannelEvent extends DiscordGuildEvent {
	Channel getChannel();
}
