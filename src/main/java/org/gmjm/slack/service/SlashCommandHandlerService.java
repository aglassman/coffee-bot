package org.gmjm.slack.service;

import java.util.List;
import java.util.stream.Collectors;

import org.gmjm.slack.api.hook.HookRequest;
import org.gmjm.slack.api.hook.HookRequestFactory;
import org.gmjm.slack.api.hook.HookResponse;
import org.gmjm.slack.api.message.SlackMessageBuilder;
import org.gmjm.slack.api.model.SlackCommand;
import org.gmjm.slack.command.CommandHandlerRepository;
import org.gmjm.slack.command.NamedCommand;
import org.gmjm.slack.command.SlackRequestContext;
import org.gmjm.slack.command.processor.SlashCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public abstract class SlashCommandHandlerService<T extends SlackRequestContext> extends SlashCommandProcessor
{

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected HookRequestFactory hookRequestFactory;

	@Autowired
	protected HookRequest incomingWebHook;


	public SlashCommandHandlerService(String slashCommandName)
	{
		super(slashCommandName);
	}

	public SlashCommandHandlerService(String slashCommandName, HookRequestFactory hookRequestFactory) {
		super(slashCommandName);
		this.hookRequestFactory = hookRequestFactory;
	}

	/**
	 * For test purposes only.
	 */
	void testBootstrap(HookRequestFactory hookRequestFactory) {
		logger = LoggerFactory.getLogger(this.getClass());
		this.hookRequestFactory = hookRequestFactory;
	}

	@Override
	public void process(SlackCommand slackCommand) {
		Assert.notNull(slackCommand);

		logger.info("processing: " + slackCommand.getAll().toString());

		if(getCommandHandlerRepository() == null) {
			throw new IllegalStateException("commandHandlerRepository cannot be null");
		}

		T slackRequestContext = getSlackRequestContext(slackCommand);

		sendPrivate(slackRequestContext);
		sendPublic(slackRequestContext);
	}

	private void sendPrivate(final T slackRequestContext) {

		try {

			final String commandKey = getCommandKey(slackRequestContext);

			List<NamedCommand<T>> commands =
				getNamedCommands(commandKey, CommandHandlerRepository.ResponseType.EPHEMERAL);

			if(commands.size() == 0) {
				logger.info("No commands found for commandKey: " + commandKey);
			}

			commands.stream()
				.map(Object::toString)
				.map(s -> "Found: " + s)
				.forEach(logger::info);

			List<SlackMessageBuilder> outgoingMessageBuilders = processCommands(slackRequestContext, commands);

			outgoingMessageBuilders.forEach(messageBuilder -> {
				privateCallback(messageBuilder,slackRequestContext)
					.setResponseType("ephemeral");
			});

			List<HookResponse> hookResponses = buildAndSendPrivate(slackRequestContext, outgoingMessageBuilders);

			processResponses(hookResponses);

		} catch (Exception e) {
			logger.error("Failed to process command.",e);
		}
	}


	private List<NamedCommand<T>> getNamedCommands(String commandKey, CommandHandlerRepository.ResponseType ephemeral)
	{
		return getCommandHandlerRepository()
			.listFunctionsFor(
				commandKey,
				ephemeral);
	}


	private List<SlackMessageBuilder> processCommands(T slackRequestContext, List<NamedCommand<T>> commands)
	{
		return commands.stream()
			.map(namedCommand -> namedCommand.apply(slackRequestContext))
			.collect(Collectors.toList());
	}


	private void processResponses(List<HookResponse> hookResponses)
	{
		hookResponses.stream()
			.filter(hookResponse -> HookResponse.Status.FAILED.equals(hookResponse.getStatus()))
			.forEach(hookResponse -> {
				logger.error("Failed to send private response: " + hookResponse.getMessage());
			});
	}


	private List<HookResponse> buildAndSendPrivate(T slackRequestContext, List<SlackMessageBuilder> outgoingMessageBuilders)
	{
		return outgoingMessageBuilders.stream()
			.map(slackMessageBuilder -> {
				String toSend = slackMessageBuilder.build();
				logger.info("sending: " + toSend);
				return hookRequestFactory
					.createHookRequest(slackRequestContext.slackCommand.getResponseUrl())
					.send(toSend);
			})
			.collect(Collectors.toList());
	}

	private List<HookResponse> buildAndSendPublic(T slackRequestContext, List<SlackMessageBuilder> outgoingMessageBuilders)
	{
		return outgoingMessageBuilders.stream()
			.map(slackMessageBuilder -> {
				String toSend = slackMessageBuilder.build();
				logger.info("sending: " + toSend);
				return incomingWebHook.send(toSend);
			})
			.collect(Collectors.toList());
	}



	private void sendPublic(final T slackRequestContext) {
		try {

			final String commandKey = getCommandKey(slackRequestContext);

			List<NamedCommand<T>> commands =
				getNamedCommands(commandKey, CommandHandlerRepository.ResponseType.PUBLIC);

			if(commands.size() == 0) {
				logger.info("No commands found for commandKey: " + commandKey);
			}

			commands.stream()
				.map(Object::toString)
				.map(s -> "Found: " + s)
				.forEach(logger::info);

			List<SlackMessageBuilder> outgoingMessageBuilders = processCommands(slackRequestContext, commands);

			outgoingMessageBuilders.forEach(messageBuilder -> {
				publicCallback(messageBuilder,slackRequestContext);
			});

			List<HookResponse> hookResponses = buildAndSendPublic(slackRequestContext, outgoingMessageBuilders);

			processResponses(hookResponses);

		} catch (Exception e) {
			logger.error("Failed to process command.",e);
		}
	}

	abstract protected SlackMessageBuilder publicCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext);
	abstract protected SlackMessageBuilder privateCallback(SlackMessageBuilder slackMessageBuilder, SlackRequestContext slackRequestContext);
	abstract protected CommandHandlerRepository<T> getCommandHandlerRepository();
	abstract protected T getSlackRequestContext(SlackCommand slackCommand);
	abstract protected String getCommandKey(T slackRequestContext);
}
