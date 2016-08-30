package org.gmjm.slack.brew.service;

import java.util.Arrays;

class BrewCommand {

	public final String command;
	public final String text;

	public BrewCommand(String inputText) {
		if(inputText == null) {
			this.command = "";
			this.text = null;
		} else
		{
			String[] splitText = inputText.trim().split(" ");
			this.command = splitText[0];

			this.text = Arrays.stream(splitText)
				.skip(1)
				.reduce((a, b) -> a + " " + b)
				.map(text -> text.trim())
				.orElse("");
		}

	}

	public BrewCommand(String command, String text)
	{
		this.command = command;
		this.text = text;
	}


	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer("BrewCommand{");
		sb.append("command='").append(command).append('\'');
		sb.append(", text='").append(text).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
