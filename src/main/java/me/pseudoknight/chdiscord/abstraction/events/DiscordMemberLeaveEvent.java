package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public interface DiscordMemberLeaveEvent extends BindableEvent {
	User getUser();
	Member getMember();
}
