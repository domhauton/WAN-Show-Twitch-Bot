# WAN-Show-Twitch-Bot

[![Build Status](https://travis-ci.org/domhauton/wanbot.svg?branch=master)](https://travis-ci.org/domhauton/wanbot) [![Dependency Status](https://www.versioneye.com/user/projects/592b3bbbc0295d003a53faca/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/592b3bbbc0295d003a53faca) [![codecov](https://codecov.io/gh/domhauton/wanbot/branch/master/graph/badge.svg)](https://codecov.io/gh/domhauton/wanbot) [![license](https://img.shields.io/github/license/mashape/apistatus.svg)]()

[![Docker Version](https://images.microbadger.com/badges/version/domhauton/wanbot.svg)](https://hub.docker.com/r/domhauton/wanbot) [![Docker Pulls](https://img.shields.io/docker/pulls/domhauton/wanbot.svg)](https://hub.docker.com/r/domhauton/wanbot) [![Docker Size](https://images.microbadger.com/badges/image/domhauton/wanbot.svg)](https://microbadger.com/images/domhauton/wanbot) [![Docker Build](https://img.shields.io/docker/build/domhauton/wanbot.svg)](https://hub.docker.com/r/domhauton/wanbot)

An IRC Chat bot for moderating the weekly WAN show Twitch stream https://www.twitch.tv/linustech

# Features
- Repeated Message Detection
- Advanced blacklisting including regex, words and messages.
- Auto-link shortening
- Ignore mods (Will not attempt to check current moderators for breaches)
- Multi-tiered permission system - Prevents wanbot operators adding more operators
- Incremental Timeouts, timeouts will increase with number of breaches.
- Twitch Message Rate Bypass (Will open up multiple message streams to Twitch IRC Servers to avoid DoS protection).
- DM Users after ban
