package org.gmjm.slack.brew.service;

import java.util.List;
import java.util.function.Function;

import org.gmjm.slack.api.message.SlackMessageBuilder;
import org.gmjm.slack.core.message.JsonMessageFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BrewCommandHandlersTest
{

	@Test
	public void testCommandHandlerAnnotationRegistration() {
		BrewCommandHandlerRepository bch = new BrewCommandHandlerRepository();
		bch.slackMessageFactory = new JsonMessageFactory();

		List<Function<BrewRequestContext,SlackMessageBuilder>> functions =  bch.listFunctionsFor("help");

		assertEquals(1,functions.size());

		SlackMessageBuilder smb = functions.get(0).apply(null);

		System.out.println(smb.build());

	}

}
