package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashMap;

@MSExtension("CHDiscord")
public class Extension extends AbstractExtension {

	static JDA jda = null;
	static Guild guild = null;
	static HashMap<String, TextChannel> channels = new HashMap<>();

	public Version getVersion() {
		return new SimpleVersion(1,1,2);
	}

	@Override
	public void onStartup() {
		System.out.println("CHDiscord " + getVersion() + " loaded.");
	}

	@Override
	public void onShutdown() {
		if(jda != null) {
			jda.shutdownNow();
			jda = null;
			guild = null;
			channels.clear();
		}
		System.out.println("CHDiscord " + getVersion() + " unloaded.");
	}

}
