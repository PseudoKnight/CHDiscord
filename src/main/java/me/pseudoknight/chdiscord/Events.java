package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
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
		public CHVersion since() {
			return CHVersion.V3_3_2;
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
		public boolean modifyEvent(String s, Construct construct, BindableEvent bindableEvent) {
			return false;
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
			return "{username: <string match> | channel: <string match>} "
					+ "This event is called when a user sends a message in the Discord server."
					+ "{username: The Discord username | nickname: The display name on Discord"
					+ " | userid: The Discord user's unique id"
					+ " | channel: The channel the message was sent | message: The message the user sent."
					+ " | id: The message id.} "
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof DiscordGuildMessageReceivedEvent) {
				DiscordGuildMessageReceivedEvent event = (DiscordGuildMessageReceivedEvent)e;

				if(prefilter.containsKey("username") && !event.getMember().getUser().getName().equals(prefilter.get("username").val())) {
					return false;
				}
				if(prefilter.containsKey("channel") && !event.getChannel().getName().equals(prefilter.get("channel").val())) {
					return false;
				}

				return true;
			}
			return false;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			DiscordGuildMessageReceivedEvent event = (DiscordGuildMessageReceivedEvent) e;
			Message msg = event.getMessage();
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = new HashMap<>();

			Member mem = event.getMember();
			if(mem != null) {
				map.put("nickname", new CString(mem.getEffectiveName(), t));
			} else {
				map.put("nickname", new CString(event.getAuthor().getName(), t));
			}

			map.put("username", new CString(event.getAuthor().getName(), t));
			map.put("userid", new CInt(event.getAuthor().getIdLong(), t));
			map.put("channel", new CString(event.getChannel().getName(), t));
			map.put("message", new CString(msg.getContentDisplay(), t));
			map.put("id", new CInt(msg.getIdLong(), t));

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
					+ " | message: The message the user sent. | id: The message id.} "
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordPrivateMessageReceivedEvent;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			DiscordPrivateMessageReceivedEvent event = (DiscordPrivateMessageReceivedEvent) e;
			Message msg = event.getMessage();
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = new HashMap<>();

			map.put("username", new CString(event.getAuthor().getName(), t));
			map.put("userid", new CInt(event.getAuthor().getIdLong(), t));
			map.put("message", new CString(msg.getContentDisplay(), t));
			map.put("id", new CInt(msg.getIdLong(), t));

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
					+ "{username: The Discord username | nickname: The display name on Discord"
					+ " | userid: The Discord user's unique id"
					+ " | channel: The channel the user joined} "
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordVoiceJoinEvent;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			DiscordVoiceJoinEvent event = (DiscordVoiceJoinEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = new HashMap<>();

			map.put("username", new CString(event.getMember().getUser().getName(), t));
			map.put("userid", new CInt(event.getMember().getUser().getIdLong(), t));
			map.put("nickname", new CString(event.getMember().getEffectiveName(), t));
			map.put("channel", new CString(event.getChannel().getName(), t));

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
					+ "{username: The Discord username | nickname: The display name on Discord"
					+ " | userid: The Discord user's unique id"
					+ " | channel: The channel the user left} "
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof DiscordVoiceLeaveEvent) {
				return true;
			}
			return false;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			DiscordVoiceLeaveEvent event = (DiscordVoiceLeaveEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = new HashMap<>();

			map.put("username", new CString(event.getMember().getUser().getName(), t));
			map.put("userid", new CInt(event.getMember().getUser().getIdLong(), t));
			map.put("nickname", new CString(event.getMember().getEffectiveName(), t));
			map.put("channel", new CString(event.getChannel().getName(), t));

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
					+ "{username: The Discord username | nickname: The display name on Discord"
					+ " | userid: The Discord user's unique id} "
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordMemberJoinEvent;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			DiscordMemberJoinEvent event = (DiscordMemberJoinEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = new HashMap<>();

			map.put("username", new CString(event.getMember().getUser().getName(), t));
			map.put("userid", new CInt(event.getMember().getUser().getIdLong(), t));
			map.put("nickname", new CString(event.getMember().getEffectiveName(), t));

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
					+ "{username: The Discord username | nickname: The display name on Discord"
					+ " | userid: The Discord user's unique id} "
					+ "{} "
					+ "{}";
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordMemberLeaveEvent;
		}

		@Override
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			DiscordMemberLeaveEvent event = (DiscordMemberLeaveEvent) e;
			Target t = Target.UNKNOWN;
			Map<String, Construct> map = new HashMap<>();

			Member mem = event.getMember();
			User user = event.getUser();
			map.put("username", new CString(user.getName(), t));
			map.put("userid", new CInt(user.getIdLong(), t));
			if(mem != null) {
				map.put("nickname", new CString(mem.getEffectiveName(), t));
			} else {
				map.put("nickname", new CString(user.getName(), t));
			}

			return map;
		}
	}
}
