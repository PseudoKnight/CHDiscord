package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public interface DiscordVoiceUpdateEvent extends BindableEvent {
	AudioChannel getChannelJoined();
	AudioChannel getChannelLeft();
	Member getMember();
}
