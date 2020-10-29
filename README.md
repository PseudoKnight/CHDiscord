# CHDiscord

This is CommandHelper extension that uses the JDA library to talk to your Discord server.

You'll need to have created a Discord application [here](https://discordapp.com/developers/applications/me).
Then add a Bot to it. Grab the "TOKEN" for the Bot that you'll use to connect this extension to Discord.
Finally, you'll need your server id, which you can get by right-clicking your server name and clicking "Copy ID".

You have to run discord_connect() before you can use the other functionality of this extension, otherwise a
NotFoundException will be thrown.

##### Additional Requirements as of Version 2.4.0:
- The Server Members Intent, which is used for member caching, is required on the Bot page
- CommandHelper 3.3.2 is required (check master branch for later versions)

## Functions

### discord_connect(token, server_id, [callback] | profile, [callback])
Connects to Discord server via token and server id.
The optional callback closure will be executed when a connection is made.
The profile may be a string, which should refer to a profile defined in profiles.xml,
with the keys token and serverId, or an array, with the same keys.

The profile should be defined such as

    <profile id="discordCredentials">
        <type>discord</type>
        <token>abcdefg</token>
        <serverId>12345</serverId>
    </profile>

### discord_disconnect()
Disconnects from the Discord server.

### discord_broadcast([channel], string)
Broadcasts text to the specified channel (or default). 
Message must not be empty, else it will throw an IllegalArgumentException.
Requires the 'Send Messages' permission.

### discord_delete_message(channel, id)
Deletes a message on a channel with the given id.
Requires the 'Manage Messages' permission.

### discord_private_message(user, string)
Sends a private message to the specified Discord server member.
The user numeric id or name can be used to specify which server member to send to.
If there are multiple members with the same user name, only the first one is messaged.
Therefore it is recommended to use the user id.

### discord_set_activity(type, string, [url])
Sets the activity tag for the bot.
Activity type can be one of DEFAULT, STREAMING, or LISTENING
Activity string can be anything but an empty string.
If streaming, a valid Twitch URL must also be provided.
If not, or it's invalid, type will revert to DEFAULT (ie. playing).

### discord_set_channel_topic(channel, string)
Sets a text channel's topic. (Currently rate-limited to twice every 10 minutes)
Requires the 'Manage Channels' permission.

### discord_member_get_roles(member)
Gets an associative array of all server roles for a member.
The key is the role name, and the value is the role numeric id.
Throws NotFoundException if a member by that name doesn't exist.

### discord_member_set_roles(member, role(s))
Sets the roles for a server member.
The role argument can be an array or a single role.
Like members, a role can be the name or the numeric id.
Throws NotFoundException if a member or role by that name doesn't exist.
Requires the 'Manage Roles' permission.

### discord_member_get_nickname(member)
Get the server nickname for a member.
Member can be a user's numeric id or name.
Throws NotFoundException if a member by that name or id doesn't exist.

### discord_member_set_nickname(member, string)
Set the server nickname for a member.
Member can be a user's numeric id or name.
Throws NotFoundException if a member by that name or id doesn't exist.
Requires the 'Manage Nicknames' permission.

## Events

### discord_message_received
This event is called when a user sends a message in the Discord server.
Prefilters: username, channel
Data: userid, username, nickname, channel, message, id

### discord_private_message_received
This event is called when a user sends a private message to the bot.
Data: userid, username, message, id

### discord_voice_joined
This event is called when a user joined a voice channel.
Data: userid, username, nickname, channel

### discord_voice_left
This event is called when a user left a voice channel.
Data: userid, username, nickname, channel

### discord_member_joined
This event is called when a user joined the Discord server.
Data: userid, username, nickname
