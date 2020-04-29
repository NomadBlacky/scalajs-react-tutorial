FROM adoptopenjdk/openjdk8:latest

RUN apt-get update && apt-get install --yes curl git openssh-client npm
