package org.gmjm.slack.brew.service;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.FastDateFormat;
import org.gmjm.slack.api.message.SlackMessageBuilder;
import org.gmjm.slack.api.model.SlackCommand;
import org.gmjm.slack.brew.domain.Brew;
import org.gmjm.slack.brew.repositories.BrewRepository;
import org.gmjm.slack.command.NamedCommand;
import org.gmjm.slack.core.message.JsonMessageFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class BrewCommandHandlersTest {


	BrewCommandHandlerRepository brewCommandHandlerRepository;

	private static FastDateFormat fdf = FastDateFormat.getInstance("EEE, MMM d @ h:mm a", TimeZone.getTimeZone("CST"),null);


	@Before
	public void before() {
		brewCommandHandlerRepository = new BrewCommandHandlerRepository(
			"BREW_MASTER_ID",
			"brewMaster",
			new JsonMessageFactory()
		);


	}

	public BrewRequestContext createBrewRequestContext(String text) {
		SlackCommand slackCommand = mock(SlackCommand.class);
		BrewRepository brewRepository = mock(BrewRepository.class);
		return new BrewRequestContext(
			slackCommand,
			brewRepository,
			new BrewCommand(text),
			"userName"
		);
	}


	@Test
	public void testCommandHandlerAnnotationRegistration() {

		List<NamedCommand<BrewRequestContext>> namedCommands = brewCommandHandlerRepository.listAllFunctions();

		assertEquals(9,namedCommands.size());

		List<String> commandNames = namedCommands.stream()
			.map(namedCommand -> namedCommand.getCommandName())
			.distinct()
			.collect(Collectors.toList());

		assertTrue(commandNames.contains("help"));
		assertTrue(commandNames.contains("brew"));
		assertTrue(commandNames.contains("today"));
		assertTrue(commandNames.contains(""));
		assertTrue(commandNames.contains("gone"));
		assertTrue(commandNames.contains("debug"));
		assertTrue(commandNames.contains("last"));

	}

	@Test
	public void testBrewHandlerPublicNoText() {
		SlackMessageBuilder builder = brewCommandHandlerRepository.brew(createBrewRequestContext(""));

		assertEquals("{\"attachments\":[],\"text\":\"Include the name of the coffee you brewed! Example, /coffee brew Blue Heeler\"}",
			builder.build());
	}

	@Test
	public void testBrewHandlerPublicWithText() {
		BrewRequestContext brc = createBrewRequestContext("brew blue heeler");
		when(brc.slackCommand.getMsgFriendlyUser()).thenReturn("<@JSIEJ3234|John>");

		ArgumentCaptor<Brew> brewCap = ArgumentCaptor.forClass(Brew.class);

		when(brc.brewRepository.save(any(Brew.class))).thenReturn(null);

		SlackMessageBuilder builder = brewCommandHandlerRepository.brew(brc);

		assertEquals("{\"attachments\":[],\"text\":\"<@JSIEJ3234|John> brewed a pot of blue heeler.\"}",
			builder.build());

		verify(brc.brewRepository).save(brewCap.capture());

		Brew capturedBrew = brewCap.getValue();

		assertEquals("blue heeler",capturedBrew.getBrewName());
		assertEquals("userName",capturedBrew.getBrewedBy());
		assertNotNull(capturedBrew.getBrewDate());
		assertFalse(capturedBrew.isGone());
	}

	@Test
	public void testBrewHandlerPrivateNoText() {
		SlackMessageBuilder builder = brewCommandHandlerRepository.brewPrivate(createBrewRequestContext("brew"));

		assertEquals("{\"attachments\":[],\"text\":\"You truly are a brew master null.\"}",
			builder.build());
	}

	@Test
	public void testBrewHandlerPrivateWithText() {
		BrewRequestContext brc = createBrewRequestContext("brew blue heeler");
		when(brc.slackCommand.getMsgFriendlyUser()).thenReturn("<@JSIEJ3234|John>");
		SlackMessageBuilder builder = brewCommandHandlerRepository.brewPrivate(brc);

		assertEquals("{\"attachments\":[],\"text\":\"You truly are a brew master <@JSIEJ3234|John>.\"}",
			builder.build());
	}

	@Test
	public void testTodayNoCoffee() {
		BrewRequestContext brc = createBrewRequestContext("today");
		SlackMessageBuilder builder = brewCommandHandlerRepository.today(brc);

		when(brc.brewRepository.findTop20ByOrderByBrewDateDesc()).thenReturn(Collections.emptyList());

		assertEquals("{\"attachments\":[],\"text\":\"No coffee brewed yet, <@BREW_MASTER_ID|brewMaster> go make some!\"}",
			builder.build());
	}

	@Test
	public void testTodayWithCoffee() {
		BrewRequestContext brc = createBrewRequestContext("today");
		Date date = new Date();
		String dateString = fdf.format(date);

		when(brc.brewRepository.findTop20ByOrderByBrewDateDesc()).thenReturn(
			Arrays.asList(
				new Brew(1L,"Blue Heeler", date, "aglassman", false)
			)
		);
		SlackMessageBuilder builder = brewCommandHandlerRepository.today(brc);

		assertEquals("{\"attachments\":[],\"text\":\"Blue Heeler was brewed by aglassman on " + dateString + ", and is still available.\"}",
			builder.build());

	}

	@Test
	public void testTodayWithCoffeeGone() {
		BrewRequestContext brc = createBrewRequestContext("gone");
		Date date = new Date();
		String dateString = fdf.format(date);

		when(brc.brewRepository.findTop20ByOrderByBrewDateDesc()).thenReturn(
			Arrays.asList(
				new Brew(1L,"Blue Heeler", date, "aglassman", true)
			)
		);
		SlackMessageBuilder builder = brewCommandHandlerRepository.today(brc);

		assertEquals("{\"attachments\":[],\"text\":\"Blue Heeler was brewed by aglassman on " + dateString + ", and is all gone.\"}",
			builder.build());

	}

	@Test
	public void testGetTodaysBrews() {
		BrewRepository brewRepository = mock(BrewRepository.class);

		Date date = new Date();
		Date oldDate = new Date(date.getTime() - 43200000);

		when(brewRepository.findTop20ByOrderByBrewDateDesc()).thenReturn(
			Arrays.asList(
				new Brew(1L,"Blue Heeler 1", date, "aglassman", false),
				new Brew(2L,"Blue Heeler 2", date, "aglassman", false),
				new Brew(3L,"Blue Heeler 3", date, "aglassman", true),
				new Brew(4L,"Blue Heeler 4", oldDate, "aglassman", true)
			)
		);

		List<Brew> todaysBrews = brewCommandHandlerRepository.getTodaysBrews(brewRepository);

		assertEquals(3,todaysBrews.size());

	}

	@Test
	public void testGetTodaysBrews12hourCornerCase() {
		BrewRepository brewRepository = mock(BrewRepository.class);

		Date date = new Date();
		Date oldDate = new Date(date.getTime() - 43198000);

		when(brewRepository.findTop20ByOrderByBrewDateDesc()).thenReturn(
			Arrays.asList(
				new Brew(1L,"Blue Heeler 1", date, "aglassman", false),
				new Brew(2L,"Blue Heeler 2", date, "aglassman", false),
				new Brew(3L,"Blue Heeler 3", date, "aglassman", true),
				new Brew(4L,"Blue Heeler 4", oldDate, "aglassman", true)
			)
		);

		List<Brew> todaysBrews = brewCommandHandlerRepository.getTodaysBrews(brewRepository);

		assertEquals(4,todaysBrews.size());

	}


	@Test
	public void testGone() {
		BrewRequestContext brc = createBrewRequestContext("gone");

		Date date = new Date();

		Brew b1 = new Brew(1L,"Blue Heeler", date, "aglassman", false);
		Brew b2 = new Brew(1L,"Blue Heeler", date, "aglassman", false);

		when(brc.brewRepository.findByGone(false)).thenReturn(
			Arrays.asList(
				b1,
				b2
			)
		);
		SlackMessageBuilder builder = brewCommandHandlerRepository.gone(brc);

		assertEquals("{\"attachments\":[],\"text\":\"<@BREW_MASTER_ID|brewMaster>, go make some more coffee!\"}",
			builder.build());

		assertTrue(b1.isGone());
		assertTrue(b2.isGone());

		verify(brc.brewRepository).save(b1);
		verify(brc.brewRepository).save(b2);

	}

	@Test
	public void testLast() {
		BrewRequestContext brc = createBrewRequestContext("last 2");

		Date date = new Date();
		String dateString = fdf.format(date);

		Brew b1 = new Brew(1L,"Blue Heeler", date, "aglassman", false);
		Brew b2 = new Brew(1L,"Dark Sumatra", date, "frank", false);
		Brew b3 = new Brew(1L,"Dark Sumatra", date, "frank", false);

		when(brc.brewRepository.findTop20ByOrderByBrewDateDesc()).thenReturn(
			Arrays.asList(
				b1,
				b2,
				b3
			)
		);
		SlackMessageBuilder builder = brewCommandHandlerRepository.last(brc);

		assertEquals("{\"attachments\":[],\"text\":\"Blue Heeler was brewed by aglassman" +
			" on " + dateString + ", and is still available.\\nDark Sumatra was brewed by frank on " +
			dateString + ", and is still available.\"}",
			builder.build());


	}

	@Test
	public void testLastNone() {
		BrewRequestContext brc = createBrewRequestContext("last 2");

		Date date = new Date();
		String dateString = fdf.format(date);


		when(brc.brewRepository.findTop20ByOrderByBrewDateDesc()).thenReturn(
			Arrays.asList()
		);
		SlackMessageBuilder builder = brewCommandHandlerRepository.last(brc);

		assertEquals("{\"attachments\":[],\"text\":\"No brews found.\"}",
			builder.build());


	}
}
