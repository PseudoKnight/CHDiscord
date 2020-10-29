package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CREInsufficientPermissionException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.PermissionException;

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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
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

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			Member mem = Discord.GetMember(args[0], t);
			List<Role> roles = new ArrayList<>();
			if(args[1] instanceof CArray) {
				CArray ca = (CArray) args[1];
				for(Construct key : ((CArray) args[1]).keySet()) {
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
