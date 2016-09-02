package org.gmjm.slack.command.processor;

import org.gmjm.slack.api.hook.HookResponse;
import org.gmjm.slack.api.message.SlackMessageBuilder;
import org.gmjm.slack.api.model.SlackCommand;
import org.gmjm.slack.command.CommandHandlerRepository;
import org.gmjm.slack.command.SlackRequestContext;
import org.gmjm.slack.service.SlashCommandHandlerService;
import org.slf4j.Logger;

public abstract class SlashCommandProcessor implements SlackCommandProcessor
{

	protected final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

	public static final SlashCommandProcessor NO_OP = new SlashCommandHandlerService("NO_OP") {

		@Override
		protected CommandHandlerRepository getCommandHandlerRepository()
		{
			return null;
		}

		@Override
		public void process(SlackCommand slackCommand) {
			logger.info("SlashCommandProcess.NO_OP");
		}

		@Override
		protected SlackRequestContext getSlackRequestContext(SlackCommand slackCommand)
		{
			return null;
		}


		@Override
		protected String getCommandKey(SlackRequestContext slackRequestContext)
		{
			return null;
		}


		@Override
		protected SlackMessageBuilder privateCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext)
		{
			return null;
		}


		@Override
		protected SlackMessageBuilder publicCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext)
		{
			return null;
		}
	};

	private final String slashCommandName;


	public SlashCommandProcessor(String slashCommandName)
	{
		if(slashCommandName == null) {
			throw new IllegalArgumentException("slahshCommandName cannot be null");
		}
		this.slashCommandName = slashCommandName;
	}


	public String getSlashCommandName()
	{
		return slashCommandName;
	}
}
