package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.IllegalStateException;
import net.dv8tion.jda.api.exceptions.IllegalArgumentException;

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
	public static class discord_member_set_roles extends Discord.Function {

		public String getName() {
			return "discord_member_set_roles";
		}

		public String docs() {
			return "void {member, role(s)} Sets the roles for a server member."
					+ " The role argument can be an array or a single role."
					+ " Like members, a role can be the name or the numeric id."
					+ " Throws NotFoundException if a member or role by that name doesn't exist."
					+ " Requires the 'Manage Roles' permission.";
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

	@api
	public static class discord_member_move_voice_channel extends Discord.Function {

		public String getName() {
			return "discord_member_move_voice_channel";
		}

		public String docs() {
			return "void {member, channel} Moves a member to another voice channel."
					+ " The member must already be connected to a voice channel in the guild."
					+ " Member can be a user's numeric id or name."
					+ " Channel can be a voice channel's numeric id or name."
					+ " Throws NotFoundException if a member or channel by that name doesn't exist."
					+ " Throws InsufficientPermissionException if the member does not have access to the destination channel."
					+ " Requires the 'Move Members' permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}

			Member member = Discord.GetMember(args[0], t);
			VoiceChannel channel = Discord.GetVoiceChannel(args[1], t);

			try {
				Discord.guild.moveVoiceMember(member, channel).queue();
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
}
