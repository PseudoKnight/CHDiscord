package me.pseudoknight.chdiscord;

import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

public class ChannelFunctions {
	public static String docs() {
		return "Functions for managing a Discord channel.";
	}

	@api
	public static class discord_broadcast extends Discord.Function {

		public String getName() {
			return "discord_broadcast";
		}

		public String docs() {
			return "void {[channel], string} Broadcasts text to the specified channel (or server default)."
					+ " Message must not be empty, else it will throw a IllegalArgumentException."
					+ " Requires the 'Send Messages' permission.";
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
				channel = Discord.GetTextChannel(args[0], t);
			} else {
				channel = Discord.guild.getDefaultChannel();
				if(channel == null) {
					throw new CRENotFoundException("Default channel for bot not found.", t);
				}
			}
			String message = args[args.length - 1].val();
			try {
				channel.sendMessage(message).queue();
			} catch(PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch(IllegalArgumentException ex) {
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
	public static class discord_delete_message extends Discord.Function {

		public String getName() {
			return "discord_delete_message";
		}

		public String docs() {
			return "void {channel, id} Deletes a message on a channel with the given id."
					+ " Requires 'Manage Messages' permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			TextChannel channel = Discord.GetTextChannel(args[0], t);
			long id = ArgumentValidation.getInt(args[1], t);
			try {
				channel.deleteMessageById(id).queue();
			} catch(PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch(IllegalArgumentException ex) {
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
	public static class discord_set_channel_topic extends Discord.Function {

		public String getName() {
			return "discord_set_channel_topic";
		}

		public String docs() {
			return "void {channel, string} Sets a text channel's topic."
					+ " Requires the 'Manage Channels' permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			if(Discord.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			TextChannel channel = Discord.GetTextChannel(args[0], t);
			String message = args[1].val();
			try {
				channel.getManager().setTopic(message).queue();
			} catch(PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch(IllegalArgumentException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREFormatException.class, CREIllegalArgumentException.class,
					CREInsufficientPermissionException.class};
		}
	}
}
