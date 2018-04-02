package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;

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
					+ " | channel: The channel the message was sent | message: The message the user sent.} "
					+ "{} "
					+ "{}";
		}

		@Override
		public CHVersion since() {
			return CHVersion.V3_3_2;
		}

		@Override
		public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
			if (e instanceof DiscordListener.DiscordMessageReceivedEvent) {
				DiscordListener.DiscordMessageReceivedEvent event = (DiscordListener.DiscordMessageReceivedEvent)e;

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
		public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
			if (e instanceof DiscordListener.DiscordMessageReceivedEvent) {
				DiscordListener.DiscordMessageReceivedEvent event = (DiscordListener.DiscordMessageReceivedEvent) e;
				Target t = Target.UNKNOWN;
				Map<String, Construct> map = new HashMap<>();

				map.put("username", new CString(event.getMember().getUser().getName(), t));
				map.put("nickname", new CString(event.getMember().getEffectiveName(), t));
				map.put("channel", new CString(event.getChannel().getName(), t));
				map.put("message", new CString(event.getMessage().getContentRaw(), t));

				return map;
			}
			throw new EventException("Cannot convert e to DiscordMessageReceivedEvent");
		}

		@Override
		public Driver driver() {
			return Driver.EXTENSION;
		}

		@Override
		public boolean modifyEvent(String s, Construct construct, BindableEvent bindableEvent) {
			return false;
		}
	}
}
