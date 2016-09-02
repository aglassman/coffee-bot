package org.gmjm.slack.command.processor;

import java.util.function.Function;

import org.gmjm.slack.api.message.SlackMessageBuilder;
import org.gmjm.slack.api.message.SlackMessageFactory;
import org.gmjm.slack.command.CommandHandlerRepository;
import org.gmjm.slack.command.NamedCommand;
import org.gmjm.slack.core.message.JsonMessageFactory;
import org.junit.Test;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertEquals;

public class NamedCommandTest
{

	SlackMessageFactory smf = new JsonMessageFactory();
	SlackMessageBuilder smb = smf.createMessageBuilder();

	@Test
	public void testFunctionMatch() {
		Function<String,SlackMessageBuilder> func = (s) -> smb;

		NamedCommand nc = new NamedCommand("test", CommandHandlerRepository.ResponseType.PUBLIC,func);

		assertEquals(func.apply("test"), nc.apply("test"));
	}

	@Test
	public void testNameMatch() {
		Function<String,SlackMessageBuilder> func = (s) -> smb;

		NamedCommand nc = new NamedCommand( " test	", CommandHandlerRepository.ResponseType.PUBLIC,func);

		assertTrue(nc.matches("		test "));
		assertTrue(nc.matches("	test "));
		assertTrue(nc.matches("test"));
		assertTrue(nc.matches("Test"));
		assertFalse(nc.matches("test1"));
	}

	@Test
	public void testTypeMatch() {
		Function<String,SlackMessageBuilder> func = (s) -> smb;

		NamedCommand nc = new NamedCommand( " test	", CommandHandlerRepository.ResponseType.PUBLIC,func);

		assertTrue(nc.matches(CommandHandlerRepository.ResponseType.PUBLIC));
		assertFalse(nc.matches(CommandHandlerRepository.ResponseType.EPHEMERAL));
	}

	@Test
	public void testNameAndTypeMatch() {
		Function<String,SlackMessageBuilder> func = (s) -> smb;

		NamedCommand nc = new NamedCommand( " test	", CommandHandlerRepository.ResponseType.PUBLIC,func);

		assertTrue(nc.matches("test",CommandHandlerRepository.ResponseType.PUBLIC));
		assertFalse(nc.matches("test1",CommandHandlerRepository.ResponseType.PUBLIC));
		assertFalse(nc.matches("test",CommandHandlerRepository.ResponseType.EPHEMERAL));
		assertFalse(nc.matches("test1",CommandHandlerRepository.ResponseType.EPHEMERAL));

	}

}
