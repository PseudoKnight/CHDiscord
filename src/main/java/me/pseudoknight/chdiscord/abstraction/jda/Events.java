package me.pseudoknight.chdiscord.abstraction.jda;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.abstraction;
import me.pseudoknight.chdiscord.abstraction.events.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
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
	public static class JDADiscordVoiceUpdateEvent implements DiscordVoiceUpdateEvent {
		GuildVoiceUpdateEvent e;

		JDADiscordVoiceUpdateEvent(GuildVoiceUpdateEvent e) {
			this.e = e;
		}

		public AudioChannel getChannelLeft() {
			return e.getChannelLeft();
		}

		public AudioChannel getChannelJoined() {
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
