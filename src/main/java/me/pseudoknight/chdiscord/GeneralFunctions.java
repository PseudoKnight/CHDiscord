package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.Activity;

public class GeneralFunctions {
	public static String docs() {
		return "Functions for managing this Discord bot.";
	}

	@api
	public static class discord_connect extends Discord.Function {

		public String getName() {
			return "discord_connect";
		}

		public String docs() {
			return "boolean {token, server_id, [callback] | profile, [callback]} Connects to Discord server via token and server id."
					+ " The server id can be retrieved by right-clicking the server name and clicking \"Copy ID\"."
					+ " The optional callback closure will be executed when a connection is made. The profile may be"
					+ " a string, which should refer to a profile defined in profiles.xml, with the keys token and"
					+ " serverId, or an array, with the same keys.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		private Extension.DiscordProfile getProfile(Environment environment, String profileName, Target t) {
			Profiles.Profile p;
			try {
				p = environment.getEnv(StaticRuntimeEnv.class).getProfiles().getProfileById(profileName);
			} catch (Profiles.InvalidProfileException ex) {
				throw new CREFormatException(ex.getMessage(), t, ex);
			}
			if(!(p instanceof Extension.DiscordProfile)) {
				throw new CRECastException("Profile type is expected to be \"discord\", but \"" + p.getType()
						+ "\"  was found.", t);
			}
			return (Extension.DiscordProfile) p;
		}

		public Mixed exec(Target t, Environment env, Mixed... args) throws ConfigRuntimeException {
			CClosure callback = null;
			Mixed profile = null;
			String token = null;
			String serverId = null;
			switch (args.length) {
				case 1:
					// Just a profile
					profile = args[0];
					break;
				case 2:
					// Not sure, could be (profile, callback) or (token, serverId)
					if(args[1].isInstanceOf(CClosure.class)) {
						// profile, callback
						profile = args[0];
						callback = ArgumentValidation.getObject(args[1], t, CClosure.class);
					} else {
						// token, serverId
						token = args[0].val();
						serverId = args[1].val();
					}	break;
				case 3:
					// Individual options
					token = args[0].val();
					serverId = args[1].val();
					callback = ArgumentValidation.getObject(args[2], t, CClosure.class);
					break;
				default:
					throw new CREIllegalArgumentException("Not enough/too many parameters", t);
			}
			if(profile != null) {
				if(profile.isInstanceOf(CArray.class)) {
					CArray prof = (CArray) profile;
					token = prof.get("token", t).val();
					serverId = prof.get("serverId", t).val();
				} else {
					Extension.DiscordProfile p = getProfile(env, profile.val(), t);
					token = p.getToken();
					serverId = p.getServerId();
				}
			}
			Discord.Connect(token, serverId, callback, env, t);
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRECastException.class, CREIllegalArgumentException.class};
		}
	}

	@api
	public static class discord_disconnect extends Discord.Function {

		public String getName() {
			return "discord_disconnect";
		}

		public String docs() {
			return "void {} Disconnects from the Discord server.";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public Mixed exec(Target t, final Environment env, Mixed... args) throws ConfigRuntimeException {
			Discord.Disconnect();
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return null;
		}
	}

	@api
	public static class discord_set_activity extends Discord.Function {

		public String getName() {
			return "discord_set_activity";
		}

		public String docs() {
			return "void {[type], string, [url]} Sets the activity tag for the bot."
					+ " Activity type can be one of " + StringUtils.Join(Activity.ActivityType.values(), ", ", ", or ") + "."
					+ " Activity string can be anything but an empty string."
					+ " If streaming, a valid Twitch URL must also be provided."
					+ " If not, or it's invalid, type will revert to PLAYING.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2, 3};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			try {
				Activity.ActivityType type = Activity.ActivityType.valueOf(args[0].val().toUpperCase());
				Activity activity = null;
				if (args.length == 1) {
					if(!(args[0] instanceof CNull)) {
						activity = Activity.playing(args[0].val());
					}
				} else if (type == Activity.ActivityType.STREAMING && args.length == 3) {
					activity = Activity.of(type, args[1].val(), args[2].val());
				} else {
					activity = Activity.of(type, args[1].val());
				}
				Discord.jda.getPresence().setActivity(activity);
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException(ex.getMessage(), t);
			}
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class};
		}
	}
}
