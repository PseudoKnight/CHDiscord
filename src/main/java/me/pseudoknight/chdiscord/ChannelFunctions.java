package me.pseudoknight.chdiscord;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.managers.channel.middleman.StandardGuildMessageChannelManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.function.Consumer;

public class ChannelFunctions {
	public static String docs() {
		return "Functions for managing a Discord channel.";
	}

	static final String CHANNEL_ARGUMENT = " The 'channel' argument can be a channel's unique int id. A channel name"
			+ " can also be used if you use only unique channel names, otherwise the first matching channel will be used.";

	@api
	public static class discord_broadcast extends Discord.Function {

		public String getName() {
			return "discord_broadcast";
		}

		public String docs() {
			return "void {[channel], message, [callback]} Broadcasts text and embeds to the specified channel."
					+ CHANNEL_ARGUMENT
					+ " If omitted, this function will use the server default channel."
					+ " Message can be a string or a message array object."
					+ " Callback closure is eventually executed with the message id for this message."
					+ " Message array must contain at least one of the following keys: 'content', 'embed', or 'embeds'."
					+ " Embed array can include any of the following keys: 'title', 'url' (requires title), 'description',"
					+ " 'image', 'thumbnail', 'color' (rgb array), 'footer' (contains 'text' and optionally 'icon_url'),"
					+ " 'author' (contains 'name' and optionally 'url' and/or 'icon_url'), and 'fields'"
					+ " (an array of field arrays, each with 'name', 'value', and optionally an 'inline' boolean)."
					+ " Messages have a 2000 character limit."
					+ " Requires the 'Send Messages' permission (and 'Embed Links' permission if only sending an embed)";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			GuildMessageChannel channel;
			Mixed message;
			Consumer<Message> onSuccess = null;
			if(args.length > 1) {
				channel = Discord.GetMessageChannel(args[0], t);
				message = args[1];
				if(args.length == 3) {
					if(!(args[2] instanceof CClosure)) {
						throw new CREIllegalArgumentException("Expected a closure but got: " + args[2].val(), t);
					}
					final CClosure closure = (CClosure) args[2];
					onSuccess = (msg) -> StaticLayer.GetConvertor().runOnMainThreadLater(null, () ->
							closure.executeCallable(new CInt(msg.getIdLong(), t)));
				}
			} else {
				DefaultGuildChannelUnion defaultChannel = Discord.guild.getDefaultChannel();
				if(defaultChannel == null || defaultChannel.getType() != ChannelType.TEXT) {
					throw new CRENotFoundException("Default channel for bot not found.", t);
				}
				channel = defaultChannel.asTextChannel();
				message = args[0];
			}
			try {
				MessageCreateData data = Discord.GetMessage(message, t);
				channel.sendMessage(data).queue(onSuccess);
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
			return "void {channel, id} Deletes a message with the given id on a channel."
					+ CHANNEL_ARGUMENT
					+ " Requires 'Manage Messages' permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			GuildMessageChannel channel = Discord.GetMessageChannel(args[0], t);
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
			return "void {channel, string} Sets a topic for a text or news channel."
					+ CHANNEL_ARGUMENT
					+ " Currently rate-limited to twice every 10 minutes."
					+ " Only Text and News channels support topics, otherwise an IllegalArgumentException is thrown."
					+ " Requires the 'Manage Channels' permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			GuildMessageChannel channel = Discord.GetMessageChannel(args[0], t);
			String message = args[1].val();
			try {
				ChannelManager<?, ?> channelManager = channel.getManager();
				if(channelManager instanceof StandardGuildMessageChannelManager) {
					((StandardGuildMessageChannelManager<?, ?>) channelManager).setTopic(message).queue();
				} else {
					throw new CREIllegalArgumentException("Cannot set topic for this channel type.", t);
				}
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
