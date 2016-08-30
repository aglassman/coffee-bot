package org.gmjm.slack.service;

import org.gmjm.slack.api.hook.HookRequest;
import org.gmjm.slack.api.hook.HookRequestFactory;
import org.gmjm.slack.api.hook.HookResponse;
import org.gmjm.slack.api.message.SlackMessageBuilder;
import org.gmjm.slack.api.model.SlackCommand;
import org.gmjm.slack.command.CommandHandlerRepository;
import org.gmjm.slack.command.SlackRequestContext;
import org.gmjm.slack.command.processor.SlashCommandProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SlashCommandHandlerService<T extends SlackRequestContext> extends SlashCommandProcessor
{


	@Autowired
	protected HookRequestFactory hookRequestFactory;

	@Autowired
	protected HookRequest hookRequest;

	protected CommandHandlerRepository<T> commandHandlerRepository;


	public SlashCommandHandlerService(String slashCommandName)
	{
		super(slashCommandName);
	}

	@Override
	public void process(SlackCommand slackCommand) {

		if(commandHandlerRepository == null) {
			throw new IllegalStateException("commandHandlerRepository cannot be null");
		}

		T slackRequestContext = getSlackRequestContext(slackCommand);

		sendPrivate(slackRequestContext);
		sendPublic(slackRequestContext);
	}

	abstract protected T getSlackRequestContext(SlackCommand slackCommand);

	private void sendPrivate(final T slackRequestContext) {

		try {

			commandHandlerRepository.listFunctionsFor(slackRequestContext.getSlackCommand().getCommand(), CommandHandlerRepository.ResponseType.EPHEMERAL)
				.stream()
				.map(command -> command.apply(slackRequestContext))
				.map(builder -> {
					return privateCallback(builder,slackRequestContext)
						.setResponseType("ephemeral");
				})
				.map(slackMessageBuilder -> {
					return hookRequestFactory
						.createHookRequest(slackRequestContext.slackCommand.getResponseUrl())
						.send(slackMessageBuilder.build());
				})
				.filter(HookResponse.Status.FAILED::equals)
				.forEach(hookResponse -> {
					logger.error("Failed to send private response: " + hookResponse.getMessage());
				});

		} catch (Exception e) {
			logger.error("Failed to execute hookRequest.",e);
		}
	}

	abstract protected SlackMessageBuilder privateCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext);

	private void sendPublic(final T slackRequestContext) {
		try {

			commandHandlerRepository.listFunctionsFor(slackRequestContext.getSlackCommand().getCommand(), CommandHandlerRepository.ResponseType.PUBLIC)
				.stream()
				.map(brewCommand -> brewCommand.apply(slackRequestContext))
				.map(builder -> publicCallback(builder,slackRequestContext))
				.map(slackMessageBuilder -> {
					return hookRequestFactory
						.createHookRequest(slackRequestContext.slackCommand.getResponseUrl())
						.send(slackMessageBuilder.build());
				})
				.filter(HookResponse.Status.FAILED::equals)
				.forEach(hookResponse -> {
					logger.error("Failed to send public response: " + hookResponse.getMessage());
				});

		} catch (Exception e) {
			logger.error("Failed to execute hookRequest.",e);
		}
	}

	abstract protected SlackMessageBuilder publicCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext);
}
