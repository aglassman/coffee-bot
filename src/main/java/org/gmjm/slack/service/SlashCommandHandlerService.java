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

	public SlashCommandHandlerService(String slashCommandName)
	{
		super(slashCommandName);
	}

	abstract protected CommandHandlerRepository<T> getCommandHandlerRepository();

	@Override
	public void process(SlackCommand slackCommand) {

		logger.info("processing: " + slackCommand.getAll().toString());

		if(getCommandHandlerRepository() == null) {
			throw new IllegalStateException("commandHandlerRepository cannot be null");
		}

		T slackRequestContext = getSlackRequestContext(slackCommand);

		sendPrivate(slackRequestContext);
		sendPublic(slackRequestContext);
	}

	abstract protected T getSlackRequestContext(SlackCommand slackCommand);
	abstract protected String getCommandKey(T slackRequestContext);

	private void sendPrivate(final T slackRequestContext) {

		try {

			getCommandHandlerRepository().listFunctionsFor(getCommandKey(slackRequestContext), CommandHandlerRepository.ResponseType.EPHEMERAL)
				.stream()
				.peek(namedCommand -> logger.info("Found: " + namedCommand.toString()))
				.map(namedCommand -> namedCommand.apply(slackRequestContext))
				.map(builder -> {
					return privateCallback(builder,slackRequestContext)
						.setResponseType("ephemeral");
				})
				.map(slackMessageBuilder -> {
					String toSend = slackMessageBuilder.build();
					logger.info("sendPrivate: " + toSend);
					return hookRequestFactory
						.createHookRequest(slackRequestContext.slackCommand.getResponseUrl())
						.send(toSend);
				})
				.filter(HookResponse.Status.FAILED::equals)
				.forEach(hookResponse -> {
					logger.error("Failed to send private response: " + hookResponse.getMessage());
				});

		} catch (Exception e) {
			logger.error("Failed to process command.",e);
		}
	}

	abstract protected SlackMessageBuilder privateCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext);

	private void sendPublic(final T slackRequestContext) {
		try {

			getCommandHandlerRepository().listFunctionsFor(getCommandKey(slackRequestContext), CommandHandlerRepository.ResponseType.PUBLIC)
				.stream()
				.peek(namedCommand -> logger.info("Found: " + namedCommand.toString()))
				.map(namedCommand -> namedCommand.apply(slackRequestContext))
				.map(builder -> publicCallback(builder,slackRequestContext))
				.map(slackMessageBuilder -> {
					String toSend = slackMessageBuilder.build();
					logger.info("sendPublic: " + toSend);
					return hookRequestFactory
						.createHookRequest(slackRequestContext.slackCommand.getResponseUrl())
						.send(toSend);
				})
				.filter(HookResponse.Status.FAILED::equals)
				.forEach(hookResponse -> {
					logger.error("Failed to send public response: " + hookResponse.getMessage());
				});

		} catch (Exception e) {
			logger.error("Failed to process command.",e);
		}
	}

	abstract protected SlackMessageBuilder publicCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext);
}
