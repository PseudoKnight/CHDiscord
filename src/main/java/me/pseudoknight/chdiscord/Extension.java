package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;

import java.util.Map;

@MSExtension("CHDiscord")
public class Extension extends AbstractExtension {

	public Version getVersion() {
		return new SimpleVersion(2,13,0);
	}

	@Override
	public void onStartup() {
		if(!Implementation.GetServerType().equals(Implementation.Type.SHELL)) {
			Static.getLogger().info("CHDiscord " + getVersion() + " loaded.");
		}
	}

	@Override
	public void onShutdown() {
		if(!Implementation.GetServerType().equals(Implementation.Type.SHELL)) {
			Static.getLogger().info( "CHDiscord " + getVersion() + " unloaded.");
		}
	}

	@Profiles.ProfileType(type = "discord")
	public static class DiscordProfile extends Profiles.Profile {
		private final String token;
		private final String serverId;
		public DiscordProfile(String id, Map<String, String> elements) throws Profiles.InvalidProfileException {
			super(id);
			if(elements.containsKey("token")) {
				token = elements.get("token");
			} else {
				throw new Profiles.InvalidProfileException("token is a required parameter in the profile");
			}
			if(elements.containsKey("serverId")) {
				serverId = elements.get("serverId");
			} else {
				throw new Profiles.InvalidProfileException("serverId is a required parameter in the profile");
			}
		}

		public String getToken() {
			return this.token;
		}

		public String getServerId() {
			return this.serverId;
		}
	}

}
