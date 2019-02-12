package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
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
import java.util.Map;

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

	@Profiles.ProfileType(type = "discord")
	public static class DiscordProfile extends Profiles.Profile {
		private final String token;
		private final String serverId;
		public DiscordProfile(String id, Map<String, String> elements) throws Profiles.InvalidProfileException {
			super(id);
			if(elements.containsKey("token")) {
				token = elements.get("token");
			} else {
				throw new Profiles.InvalidProfileException("token and serverId are required parameters in the profile");
			}
			if(elements.containsKey("serverId")) {
				serverId = elements.get("serverId");
			} else {
				throw new Profiles.InvalidProfileException("token and serverId are required parameters in the profile");
			}
		}

		public String getToken() {
			return this.token;
		}

		public String getServerId() {
			return this.serverId;
		}
	}

	@api
	public static class discord_connect extends DiscordFunction {

		public String getName() {
			return "discord_connect";
		}

		public String docs() {
			return "boolean {token, server_id, [callback] | profile, [callback]} Connects to Discord server via token and server id."
					+ " The server id can be retrieved by right-clicking the server name and clicking \"Copy ID\"."
					+ " The optional callback closure will be executed when a connection is made. The profile may be"
					+ " a string, which should refer to a profile defined in profiles.xml, with the keys token and"
					+ " serverId, or an array, with the same keys.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		private DiscordProfile getProfile(Environment environment, String profileName, Target t) {
			Profiles.Profile p;
			try {
				p = environment.getEnv(GlobalEnv.class).getProfiles().getProfileById(profileName);
			} catch (Profiles.InvalidProfileException ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
			if(!(p instanceof DiscordProfile)) {
				throw new CRECastException("Profile type is expected to be \"discord\", but \"" + p.getType()
						+ "\"  was found.", t);
			}
			return (DiscordProfile) p;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CClosure callback = null;
			Mixed profile = null;
			String token = null;
			String serverId = null;
			switch (args.length) {
				case 1:
					// Just a profile
					profile = args[0];
					break;
				case 2:
					// Not sure, could be (profile, callback) or (token, serverId)
					if(args[1].isInstanceOf(CClosure.class)) {
						// profile, callback
						profile = args[0];
						callback = ArgumentValidation.getObject(args[1], t, CClosure.class);
					} else {
						// token, serverId
						token = args[0].val();
						serverId = args[1].val();
					}	break;
				case 3:
					// Individual options
					token = args[0].val();
					serverId = args[1].val();
					callback = Static.getObject(args[2], t, CClosure.class);
					break;
				default:
					throw new CREIllegalArgumentException("Not enough/too many parameters", t);
			}
			if(profile != null) {
				if(profile.isInstanceOf(CArray.class)) {
					CArray prof = (CArray) profile;
					token = prof.get("token", t).val();
					serverId = prof.get("serverId", t).val();
				} else {
					DiscordProfile p = getProfile(env, profile.val(), t);
					token = p.getToken();
					serverId = p.getServerId();
				}
			}
			Extension.connectDiscord(token, serverId, callback, env, t);
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
