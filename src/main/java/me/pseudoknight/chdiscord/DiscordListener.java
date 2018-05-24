package me.pseudoknight.chdiscord;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if(event.getAuthor().equals(Extension.jda.getSelfUser())) {
			return;
		}
		final JDADiscordMessageReceivedEvent mre = new JDADiscordMessageReceivedEvent(event);
		try {
			StaticLayer.GetConvertor().runOnMainThreadAndWait(() -> {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_message_received", mre);
				return null;
			});
		} catch(Exception ex) {
			Logger.getLogger(DiscordListener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		final JDADiscordVoiceJoinEvent vje = new JDADiscordVoiceJoinEvent(event);
		try {
			StaticLayer.GetConvertor().runOnMainThreadAndWait(() -> {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_joined", vje);
				return null;
			});
		} catch(Exception ex) {
			Logger.getLogger(DiscordListener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		final JDADiscordVoiceLeaveEvent vle = new JDADiscordVoiceLeaveEvent(event);
		try {
			StaticLayer.GetConvertor().runOnMainThreadAndWait(() -> {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_left", vle);
				return null;
			});
		} catch(Exception ex) {
			Logger.getLogger(DiscordListener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public interface DiscordMessageReceivedEvent extends BindableEvent {
		MessageChannel getChannel();
		User getAuthor();
		Member getMember();
		Message getMessage();
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordMessageReceivedEvent implements DiscordMessageReceivedEvent {
		GuildMessageReceivedEvent e;

		JDADiscordMessageReceivedEvent(GuildMessageReceivedEvent e) {
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

	public interface DiscordVoiceJoinEvent extends BindableEvent {
		VoiceChannel getChannel();
		Member getMember();
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordVoiceJoinEvent implements DiscordVoiceJoinEvent {
		GuildVoiceJoinEvent e;

		JDADiscordVoiceJoinEvent(GuildVoiceJoinEvent e) {
			this.e = e;
		}

		public VoiceChannel getChannel() {
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

	public interface DiscordVoiceLeaveEvent extends BindableEvent {
		VoiceChannel getChannel();
		Member getMember();
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class JDADiscordVoiceLeaveEvent implements DiscordVoiceLeaveEvent {
		GuildVoiceLeaveEvent e;

		JDADiscordVoiceLeaveEvent(GuildVoiceLeaveEvent e) {
			this.e = e;
		}

		public VoiceChannel getChannel() {
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

}
