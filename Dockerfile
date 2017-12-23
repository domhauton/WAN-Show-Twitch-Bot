# A docker image for the linustechtips wanbot
#

FROM openjdk/8-jdk

MAINTAINER Dominic Hauton <domhauton@gmail.com>

USER dockeruser
WORKDIR /home/dockeruser

ENV INSTALL_DIR=/opt/wanbot

COPY build/libs $INSTALL_DIR/app
COPY build/output/libs $INSTALL_DIR/libs
COPY prod.yml ./.config/wanbot.yml

LABEL org.label-schema.name="wanbot" \
        org.label-schema.description="Chat bot monitoring the linustechtips wan show twitch chat." \
        org.label-schema.vcs-url="https://github.com/domhauton/wanbot" \
        org.label-schema.usage="README.md" \
        org.label-schema.schema-version="1.0"

RUN java -cp "$INSTALL_DIR/libs/*:$(ls $INSTALL_DIR/app/*.jar)" com.domhauton.wanbot.Main