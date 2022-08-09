package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.exceptions.PermissionException;

public class MemberFunctions {
	public static String docs() {
		return "Functions for managing Discord users/members.";
	}

	@api
	public static class discord_private_message extends Discord.Function {

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
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class};
		}
	}

	@api
	public static class discord_member_get_nickname extends Discord.Function {

		public String getName() {
			return "discord_member_get_nickname";
		}

		public String docs() {
			return "string {member} Get the server nickname for a member."
					+ " Member can be a user's numeric id or name."
					+ " Throws NotFoundException if a member by that name or id doesn't exist.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			Member member = Discord.GetMember(args[0], t);
			return new CString(member.getNickname(), t);
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class};
		}
	}

	@api
	public static class discord_member_set_nickname extends Discord.Function {

		public String getName() {
			return "discord_member_set_nickname";
		}

		public String docs() {
			return "void {member, string} Set the server nickname for a member."
					+ " Member can be a user's numeric id or name."
					+ " Throws NotFoundException if a member by that name or id doesn't exist."
					+ " Requires the 'Manage Nicknames' permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			Member member = Discord.GetMember(args[0], t);
			String newNickname = args[1].val();
			try {
				member.modifyNickname(newNickname).queue();
			} catch (PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class,
				CREInsufficientPermissionException.class};
		}
	}

	@api
	public static class discord_member_get_voice_channel extends Discord.Function {

		public String getName() {
			return "discord_member_get_voice_channel";
		}

		public String docs() {
			return "string {member} Get the ID of the member's current voice channel."
					+ " If the member is not connected to a voice channel, null is returned."
					+ " Member can be a user's numeric id or name."
					+ " Throws NotFoundException if a member by that name or id doesn't exist.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			Member member = Discord.GetMember(args[0], t);
			try {
				GuildVoiceState voiceState = member.getVoiceState();
				if(voiceState == null) {
					return CNull.NULL;
				}
				AudioChannel channel = voiceState.getChannel();
				if(channel == null) {
					return CNull.NULL;
				}
				return new CString(channel.getId(), t);
			} catch (PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			}
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREInsufficientPermissionException.class};
		}
	}

	@api
	public static class discord_member_is_muted extends Discord.Function {

		public String getName() {
			return "discord_member_is_muted";
		}

		public String docs() {
			return "boolean {member} Check if a user is muted, either self muted or server muted."
					+ " Member can be a user's numeric id or name."
					+ " Throws NotFoundException if a member by that name or id doesn't exist.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			Member member = Discord.GetMember(args[0], t);
			try {
				GuildVoiceState voiceState = member.getVoiceState();
				if(voiceState == null) {
					return CBoolean.FALSE;
				}
				return CBoolean.get(voiceState.isMuted());
			} catch (PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			}
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREInsufficientPermissionException.class};
		}
	}

	@api
	public static class discord_member_set_muted extends Discord.Function {

		public String getName() {
			return "discord_member_set_muted";
		}

		public String docs() {
			return "void {member, boolean} Set a user's server muted state."
					+ " Member can be a user's numeric id or name."
					+ " Throws NotFoundException if a member by that name or id doesn't exist or is not connected to a voice channel."
					+ " Requires the 'Mute Members' permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			
			Member member = Discord.GetMember(args[0], t);
			boolean muteState = ArgumentValidation.getBooleanObject(args[1], t);
			try {
				member.mute(muteState).queue();
			} catch (PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch (IllegalStateException ex) {
				throw new CRENotFoundException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREInsufficientPermissionException.class};
		}
	}
}
