# WAN-Show-Twitch-Bot

An IRC Chat bot for moderating the weekly WAN show Twitch stream https://www.twitch.tv/linustech

# Features
- Repeated Message Detection
- Advanced blacklisting including regex, words and messages.
- Auto-link shortening for tracking clicks through bit.ly
- Ignore mods (Will not attempt to check current moderators for breaches)
- Multi-tiered permission system - Prevents bot operators adding more operators
- Incremental Timeouts, timeouts will increase with number of breaches.
- Twitch Message Rate Bypass (Will open up multiple message streams to Twitch IRC Servers to avoid DoS protection).
- DM Users after ban
