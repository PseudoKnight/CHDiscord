package me.pseudoknight.chdiscord;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRENotFoundException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.core.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.core.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;

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
	public static class discord_broadcast extends DiscordFunction {

		public String getName() {
			return "discord_broadcast";
		}

		public String docs() {
			return "void {[channel], string} Broadcasts text to the specified channel (or default).";
		}

		public Integer[] numArgs() {
			return new Integer[]{1, 2};
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			TextChannel channel = args.length == 2 ? DiscordUtil.getTextChannelById(args[0].val()) : DiscordSRV.getPlugin().getMainTextChannel();
			String message = args[args.length - 1].val();
			DiscordUtil.sendMessage(channel, message);
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{};
		}
	}

	@api
	public static class discord_private_message extends DiscordFunction {

		public String getName() {
			return "discord_private_message";
		}

		public String docs() {
			return "void {user, string} Sends a private message to the specified Discord server member.";
		}

		public Integer[] numArgs() {
			return new Integer[]{2};
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			Member m = DiscordUtil.getMemberById(args[0].val());
			if(m == null) {
				throw new CRENotFoundException("A member with the name \"" + args[0].val() + "\" was not found on Discord.", t);
			}
			String message = args[1].val();
			DiscordUtil.privateMessage(m.getUser(), message);
			return CVoid.VOID;
		}

		public Class<? extends CREThrowable>[] thrown() {
			return new Class[]{CRENotFoundException.class};
		}
	}

}
