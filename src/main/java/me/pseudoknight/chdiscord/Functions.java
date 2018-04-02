package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.util.List;

public class Functions {
	public static String docs() {
		return "Functions for the DiscordSRV plugin.";
	}

	public static abstract class DiscordFunction extends AbstractFunction {
		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return false;
		}

		public Version since() {
			return CHVersion.V3_3_2;
		}
	}

	@api
	public static class discord_connect extends DiscordFunction {

		public String getName() {
			return "discord_connect";
		}

		public String docs() {
			return "boolean {token, server_id} Connects to Discord server via token and server id. (The server id can"
					+ " be retrieved by right-clicking the server name and clicking \"Copy ID\".)";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment env, Construct... args) throws ConfigRuntimeException {
			if(Extension.jda != null) {
				Extension.jda.shutdownNow();
			}
			Thread connect = new Thread(() -> {
				try {
					Extension.jda = new JDABuilder(AccountType.BOT)
							.setToken(args[0].val())
							.setAudioEnabled(false)
							.setAutoReconnect(true)
							.addEventListener(new DiscordListener())
							.buildAsync();
				} catch(LoginException ex) {
					throw new CRENotFoundException("Could not connect to Discord server.", t);
				}

				while(Extension.jda.getStatus() != JDA.Status.CONNECTED) {
					try {
						Thread.sleep(10);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}

				StaticLayer.GetConvertor().runOnMainThreadLater(env.getEnv(GlobalEnv.class).GetDaemonManager(), () -> {
					Extension.guild = Extension.jda.getGuildById(args[1].val());
					for(TextChannel channel : Extension.guild.getTextChannels()) {
						Extension.channels.put(channel.getName(), channel);
					}
				});
			}, "DiscordConnect");
			connect.start();
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}
	}

	@api
	public static class discord_broadcast extends DiscordFunction {

		public String getName() {
			return "discord_broadcast";
		}

		public String docs() {
			return "void {[channel], string} Broadcasts text to the specified channel (or server default).";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if(Extension.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			TextChannel channel = args.length == 2 ? Extension.channels.get(args[0].val()) : Extension.guild.getDefaultChannel();
			if(channel == null) {
				throw new CRENotFoundException("Channel by the name " + args[0].val() + " not found.", t);
			}
			String message = args[args.length - 1].val();
			channel.sendMessage(message).queue();
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

	@api
	public static class discord_private_message extends DiscordFunction {

		public String getName() {
			return "discord_private_message";
		}

		public String docs() {
			return "void {member, string} Sends a private message to the specified Discord server member.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			if(Extension.guild == null) {
				throw new CRENotFoundException("Not connected to Discord server.", t);
			}
			List<Member> m = Extension.guild.getMembersByName(args[0].val(), false);
			if(m.isEmpty()) {
				throw new CRENotFoundException("A member with the name \"" + args[0].val() + "\" was not found on Discord server.", t);
			}
			final String message = args[1].val();
			m.get(0).getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

}
