package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.CRE.*;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.natives.interfaces.Mixed;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.requests.*;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import net.dv8tion.jda.internal.requests.RestActionImpl;

import java.util.EnumSet;

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
			return "boolean {token, serverId, [callback] | profile, [callback]} Connects to Discord server via token and server id."
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
				// TODO: withState
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

	@api
	public static class discord_set_allowed_mentions extends Discord.Function {

		public String getName() {
			return "discord_set_allowed_mentions";
		}

		public String docs() {
			return "void {array} Sets a list of mention types that will be parsed by default in sent messages."
					+ " Array can include 'USER', 'ROLE', and 'EVERYONE'."
					+ " If given null, it resets the default to all types.";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			if(args[0] instanceof CNull) {
				MessageRequest.setDefaultMentions(null);
				return CVoid.VOID;
			}
			CArray array = ArgumentValidation.getArray(args[0], t);
			if(array.isAssociative()) {
				throw new CREIllegalArgumentException("Mention type array must not be associative.", t);
			}
			EnumSet<Message.MentionType> mentionTypes = EnumSet.noneOf(Message.MentionType.class);
			for(Mixed value : array.asList()) {
				try {
					mentionTypes.add(Message.MentionType.valueOf(value.val()));
				} catch (IllegalArgumentException ex) {
					throw new CREIllegalArgumentException("Invalid mention type: " + value.val(), t);
				}
			}
			MessageRequest.setDefaultMentions(mentionTypes);
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class, CRECastException.class};
		}
	}

	@api
	public static class discord_get_servers extends Discord.Function {

		public String getName() {
			return "discord_get_servers";
		}

		public String docs() {
			return "array {} Gets an array of ids for all the guild servers that the bot is added to.";
		}

		public Integer[] numArgs() {
			return new Integer[]{0};
		}

		public Mixed exec(Target t, final Environment env, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);
			CArray array = new CArray(t);
			for(Guild guild : Discord.jda.getGuilds()) {
				array.push(new CInt(guild.getIdLong(), t), t);
			}
			return array;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

	@api
	public static class discord_request extends Discord.Function {

		public String getName() {
			return "discord_request";
		}

		public String docs() {
			return "void {method, route, [dataObject], [onSuccess], [onFailure]} Sends a custom HTTP request to Discord."
					+ " This is for advanced users that need to use Discord API that is not yet added to this extension."
					+ " You must refer to the Discord documentation for routes, methods, parameters, and permissions."
					+ " The method argument can be one of GET, PATCH, DELETE, PUT or POST."
					+ " The dataObject argument is the JSON parameters, and can be an array, string or null."
					+ " If the request was successful, the onSuccess closure will be executed and passed an array of"
					+ " response data. If the request failed, the onFailure closure will instead be executed and passed"
					+ " a failure message. If not provided, the default handler will instead log any failures.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2, 3, 4, 5};
		}

		public Mixed exec(Target t, final Environment env, Mixed... args) throws ConfigRuntimeException {
			Discord.CheckConnection(t);

			Method method;
			try {
				method = Method.valueOf(args[0].val());
			} catch (IllegalArgumentException ex) {
				throw new CREIllegalArgumentException("Invalid HTTP method: " + args[0].val(), t);
			}
			Route route = Route.custom(method, args[1].val());

			DataObject dataObject;
			if(args.length < 3) {
				dataObject = null;
			} else if(args[2].isInstanceOf(CArray.TYPE)) {
				try {
					dataObject = DataObject.fromJson(Construct.json_encode(args[2], t));
				} catch (MarshalException e) {
					throw new CREFormatException(e.getMessage(), t);
				}
			} else {
				String data = Construct.nval(args[2]);
				if(data == null) {
					dataObject = null;
				} else {
					try {
						dataObject = DataObject.fromJson(data);
					} catch (ParsingException ex) {
						throw new CREFormatException(ex.getMessage(), t);
					}
				}
			}
			if(dataObject != null && (method == Method.GET || method == Method.HEAD)) {
				throw new CREIllegalArgumentException("Method " + method.name() + " most not have data", t);
			}

			CClosure onSuccess;
			if(args.length > 3) {
				if (!(args[3] instanceof CClosure)) {
					throw new CREIllegalArgumentException("Expected onSuccess to be a closure but got: " + args[3].val(), t);
				}
				onSuccess = (CClosure) args[3];
			} else {
				onSuccess = null;
			}

			CClosure onFailure;
			if(args.length > 4) {
				if (!(args[4] instanceof CClosure)) {
					throw new CREIllegalArgumentException("Expected onFailure to be a closure but got: " + args[4].val(), t);
				}
				onFailure = (CClosure) args[4];
			} else {
				onFailure = null;
			}

			Route.CompiledRoute compiledRoute = route.compile();
			RestAction<Mixed> action = new RestActionImpl<>(Discord.jda, compiledRoute, dataObject, (response, request) -> {
				try {
					return Construct.json_decode(response.getObject().toString(), t);
				} catch (MarshalException e) {
					Static.getLogger().severe(e.getMessage());
					return CNull.NULL;
				}
			});
			action.queue(onSuccess == null ? null :(Mixed m) -> StaticLayer.GetConvertor().runOnMainThreadLater(null, () -> {
				onSuccess.executeCallable(m);
			}), onFailure == null ? null : (Throwable ex) -> StaticLayer.GetConvertor().runOnMainThreadLater(null, () -> {
				onFailure.executeCallable(new CString(ex.getMessage(), t));
			}));
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class, CREIllegalArgumentException.class, CREFormatException.class};
		}
	}
}
