package me.pseudoknight.chdiscord.abstraction.jda;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.abstraction;
import me.pseudoknight.chdiscord.abstraction.events.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Events {
	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordGuildMessageReceivedEvent implements DiscordGuildMessageReceivedEvent {
		MessageReceivedEvent e;

		JDADiscordGuildMessageReceivedEvent(MessageReceivedEvent e) {
			this.e = e;
		}

		public MessageChannel getChannel() {
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
		public Object _GetObject() {
			return e;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordPrivateMessageReceivedEvent implements DiscordPrivateMessageReceivedEvent {
		MessageReceivedEvent e;

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

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordVoiceJoinEvent implements DiscordVoiceJoinEvent {
		GuildVoiceJoinEvent e;

		JDADiscordVoiceJoinEvent(GuildVoiceJoinEvent e) {
			this.e = e;
		}

		public AudioChannel getChannel() {
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

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordVoiceLeaveEvent implements DiscordVoiceLeaveEvent {
		GuildVoiceLeaveEvent e;

		JDADiscordVoiceLeaveEvent(GuildVoiceLeaveEvent e) {
			this.e = e;
		}

		public AudioChannel getChannel() {
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

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordMemberJoinEvent implements DiscordMemberJoinEvent {
		GuildMemberJoinEvent e;

		JDADiscordMemberJoinEvent(GuildMemberJoinEvent e) {
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

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordMemberLeaveEvent implements DiscordMemberLeaveEvent {
		GuildMemberRemoveEvent e;

		JDADiscordMemberLeaveEvent(GuildMemberRemoveEvent e) {
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
