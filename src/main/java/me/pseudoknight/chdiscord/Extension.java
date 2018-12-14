package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.ProgramFlowManipulationException;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import me.pseudoknight.chdiscord.abstraction.jda.Listener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.util.HashMap;

@MSExtension("CHDiscord")
public class Extension extends AbstractExtension {

	public static JDA jda = null;

	static Guild guild = null;
	static HashMap<String, TextChannel> channels = new HashMap<>();

	private static Thread connection;
	private static DaemonManager dm;

	public Version getVersion() {
		return new SimpleVersion(1,3,0, "SNAPSHOT");
	}

	@Override
	public void onStartup() {
		if(!Implementation.GetServerType().equals(Implementation.Type.SHELL)) {
			System.out.println("CHDiscord " + getVersion() + " loaded.");
		}
	}

	@Override
	public void onShutdown() {
		if(!Implementation.GetServerType().equals(Implementation.Type.SHELL)) {
			System.out.println("CHDiscord " + getVersion() + " unloaded.");
		}
	}

	static void connectDiscord(String token, String guildID, CClosure callback, Environment env, Target t) {
		if(jda != null) {
			disconnectDiscord();
		}
		dm = env.getEnv(GlobalEnv.class).GetDaemonManager();
		connection = new Thread(() -> {
			try {
				jda = new JDABuilder(AccountType.BOT)
						.setToken(token)
						.setAudioEnabled(false)
						.setAutoReconnect(true)
						.addEventListener(new Listener())
						.build()
						.awaitReady();

				guild = jda.getGuildById(guildID);
				for(TextChannel channel : Extension.guild.getTextChannels()) {
					channels.put(channel.getName(), channel);
				}

			} catch(LoginException | IllegalStateException | InterruptedException ex) {
				CHLog.GetLogger().e(CHLog.Tags.RUNTIME, "Could not connect to Discord server.", t);
				dm.deactivateThread(null);
				return;
			}

			if(callback != null) {
				StaticLayer.GetConvertor().runOnMainThreadLater(dm, () -> {
					try {
						callback.execute();
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
		StaticLayer.GetConvertor().addShutdownHook(Extension::disconnectDiscord);
	}

	static void disconnectDiscord() {
		if(jda != null) {
			jda.shutdownNow();
			dm.deactivateThread(connection);
		}

		jda = null;
		guild = null;
		channels.clear();
		dm = null;
		connection = null;
	}

}
