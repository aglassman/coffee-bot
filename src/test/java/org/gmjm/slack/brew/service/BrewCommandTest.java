package org.gmjm.slack.brew.service;

import org.junit.Test;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class BrewCommandTest
{
	@Test
	public void testBrewCommand_normal()
	{
		BrewCommand brewCommand = new BrewCommand("brew Blue Heeler");
		assertEquals("Brew Command","brew",brewCommand.command);
		assertEquals("Brew Text","Blue Heeler",brewCommand.text);
	}

	@Test
	public void testBrewCommand_withSpace()
	{
		BrewCommand brewCommand = new BrewCommand(" 	brew  	 Blue Heeler 	 ");
		assertEquals("Brew Command","brew",brewCommand.command);
		assertEquals("Brew Text","Blue Heeler",brewCommand.text);
	}
}
