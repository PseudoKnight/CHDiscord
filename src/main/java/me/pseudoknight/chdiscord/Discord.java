package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.constructs.CClosure;
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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
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
								GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES))
						.disableCache(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS,
								CacheFlag.EMOTE, CacheFlag.MEMBER_OVERRIDES))
						.setAutoReconnect(true)
						.addEventListeners(new Listener())
						.build()
						.awaitReady();

			} catch(LoginException | IllegalStateException | InterruptedException ex) {
				MSLog.GetLogger().e(MSLog.Tags.RUNTIME, "Could not connect to Discord.", t);
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

	public static Object GetAllMembers() {
		List<Member> members = guild.getMembers();
		return members;
	}
}
