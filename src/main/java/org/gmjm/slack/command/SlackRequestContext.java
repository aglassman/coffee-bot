package org.gmjm.slack.command;

import org.gmjm.slack.api.model.SlackCommand;

public class SlackRequestContext
{
	public final SlackCommand slackCommand;


	public SlackRequestContext(SlackCommand slackCommand)
	{
		this.slackCommand = slackCommand;
	}


	public SlackCommand getSlackCommand()
	{
		return slackCommand;
	}
}
