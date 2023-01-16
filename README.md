# CHDiscord

This is extension for the CommandHelper plugin that uses the JDA library to talk to your Discord server.

CommandHelper 3.3.4 build #3978 or later is required.  
There are builds backported to CommandHelper 3.3.2 as well.

You'll need to have created a Discord application [here](https://discordapp.com/developers/applications/me).
Then add a Bot to it. Grab the "TOKEN" for the Bot that you'll use to connect this extension to Discord.
Enable the 'SERVER MEMBERS INTENT' and 'MESSAGE CONTENT INTENT' of the Privileged Gateway Intents.
Finally, you'll need your server id, which you can get by right-clicking your server name in Discord and clicking "Copy ID".

## Bot Functions

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

You have to run discord_connect() before you can use the other functionality of this extension, otherwise a
NotFoundException will be thrown. You cannot use other functions until the callback closure is executed.

### discord_disconnect()
Disconnects from the Discord server.

### discord_set_activity(type, string, [url])
Sets the activity tag for the bot.  
Activity type can be one of PLAYER, STREAMING, LISTENING, WATCHING, CUSTOM_STATUS, COMPETING.  
Activity string can be anything but an empty string.  
If streaming, a valid Twitch URL must also be provided.  
If not, or it's invalid, type will revert to PLAYING.

## Channel Functions
* The `channel` argument can be a channel's unique int id. A channel name can also be used if you use
only unique channel names, otherwise the first matching channel will be used.

### discord_broadcast([channel], message, [callback])
Broadcasts text and embeds to the specified channel.  
If channel is omitted, this function will use the server default channel.  
Message can be a string or a message array object.  
Callback closure is eventually executed with the message id for this message.  
Message array must contain at least one of the following keys: 'content', 'embed', or 'embeds'.  
Embed array can include any of the following keys: 'title', 'url' (requires title), 'description',
'image' (URL), 'thumbnail' (URL), 'color' (rgb array), 'footer' (contains 'text' and optionally 'icon_url'),
'author' (contains 'name' and optionally 'url' and/or 'icon_url'), and 'fields'
(an array of field arrays, each with 'name', 'value', and optionally an 'inline' boolean).  
Requires the 'Send Messages' permission (and 'Embed Links' permission if only sending an embed)

Message object array format: (as displayed, top to bottom, left to right)
```
{
  content: "This displays above the embed as normal text.",
  embeds: [{
    color: {
      r: 255,
      g: 255,
      b: 255
    },
    author: {
      icon_url: "https://website.com/author_avatar.png",
      name: "PseudoKnight",
      url: "https://website.com/author_link/"
    },
    thumbnail: "https://website.com/top_right_thumbnail.png",
    title: "Large Bold Text",
    url: "https://website.com/title_link/",
    description: "Normal sized text just under title.",
    fields: [
      {
        name: "Field A",
        value: "Value A Below Name",
        inline: true
      }
    ],
    image: "https://website.com/image.png",
    footer: {
        icon_url: "https://website.com/footer_icon.png",
        text: "Small text at the bottom."
    }
  }]
}
```

### discord_delete_message(channel, id)
Deletes a message with the given id on a channel.  
Requires the 'Manage Messages' permission.

### discord_set_channel_topic(channel, string)
Sets a topic for a text or news channel. (Currently rate-limited to twice every 10 minutes)  
Only Text and News channels support topics, otherwise an IllegalArgumentException is thrown.  
Requires the 'Manage Channels' permission.

## Member Functions
* The `member` argument is user's unique int id. A username can also be used, but if the name is not unique in
the guild only the first matching user will be used. Throws NotFoundException if a member by that id doesn't exist.

### discord_private_message(member, string)
Sends a private message to a user.

### discord_member_get_roles(member)
Gets an associative array of all server roles for a member.  
The key is the role name, and the value is the role int id.

### discord_member_set_roles(member, role(s))
Sets the roles for a server member.  
The role argument can be an array or a single role.  
A role is either a unique int id or name.  
Throws NotFoundException if a role by that id doesn't exist.  
Requires the 'Manage Roles' permission.

### discord_member_get_nickname(member)
Get the server nickname for a guild member.  

### discord_member_set_nickname(member, string)
Set the server nickname for a guild member.  
Requires the 'Manage Nicknames' permission.

## Voice Functions

### discord_member_get_voice_channel(member)
Get the ID of member's current voice channel.  
If the member is not connected to a voice channel, null is returned.

### discord_member_move_voice_channel(member, channel)
Moves a member to another voice channel.  
The member must already be connected to a voice channel in the guild.  
Throws IllegalArgumentException if member is not connected to a voice channel.  
Throws InsufficientPermissionException if the member does not have access to the destination channel.  
Requires the 'Move Members' permission.

### discord_member_is_muted(member)
Check if a user is muted, either self muted or server muted.

### discord_member_set_muted(member, boolean)
Set a user's server muted state.  
Throws IllegalArgumentException if member is not connected to a voice channel.  
Requires the 'Mute Members' permission.

## Events

### discord_message_received
This event is called when a user sends a message in the Discord server.  
Prefilters: username, channel  
Data: userid, username, nickname, channel, channelid, channeltype, message, id, attachments {{url, filename, description}}

### discord_private_message_received
This event is called when a user sends a private message to the bot.  
Data: userid, username, message, id, attachments {{url, filename, description}}

### discord_voice_joined
This event is called when a user joined a voice channel.  
Data: userid, username, nickname, channel, channelid

### discord_voice_left
This event is called when a user left a voice channel.  
Data: userid, username, nickname, channel, channelid

### discord_member_joined
This event is called when a user joined the Discord server.  
Data: userid, username, nickname

### discord_member_left
This event is called when a user left the Discord server, including kick/ban.  
Data: userid, username, nickname
