#
# WANBOT Dockerfile
#

FROM openjdk/8-jdk
MAINTAINER Dominic Hauton <domhauton@gmail.com>
ADD build/libs/wanbot-*-all.jar /opt/wanbot/
RUN java -jar /opt/wanbot/wanbot-*-all.jar