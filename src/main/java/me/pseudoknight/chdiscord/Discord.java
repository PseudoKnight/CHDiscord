package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;
import me.pseudoknight.chdiscord.abstraction.events.DiscordGuildChannelEvent;
import me.pseudoknight.chdiscord.abstraction.events.DiscordGuildEvent;
import me.pseudoknight.chdiscord.abstraction.jda.Listener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IGuildChannelContainer;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

public class Discord {

	public static JDA jda = null;

	private static Guild defaultGuild = null;
	private static Thread connection;
	private static DaemonManager dm;
	private static boolean connecting = false;

	static void Connect(String token, String guildID, CClosure callback, Environment env, Target t) {
		if(connecting) {
			MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "Attempted to connect to Discord multiple times simultaneously.", t);
			return;
		}
		if(jda != null) {
			Disconnect();
		}
		connecting = true;
		dm = env.getEnv(StaticRuntimeEnv.class).GetDaemonManager();
		connection = new Thread(() -> {
			try {
				jda = JDABuilder.create(token, EnumSet.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES,
								GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT,
								GatewayIntent.GUILD_MESSAGE_REACTIONS))
						.disableCache(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.STICKER,
								CacheFlag.EMOJI, CacheFlag.MEMBER_OVERRIDES, CacheFlag.ONLINE_STATUS,
								CacheFlag.SCHEDULED_EVENTS, CacheFlag.ROLE_TAGS, CacheFlag.FORUM_TAGS))
						.setAutoReconnect(true)
						.addEventListeners(new Listener())
						.build()
						.awaitReady();

			} catch(InvalidTokenException | IllegalStateException | InterruptedException | ErrorResponseException ex) {
				MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "Could not connect to Discord. " + ex.getMessage(), t);
				Disconnect();
				return;
			} finally {
				connecting = false;
			}

			defaultGuild = jda.getGuildById(guildID);
			if(defaultGuild == null) {
				MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "The specified Discord server does not exist: " + guildID, t);
				Disconnect();
				return;
			}

			RestAction.setDefaultFailure((ex) -> HandleFailure(ex, Target.UNKNOWN));

			if(callback != null) {
				StaticLayer.GetConvertor().runOnMainThreadLater(dm, () -> {
					try {
						callback.executeCallable();
					} catch (ConfigRuntimeException ex) {
						ConfigRuntimeException.HandleUncaughtException(ex, env);
					} catch (ProgramFlowManipulationException ex) {
						// do nothing
					}
				});
			}
		}, "DiscordConnect");
		dm.activateThread(connection);
		connection.start();
		StaticLayer.GetConvertor().addShutdownHook(Discord::Disconnect);
	}

	static void CheckConnection(Target t) {
		if(jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
			throw new CRENotFoundException("Not connected to Discord.", t);
		}
	}

	static void Disconnect() {
		if(jda != null) {
			jda.shutdownNow();
			if(!CommandHelperPlugin.self.isEnabled()) {
				try {
					jda.awaitShutdown(Duration.ofSeconds(1));
				} catch (InterruptedException proceedAnyway) {}
			}
			dm.deactivateThread(connection);
		}

		jda = null;
		defaultGuild = null;
		dm = null;
		connection = null;
	}

	static void HandleFailure(Throwable ex, Target t) {
		if(ex instanceof CancellationException || ex instanceof TimeoutException || t != Target.UNKNOWN) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, ex.getMessage(), t);
		} else if(ex.getCause() != null) {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, ex.getMessage() + "\nCaused by: "
					+ StackTraceUtils.GetStacktrace(ex.getCause()), t);
		} else {
			MSLog.GetLogger().e(MSLog.Tags.GENERAL, ex, t);
		}
	}

	public static abstract class Function extends AbstractFunction {
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

	/**
	 * Returns the Guild from an event's environment context,
	 * or the default guild specified when connecting.
	 *
	 * @param env The execution environment
	 * @return Guild
	 */
	static Guild GetGuild(Environment env) {
		BoundEvent.ActiveEvent event = env.getEnv(GlobalEnv.class).GetEvent();
		if(event != null && event.getUnderlyingEvent() instanceof DiscordGuildEvent) {
			return ((DiscordGuildEvent) event.getUnderlyingEvent()).getGuild();
		}
		return defaultGuild;
	}

	/**
	 * Returns the Guild with the given id.
	 *
	 * @param id The guild id
	 * @param t Code target
	 * @return Guild
	 */
	static Guild GetGuild(Mixed id, Target t) {
		if(id instanceof CNull) {
			return defaultGuild;
		}

		Guild guild;

		// Find by unique int id
		if(id instanceof CInt) {
			guild = jda.getGuildById(((CInt) id).getInt());
			if(guild == null) {
				throw new CRENotFoundException("A guild with the id \"" + id.val() + "\" was not found.", t);
			}
			return guild;
		}

		if(id.val().isEmpty()) {
			throw new CREIllegalArgumentException("A guild id was expected but was given an empty string.", t);
		}

		// Find by unique string id.
		try {
			guild = jda.getGuildById(id.val());
			if(guild != null) {
				return guild;
			}
		} catch (NumberFormatException ignored) {}

		throw new CRENotFoundException("A guild with the id \"" + id.val() + "\" was not found.", t);
	}

	/**
	 * Returns the User with the given id.
	 *
	 * @param id The user id
	 * @param t Code target
	 * @return User
	 */
	static User GetUser(Mixed id, Target t) {
		User usr;

		// Find by unique int id
		if(id instanceof CInt) {
			usr = jda.getUserById(((CInt) id).getInt());
			if(usr == null) {
				throw new CRENotFoundException("A user with the id \"" + id.val() + "\" was not found.", t);
			}
			return usr;
		}

		if(id.val().isEmpty()) {
			throw new CREIllegalArgumentException("A user id was expected but was given an empty string.", t);
		}

		// Find by unique string id.
		try {
			usr = jda.getUserById(id.val());
			if(usr != null) {
				return usr;
			}
		} catch (NumberFormatException ignored) {}

		// Find by username
		List<User> usrs = jda.getUsersByName(id.val(), false);
		if(!usrs.isEmpty()) {
			return usrs.get(0);
		}

		throw new CRENotFoundException("A user with the id \"" + id.val() + "\" was not found.", t);
	}

	/**
	 * Returns the Member with the given user id.
	 *
	 * @param id The user id or name
	 * @param guild The guild server with the desired member
	 * @param t Code target
	 * @return Member
	 */
	static Member GetMember(Mixed id, Guild guild, Target t) {
		Member mem;

		// Find by unique int id
		if(id instanceof CInt) {
			mem = guild.getMemberById(((CInt) id).getInt());
			if(mem == null) {
				throw new CRENotFoundException("A member with the id \"" + id.val() + "\" was not found on Discord server.", t);
			}
			return mem;
		}

		if(id.val().isEmpty()) {
			throw new CREIllegalArgumentException("A user id was expected but was given an empty string.", t);
		}

		// Find by unique string id.
		try {
			mem = guild.getMemberById(id.val());
			if(mem != null) {
				return mem;
			}
		} catch (NumberFormatException ignored) {}

		// Find by username
		List<Member> mems = guild.getMembersByName(id.val(), false);
		if(!mems.isEmpty()) {
			return mems.get(0);
		}

		throw new CRENotFoundException("A member with the id \"" + id.val() + "\" was not found on Discord server.", t);
	}

	/**
	 * Returns the Role of the given id or name.
	 *
	 * @param id The role id or name
	 * @param guild The guild server with the desired role
	 * @param t Code target
	 * @return Role
	 */
	static Role GetRole(Mixed id, Guild guild, Target t) {
		Role role;
		if(id instanceof CInt) {
			role = guild.getRoleById(((CInt) id).getInt());
		} else {
			if(id.val().isEmpty()) {
				throw new CREIllegalArgumentException("A role id was expected but was given an empty string.", t);
			}
			try {
				role = guild.getRoleById(id.val());
			} catch (NumberFormatException ex) {
				List<Role> r = guild.getRolesByName(id.val(), false);
				if(r.isEmpty()) {
					throw new CRENotFoundException("A role with the name \"" + id.val() + "\" was not found on Discord server.", t);
				}
				role = r.get(0);
			}
		}
		if(role == null) {
			throw new CRENotFoundException("A role with the id \"" + id.val() + "\" was not found on Discord server.", t);
		}
		return role;
	}

	/**
	 * Returns the GuildMessageChannel from the given event bind environment.
	 * If not running within an event bind environment, this instead returns the default channel of the default guild
	 * specified during connection.
	 *
	 * @param env The runtime Environment
	 * @param t Code target
	 * @return GuildMessageChannel
	 */
	static GuildMessageChannel GetMessageChannel(Environment env, Target t) {
		BindableEvent event = env.getEnv(GlobalEnv.class).GetEvent().getUnderlyingEvent();
		if(event instanceof DiscordGuildChannelEvent) {
			Channel channel = ((DiscordGuildChannelEvent) event).getChannel();
			if(channel instanceof GuildMessageChannel) {
				return (GuildMessageChannel) channel;
			}
		}
		DefaultGuildChannelUnion channel = defaultGuild.getDefaultChannel();
		if(channel == null) {
			throw new CRENotFoundException("There is no publicly viewable message channel.", t);
		}
		return channel.asStandardGuildMessageChannel();
	}

	/**
	 * Returns the GuildMessageChannel of the given id.
	 *
	 * @param id The channel name or id
	 * @param t Code target
	 * @return GuildMessageChannel
	 */
	static GuildMessageChannel GetMessageChannel(Mixed id, Target t) {
		return GetMessageChannel(id, null, t);
	}

	/**
	 * Returns the GuildMessageChannel of the given id.
	 *
	 * @param id The channel name or id
	 * @param guild The guild from which to get the channel
	 * @param t Code target
	 * @return GuildMessageChannel
	 */
	static GuildMessageChannel GetMessageChannel(Mixed id, Guild guild, Target t) {
		IGuildChannelContainer<?> container = guild == null ? jda : guild;
		long channelId;
		if(id instanceof CInt) {
			channelId = ((CInt) id).getInt();
		} else {
			if(id.val().isEmpty()) {
				throw new CREIllegalArgumentException("A channel id was expected but was given an empty string.", t);
			}
			try {
				channelId = MiscUtil.parseLong(id.val());
			} catch (NumberFormatException ex) {
				// get channel by name
				List<? extends GuildMessageChannel> channels = container.getTextChannelsByName(id.val(), false);
				if(channels.isEmpty()) {
					channels = container.getNewsChannelsByName(id.val(), false);
				}
				if(channels.isEmpty()) {
					channels = container.getVoiceChannelsByName(id.val(), false);
				}
				if(channels.isEmpty()) {
					channels = container.getThreadChannelsByName(id.val(), false);
				}
				if(channels.isEmpty()) {
					channels = container.getStageChannelsByName(id.val(), false);
				}
				if(channels.isEmpty()) {
					throw new CRENotFoundException("A channel with the name \"" + id.val() + "\" was not found.", t);
				}
				if(channels.size() > 1) {
					Static.getLogger().warning("More than one channel found with the name: " + id.val());
				}
				return channels.get(0);
			}
		}
		GuildMessageChannel channel = container.getTextChannelById(channelId);
		if(channel == null) {
			channel = container.getNewsChannelById(channelId);
		}
		if(channel == null) {
			channel = container.getVoiceChannelById(channelId);
		}
		if(channel == null) {
			channel = container.getThreadChannelById(channelId);
		}
		if(channel == null) {
			channel = container.getStageChannelById(channelId);
		}
		if(channel == null) {
			throw new CRENotFoundException("A channel with the id \"" + id.val() + "\" was not found.", t);
		}
		return channel;
	}

	static MessageCreateData CreateMessage(CArray array, Guild guild, Target t) {
		MessageCreateBuilder  builder = new MessageCreateBuilder();
		if(!array.isAssociative()) {
			throw new CREIllegalArgumentException("Message array must be associative.", t);
		}
		if(array.containsKey("embed")) {
			builder.setEmbeds(CreateEmbed(array.get("embed", t), t));
		} else if(array.containsKey("embeds")) {
			CArray cEmbeds = ArgumentValidation.getArray(array.get("embeds", t), t);
			MessageEmbed[] embeds = new MessageEmbed[(int) cEmbeds.size()];
			for(int i = 0; i < cEmbeds.size(); i++) {
				embeds[i] = CreateEmbed(cEmbeds.get(i, t), t);
			}
			builder.setEmbeds(embeds);
		}
		if(array.containsKey("content")) {
			builder.setContent(array.get("content", t).val());
		}
		if(array.containsKey("allowed_mentions")) {
			CArray allowedMentionsArray = ArgumentValidation.getArray(array.get("allowed_mentions", t), t);
			CArray parseArray = null;
			if(allowedMentionsArray.isAssociative()) {
				if(allowedMentionsArray.containsKey("parse")) {
					parseArray = ArgumentValidation.getArray(allowedMentionsArray.get("parse", t), t);
					if(parseArray.isAssociative()) {
						throw new CREIllegalArgumentException("Allowed mention parse array must not be associative.", t);
					}
				}
				if(allowedMentionsArray.containsKey("users")) {
					CArray usersArray = ArgumentValidation.getArray(allowedMentionsArray.get("users", t), t);
					if(usersArray.isAssociative()) {
						throw new CREIllegalArgumentException("User mention array must not be associative.", t);
					}
					List<String> usersMentions = new ArrayList<>((int) usersArray.size());
					for(Mixed value : usersArray.asList()) {
						usersMentions.add((guild == null ? GetUser(value, t) : GetMember(value, guild, t)).getId());
					}
					builder.mentionUsers(usersMentions);
				}
				if(allowedMentionsArray.containsKey("roles")) {
					CArray rolesArray = ArgumentValidation.getArray(allowedMentionsArray.get("roles", t), t);
					if(rolesArray.isAssociative()) {
						throw new CREIllegalArgumentException("Role mention array must not be associative.", t);
					}
					List<String> roleMentions = new ArrayList<>((int) rolesArray.size());
					for(Mixed value : rolesArray.asList()) {
						roleMentions.add(GetRole(value, guild == null ? defaultGuild : guild, t).getId());
					}
					builder.mentionRoles(roleMentions);
				}
				if(allowedMentionsArray.containsKey("replied_user")) {
					builder.mentionRepliedUser(ArgumentValidation.getBooleanObject(allowedMentionsArray.get("replied_user", t), t));
				}
			} else {
				parseArray = allowedMentionsArray;
			}
			if(parseArray != null) {
				EnumSet<Message.MentionType> mentionTypes = EnumSet.noneOf(Message.MentionType.class);
				for (Mixed value : parseArray.asList()) {
					try {
						mentionTypes.add(Message.MentionType.valueOf(value.val()));
					} catch (IllegalArgumentException ex) {
						throw new CREIllegalArgumentException("Invalid mention type: " + value.val(), t);
					}
				}
				builder.setAllowedMentions(mentionTypes);
			}
		}
		return builder.build();
	}

	static MessageEmbed CreateEmbed(Mixed m, Target t) {
		CArray embed = ArgumentValidation.getArray(m, t);
		if(!embed.isAssociative()) {
			throw new CREIllegalArgumentException("Embed array must be associative.", t);
		}
		EmbedBuilder builder = new EmbedBuilder();
		if(embed.containsKey("title")) {
			if(embed.containsKey("url")) {
				builder.setTitle(embed.get("title", t).val(), embed.get("url", t).val());
			} else {
				builder.setTitle(embed.get("title", t).val());
			}
		} else if(embed.containsKey("url")) {
			builder.setUrl(embed.get("url", t).val());
		}
		if(embed.containsKey("description")) {
			builder.setDescription(embed.get("description", t).val());
		}
		if(embed.containsKey("image")) {
			builder.setImage(embed.get("image", t).val());
		}
		if(embed.containsKey("thumbnail")) {
			builder.setThumbnail(embed.get("thumbnail", t).val());
		}
		if(embed.containsKey("color")) {
			builder.setColor(GetColor(embed.get("color", t), t));
		}
		if(embed.containsKey("footer")) {
			Mixed cFooter = embed.get("footer", t);
			if(cFooter instanceof CArray) {
				CArray footerArray = ArgumentValidation.getArray(cFooter, t);
				if (!footerArray.isAssociative()) {
					throw new CREIllegalArgumentException("Footer array must be associative.", t);
				}
				String text = footerArray.get("text", t).val();
				if (footerArray.containsKey("icon_url")) {
					builder.setFooter(text, footerArray.get("icon_url", t).val());
				} else {
					builder.setFooter(text);
				}
			} else {
				builder.setFooter(cFooter.val());
			}
		}
		if(embed.containsKey("author")) {
			CArray cAuthor = ArgumentValidation.getArray(embed.get("author", t), t);
			if(!cAuthor.isAssociative()) {
				throw new CREIllegalArgumentException("Author array must be associative.", t);
			}
			String name = cAuthor.get("name", t).val();
			String url = null;
			String iconUrl = null;
			if(cAuthor.containsKey("url")) {
				url = cAuthor.get("url", t).val();
			}
			if(cAuthor.containsKey("icon_url")) {
				iconUrl = cAuthor.get("icon_url", t).val();
			}
			builder.setAuthor(name, url, iconUrl);
		}
		if(embed.containsKey("fields")) {
			CArray cFields = ArgumentValidation.getArray(embed.get("fields", t), t);
			if(cFields.isAssociative()) {
				throw new CREIllegalArgumentException("Fields array must not be associative.", t);
			}
			for(Mixed entry : cFields.asList()) {
				CArray cField = ArgumentValidation.getArray(entry, t);
				if(cFields.isAssociative()) {
					throw new CREIllegalArgumentException("Field array must be associative.", t);
				}
				String name = cField.get("name", t).val();
				String value = cField.get("value", t).val();
				boolean inline = false;
				if(cField.containsKey("inline")) {
					inline = ArgumentValidation.getBooleanObject(cField.get("inline", t), t);
				}
				builder.addField(name, value, inline);
			}
		}
		if(embed.containsKey("timestamp")) {
			long timestamp = ArgumentValidation.getInt(embed.get("timestamp", t), t);
			builder.setTimestamp(Instant.ofEpochMilli(timestamp));
		}
		return builder.build();
	}

	/**
	 * Returns the VoiceChannel of the given id or name.
	 *
	 * @param id The voice channel id or name
	 * @param guild The guild for the voice channel
	 * @param t Code target
	 * @return VoiceChannel
	 */
	static VoiceChannel GetVoiceChannel(Mixed id, Guild guild, Target t) {
		VoiceChannel channel;
		if(id instanceof CInt) {
			channel = guild.getVoiceChannelById(((CInt) id).getInt());
		} else {
			if(id.val().isEmpty()) {
				throw new CREIllegalArgumentException("A voice channel id or name was expected but was given an empty string.", t);
			}
			try {
				channel = guild.getVoiceChannelById(id.val());
			} catch (NumberFormatException ex) {
				List<VoiceChannel> channels = guild.getVoiceChannelsByName(id.val(), false);
				if(channels.isEmpty()) {
					throw new CRENotFoundException("A channel with the name \"" + id.val() + "\" was not found on Discord server.", t);
				}
				channel = channels.get(0);
			}
		}
		if(channel == null) {
			throw new CRENotFoundException("A voice channel with the id \"" + id.val() + "\" was not found on Discord server.", t);
		}
		return channel;
	}

	enum ColorName {
		WHITE(Color.WHITE),
		SILVER(Color.LIGHT_GRAY),
		GRAY(Color.GRAY),
		BLACK(Color.BLACK),
		RED(Color.RED),
		MAROON(128, 0, 0),
		YELLOW(Color.YELLOW),
		OLIVE(128, 128, 0),
		LIME(Color.GREEN),
		GREEN(0, 128, 0),
		AQUA(Color.CYAN),
		TEAL(0, 128, 128),
		BLUE(Color.BLUE),
		NAVY(0, 0, 128),
		FUCHSIA(Color.MAGENTA),
		PURPLE(128, 0, 128),
		ORANGE(255, 165, 0);

		final Color c;

		ColorName(int r, int g, int b) {
			this.c = new Color(r, g, b);
		}

		ColorName(Color c) {
			this.c = c;
		}

		Color getColor() {
			return c;
		}
	}

	static Color GetColor(Mixed m, Target t) {
		if(m instanceof CArray) {
			CArray array = (CArray) m;
			int red;
			int green;
			int blue;
			if(array.isAssociative()) {
				if(array.containsKey("r")) {
					red = ArgumentValidation.getInt32(array.get("r", t), t);
					green = ArgumentValidation.getInt32(array.get("g", t), t);
					blue = ArgumentValidation.getInt32(array.get("b", t), t);
				} else if(array.containsKey("red")) {
					red = ArgumentValidation.getInt32(array.get("red", t), t);
					green = ArgumentValidation.getInt32(array.get("green", t), t);
					blue = ArgumentValidation.getInt32(array.get("blue", t), t);
				} else if(array.containsKey(0)) {
					red = ArgumentValidation.getInt32(array.get(0, t), t);
					green = ArgumentValidation.getInt32(array.get(1, t), t);
					blue = ArgumentValidation.getInt32(array.get(2, t), t);
				} else {
					throw new CREFormatException("Expected a color array but was missing rgb keys.", t);
				}
			} else {
				red = ArgumentValidation.getInt32(array.get(0, t), t);
				green = ArgumentValidation.getInt32(array.get(1, t), t);
				blue = ArgumentValidation.getInt32(array.get(2, t), t);
			}
			try {
				return new Color(red, green, blue);
			} catch (IllegalArgumentException ex) {
				throw new CRERangeException(ex.getMessage(), t, ex);
			}
		} else {
			try {
				return ColorName.valueOf(m.val()).getColor();
			} catch(IllegalArgumentException ignore){}
			MSLog.GetLogger().w(MSLog.Tags.RUNTIME, "Invalid color: " + m.val(), t);
			return null;
		}
	}
}
