package org.gmjm.slack.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.gmjm.slack.api.model.SlackCommand;
import org.gmjm.slack.command.processor.SlashCommandProcessor;
import org.gmjm.slack.core.model.SlackCommandMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
final class SlashCommandController
{

	private static final Logger logger = LoggerFactory.getLogger(SlashCommandController.class);

	@Value("#{'${slack.valid_command_tokens}'.split(',')}")
	private List<String> tokens;

	@Autowired
	private List<SlashCommandProcessor> commandProcessors;

	public SlashCommandController(){}

	public SlashCommandController(List<String> validTokens, List<SlashCommandProcessor> commandProcessors)
	{
		this.tokens = validTokens;
		this.commandProcessors = commandProcessors;
	}


	@RequestMapping(method = RequestMethod.POST, value = "/")
	ResponseEntity<String> processCommand(
		@RequestParam Map<String, String> params
	)
	{
		final SlackCommand slackCommand = new SlackCommandMapImpl(params);

		if(!tokens.contains(slackCommand.getToken())) {
			return new ResponseEntity<String>("Invalid token.", HttpStatus.FORBIDDEN);
		}

		Optional<SlashCommandProcessor> optionalProcessor = commandProcessors.stream()
			.filter(commandProcessor -> commandProcessor.getSlashCommandName().equals(slackCommand.getCommand()))
			.findFirst();

		if(optionalProcessor.isPresent()) {
			optionalProcessor.get().process(slackCommand);
		} else {
			logger.error(String.format("No SlashCommandProcessor found for command: ",slackCommand.getCommand()));
		}

		return ok(null);
	}

	private static ResponseEntity<String> ok(String string)
	{
		return new ResponseEntity<>(string, HttpStatus.OK);
	}

}

