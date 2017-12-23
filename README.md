# CHDiscord

This is a very basic extension that uses the DiscordSRV plugin API to talk to a Discord server.

If you wish to override DiscordSRV's automatic handling of chat integration, you must disable DiscordChatChannelDiscordToMinecraft and DiscordChatChannelMinecraftToDiscord in the DiscordSRV config.yml. You can also set things like MinecraftPlayerJoinMessageFormat to "" in messages.yml to create your own custom messages for events.

## Functions

### discord_broadcast([channel], string)
Broadcasts text to the specified channel (or default).

### discord_private_message(user, string)
Sends a private message to the specified Discord server member.

## Events

### discord_message_received
This event is called when a user sends a message in the Discord server.
Prefilters: username, channel
Data: username, nickname, channel, message