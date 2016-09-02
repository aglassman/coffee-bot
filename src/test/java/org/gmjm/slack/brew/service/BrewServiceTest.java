package org.gmjm.slack.brew.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrewServiceTest
{
	BrewService brewService = new BrewService();

	@Test
	public void testCommandKey() {
		BrewRequestContext brc = new BrewRequestContext(
			null,
			null,
			new BrewCommand("brew","coffee type"),
			"user"
		);

		assertEquals("brew",brewService.getCommandKey(brc));
	}
}
