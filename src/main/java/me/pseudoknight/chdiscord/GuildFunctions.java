package me.pseudoknight.chdiscord;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.cache.MemberCacheView;

import java.util.ArrayList;
import java.util.List;

public class GuildFunctions {
	public static String docs() {
		return "Functions for managing a Discord server (guild).";
	}

	static final String SERVER_ARGUMENT = " The `server` argument is the guild server's unique int id."
			+ " It is always optional and will fall back to event bind context or the default server.";

	@api
	public static class discord_member_get_roles extends Discord.Function {

		public String getName() {
			return "discord_member_get_roles";
		}

		public String docs() {
			return "array {[server], member} Gets an associative array of all server roles for a member."
					+ SERVER_ARGUMENT
					+ MemberFunctions.MEMBER_ARGUMENT
					+ " The key is the role name, and the value is the role numeric id."
					+ " Throws NotFoundException if a member by that name doesn't exist.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			Member mem;
			if(args.length == 2) {
				mem = Discord.GetMember(args[1], Discord.GetGuild(args[0], t), t);
			} else {
				mem = Discord.GetMember(args[0], Discord.GetGuild(environment), t);
			}
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
	public static class discord_member_set_roles extends Discord.Function {

		public String getName() {
			return "discord_member_set_roles";
		}

		public String docs() {
			return "void {[server], member, role(s), [reason]} Sets the roles for a server member."
					+ SERVER_ARGUMENT
					+ MemberFunctions.MEMBER_ARGUMENT
					+ " The role argument can be an array or a single role."
					+ " A role is either a unique int id or name."
					+ " Optional reason string is supported."
					+ " Throws NotFoundException if a role by that name doesn't exist."
					+ " Requires the `Manage Roles` permission and a role higher than any set roles.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			Guild guild;
			Member mem;
			String reason = null;
			List<Role> roles = new ArrayList<>();
			int rolesIndex = 1;
			if(args.length == 4) {
				guild = Discord.GetGuild(args[0], t);
				mem = Discord.GetMember(args[1], guild, t);
				rolesIndex = 2;
				reason = args[3].val();
			} else if(args.length == 3) {
				if(args[2].isInstanceOf(CArray.TYPE)) {
					guild = Discord.GetGuild(args[0], t);
					mem = Discord.GetMember(args[1], guild, t);
					rolesIndex = 2;
				} else if(args[1].isInstanceOf(CArray.TYPE)) {
					guild = Discord.GetGuild(environment);
					mem = Discord.GetMember(args[0], guild, t);
					reason = args[2].val();
				} else {
					// Single role argument, so we have to assume the first argument is a Guild and try/catch.
					try {
						guild = Discord.GetGuild(args[0], t);
						mem = Discord.GetMember(args[1], guild, t);
						rolesIndex = 2;
					} catch(CRENotFoundException ex) {
						guild = Discord.GetGuild(environment);
						mem = Discord.GetMember(args[0], guild, t);
						reason = args[2].val();
					}
				}
			} else {
				guild = Discord.GetGuild(environment);
				mem = Discord.GetMember(args[0], guild, t);
			}
			if(args[rolesIndex].isInstanceOf(CArray.TYPE)) {
				CArray ca = (CArray) args[rolesIndex];
				for(Mixed key : ca.keySet()) {
					roles.add(Discord.GetRole(ca.get(key, t), guild, t));
				}
			} else {
				roles.add(Discord.GetRole(args[rolesIndex], guild, t));
			}
			try {
				guild.modifyMemberRoles(mem, roles).reason(reason).queue();
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

	@api
	public static class discord_member_move_voice_channel extends Discord.Function {

		public String getName() {
			return "discord_member_move_voice_channel";
		}

		public String docs() {
			return "void {[server], member, channel} Moves a member to another voice channel."
					+ SERVER_ARGUMENT
					+ MemberFunctions.MEMBER_ARGUMENT
					+ " The member must already be connected to a voice channel in the guild."
					+ ChannelFunctions.CHANNEL_ARGUMENT
					+ " Throws IllegalArgumentException if member is not connected to a voice channel."
					+ " Throws InsufficientPermissionException if the member and bot do not have access to the destination channel."
					+ " Requires the `Move Members` permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);

			Guild guild;
			Member member;
			VoiceChannel channel;
			if(args.length == 2) {
				guild = Discord.GetGuild(environment);
				member = Discord.GetMember(args[0], guild, t);
				channel = Discord.GetVoiceChannel(args[1], guild, t);
			} else {
				guild = Discord.GetGuild(args[0], t);
				member = Discord.GetMember(args[1], guild, t);
				channel = Discord.GetVoiceChannel(args[2], guild, t);
			}

			try {
				guild.moveVoiceMember(member, channel).queue();
			} catch (PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch (IllegalArgumentException | IllegalStateException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREInsufficientPermissionException.class,
					CREIllegalArgumentException.class};
		}
	}

	@api
	public static class discord_retrieve_invites extends Discord.Function {

		public String getName() {
			return "discord_retrieve_invites";
		}

		public String docs() {
			return "void {[server], closure} Retrieves an array of invite arrays for this guild server."
					+ SERVER_ARGUMENT
					+ " Passes the array to the callback closure."
					+ " Each invite array contains data about the invite, which has the keys 'code' and "
					+ " 'channelid', and optionally 'userid' of the inviter, 'uses' and 'max_uses'."
					+ " Requires the `Manage Server` permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			Guild guild;
			int callbackIndex = 0;
			if(args.length == 2) {
				guild = Discord.GetGuild(args[0], t);
				callbackIndex = 1;
			} else {
				guild = Discord.GetGuild(environment);
			}
			final CClosure closure = (CClosure) args[callbackIndex];
			try {
				guild.retrieveInvites().queue((List<Invite> list) -> {
					CArray array = new CArray(t);
					for (Invite invite : list) {
						CArray inviteArray = CArray.GetAssociativeArray(t);
						inviteArray.set("code", invite.getCode());
						Invite.Channel channel = invite.getChannel();
						if (channel != null) { // currently only null for group invites, but checking anyway
							inviteArray.set("channelid", new CInt(channel.getIdLong(), t), t);
						}
						User inviter = invite.getInviter();
						if (inviter != null) {
							inviteArray.set("userid", new CInt(inviter.getIdLong(), t), t);
						}
						if(invite.isExpanded()) {
							inviteArray.set("uses", new CInt(invite.getUses(), t), t);
							inviteArray.set("max_uses", new CInt(invite.getMaxUses(), t), t);
						}
						array.push(inviteArray, t);
					}
					StaticLayer.GetConvertor().runOnMainThreadLater(null, () ->
							closure.executeCallable(array));
				});
			} catch(PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREInsufficientPermissionException.class};
		}
	}

	@api
	public static class discord_get_members extends Discord.Function {

		public String getName() {
			return "discord_get_members";
		}

		public String docs() {
			return "array {[server]} Gets an array of all cached members in this guild server."
					+ SERVER_ARGUMENT
					+ " Array contains a list of user int ids."
					+ " Members may not be cached immediately upon bot connection.";
		}

		public Integer[] numArgs() {
			return new Integer[]{0, 1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			Guild guild;
			if(args.length == 1) {
				guild = Discord.GetGuild(args[0], t);
			} else {
				guild = Discord.GetGuild(environment);
			}
			MemberCacheView memberCache = guild.getMemberCache();
			CArray array = new CArray(t, (int) memberCache.size());
			memberCache.forEach((Member mem) -> array.push(new CInt(mem.getIdLong(), t), t));
			return array;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

	@api
	public static class discord_get_members_with_role extends Discord.Function {

		public String getName() {
			return "discord_get_members_with_role";
		}

		public String docs() {
			return "array {[server], role} Gets an array of cached members in this guild server with a given role."
					+ SERVER_ARGUMENT
					+ " Array contains a list of user int ids."
					+ " Members may not be cached immediately upon bot connection.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			Guild guild;
			Role role;
			if(args.length == 1) {
				guild = Discord.GetGuild(environment);
				role = Discord.GetRole(args[0], guild, t);
			} else {
				guild = Discord.GetGuild(args[0], t);
				role = Discord.GetRole(args[1], guild, t);
			}
			MemberCacheView memberCache = guild.getMemberCache();
			Iterable<Member> members = memberCache.getElementsWithRoles(role);
			CArray array = new CArray(t, (int) memberCache.size());
			members.forEach((Member mem) -> array.push(new CInt(mem.getIdLong(), t), t));
			return array;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class};
		}
	}
}
