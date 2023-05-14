package me.pseudoknight.chdiscord;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
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

	@api
	public static class discord_member_get_roles extends Discord.Function {

		public String getName() {
			return "discord_member_get_roles";
		}

		public String docs() {
			return "array {member} Gets an associative array of all server roles for a member."
					+ MemberFunctions.MEMBER_ARGUMENT
					+ " The key is the role name, and the value is the role numeric id."
					+ " Throws NotFoundException if a member by that name doesn't exist.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
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
	public static class discord_member_set_roles extends Discord.Function {

		public String getName() {
			return "discord_member_set_roles";
		}

		public String docs() {
			return "void {member, role(s)} Sets the roles for a server member."
					+ MemberFunctions.MEMBER_ARGUMENT
					+ " The role argument can be an array or a single role."
					+ " A role is either a unique int id or name."
					+ " Throws NotFoundException if a role by that name doesn't exist."
					+ " Requires the `Manage Roles` permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
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
				Discord.GetDefaultGuild().modifyMemberRoles(mem, roles).queue();
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
			return "void {member, channel} Moves a member to another voice channel."
					+ MemberFunctions.MEMBER_ARGUMENT
					+ " The member must already be connected to a voice channel in the guild."
					+ ChannelFunctions.CHANNEL_ARGUMENT
					+ " Throws IllegalArgumentException if member is not connected to a voice channel."
					+ " Throws InsufficientPermissionException if the member and bot do not have access to the destination channel."
					+ " Requires the `Move Members` permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);

			Member member = Discord.GetMember(args[0], t);
			VoiceChannel channel = Discord.GetVoiceChannel(args[1], t);

			try {
				Discord.GetDefaultGuild().moveVoiceMember(member, channel).queue();
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
			return "void {closure} Retrieves an array of invite arrays for this guild."
					+ " Passes the array to the callback closure."
					+ " Each invite array contains data about the invite, including the keys 'code',"
					+ " 'channelid', and optionally 'userid' of the inviter."
					+ " Requires the `Manage Server` permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			final CClosure closure = (CClosure) args[0];
			try {
				Discord.GetDefaultGuild().retrieveInvites().queue((List<Invite> list) -> {
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
			return "array {} Gets an array of all cached members in this guild."
					+ " Array contains a list of user int ids.";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			MemberCacheView memberCache = Discord.GetDefaultGuild().getMemberCache();
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
			return "array {role} Gets an array of cached members in this guild with a given role."
					+ " Array contains a list of user int ids.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			Role role = Discord.GetRole(args[0], t);
			MemberCacheView memberCache = Discord.GetDefaultGuild().getMemberCache();
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
