package org.gmjm.slack.command.processor;

public abstract class SlashCommandProcessor implements SlackCommandProcessor
{

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
