package me.pseudoknight.chdiscord;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.attribute.IMemberContainer;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.managers.channel.ChannelManager;
import net.dv8tion.jda.api.managers.channel.middleman.StandardGuildMessageChannelManager;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.List;
import java.util.function.Consumer;

public class ChannelFunctions {
	public static String docs() {
		return "Functions for managing a Discord channel.";
	}

	static final String CHANNEL_ARGUMENT = " The `channel` argument can be a channel's unique int id. A channel's exact"
			+ " name can also be used, but if it's not unique, the first matching channel will be used."
			+ " If a channel is omitted, it will attempt to use the channel from the event bind context."
			+ " If not in an event bind, it will use the default channel.";

	@api
	public static class discord_broadcast extends Discord.Function {

		public String getName() {
			return "discord_broadcast";
		}

		public String docs() {
			return "void {[server], [channel], message, [callback]} Broadcasts text and embeds to the specified channel."
					+ GuildFunctions.SERVER_ARGUMENT
					+ CHANNEL_ARGUMENT
					+ " If channel is omitted, the channel from an event or first publicly viewable channel will be used."
					+ " Message can be a string or a message array object."
					+ " Callback closure is eventually executed with the message id for this message. (cannot be null)"
					+ " Message array must contain at least one of the following keys: 'content', 'embed', or 'embeds'."
					+ " It can also contain 'reference_id' (int) for replies,"
					+ " and 'allowed_mentions' (array with optional 'parse', 'users', 'roles', and 'replied_user' keys)"
					+ " to override default behavior set by discord_set_allowed_mentions()."
					+ " Embed array can include any of the following keys: 'title', 'url', 'description',"
					+ " 'timestamp' (int), 'image' (URL), 'thumbnail' (URL), 'color' (rgb array),"
					+ " 'footer' (array with 'text' and optionally 'icon_url' keys),"
					+ " 'author' (array with 'name' and optionally 'url' and/or 'icon_url' keys), and 'fields'"
					+ " (an array of field arrays, each with 'name', 'value', and optionally an 'inline' boolean)."
					+ " Messages have a 2000 character limit."
					+ " Requires the `View Channels` and `Send Messages` permissions."
					+ " (or `Send Messages in Threads` for thread channels)"
					+ " Also requires `Read Message History` permission for channel replies using 'reference_id'.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3, 4};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			Guild guild = null;
			GuildMessageChannel channel;
			Mixed message;
			CClosure callback;
			if (args.length == 4) {
				guild = Discord.GetGuild(args[0], t);
				channel = Discord.GetMessageChannel(args[1], guild, t);
				message = args[2];
				if (!(args[3] instanceof CClosure)) {
					throw new CREIllegalArgumentException("Expected a closure but got: " + args[3].val(), t);
				}
				callback = (CClosure) args[3];
			} else if(args.length == 3) {
				if(args[2] instanceof CClosure) {
					channel = Discord.GetMessageChannel(args[0], t);
					message = args[1];
					callback = (CClosure) args[2];
				} else {
					guild = Discord.GetGuild(args[0], t);
					channel = Discord.GetMessageChannel(args[1], guild, t);
					message = args[2];
					callback = null;
				}
			} else if(args.length == 2) {
				if(args[1] instanceof CClosure) {
					channel = Discord.GetMessageChannel(environment, t);
					message = args[0];
					callback = (CClosure) args[1];
				} else {
					channel = Discord.GetMessageChannel(args[0], t);
					message = args[1];
					callback = null;
				}
			} else {
				channel = Discord.GetMessageChannel(environment, t);
				message = args[0];
				callback = null;
			}
			Consumer<Message> onSuccess = null;
			if(callback != null) {
				onSuccess = (msg) -> StaticLayer.GetConvertor().runOnMainThreadLater(null, () ->
						callback.executeCallable(new CInt(msg.getIdLong(), t)));
			}
			try {
				if(message.isInstanceOf(CArray.TYPE)) {
					CArray messageArray = (CArray) message;
					try(MessageCreateData data = Discord.CreateMessage(messageArray, guild, t)) {
						MessageCreateAction action = channel.sendMessage(data);
						if (messageArray.containsKey("reference_id")) {
							action.setMessageReference(ArgumentValidation.getInt(messageArray.get("reference_id", t), t));
						}
						action.queue(onSuccess);
					}
				} else {
					channel.sendMessage(message.val()).queue(onSuccess);
				}
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
			return "void {[server], channel, id} Deletes a message with the given id on a channel."
					+ GuildFunctions.SERVER_ARGUMENT
					+ CHANNEL_ARGUMENT
					+ " Requires the `View Channels` permission. (and `Manage Messages` if message is from other user)";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			GuildMessageChannel channel;
			long id;
			if(args.length == 3) {
				channel = Discord.GetMessageChannel(args[1], Discord.GetGuild(args[0], t), t);
				id = ArgumentValidation.getInt(args[2], t);
			} else {
				channel = Discord.GetMessageChannel(args[0], t);
				id = ArgumentValidation.getInt(args[1], t);
			}
			try {
				channel.deleteMessageById(id).queue(null, (ex) -> Discord.HandleFailure(ex, t));
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
			return "void {[server], channel, string} Sets a topic for a text or news channel."
					+ GuildFunctions.SERVER_ARGUMENT
					+ CHANNEL_ARGUMENT
					+ " Standard channels have a 1024-character limit for topics."
					+ " Only Text and News channels support topics, otherwise an IllegalArgumentException is thrown."
					+ " Requires the `Manage Channels` permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			GuildMessageChannel channel;
			String topic;
			if(args.length == 3) {
				channel = Discord.GetMessageChannel(args[1], Discord.GetGuild(args[0], t), t);
				topic = args[2].val();
			} else {
				channel = Discord.GetMessageChannel(args[0], t);
				topic = args[1].val();
			}
			try {
				ChannelManager<?, ?> channelManager = channel.getManager();
				if(channelManager instanceof StandardGuildMessageChannelManager) {
					((StandardGuildMessageChannelManager<?, ?>) channelManager).setTopic(topic).queue(null, (ex) -> Discord.HandleFailure(ex, t));
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

	@api
	public static class discord_channel_members extends Discord.Function {

		public String getName() {
			return "discord_channel_members";
		}

		public String docs() {
			return "array {[server], channel} Returns an array of ids for members in a channel."
					+ "For voice channels, this returns all members that are currently connected to the channel."
					+ "Otherwise, for text channels this returns all members with the `View Channels` permission.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			GuildMessageChannel channel;
			if(args.length == 2) {
				channel = Discord.GetMessageChannel(args[1], Discord.GetGuild(args[0], t), t);
			} else {
				channel = Discord.GetMessageChannel(args[0], t);
			}
			try {
				if(channel instanceof IMemberContainer) {
					List<Member> memberList = ((IMemberContainer) channel).getMembers();
					CArray memberArray = new CArray(t, memberList.size());
					for(Member mem : memberList) {
						memberArray.push(new CInt(mem.getIdLong(), t), t);
					}
					return memberArray;
				}
				return new CArray(t);
			} catch(PermissionException ex) {
				throw new CREInsufficientPermissionException(ex.getMessage(), t);
			} catch(IllegalArgumentException ex) {
				throw new CREFormatException(ex.getMessage(), t);
			}
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREFormatException.class, CREIllegalArgumentException.class,
					CREInsufficientPermissionException.class};
		}
	}
}
