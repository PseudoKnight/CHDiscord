package me.pseudoknight.chdiscord.abstraction.events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;

public interface DiscordReactionAddedEvent extends DiscordGuildChannelEvent {
	Member getReactor();
	long getMessageId();
	EmojiUnion getEmoji();
	long getMessageAuthorId();
}
