package me.pseudoknight.chdiscord.abstraction.jda;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import me.pseudoknight.chdiscord.Discord;
import me.pseudoknight.chdiscord.abstraction.jda.Events.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Listener extends ListenerAdapter {

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if(Discord.jda == null || event.getAuthor().equals(Discord.jda.getSelfUser()) || event.getAuthor().isSystem()) {
			return;
		}
		if(event.getChannelType() == ChannelType.PRIVATE) {
			final JDADiscordPrivateMessageReceivedEvent e = new JDADiscordPrivateMessageReceivedEvent(event);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_private_message_received", e));
		} else if(event.isFromGuild()) {
			final JDADiscordGuildMessageReceivedEvent e = new JDADiscordGuildMessageReceivedEvent(event);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_message_received", e));
		}
	}

	@Override
	public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
		if(!event.isFromGuild() || Discord.jda == null || event.getAuthor().equals(Discord.jda.getSelfUser())
				|| event.getAuthor().isSystem()) {
			return;
		}
		final JDADiscordGuildMessageUpdatedEvent e = new JDADiscordGuildMessageUpdatedEvent(event);
		StaticLayer.GetConvertor().runOnMainThreadLater(null,
				() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_message_updated", e));
	}

	@Override
	public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
		if(!event.isFromGuild() || Discord.jda == null || Discord.jda.getSelfUser().equals(event.getUser())) {
			return;
		}
		event.retrieveMember().queue((member) -> {
			final JDADiscordReactionAddedEvent e = new JDADiscordReactionAddedEvent(event, member);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_reaction_added", e));
		});
	}

	@Override
	public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
		if(!event.isFromGuild() || Discord.jda == null || Discord.jda.getSelfUser().equals(event.getUser())) {
			return;
		}
		event.retrieveMember().queue((member) -> {
			final JDADiscordReactionRemovedEvent e = new JDADiscordReactionRemovedEvent(event, member);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_reaction_removed", e));
		});
	}

	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
		if(event.getChannelJoined() != null && event.getChannelLeft() == null) {
			final JDADiscordVoiceJoinedEvent e = new JDADiscordVoiceJoinedEvent(event);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_joined", e));
		} else if(event.getChannelLeft() != null && event.getChannelJoined() == null) {
			final JDADiscordVoiceLeftEvent e = new JDADiscordVoiceLeftEvent(event);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_left", e));
		} else {
			final JDADiscordVoiceMovedEvent e = new JDADiscordVoiceMovedEvent(event);
			StaticLayer.GetConvertor().runOnMainThreadLater(null,
					() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_voice_moved", e));
		}
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		final JDADiscordMemberJoinEvent e = new JDADiscordMemberJoinEvent(event);
		StaticLayer.GetConvertor().runOnMainThreadLater(null,
				() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_member_joined", e));
	}

	@Override
	public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
		final JDADiscordMemberLeaveEvent e = new JDADiscordMemberLeaveEvent(event);
		StaticLayer.GetConvertor().runOnMainThreadLater(null,
				() -> EventUtils.TriggerListener(Driver.EXTENSION, "discord_member_left", e));
	}

}
