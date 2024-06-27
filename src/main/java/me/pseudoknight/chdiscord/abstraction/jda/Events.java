package me.pseudoknight.chdiscord.abstraction.jda;

import me.pseudoknight.chdiscord.abstraction.events.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;

public class Events {
	public static class JDADiscordGuildEvent implements DiscordGuildEvent {
		private final GenericGuildEvent e;

		JDADiscordGuildEvent(GenericGuildEvent e) {
			this.e = e;
		}

		@Override
		public Guild getGuild() {
			return e.getGuild();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordGuildMessageReceivedEvent implements DiscordGuildMessageReceivedEvent {
		private final MessageReceivedEvent e;

		JDADiscordGuildMessageReceivedEvent(MessageReceivedEvent e) {
			this.e = e;
		}

		public Channel getChannel() {
			return e.getChannel();
		}

		public User getAuthor() {
			return e.getAuthor();
		}

		public Member getMember() {
			return e.getMember();
		}

		public Message getMessage() {
			return e.getMessage();
		}

		@Override
		public Guild getGuild() {
			return e.getGuild();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordPrivateMessageReceivedEvent implements DiscordPrivateMessageReceivedEvent {
		private final MessageReceivedEvent e;

		JDADiscordPrivateMessageReceivedEvent(MessageReceivedEvent e) {
			this.e = e;
		}

		public User getAuthor() {
			return e.getAuthor();
		}

		public Message getMessage() {
			return e.getMessage();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordGuildMessageUpdatedEvent implements DiscordGuildMessageUpdatedEvent {
		private final MessageUpdateEvent e;

		JDADiscordGuildMessageUpdatedEvent(MessageUpdateEvent e) {
			this.e = e;
		}

		public Channel getChannel() {
			return e.getChannel();
		}

		public User getAuthor() {
			return e.getAuthor();
		}

		public Member getMember() {
			return e.getMember();
		}

		public Message getMessage() {
			return e.getMessage();
		}

		@Override
		public Guild getGuild() {
			return e.getGuild();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordReactionAddedEvent implements DiscordReactionAddedEvent {
		private final MessageReactionAddEvent e;
		private final Member m;

		JDADiscordReactionAddedEvent(MessageReactionAddEvent e, Member m) {
			this.e = e;
			this.m = m;
		}

		public MessageChannel getChannel() {
			return e.getChannel();
		}

		public Member getReactor() {
			return m;
		}

		public long getMessageId() {
			return e.getMessageIdLong();
		}

		public Guild getGuild() {
			return e.getGuild();
		}

		public EmojiUnion getEmoji() {
			return e.getEmoji();
		}

		@Override
		public long getMessageAuthorId() {
			return e.getMessageAuthorIdLong();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}


	public static class JDADiscordReactionRemovedEvent implements DiscordReactionRemovedEvent {
		private final MessageReactionRemoveEvent e;
		private final Member m;

		JDADiscordReactionRemovedEvent(MessageReactionRemoveEvent e, Member m) {
			this.e = e;
			this.m = m;
		}

		public MessageChannel getChannel() {
			return e.getChannel();
		}

		public Member getReactor() {
			return m;
		}

		public long getMessageId() {
			return e.getMessageIdLong();
		}

		public Guild getGuild() {
			return e.getGuild();
		}

		public EmojiUnion getEmoji() {
			return e.getEmoji();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordVoiceJoinedEvent extends JDADiscordGuildEvent implements DiscordVoiceJoinedEvent {
		private final GuildVoiceUpdateEvent e;

		JDADiscordVoiceJoinedEvent(GuildVoiceUpdateEvent e) {
			super(e);
			this.e = e;
		}

		@Override
		public Channel getChannel() {
			return e.getChannelJoined();
		}

		public Member getMember() {
			return e.getMember();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordVoiceLeftEvent extends JDADiscordGuildEvent implements DiscordVoiceLeftEvent {
		private final GuildVoiceUpdateEvent e;

		JDADiscordVoiceLeftEvent(GuildVoiceUpdateEvent e) {
			super(e);
			this.e = e;
		}

		@Override
		public Channel getChannel() {
			return e.getChannelLeft();
		}

		public Member getMember() {
			return e.getMember();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordVoiceMovedEvent extends JDADiscordGuildEvent implements DiscordVoiceMovedEvent {
		private final GuildVoiceUpdateEvent e;

		JDADiscordVoiceMovedEvent(GuildVoiceUpdateEvent e) {
			super(e);
			this.e = e;
		}

		@Override
		public AudioChannelUnion getChannelLeft() {
			return e.getChannelLeft();
		}

		@Override
		public AudioChannelUnion getChannel() {
			return e.getChannelJoined();
		}

		public Member getMember() {
			return e.getMember();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordMemberJoinEvent extends JDADiscordGuildEvent implements DiscordMemberJoinEvent {
		private final GuildMemberJoinEvent e;

		JDADiscordMemberJoinEvent(GuildMemberJoinEvent e) {
			super(e);
			this.e = e;
		}

		public Member getMember() {
			return e.getMember();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

	public static class JDADiscordMemberLeaveEvent extends JDADiscordGuildEvent implements DiscordMemberLeaveEvent {
		private final GuildMemberRemoveEvent e;

		JDADiscordMemberLeaveEvent(GuildMemberRemoveEvent e) {
			super(e);
			this.e = e;
		}

		public User getUser() {
			return e.getUser();
		}

		public Member getMember() {
			return e.getMember();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}
}
