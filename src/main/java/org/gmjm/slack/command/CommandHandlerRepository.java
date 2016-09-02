package org.gmjm.slack.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gmjm.slack.api.message.SlackMessageBuilder;

public class CommandHandlerRepository<T extends SlackRequestContext>
{

	private static Logger logger = Logger.getLogger(CommandHandlerRepository.class);

	public enum ResponseType
	{
		EPHEMERAL, PUBLIC, ALL
	}


	private final List<NamedCommand<T>> commandList = new ArrayList<>();


	public CommandHandlerRepository(){
		registerAnnotations();
	}

	private void registerAnnotations()
	{
		CommandHandlerRepository self = this;
		Arrays.stream(this.getClass().getDeclaredMethods())
			.filter(method -> method.getAnnotation(Register.class) != null)
			.forEach(method -> {
				Register toRegister = method.getAnnotation(Register.class);
				method.setAccessible(true);
				register(
					"*".equals(toRegister.name()) ? method.getName() : toRegister.name(),
					new Function<T, SlackMessageBuilder>()
					{
						@Override
						public SlackMessageBuilder apply(T t)
						{
							try
							{
								return (SlackMessageBuilder) method.invoke(self,t);
							}
							catch (Exception e)
							{
								throw new RuntimeException(e);
							}
						}
					}
					, toRegister.value());
			});
	}

	private void register(String commandName, Function<T, SlackMessageBuilder> commandFunction)
	{
		register(commandName, commandFunction, ResponseType.ALL);
	}


	private void register(String commandName, Function<T, SlackMessageBuilder> commandFunction, ResponseType responseType)
	{
		logger.info(String.format("Registered: [%s][%s][%s]",responseType, commandName, commandFunction ));
		commandList.add(new NamedCommand<>(commandName, responseType, commandFunction));
	}

	public List<NamedCommand<T>> listFunctionsFor(final String commandName)
	{
		return commandList.stream()
			.filter(namedCommand -> namedCommand.matches(commandName))
			.collect(Collectors.toList());
	}

	public List<NamedCommand<T>> listFunctionsFor(ResponseType responseType)
	{
		return commandList.stream()
			.filter(namedCommand -> namedCommand.matches(responseType))
			.collect(Collectors.toList());
	}

	public List<NamedCommand<T>> listFunctionsFor(String commandName, ResponseType responseType)
	{
		return commandList.stream()
			.filter(namedCommand -> namedCommand.matches(commandName,responseType))
			.collect(Collectors.toList());
	}

	public List<NamedCommand<T>> listAllFunctions() {
		return Collections.unmodifiableList(commandList);
	}

}
