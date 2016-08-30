package org.gmjm.slack.brew.service;

import org.gmjm.slack.api.hook.HookRequest;
import org.gmjm.slack.api.hook.HookRequestFactory;
import org.gmjm.slack.api.hook.HookResponse;
import org.gmjm.slack.api.message.SlackMessageBuilder;
import org.gmjm.slack.api.model.SlackCommand;
import org.gmjm.slack.brew.repositories.BrewRepository;
import org.gmjm.slack.command.CommandHandlerRepository;
import org.gmjm.slack.command.SlackRequestContext;
import org.gmjm.slack.service.SlashCommandHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("brewService")
public class BrewService extends SlashCommandHandlerService<BrewRequestContext>
{
	private static final String COFFEE_BOT_USERNAME = "coffee-master";
	private static final String COFFEE_EMOJI = "coffee";
	private static final String COFFEE_CHANNEL = "coffee";

	@Autowired
	protected HookRequest hookRequest;

	@Autowired
	@Qualifier("brewCommandHandlerRepository")
	protected CommandHandlerRepository<BrewRequestContext> commandHandlerRepository;

	@Autowired
	BrewRepository brewRepository;


	public BrewService()
	{
		super("/coffee");
	}


	@Override
	protected BrewRequestContext getSlackRequestContext(SlackCommand slackCommand)
	{
		return new BrewRequestContext(
			slackCommand,
			brewRepository,
			new BrewCommand(slackCommand.getText()),
			slackCommand.getUserName());
	}


	@Override
	protected SlackMessageBuilder privateCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext)
	{
		return slackMessageBuilder
			.setIconEmoji(COFFEE_EMOJI)
			.setUsername(COFFEE_BOT_USERNAME);
	}

	@Override
	protected SlackMessageBuilder publicCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext)
	{
		return slackMessageBuilder
			.setIconEmoji(COFFEE_EMOJI)
			.setChannel(COFFEE_CHANNEL)
			.setUsername(COFFEE_BOT_USERNAME);

	}
}
