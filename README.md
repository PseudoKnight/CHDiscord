# CHDiscord

This is CommandHelper extension that uses the JDA library to talk to your Discord server.

You'll need a Discord Bot "Token" from an app you can create [here](https://discordapp.com/developers/applications/me) as well as your server id that you can get by right-clicking your server name and clicking "Copy ID". You have to run discord_connect(token, server_id) before you can use the other functionality of this extension.

NOTE: This could conflict with other Discord plugins, including DiscordSRV. This is meant as a standalone solution for handling all Discord integration for your server.

## Functions

### discord_connect(token, server_id)
Connects to Discord server via token and server id.

### discord_broadcast([channel], string)
Broadcasts text to the specified channel (or default).

### discord_private_message(user, string)
Sends a private message to the specified Discord server member.

## Events

### discord_message_received
This event is called when a user sends a message in the Discord server.
Prefilters: username, channel
Data: username, nickname, channel, message