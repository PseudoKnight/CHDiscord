package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;

public interface DiscordVoiceLeaveEvent extends BindableEvent {
	AudioChannel getChannel();
	Member getMember();
}
