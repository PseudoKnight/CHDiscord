package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import me.pseudoknight.chdiscord.abstraction.events.DiscordGuildMessageReceivedEvent;
import me.pseudoknight.chdiscord.abstraction.events.DiscordPrivateMessageReceivedEvent;
import me.pseudoknight.chdiscord.abstraction.events.DiscordVoiceJoinEvent;
import me.pseudoknight.chdiscord.abstraction.events.DiscordVoiceLeaveEvent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.util.HashMap;
import java.util.Map;

public class Events {
	public static String docs() {
		return "This provides hooks for DiscordSRV events.";
	}

	@api
	public static class discord_message_received extends AbstractEvent {

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
					+ " | channel: The channel the message was sent | message: The message the user sent.} "
					+ "{} "
					+ "{}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
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
		public BindableEvent convert(CArray cArray, Target target) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if (e instanceof DiscordGuildMessageReceivedEvent) {
				DiscordGuildMessageReceivedEvent event = (DiscordGuildMessageReceivedEvent) e;
				Message msg = event.getMessage();
				Target t = Target.UNKNOWN;
				Map<String, Mixed> map = new HashMap<>();

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

				return map;
			}
			throw new EventException("Cannot convert e to DiscordMessageReceivedEvent");
		}

		@Override
		public Driver driver() {
			return Driver.EXTENSION;
		}

		@Override
		public boolean modifyEvent(String s, Mixed construct, BindableEvent bindableEvent) {
			return false;
		}
	}

	@api
	public static class discord_direct_message_received extends AbstractEvent {

		@Override
		public String getName() {
			return "discord_direct_message_received";
		}

		@Override
		public String docs() {
			return "{} "
					+ "This event is called when a user sends a direct message to the bot."
					+ "{username: The Discord username | userid: The Discord user's unique id"
					+ " | message: The message the user sent.} "
					+ "{} "
					+ "{}";
		}

		@Override
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			return e instanceof DiscordPrivateMessageReceivedEvent;
		}

		@Override
		public BindableEvent convert(CArray cArray, Target target) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if (e instanceof DiscordPrivateMessageReceivedEvent) {
				DiscordPrivateMessageReceivedEvent event = (DiscordPrivateMessageReceivedEvent) e;
				Message msg = event.getMessage();
				Target t = Target.UNKNOWN;
				Map<String, Mixed> map = new HashMap<>();

				map.put("username", new CString(event.getAuthor().getName(), t));
				map.put("userid", new CInt(event.getAuthor().getIdLong(), t));
				map.put("message", new CString(msg.getContentDisplay(), t));

				return map;
			}
			throw new EventException("Cannot convert e to DiscordPrivateMessageReceivedEvent");
		}

		@Override
		public Driver driver() {
			return Driver.EXTENSION;
		}

		@Override
		public boolean modifyEvent(String s, Mixed construct, BindableEvent bindableEvent) {
			return false;
		}
	}

	@api
	public static class discord_voice_joined extends AbstractEvent {

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
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof DiscordVoiceJoinEvent) {
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray cArray, Target target) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(e instanceof DiscordVoiceJoinEvent) {
				DiscordVoiceJoinEvent event = (DiscordVoiceJoinEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> map = new HashMap<>();

				map.put("username", new CString(event.getMember().getUser().getName(), t));
				map.put("userid", new CInt(event.getMember().getUser().getIdLong(), t));
				map.put("nickname", new CString(event.getMember().getEffectiveName(), t));
				map.put("channel", new CString(event.getChannel().getName(), t));

				return map;
			}
			throw new EventException("Cannot convert e to DiscordVoiceJoinEvent");
		}

		@Override
		public Driver driver() {
			return Driver.EXTENSION;
		}

		@Override
		public boolean modifyEvent(String s, Mixed construct, BindableEvent bindableEvent) {
			return false;
		}
	}

	@api
	public static class discord_voice_left extends AbstractEvent {

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
		public MSVersion since() {
			return MSVersion.V3_3_2;
		}

		@Override
		public boolean matches(Map<String, Mixed> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if(e instanceof DiscordVoiceLeaveEvent) {
				return true;
			}
			return false;
		}

		@Override
		public BindableEvent convert(CArray cArray, Target target) {
			return null;
		}

		@Override
		public Map<String, Mixed> evaluate(BindableEvent e) throws EventException {
			if(e instanceof DiscordVoiceLeaveEvent) {
				DiscordVoiceLeaveEvent event = (DiscordVoiceLeaveEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Mixed> map = new HashMap<>();

				map.put("username", new CString(event.getMember().getUser().getName(), t));
				map.put("userid", new CInt(event.getMember().getUser().getIdLong(), t));
				map.put("nickname", new CString(event.getMember().getEffectiveName(), t));
				map.put("channel", new CString(event.getChannel().getName(), t));

				return map;
			}
			throw new EventException("Cannot convert e to DiscordVoiceLeaveEvent");
		}

		@Override
		public Driver driver() {
			return Driver.EXTENSION;
		}

		@Override
		public boolean modifyEvent(String s, Mixed construct, BindableEvent bindableEvent) {
			return false;
		}
	}
}
