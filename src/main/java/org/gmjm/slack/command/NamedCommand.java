package org.gmjm.slack.command;

import java.util.function.Function;

import org.gmjm.slack.api.message.SlackMessageBuilder;

public class NamedCommand<T> implements Function<T, SlackMessageBuilder>
{

	private final String commandName;
	private final CommandHandlerRepository.ResponseType responseType;
	private final Function<T, SlackMessageBuilder> commandFunction;

	public NamedCommand(String commandName, CommandHandlerRepository.ResponseType responseType, Function<T, SlackMessageBuilder> commandFunction)
	{
		this.commandName = commandName;
		this.responseType = responseType;
		this.commandFunction = commandFunction;
	}


	public boolean matches(String commandName)
	{
		boolean val = this.commandName.equals(commandName);
		return val;
	}

	public boolean matches(CommandHandlerRepository.ResponseType responseType)
	{
		if (this.responseType.equals(CommandHandlerRepository.ResponseType.ALL)
			|| this.responseType.equals(responseType))
		{
			return true;
		}

		return false;
	}

	public boolean matches(String commandName, CommandHandlerRepository.ResponseType responseType)
	{
		return matches(commandName) && matches(responseType);
	}

	@Override
	public SlackMessageBuilder apply(T t)
	{
		return commandFunction.apply(t);
	}


	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		NamedCommand that = (NamedCommand) o;

		if (!commandName.equals(that.commandName))
		{
			return false;
		}
		return responseType == that.responseType;

	}


	@Override
	public int hashCode()
	{
		int result = commandName.hashCode();
		result = 31 * result + responseType.hashCode();
		return result;
	}


	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer("NamedCommand{");
		sb.append("commandName='").append(commandName).append('\'');
		sb.append(", responseType=").append(responseType);
		sb.append(", commandFunction=").append(commandFunction);
		sb.append('}');
		return sb.toString();
	}
}
