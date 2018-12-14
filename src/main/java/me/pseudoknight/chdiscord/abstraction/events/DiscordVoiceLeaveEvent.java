package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;

public interface DiscordVoiceLeaveEvent extends BindableEvent {
	VoiceChannel getChannel();
	Member getMember();
}
