# Overview
coffee-bot is a Slack integration written in Spring Boot that allows you to be notified when a co-worker brews a fresh pot.

* 100% open source
* Built using SpringCloud / SpringBoot / Spring Data.
* All Brew objects are stored in an in memory database, or in an attached repository (ClearDB via a PWS Marketplace Service).
* Auto builds and deploys to PWS using TravisCI.
* All configurations can be specified at runtime.
* Includes a complete framework to implement new Slack Slash Commands.
* Custom annotation processor to register new Slack Slash Commands.
* Slack web hooks, and message builders built using [slack-integrations-api](https://github.com/GreaterMKEMeetup/slack-integrations-api) created by Greater Milwaukee Java Meetup.

# Slack Usage

Commands

* /coffee help
  * Replies with a list of available commands, and how to use them.
* /coffee brew Blue Heeler
  * Adds a pot of Blue Heeler to the database, and pings the #coffee channel.
* /coffee gone
  * Sets all 'available' pots of coffee to 'gone', and pings the "Brew Master" to make some more coffee.
* /coffee today
  * Prints a list of all the pots of coffee that have been brewed today, and if there is any left.

# Environment Variables

* **slack.brew.brew_master_id** - The Brew Master's Slack ID - example: ASDF1234 
* **slack.brew.brew_master_username** - The Brew Master's name - example: Jim
* **slack.brew.valid_command_tokens** - The command token set in the Slack Custom Slash Request configuration screen.  Specify as a CSV string.  If the incoming slash command doesn't contain a valid token, the request will not be processed.
* **slack.webhook.url** - The url for an incoming web hook.  This app needs an incoming web hook so that it can always post to the #coffee channel, despite what channel the command was executed in. - example: ht<i></i>tps://hooks.slack.com/services/ASDF1234/QWERT7890/aaV3298473DRAQBD

# How it works

The app is built using Spring Data JPA.  When running locally, the DB is H2.  When running in PWS, the DB is a ClearDB Spark instance that is persistent.  This is setup via the manifest.yml file that is auto-generated in the gradle build.

More detials and diagrams to come.
