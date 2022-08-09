package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CRE.CREIllegalArgumentException;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;
import me.pseudoknight.chdiscord.abstraction.jda.Listener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.util.EnumSet;
import java.util.List;

public class Discord {

	public static JDA jda = null;

	static Guild guild = null;

	private static Thread connection;
	private static DaemonManager dm;

	static void Connect(String token, String guildID, CClosure callback, Environment env, Target t) {
		if(jda != null) {
			Disconnect();
		}
		dm = env.getEnv(StaticRuntimeEnv.class).GetDaemonManager();
		connection = new Thread(() -> {
			try {
				jda = JDABuilder.create(token, EnumSet.of(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES,
								GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
						.disableCache(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.STICKER,
								CacheFlag.EMOJI, CacheFlag.MEMBER_OVERRIDES, CacheFlag.ONLINE_STATUS))
						.setAutoReconnect(true)
						.addEventListeners(new Listener())
						.build()
						.awaitReady();

			} catch(LoginException | IllegalStateException | InterruptedException | ErrorResponseException ex) {
				MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "Could not connect to Discord. " + ex.getMessage(), t);
				Disconnect();
				return;
			}

			guild = jda.getGuildById(guildID);
			if(guild == null) {
				MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "The specified Discord server does not exist: " + guildID, t);
				Disconnect();
				return;
			}

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

	static void Disconnect() {
		if(jda != null) {
			jda.shutdownNow();
			dm.deactivateThread(connection);
		}

		jda = null;
		guild = null;
		dm = null;
		connection = null;
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

	static Member GetMember(Mixed m, Target t) {
		Member mem;
		if(m instanceof CInt) {
			mem = guild.getMemberById(((CInt) m).getInt());
			if(mem == null) {
				throw new CRENotFoundException("A member with the id \"" + m.val() + "\" was not found on Discord server.", t);
			}
		} else {
			try {
				mem = guild.getMemberById(m.val());
				if(mem == null) {
					throw new CRENotFoundException("A member with the id \"" + m.val() + "\" was not found on Discord server.", t);
				}
			} catch (NumberFormatException ex) {
				if(m.val().isEmpty()) {
					throw new CREIllegalArgumentException("A member id was expected but was given an empty string.", t);
				}
				List<Member> mems = guild.getMembersByName(m.val(), false);
				if(mems.isEmpty()) {
					throw new CRENotFoundException("A member with the name \"" + m.val() + "\" was not found on Discord server.", t);
				}
				mem = mems.get(0);
			}
		}
		return mem;
	}

	static CArray GetMemberData(Member mem, Target t) {
		CArray data = CArray.GetAssociativeArray(t);
		data.set("userid", new CInt(mem.getIdLong(), t), t);
		data.set("username", mem.getUser().getName());
		data.set("tag", mem.getUser().getAsTag());
		data.set("nickname", mem.getNickname());
		CArray roles = CArray.GetAssociativeArray(t);
		for(Role role : mem.getRoles()) {
			roles.set(role.getName(), new CInt(role.getIdLong(), t), t);
		}
		data.set("roles", roles, t);
		if(mem.hasTimeJoined()) {
			data.set("joined", new CInt(mem.getTimeJoined().toEpochSecond(), t), t);
		}
		return data;
	}

	static Role GetRole(Mixed m, Target t) {
		Role role;
		if(m instanceof CInt) {
			role = guild.getRoleById(((CInt) m).getInt());
			if(role == null) {
				throw new CRENotFoundException("A role with the id \"" + m.val() + "\" was not found on Discord server.", t);
			}
		} else {
			try {
				role = guild.getRoleById(m.val());
				if(role == null) {
					throw new CRENotFoundException("A role with the id \"" + m.val() + "\" was not found on Discord server.", t);
				}
			} catch (NumberFormatException ex) {
				if(m.val().isEmpty()) {
					throw new CREIllegalArgumentException("A role id was expected but was given an empty string.", t);
				}
				List<Role> r = guild.getRolesByName(m.val(), false);
				if(r.isEmpty()) {
					throw new CRENotFoundException("A role with the name \"" + m.val() + "\" was not found on Discord server.", t);
				}
				role = r.get(0);
			}
		}
		return role;
	}

	static TextChannel GetTextChannel(Mixed m, Target t) {
		if(m.val().isEmpty()) {
			throw new CREIllegalArgumentException("A channel name was expected but was given an empty string.", t);
		}
		List<TextChannel> channels =  guild.getTextChannelsByName(m.val(), false);
		if(channels.isEmpty()) {
			throw new CRENotFoundException("Channel by the name " + m.val() + " not found.", t);
		}
		return channels.get(0);
	}

	static Message GetMessage(Mixed m, Target t) {
		MessageBuilder builder = new MessageBuilder();
		if(m instanceof CArray) {
			CArray array = ArgumentValidation.getArray(m, t);
			if(!array.isAssociative()) {
				throw new CREIllegalArgumentException("Message array must be associative.", t);
			}
			if(array.containsKey("embed")) {
				builder.setEmbeds(GetEmbed(array.get("embed", t), t));
			} else if(array.containsKey("embeds")) {
				CArray cEmbeds = ArgumentValidation.getArray(array.get("embeds", t), t);
				MessageEmbed[] embeds = new MessageEmbed[(int) cEmbeds.size()];
				for(int i = 0; i < cEmbeds.size(); i++) {
					embeds[i] = GetEmbed(cEmbeds.get(i, t), t);
				}
				builder.setEmbeds(embeds);
			}
			if(array.containsKey("content")) {
				builder.setContent(array.get("content", t).val());
			}
		} else {
			builder.setContent(m.val());
		}
		return builder.build();
	}

	static MessageEmbed GetEmbed(Mixed m, Target t) {
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
			Mixed mColor = embed.get("color", t);
			MCColor color;
			if(mColor instanceof CArray) {
				color = ObjectGenerator.GetGenerator().color((CArray) mColor, t);
			} else {
				color = StaticLayer.GetConvertor().GetColor(mColor.val(), t);
			}
			builder.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
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
		return builder.build();
	}

	static VoiceChannel GetVoiceChannel(Mixed m, Target t) {
		VoiceChannel channel;
		if(m.val().isEmpty()) {
			throw new CREIllegalArgumentException("A voice channel id or name was expected but was given an empty string.", t);
		}

		if(m instanceof CInt) {
			channel = guild.getVoiceChannelById(((CInt) m).getInt());
			if(channel == null) {
				throw new CRENotFoundException("A voice channel with the id \"" + m.val() + "\" was not found on Discord server.", t);
			}
		} else {
			try {
				channel = guild.getVoiceChannelById(m.val());
				if(channel == null) {
					throw new CRENotFoundException("A voice channel with the id \"" + m.val() + "\" was not found on Discord server.", t);
				}
			} catch (NumberFormatException ex) {
				List<VoiceChannel> channels = guild.getVoiceChannelsByName(m.val(), false);
				if(channels.isEmpty()) {
					throw new CRENotFoundException("A channel with the name \"" + m.val() + "\" was not found on Discord server.", t);
				}
				channel = channels.get(0);
			}
		}
		return channel;
	}
}
