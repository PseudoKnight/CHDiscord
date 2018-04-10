package me.pseudoknight.chdiscord;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;

public class DiscordListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		if(event.getAuthor().equals(Extension.jda.getSelfUser())) {
			return;
		}
		final JDADiscordMessageReceivedEvent mre = new JDADiscordMessageReceivedEvent(event);
		Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_message_received", mre);
				return null;
			}
		});
	}

	@Override
	public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
		final JDADiscordVoiceJoinEvent vje = new JDADiscordVoiceJoinEvent(event);
		Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_joined", vje);
				return null;
			}
		});
	}

	@Override
	public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
		final JDADiscordVoiceLeaveEvent vle = new JDADiscordVoiceLeaveEvent(event);
		Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_left", vle);
				return null;
			}
		});
	}

	public interface DiscordMessageReceivedEvent extends BindableEvent {
		MessageChannel getChannel();
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
