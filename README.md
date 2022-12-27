# CHDiscord

This is CommandHelper extension that uses the JDA library to talk to your Discord server.

CommandHelper 3.3.4 build #3978 or later is required for CHDiscord 2.4.0 or later.  
There are builds backported to CommandHelper 3.3.2 as well.

You'll need to have created a Discord application [here](https://discordapp.com/developers/applications/me).
Then add a Bot to it. Grab the "TOKEN" for the Bot that you'll use to connect this extension to Discord.
Enable the 'SERVER MEMBERS INTENT' and 'MESSAGE CONTENT INTENT' of the Privileged Gateway Intents.
Finally, you'll need your server id, which you can get by right-clicking your server name in Discord and clicking "Copy ID".

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

You have to run discord_connect() before you can use the other functionality of this extension, otherwise a
NotFoundException will be thrown. You cannot use other functions until the callback closure is executed.

### discord_disconnect()
Disconnects from the Discord server.

### discord_broadcast([channel], message)
Broadcasts text and embeds to the specified channel (or server default).
Message can be a string or a message array object.
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

### discord_member_get_voice_channel(member)
Get the ID of member's current voice channel.
If the member is not connected to a voice channel, null is returned.
Member can be a user's numeric id or name.  
Throws NotFoundException if a member by that name or id doesn't exist.

### discord_member_is_muted(member)
Check if a user is muted, either self muted or server muted.
Member can be a user's numeric id or name.  
Throws NotFoundException if a member by that name or id doesn't exist.

### discord_member_set_muted(member, boolean)
Set a user's server muted state.
Member can be a user's numeric id or name.  
Throws NotFoundException if a member by that name or id doesn't exist or is not connected to a voice channel.  
Requires the 'Mute Members' permission.

### discord_member_move_voice_channel(member, channel)
Moves a member to another voice channel.
The member must already be connected to a voice channel in the guild.
Member and channel can be a numeric id or name.  
Throws InsufficientPermissionException if the member does not have access to the destination channel.  
Requires the 'Move Members' permission.

## Events

### discord_message_received
This event is called when a user sends a message in the Discord server.  
Prefilters: username, channel  
Data: userid, username, nickname, channel, message, id, attachments {{url, filename, description}}

### discord_private_message_received
This event is called when a user sends a private message to the bot.  
Data: userid, username, message, id, attachments {{url, filename, description}}

### discord_voice_joined
This event is called when a user joined a voice channel.  
Data: userid, username, nickname, channel

### discord_voice_left
This event is called when a user left a voice channel.  
Data: userid, username, nickname, channel

### discord_member_joined
This event is called when a user joined the Discord server.  
Data: userid, username, nickname

### discord_member_left
This event is called when a user left the Discord server, including kick/ban.
Data: userid, username, nickname
