package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.Color;

public class MemberFunctions {
	public static String docs() {
		return "Functions for managing Discord users/members.";
	}

	static final String MEMBER_ARGUMENT = " The 'member' argument is user's unique int id. A username can also be used,"
			+ " but if the name is not unique in the guild only the first matching user will be used."
			+ " Throws NotFoundException if a member by that id doesn't exist.";

	@api
	public static class discord_private_message extends Discord.Function {

		public String getName() {
			return "discord_private_message";
		}

		public String docs() {
			return "void {member, string} Sends a private message to the specified Discord server member."
					+ MEMBER_ARGUMENT
					+ " Messages have a 2000 character limit.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			Member mem = Discord.GetMember(args[0], t);
			Mixed message = args[1];
			try {
				MessageCreateData data = Discord.GetMessage(message, t);
				mem.getUser().openPrivateChannel().queue(channel -> channel.sendMessage(data).queue());
			} catch(IllegalArgumentException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class, CREFormatException.class};
		}
	}

	@api
	public static class discord_member_get_nickname extends Discord.Function {

		public String getName() {
			return "discord_member_get_nickname";
		}

		public String docs() {
			return "string {member} Get the server nickname for a member."
					+ MEMBER_ARGUMENT;
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
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
					+ MEMBER_ARGUMENT
					+ " Requires the `Manage Nicknames` permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
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
					+ MEMBER_ARGUMENT;
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
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
					+ MEMBER_ARGUMENT;
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
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
					+ MEMBER_ARGUMENT
					+ " Throws IllegalArgumentException if member is not connected to a voice channel."
					+ " Requires the 'Mute Members' permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			
			Member member = Discord.GetMember(args[0], t);
			boolean muteState = ArgumentValidation.getBooleanObject(args[1], t);
			try {
				member.mute(muteState).queue();
			} catch (PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch (IllegalStateException ex) {
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
	public static class discord_member_info extends Discord.Function {

		public String getName() {
			return "discord_member_info";
		}

		public String docs() {
			return "array {member} Gets an array of data for Discord user."
					+ " Array contains 'userid', 'username', 'bot', "
					+ " 'nickname' (for server), 'color' array (for server), and 'avatar' url (for server).";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			CArray ret = CArray.GetAssociativeArray(t);

			Member member = Discord.GetMember(args[0], t);
			ret.set("nickname", member.getNickname());
			Color color = member.getColor();
			if(color != null) {
				CArray colorArray = CArray.GetAssociativeArray(t);
				colorArray.set("r", new CInt(color.getRed(), t), t);
				colorArray.set("g", new CInt(color.getGreen(), t), t);
				colorArray.set("b", new CInt(color.getBlue(), t), t);
				ret.set("color", colorArray, t);
			} else {
				ret.set("color", CNull.NULL, t);
			}
			ret.set("avatar", member.getEffectiveAvatarUrl());

			User user = member.getUser();
			ret.set("userid", new CInt(user.getIdLong(), t), t);
			ret.set("username", user.getName());
			ret.set("bot", CBoolean.get(user.isBot()), t);
			return ret;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class};
		}
	}
}
