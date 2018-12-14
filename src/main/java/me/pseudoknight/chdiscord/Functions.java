package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.List;

public class Functions {
	public static String docs() {
		return "Functions for the DiscordSRV plugin.";
	}

	public static abstract class DiscordFunction extends AbstractFunction {
		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Version since() {
			return MSVersion.V3_3_2;
		}
	}

	@api
	public static class discord_connect extends DiscordFunction {

		public String getName() {
			return "discord_connect";
		}

		public String docs() {
			return "boolean {token, server_id, [callback]} Connects to Discord server via token and server id."
					+ " The server id can be retrieved by right-clicking the server name and clicking \"Copy ID\"."
					+ " The optional callback closure will be executed when a connection is made.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CClosure callback = null;
			if(args.length == 3) {
				callback = Static.getObject(args[2], t, CClosure.class);
			}
			Extension.connectDiscord(args[0].val(), args[1].val(), callback, env, t);
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}
	}

	@api
	public static class discord_disconnect extends DiscordFunction {

		public String getName() {
			return "discord_disconnect";
		}

		public String docs() {
			return "void {} Disconnects from the Discord server.";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public Mixed exec(Target t, final Environment env, Mixed... args) throws ConfigRuntimeException {
			Extension.disconnectDiscord();
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}
	}

	@api
	public static class discord_broadcast extends DiscordFunction {

		public String getName() {
			return "discord_broadcast";
		}

		public String docs() {
			return "void {[channel], string} Broadcasts text to the specified channel (or server default)."
					+ " Message must not be empty, else it will throw a IllegalArgumentException.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Extension.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			TextChannel channel = args.length == 2 ? Extension.channels.get(args[0].val()) : Extension.guild.getDefaultChannel();
			if(channel == null) {
				throw new CRENotFoundException("Channel by the name " + args[0].val() + " not found.", t);
			}
			String message = args[args.length - 1].val();
			try {
				channel.sendMessage(message).queue();
			} catch(IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

	@api
	public static class discord_private_message extends DiscordFunction {

		public String getName() {
			return "discord_private_message";
		}

		public String docs() {
			return "void {member, string} Sends a private message to the specified Discord server member."
					+ " The user numeric id or name can be used to specify which server member to send to."
					+ " If there are multiple members with the same user name, only the first one is messaged."
					+ " Therefore it is recommended to use the user id.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Extension.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			Member mem;
			if(args[0] instanceof CInt) {
				mem = Extension.guild.getMemberById(((CInt) args[0]).getInt());
			} else {
				List<Member> m = Extension.guild.getMembersByName(args[0].val(), false);
				if(m.isEmpty()) {
					throw new CRENotFoundException("A member with the name \"" + args[0].val() + "\" was not found on Discord server.", t);
				}
				mem = m.get(0);
			}
			final String message = args[1].val();
			mem.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

	@api
	public static class discord_set_channel_topic extends DiscordFunction {

		public String getName() {
			return "discord_set_channel_topic";
		}

		public String docs() {
			return "void {channel, string} Sets a text channel's topic.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Extension.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			TextChannel channel = Extension.channels.get(args[0].val());
			if(channel == null) {
				throw new CRENotFoundException("Channel by the name " + args[0].val() + " not found.", t);
			}
			String message = args[1].val();
			try {
				channel.getManager().setTopic(message).queue();
			} catch(PermissionException | IllegalArgumentException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREFormatException.class};
		}
	}

}
