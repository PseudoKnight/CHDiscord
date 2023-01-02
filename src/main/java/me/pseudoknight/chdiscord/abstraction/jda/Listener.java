package me.pseudoknight.chdiscord.abstraction.jda;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import me.pseudoknight.chdiscord.Discord;
import me.pseudoknight.chdiscord.abstraction.jda.Events.JDADiscordGuildMessageReceivedEvent;
import me.pseudoknight.chdiscord.abstraction.jda.Events.JDADiscordPrivateMessageReceivedEvent;
import me.pseudoknight.chdiscord.abstraction.jda.Events.JDADiscordVoiceUpdateEvent;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Listener extends ListenerAdapter {

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if(Discord.jda == null || event.getAuthor().equals(Discord.jda.getSelfUser())) {
			return;
		}
		if(event.getChannelType() == ChannelType.PRIVATE) {
			final JDADiscordPrivateMessageReceivedEvent e = new JDADiscordPrivateMessageReceivedEvent(event);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_private_message_received", e));
		} else {
			final JDADiscordGuildMessageReceivedEvent e = new JDADiscordGuildMessageReceivedEvent(event);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_message_received", e));
		}
	}

	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
		final JDADiscordVoiceUpdateEvent e = new JDADiscordVoiceUpdateEvent(event);
		if(e.getChannelJoined() != null && e.getChannelLeft() == null) {
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_joined", e));
		} else if(e.getChannelLeft() != null && e.getChannelJoined() == null) {
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_left", e));
		}
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		final Events.JDADiscordMemberJoinEvent e = new Events.JDADiscordMemberJoinEvent(event);
		StaticLayer.GetConvertor().runOnMainThreadLater(null,
				() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_member_joined", e));
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		final Events.JDADiscordMemberLeaveEvent e = new Events.JDADiscordMemberLeaveEvent(event);
		StaticLayer.GetConvertor().runOnMainThreadLater(null,
				() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_member_left", e));
	}

}
