package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.api.entities.Member;

public interface DiscordMemberJoinEvent extends BindableEvent {
	Member getMember();
}
