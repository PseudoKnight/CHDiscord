package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import github.scarsz.discordsrv.DiscordSRV;
import java.util.logging.Level;

@MSExtension("CHDiscord")
public class Extension extends AbstractExtension {

	private DiscordSRVListener discordsrvListener = new DiscordSRVListener();

	public Version getVersion() {
		return new SimpleVersion(0,0,1);
	}

	@Override
	public void onStartup() {
		try {
			DiscordSRV.api.subscribe(discordsrvListener);
			System.out.println("CHDiscord " + getVersion() + " loaded.");
		} catch(NoClassDefFoundError ex) {
			Static.getLogger().log(Level.WARNING, "DiscordSRV plugin is missing.");
		}
	}

	@Override
	public void onShutdown() {
		DiscordSRV.api.unsubscribe(discordsrvListener);
		System.out.println("CHDiscord " + getVersion() + " unloaded.");
	}

}
