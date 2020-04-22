package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Common.StringUtils;
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
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.ArrayList;
import java.util.List;

public class Functions {
	public static String docs() {
		return "Functions for Discord.";
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
			TextChannel channel = null;
			if(args.length == 2) {
				List<TextChannel> channels =  Discord.guild.getTextChannelsByName(args[0].val(), false);
				if(channels.isEmpty()) {
					throw new CRENotFoundException("Channel by the name " + args[0].val() + " not found.", t);
				}
				channel = channels.get(0);
			} else {
				channel = Discord.guild.getDefaultChannel();
				if(channel == null) {
					throw new CRENotFoundException("Default channel for bot not found.", t);
				}
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
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class};
		}
	}

	@api
	public static class discord_delete_message extends DiscordFunction {

		public String getName() {
			return "discord_delete_message";
		}

		public String docs() {
			return "void {channel, id} Deletes a message on a channel with the given id.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			List<TextChannel> channels =  Discord.guild.getTextChannelsByName(args[0].val(), false);
			if(channels.isEmpty()) {
				throw new CRENotFoundException("Channel by the name " + args[0].val() + " not found.", t);
			}
			TextChannel channel = channels.get(0);
			long id = Static.getInt(args[1], t);
			try {
				channel.deleteMessageById(id).queue();
			} catch(IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class};
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
			List<TextChannel> channels =  Discord.guild.getTextChannelsByName(args[0].val(), false);
			if(channels.isEmpty()) {
				throw new CRENotFoundException("Channel by the name " + args[0].val() + " not found.", t);
			}
			TextChannel channel = channels.get(0);
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
	public static class discord_set_activity extends DiscordFunction {

		public String getName() {
			return "discord_set_activity";
		}

		public String docs() {
			return "void {[type], string, [url]} Sets the activity tag for the bot."
					+ " Activity type can be one of " + StringUtils.Join(Activity.ActivityType.values(), ", ", ", or ") + "."
					+ " Activity string can be anything but an empty string."
					+ " If streaming, a valid Twitch URL must also be provided."
					+ " If not, or it's invalid, type will revert to DEFAULT (ie. playing).";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			try {
				Activity.ActivityType type = Activity.ActivityType.valueOf(args[0].val().toUpperCase());
				Activity activity = null;
				if (args.length == 1) {
					if(!(args[0] instanceof CNull)) {
						activity = Activity.playing(args[0].val());
					}
				} else if (type == Activity.ActivityType.STREAMING && args.length == 3) {
					activity = Activity.of(type, args[1].val(), args[2].val());
				} else {
					activity = Activity.of(type, args[1].val());
				}
				Discord.jda.getPresence().setActivity(activity);
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class};
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
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
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
	public static class discord_member_set_roles extends DiscordFunction {

		public String getName() {
			return "discord_member_set_roles";
		}

		public String docs() {
			return "void {member, role(s)} Sets the roles for a server member."
					+ " The role argument can be an array or a single role."
					+ " Like members, a role can be the name or the numeric id."
					+ " Throws NotFoundException if a member or role by that name doesn't exist."
					+ " Throws InsufficientPermissionException when the bot is not allowed by the discord server.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			Member mem = Discord.GetMember(args[0], t);
			List<Role> roles = new ArrayList<>();
			if(args[1].isInstanceOf(CArray.TYPE)) {
				CArray ca = (CArray) args[1];
				for(Mixed key : ((CArray) args[1]).keySet()) {
					roles.add(Discord.GetRole(ca.get(key, t), t));
				}
			} else {
				roles.add(Discord.GetRole(args[1], t));
			}
			try {
				Discord.guild.modifyMemberRoles(mem, roles).queue();
			} catch (PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREInsufficientPermissionException.class,
					CREIllegalArgumentException.class};
		}
	}

}
