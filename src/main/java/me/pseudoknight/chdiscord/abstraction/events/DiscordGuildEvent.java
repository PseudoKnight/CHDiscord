package me.pseudoknight.chdiscord.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;
import net.dv8tion.jda.api.entities.Guild;

public interface DiscordGuildEvent extends BindableEvent {
	Guild getGuild();
}
