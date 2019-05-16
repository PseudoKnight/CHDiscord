package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
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
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.PermissionException;

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
			return "boolean {token, server_id, [callback] | profile, [callback]} Connects to Discord server via token and server id."
					+ " The server id can be retrieved by right-clicking the server name and clicking \"Copy ID\"."
					+ " The optional callback closure will be executed when a connection is made. The profile may be"
					+ " a string, which should refer to a profile defined in profiles.xml, with the keys token and"
					+ " serverId, or an array, with the same keys.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		private Extension.DiscordProfile getProfile(Environment environment, String profileName, Target t) {
			Profiles.Profile p;
			try {
				p = environment.getEnv(GlobalEnv.class).getProfiles().getProfileById(profileName);
			} catch (Profiles.InvalidProfileException ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
			if(!(p instanceof Extension.DiscordProfile)) {
				throw new CRECastException("Profile type is expected to be \"discord\", but \"" + p.getType()
						+ "\"  was found.", t);
			}
			return (Extension.DiscordProfile) p;
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
					Extension.DiscordProfile p = getProfile(env, profile.val(), t);
					token = p.getToken();
					serverId = p.getServerId();
				}
			}
			Discord.Connect(token, serverId, callback, env, t);
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
			Discord.Disconnect();
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
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			TextChannel channel = args.length == 2 ? Discord.channels.get(args[0].val()) : Discord.guild.getDefaultChannel();
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
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			Member mem = Discord.GetMember(args[0], t);
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
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			TextChannel channel = Discord.channels.get(args[0].val());
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

	@api
	public static class discord_member_get_roles extends DiscordFunction {

		public String getName() {
			return "discord_member_get_roles";
		}

		public String docs() {
			return "array {member} Gets an associative array of all server roles for a member."
					+ " The key is the role name, and the value is the role numeric id."
					+ " Throws NotFoundException if a member by that name doesn't exist.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Member mem = Discord.GetMember(args[0], t);
			CArray roles = CArray.GetAssociativeArray(t);
			for(Role role : mem.getRoles()) {
				roles.set(role.getName(), new CInt(role.getIdLong(), t), t);
			}
			return roles;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

	@api
	public static class discord_member_add_role extends DiscordFunction {

		public String getName() {
			return "discord_member_add_role";
		}

		public String docs() {
			return "void {member, role} Adds a role to a server member."
					+ " The role parameter, like members, can be given the name or the numeric id."
					+ " Throws NotFoundException if a member or role by that name doesn't exist.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Member mem = Discord.GetMember(args[0], t);
			Role role = Discord.GetRole(args[1], t);
			Discord.guild.getController().addRolesToMember(mem, role).queue();
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

	@api
	public static class discord_member_remove_role extends DiscordFunction {

		public String getName() {
			return "discord_member_remove_role";
		}

		public String docs() {
			return "void {member, role} Remove a role from a server member."
					+ " The role parameter, like members, can be given the name or the numeric id."
					+ " Throws NotFoundException if a member or role by that name doesn't exist.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Member mem = Discord.GetMember(args[0], t);
			Role role = Discord.GetRole(args[1], t);
			Discord.guild.getController().removeRolesFromMember(mem, role).queue();
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

}
