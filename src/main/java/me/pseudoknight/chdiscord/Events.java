package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import me.pseudoknight.chdiscord.abstraction.events.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class Events {
	public static String docs() {
		return "This provides hooks for Discord events.";
	}

	public static abstract class DiscordEvent extends AbstractEvent {

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public Driver driver() {
			return Driver.EXTENSION;
		}

		@Override
		public BindableEvent convert(CArray cArray, Target target) {
			return null;
		}

		@Override
		public boolean modifyEvent(String s, Mixed construct, BindableEvent bindableEvent) {
			return false;
		}
	}

	private static void EvaluateMessage(Message msg, Map<String, Mixed> map) {
		Target t = Target.UNKNOWN;
		map.put("message", new CString(msg.getContentDisplay(), t));
		map.put("id", new CInt(msg.getIdLong(), t));
		CArray attachments = new CArray(t);
		for(Message.Attachment msgAttachment : msg.getAttachments()) {
			CArray attachment = CArray.GetAssociativeArray(t);
			attachment.set("url", new CString(msgAttachment.getUrl(), t), t);
			attachment.set("filename", new CString(msgAttachment.getFileName(), t), t);
			attachment.set("description", new CString(msgAttachment.getDescription(), t), t);
			attachments.push(attachment, t);
		}
		map.put("attachments", attachments);
		if(msg.getReferencedMessage() != null) {
			CArray reference = CArray.GetAssociativeArray(t);
			Message referencedMsg = msg.getReferencedMessage();
			reference.set("id", new CInt(referencedMsg.getIdLong(), t), t);
			reference.set("username", new CString(referencedMsg.getAuthor().getName(), t), t);
			reference.set("userid", new CInt(referencedMsg.getAuthor().getIdLong(), t), t);
			reference.set("message", new CString(referencedMsg.getContentDisplay(), t), t);
			map.put("reference", reference);
		} else {
			map.put("reference", CNull.NULL);
		}
	}

	@api
	public static class discord_message_received extends DiscordEvent {

		@Override
		public String getName() {
			return "discord_message_received";
		}

		@Override
		public String docs() {
			return "{username: <string match> Sender's name | channel: <string match> Channel's name} "
					+ "This event is called when a user sends a message in a Discord server."
					+ "{username: The username of the sender"
					+ " | nickname: The effective display name of the sender in this guild server"
					+ " | userid: The sender's unique id"
					+ " | bot: If the user is a bot"
					+ " | serverid: The guild server in which this the message was sent"
					+ " | channel: The name of the channel in which the message was sent"
					+ " | channelid: The unique id for the channel."
					+ " | channeltype: The type of channel. (TEXT, VOICE, NEWS, GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD,"
					+ " or GUILD_PRIVATE_THREAD)"
					+ " | message: The message the user sent."
					+ " | id: The message id."
					+ " | attachments: An array of attachment arrays, each with the keys 'url', 'filename', and"
					+ " 'description'."
					+ " | reference: An associative array representing the message this was a reply to, with the keys"
					+ " 'id', 'userid', 'username', and 'message'. Will be null if the message was not a reply.}"
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof DiscordGuildMessageReceivedEvent) {
				DiscordGuildMessageReceivedEvent event = (DiscordGuildMessageReceivedEvent) e;

				if(prefilter.containsKey("username")
						&& !event.getMember().getUser().getName().equals(prefilter.get("username").val())) {
					return false;
				}
				if(prefilter.containsKey("channel")
						&& !event.getChannel().getName().equals(prefilter.get("channel").val())) {
					return false;
				}

				return true;
			}
			return false;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			DiscordGuildMessageReceivedEvent event = (DiscordGuildMessageReceivedEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = new HashMap<>();

			Member mem = event.getMember();
			map.put("nickname", new CString(mem != null ? mem.getEffectiveName() : event.getAuthor().getEffectiveName(), t));
			map.put("username", new CString(event.getAuthor().getName(), t));
			map.put("userid", new CInt(event.getAuthor().getIdLong(), t));
			map.put("bot", CBoolean.get(event.getAuthor().isBot()));
			map.put("serverid", new CInt(event.getGuild().getIdLong(), t));
			map.put("channel", new CString(event.getChannel().getName(), t));
			map.put("channelid", new CInt(event.getChannel().getIdLong(), t));
			map.put("channeltype", new CString(event.getChannel().getType().name(), t));
			EvaluateMessage(event.getMessage(), map);
			return map;
		}
	}

	@api
	public static class discord_message_updated extends DiscordEvent {

		@Override
		public String getName() {
			return "discord_message_updated";
		}

		@Override
		public String docs() {
			return "{channel: <string match> Channel's name} "
					+ "This event is called when a user edits a message in a Discord server."
					+ "{username: The username of the author"
					+ " | nickname: The effective display name of the author in this guild server"
					+ " | userid: The author's unique id"
					+ " | bot: If the author is a bot"
					+ " | serverid: The guild server in which this the message was sent"
					+ " | channel: The name of the channel in which the message was sent"
					+ " | channelid: The unique id for the channel."
					+ " | channeltype: The type of channel. (TEXT, VOICE, NEWS, GUILD_NEWS_THREAD, GUILD_PUBLIC_THREAD,"
					+ " or GUILD_PRIVATE_THREAD)"
					+ " | message: The message the author edited."
					+ " | id: The message id."
					+ " | attachments: An array of attachment arrays, each with the keys 'url', 'filename', and"
					+ " 'description'."
					+ " | reference: An associative array representing the message this was a reply to, with the keys"
					+ " 'id', 'userid', 'username', and 'message'. Will be null if the message was not a reply.}"
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof DiscordGuildMessageUpdatedEvent) {
				DiscordGuildMessageUpdatedEvent event = (DiscordGuildMessageUpdatedEvent) e;
				if(prefilter.containsKey("channel")
						&& !event.getChannel().getName().equals(prefilter.get("channel").val())) {
					return false;
				}
				return true;
			}
			return false;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			DiscordGuildMessageUpdatedEvent event = (DiscordGuildMessageUpdatedEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = new HashMap<>();

			Member mem = event.getMember();
			map.put("nickname", new CString(mem != null ? mem.getEffectiveName() : event.getAuthor().getEffectiveName(), t));
			map.put("serverid", new CInt(event.getGuild().getIdLong(), t));
			map.put("channel", new CString(event.getChannel().getName(), t));
			map.put("channelid", new CInt(event.getChannel().getIdLong(), t));
			map.put("channeltype", new CString(event.getChannel().getType().name(), t));
			map.put("bot", CBoolean.get(event.getAuthor().isBot()));
			map.put("username", new CString(event.getAuthor().getName(), t));
			map.put("userid", new CInt(event.getAuthor().getIdLong(), t));
			EvaluateMessage(event.getMessage(), map);
			return map;
		}
	}

	@api
	public static class discord_private_message_received extends DiscordEvent {

		@Override
		public String getName() {
			return "discord_private_message_received";
		}

		@Override
		public String docs() {
			return "{} "
					+ "This event is called when a user sends a private message to the bot."
					+ "{username: The Discord username | userid: The Discord user's unique id"
					+ " | displayname: The Discord user's display name"
					+ " | message: The message the user sent. | id: The message id. | attachments: An array of"
					+ " attachment arrays, each with the keys 'url', 'filename', and 'description'."
					+ " | reference: An associative array representing the message this was a reply to, with the keys"
					+ " 'id', 'userid', 'username', and 'message'. Will be null if the message was not a reply.}"
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordPrivateMessageReceivedEvent;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			DiscordPrivateMessageReceivedEvent event = (DiscordPrivateMessageReceivedEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = new HashMap<>();

			map.put("username", new CString(event.getAuthor().getName(), t));
			map.put("displayname", new CString(event.getAuthor().getEffectiveName(), t));
			map.put("userid", new CInt(event.getAuthor().getIdLong(), t));
			EvaluateMessage(event.getMessage(), map);
			return map;
		}
	}

	@api
	public static class discord_voice_joined extends DiscordEvent {

		@Override
		public String getName() {
			return "discord_voice_joined";
		}

		@Override
		public String docs() {
			return "{} "
					+ "This event is called when a user joins a voice channel on the Discord server."
					+ "{username: The Discord username | nickname: The effective display name in this guild server"
					+ " | userid: The Discord user's unique id"
					+ " | serverid: The guild server in which this event occurred"
					+ " | channel: The name of the channel the user joined"
					+ " | channelid: The unique id for the channel.}"
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordVoiceJoinedEvent;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			DiscordVoiceJoinedEvent event = (DiscordVoiceJoinedEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = new HashMap<>();

			map.put("username", new CString(event.getMember().getUser().getName(), t));
			map.put("userid", new CInt(event.getMember().getUser().getIdLong(), t));
			map.put("nickname", new CString(event.getMember().getEffectiveName(), t));
			map.put("serverid", new CInt(event.getGuild().getIdLong(), t));
			map.put("channel", new CString(event.getChannel().getName(), t));
			map.put("channelid", new CInt(event.getChannel().getIdLong(), t));

			return map;
		}
	}

	@api
	public static class discord_voice_left extends DiscordEvent {

		@Override
		public String getName() {
			return "discord_voice_left";
		}

		@Override
		public String docs() {
			return "{} "
					+ "This event is called when a user leaves a voice channel on the Discord server."
					+ "{username: The Discord username | nickname: The effective display name in this guild server"
					+ " | userid: The Discord user's unique id"
					+ " | serverid: The guild server in which this event occurred"
					+ " | channel: The name of the channel the user left"
					+ " | channelid: The unique id for the channel.}"
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordVoiceLeftEvent;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			DiscordVoiceLeftEvent event = (DiscordVoiceLeftEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = new HashMap<>();

			map.put("username", new CString(event.getMember().getUser().getName(), t));
			map.put("userid", new CInt(event.getMember().getUser().getIdLong(), t));
			map.put("nickname", new CString(event.getMember().getEffectiveName(), t));
			map.put("serverid", new CInt(event.getGuild().getIdLong(), t));
			map.put("channel", new CString(event.getChannel().getName(), t));
			map.put("channelid", new CInt(event.getChannel().getIdLong(), t));

			return map;
		}
	}

	@api
	public static class discord_member_joined extends DiscordEvent {

		@Override
		public String getName() {
			return "discord_member_joined";
		}

		@Override
		public String docs() {
			return "{} "
					+ "This event is called when a user joined the Discord server."
					+ "{username: The Discord username | nickname: The effective display name in this guild server"
					+ " | userid: The Discord user's unique id | serverid: The guild server joined } "
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordMemberJoinEvent;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			DiscordMemberJoinEvent event = (DiscordMemberJoinEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = new HashMap<>();

			map.put("username", new CString(event.getMember().getUser().getName(), t));
			map.put("userid", new CInt(event.getMember().getUser().getIdLong(), t));
			map.put("nickname", new CString(event.getMember().getEffectiveName(), t));
			map.put("serverid", new CInt(event.getGuild().getIdLong(), t));

			return map;
		}
	}

	@api
	public static class discord_member_left extends DiscordEvent {

		@Override
		public String getName() {
			return "discord_member_left";
		}

		@Override
		public String docs() {
			return "{} "
					+ "This event is called when a user left the Discord server, including kick/ban."
					+ "{username: The Discord username | nickname: The effective display name in this guild server"
					+ " | userid: The Discord user's unique id | serverid: The guild server left } "
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordMemberLeaveEvent;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			DiscordMemberLeaveEvent event = (DiscordMemberLeaveEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Mixed> map = new HashMap<>();

			Member mem = event.getMember();
			User user = event.getUser();
			map.put("username", new CString(user.getName(), t));
			map.put("userid", new CInt(user.getIdLong(), t));
			if(mem != null) {
				map.put("nickname", new CString(mem.getEffectiveName(), t));
			} else {
				map.put("nickname", new CString(user.getName(), t));
			}
			map.put("serverid", new CInt(event.getGuild().getIdLong(), t));

			return map;
		}
	}
}
