package me.pseudoknight.chdiscord;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.core.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.core.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.core.entities.TextChannel;
import org.bukkit.Bukkit;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class DiscordSRVListener {

	@Subscribe(priority = ListenerPriority.MONITOR)
	public void discordMessageReceived(final DiscordGuildMessageReceivedEvent event) {
		final DiscordSRVMessageReceivedEvent mre = new DiscordSRVMessageReceivedEvent(event);
		Future f = Bukkit.getServer().getScheduler().callSyncMethod(CommandHelperPlugin.self, new Callable() {
			@Override
			public Object call() throws Exception {
				EventUtils.TriggerListener(Driver.EXTENSION, "discord_message_received", mre);
				return null;
			}
		});
	}

	public interface DiscordMessageReceivedEvent extends BindableEvent {
		TextChannel getChannel();
		Member getMember();
		Message getMessage();
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class DiscordSRVMessageReceivedEvent implements DiscordMessageReceivedEvent {
		DiscordGuildMessageReceivedEvent e;

		public DiscordSRVMessageReceivedEvent(DiscordGuildMessageReceivedEvent e) {
			this.e = e;
		}

		public TextChannel getChannel() {
			return e.getChannel();
		}

		public Member getMember() {
			return e.getMember();
		}

		public Message getMessage() {
			return e.getMessage();
		}

		@Override
		public Object _GetObject() {
			return e;
		}
	}

}
