package me.pseudoknight.chdiscord.abstraction.jda;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import me.pseudoknight.chdiscord.Discord;
import me.pseudoknight.chdiscord.abstraction.jda.Events.JDADiscordGuildMessageReceivedEvent;
import me.pseudoknight.chdiscord.abstraction.jda.Events.JDADiscordPrivateMessageReceivedEvent;
import me.pseudoknight.chdiscord.abstraction.jda.Events.JDADiscordVoiceJoinEvent;
import me.pseudoknight.chdiscord.abstraction.jda.Events.JDADiscordVoiceLeaveEvent;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Listener extends ListenerAdapter {

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().equals(Discord.jda.getSelfUser())) {
			return;
		}
		if(event.getChannelType() == ChannelType.PRIVATE) {
			final JDADiscordPrivateMessageReceivedEvent mre = new JDADiscordPrivateMessageReceivedEvent(event);
			try {
				StaticLayer.GetConvertor().runOnMainThreadLater(null, () -> {
					EventUtils.TriggerListener(Driver.EXTENSION, "discord_private_message_received", mre);
				});
			} catch(Exception ex) {
				Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			final JDADiscordGuildMessageReceivedEvent mre = new JDADiscordGuildMessageReceivedEvent(event);
			try {
				StaticLayer.GetConvertor().runOnMainThreadLater(null, () -> {
					EventUtils.TriggerListener(Driver.EXTENSION, "discord_message_received", mre);
				});
			} catch (Exception ex) {
				Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public void onGuildVoiceJoin(@Nonnull GuildVoiceJoinEvent event) {
		final JDADiscordVoiceJoinEvent vje = new JDADiscordVoiceJoinEvent(event);
		try {
			StaticLayer.GetConvertor().runOnMainThreadLater(null, () -> {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_joined", vje);
			});
		} catch(Exception ex) {
			Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void onGuildVoiceLeave(@Nonnull GuildVoiceLeaveEvent event) {
		final JDADiscordVoiceLeaveEvent vle = new JDADiscordVoiceLeaveEvent(event);
		try {
			StaticLayer.GetConvertor().runOnMainThreadLater(null, () -> {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_left", vle);
			});
		} catch(Exception ex) {
			Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
		final Events.JDADiscordMemberJoinEvent mje = new Events.JDADiscordMemberJoinEvent(event);
		try {
			StaticLayer.GetConvertor().runOnMainThreadLater(null, () -> {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_member_joined", mje);
			});
		} catch(Exception ex) {
			Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
		final Events.JDADiscordMemberLeaveEvent mle = new Events.JDADiscordMemberLeaveEvent(event);
		try {
			StaticLayer.GetConvertor().runOnMainThreadLater(null, () -> {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_member_left", mle);
			});
		} catch(Exception ex) {
			Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
