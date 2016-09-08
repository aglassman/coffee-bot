package org.gmjm.slack.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.gmjm.slack.api.model.SlackCommand;
import org.gmjm.slack.command.processor.SlashCommandProcessor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SlashCommandControllerTest
{

	@Test
	public void testProcessMatchingCommand()
	{
		String token = "asdf";
		SlashCommandProcessor slashCommandProcessor = mock(SlashCommandProcessor.class);

		SlashCommandController slashCommandController = new SlashCommandController(
			Arrays.asList(token),
			Arrays.asList(slashCommandProcessor));

		Map<String,String> params = new HashMap<>();
		params.put("command","/brew");
		params.put("token","asdf");

		ArgumentCaptor<SlackCommand> slackCommandCaptor = ArgumentCaptor.forClass(SlackCommand.class);

		when(slashCommandProcessor.getSlashCommandName()).thenReturn("/brew");

		slashCommandController.processCommand(params);

		verify(slashCommandProcessor).process(slackCommandCaptor.capture());

		SlackCommand slackCommand = slackCommandCaptor.getValue();
		assertEquals("/brew",slackCommand.getCommand());
		assertEquals("asdf",slackCommand.getToken());

	}

	@Test
	public void testProcessNoMatchingCommand()
	{
		String token = "asdf";
		SlashCommandProcessor slashCommandProcessor = mock(SlashCommandProcessor.class);

		SlashCommandController slashCommandController = new SlashCommandController(
			Arrays.asList(token),
			Arrays.asList(slashCommandProcessor));

		Map<String,String> params = new HashMap<>();
		params.put("command","/nomatch");
		params.put("token","asdf");

		when(slashCommandProcessor.getSlashCommandName()).thenReturn("/brew");

		slashCommandController.processCommand(params);

		verify(slashCommandProcessor,never()).process(any());
	}

	@Test
	public void testProcessInvalidToken()
	{
		String token = "asdf";
		SlashCommandProcessor slashCommandProcessor = mock(SlashCommandProcessor.class);

		SlashCommandController slashCommandController = new SlashCommandController(
			Arrays.asList(token),
			Arrays.asList(slashCommandProcessor));

		Map<String,String> params = new HashMap<>();
		params.put("command","/brew");
		params.put("token","qwerty");

		when(slashCommandProcessor.getSlashCommandName()).thenReturn("/brew");

		slashCommandController.processCommand(params);

		verify(slashCommandProcessor,never()).process(any());
	}

}
