package org.gmjm.slack.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.gmjm.slack.api.hook.HookRequest;
import org.gmjm.slack.api.hook.HookRequestFactory;
import org.gmjm.slack.api.hook.HookResponse;
import org.gmjm.slack.api.message.SlackMessageBuilder;
import org.gmjm.slack.api.model.SlackCommand;
import org.gmjm.slack.command.CommandHandlerRepository;
import org.gmjm.slack.command.NamedCommand;
import org.gmjm.slack.command.SlackRequestContext;
import org.gmjm.slack.core.model.SlackCommandMapImpl;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class SlashCommandHandlerServiceTest
{
	@Test(expected = IllegalArgumentException.class)
	public void processNullSlackCommand() {
		SlashCommandHandlerService<SlackRequestContext> slashCommandHandlerService =
			mock(SlashCommandHandlerService.class, CALLS_REAL_METHODS);
		slashCommandHandlerService.testBootstrap(null);

			slashCommandHandlerService.process(null);
	}


	@Test(expected = IllegalStateException.class)
	public void testNullCommandHandlerRepository() {
		SlashCommandHandlerService<SlackRequestContext> slashCommandHandlerService =
			mock(SlashCommandHandlerService.class, CALLS_REAL_METHODS);
		slashCommandHandlerService.testBootstrap(null);

		Map<String,String> params = new HashMap<>();
		params.put("token","asdf");
		SlackCommand slackCommand = new SlackCommandMapImpl(params);

		slackCommand.getAll().toString();

		when(slashCommandHandlerService.getCommandHandlerRepository()).thenReturn(null);

		slashCommandHandlerService.process(slackCommand);
	}

	@Test()
	public void testProcessSendPrivateNoCommands() {
		HookRequestFactory hookRequestFactory = mock(HookRequestFactory.class);

		SlashCommandHandlerService<SlackRequestContext> slashCommandHandlerService =
			mock(SlashCommandHandlerService.class, CALLS_REAL_METHODS);
		slashCommandHandlerService.testBootstrap(hookRequestFactory);

		Map<String,String> params = new HashMap<>();
		params.put("token","asdf");
		SlackCommand slackCommand = new SlackCommandMapImpl(params);

		slackCommand.getAll().toString();

		CommandHandlerRepository<SlackRequestContext> repository = mock(CommandHandlerRepository.class);

		when(slashCommandHandlerService.getCommandHandlerRepository()).thenReturn(repository);
		when(slashCommandHandlerService.getCommandKey(any())).thenReturn("testKey");
		when(repository.listFunctionsFor("testKey", CommandHandlerRepository.ResponseType.EPHEMERAL))
			.thenReturn(Arrays.asList());

		slashCommandHandlerService.process(slackCommand);

		verifyZeroInteractions(hookRequestFactory);
	}

	@Test()
	public void testProcessSendPrivateOneCommandFailure() {
		HookRequestFactory hookRequestFactory = mock(HookRequestFactory.class);

		SlashCommandHandlerService<SlackRequestContext> slashCommandHandlerService =
			mock(SlashCommandHandlerService.class, CALLS_REAL_METHODS);
		slashCommandHandlerService.testBootstrap(hookRequestFactory);

		Map<String,String> params = new HashMap<>();
		params.put("token","asdf");
		params.put("response_url","http://example.com");
		SlackCommand slackCommand = new SlackCommandMapImpl(params);

		slackCommand.getAll().toString();

		CommandHandlerRepository<SlackRequestContext> repository = mock(CommandHandlerRepository.class);

		SlackMessageBuilder messageBuilder =  mock(SlackMessageBuilder.class);

		Function<SlackRequestContext,SlackMessageBuilder> func = o -> messageBuilder;

		SlackRequestContext slackRequestContext = new SlackRequestContext(slackCommand);

		when(slashCommandHandlerService.getSlackRequestContext(any())).thenReturn(slackRequestContext);
		when(slashCommandHandlerService.getCommandHandlerRepository()).thenReturn(repository);
		when(slashCommandHandlerService.getCommandKey(any())).thenReturn("testKey");
		when(repository.listFunctionsFor("testKey", CommandHandlerRepository.ResponseType.EPHEMERAL))
			.thenReturn(Arrays.asList(
				new NamedCommand(
					"testKey",
					CommandHandlerRepository.ResponseType.EPHEMERAL,
					func)
			));

		when(slashCommandHandlerService.privateCallback(messageBuilder,slackRequestContext)).thenReturn(messageBuilder);

		HookRequest hookRequest = mock(HookRequest.class);
		HookResponse hookResponse = mock(HookResponse.class);
		when(hookRequestFactory.createHookRequest("http://example.com")).thenReturn(hookRequest);
		when(messageBuilder.build()).thenReturn("message text");
		when(hookRequest.send("message text")).thenReturn(hookResponse);
		when(hookResponse.getStatus()).thenReturn(HookResponse.Status.FAILED);
		when(hookResponse.getMessage()).thenReturn("mock only");

		slashCommandHandlerService.process(slackCommand);

		verify(hookResponse,times(1)).getMessage();

	}

	@Test()
	public void testProcessSendPrivateOneCommandSuccess() {
		HookRequestFactory hookRequestFactory = mock(HookRequestFactory.class);

		SlashCommandHandlerService<SlackRequestContext> slashCommandHandlerService =
			mock(SlashCommandHandlerService.class, CALLS_REAL_METHODS);
		slashCommandHandlerService.testBootstrap(hookRequestFactory);

		Map<String,String> params = new HashMap<>();
		params.put("token","asdf");
		params.put("response_url","http://example.com");
		SlackCommand slackCommand = new SlackCommandMapImpl(params);

		slackCommand.getAll().toString();

		CommandHandlerRepository<SlackRequestContext> repository = mock(CommandHandlerRepository.class);

		SlackMessageBuilder messageBuilder =  mock(SlackMessageBuilder.class);

		Function<SlackRequestContext,SlackMessageBuilder> func = o -> messageBuilder;

		SlackRequestContext slackRequestContext = new SlackRequestContext(slackCommand);

		when(slashCommandHandlerService.getSlackRequestContext(any())).thenReturn(slackRequestContext);
		when(slashCommandHandlerService.getCommandHandlerRepository()).thenReturn(repository);
		when(slashCommandHandlerService.getCommandKey(any())).thenReturn("testKey");
		when(repository.listFunctionsFor("testKey", CommandHandlerRepository.ResponseType.EPHEMERAL))
			.thenReturn(Arrays.asList(
				new NamedCommand(
					"testKey",
					CommandHandlerRepository.ResponseType.EPHEMERAL,
					func)
			));

		when(slashCommandHandlerService.privateCallback(messageBuilder,slackRequestContext)).thenReturn(messageBuilder);

		HookRequest hookRequest = mock(HookRequest.class);
		HookResponse hookResponse = mock(HookResponse.class);
		when(hookRequestFactory.createHookRequest("http://example.com")).thenReturn(hookRequest);
		when(messageBuilder.build()).thenReturn("message text");
		when(hookRequest.send("message text")).thenReturn(hookResponse);
		when(hookResponse.getStatus()).thenReturn(HookResponse.Status.SUCCESS);
		when(hookResponse.getMessage()).thenReturn("mock only");

		slashCommandHandlerService.process(slackCommand);

		verify(hookResponse,times(0)).getMessage();

	}

	@Test()
	public void testProcessSendPublicOneCommandSuccess() {
		HookRequestFactory hookRequestFactory = mock(HookRequestFactory.class);

		SlashCommandHandlerService<SlackRequestContext> slashCommandHandlerService =
			mock(SlashCommandHandlerService.class, CALLS_REAL_METHODS);
		slashCommandHandlerService.testBootstrap(hookRequestFactory);

		Map<String,String> params = new HashMap<>();
		params.put("token","asdf");
		params.put("response_url","http://example.com");
		SlackCommand slackCommand = new SlackCommandMapImpl(params);

		slackCommand.getAll().toString();

		CommandHandlerRepository<SlackRequestContext> repository = mock(CommandHandlerRepository.class);

		SlackMessageBuilder messageBuilder =  mock(SlackMessageBuilder.class);

		Function<SlackRequestContext,SlackMessageBuilder> func = o -> messageBuilder;

		SlackRequestContext slackRequestContext = new SlackRequestContext(slackCommand);

		when(slashCommandHandlerService.getSlackRequestContext(any())).thenReturn(slackRequestContext);
		when(slashCommandHandlerService.getCommandHandlerRepository()).thenReturn(repository);
		when(slashCommandHandlerService.getCommandKey(any())).thenReturn("testKey");
		when(repository.listFunctionsFor("testKey", CommandHandlerRepository.ResponseType.EPHEMERAL))
			.thenReturn(Arrays.asList(
				new NamedCommand(
					"testKey",
					CommandHandlerRepository.ResponseType.PUBLIC,
					func)
			));

		when(slashCommandHandlerService.publicCallback(messageBuilder,slackRequestContext)).thenReturn(messageBuilder);

		HookRequest hookRequest = mock(HookRequest.class);
		HookResponse hookResponse = mock(HookResponse.class);
		when(hookRequestFactory.createHookRequest("http://example.com")).thenReturn(hookRequest);
		when(messageBuilder.build()).thenReturn("message text");
		when(hookRequest.send("message text")).thenReturn(hookResponse);
		when(hookResponse.getStatus()).thenReturn(HookResponse.Status.SUCCESS);
		when(hookResponse.getMessage()).thenReturn("mock only");

		slashCommandHandlerService.process(slackCommand);

		verify(hookResponse,times(0)).getMessage();

	}


}
