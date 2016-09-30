1) coffee-bot is a Slack integration written in Spring Boot that allows you to be notified when a co-worker brews a fresh pot.

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

# Environment Varialbes

(need to update this)

# How it works

The app is built using Spring Data JPA.  When running locally, the DB is H2.  When running in PWS, the DB is a ClearDB Spark instance that is persistent.  This is setup via the manifest.yml file that is auto-generated in the gradle build.

More detials and diagrams to come.
