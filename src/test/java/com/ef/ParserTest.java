package com.ef;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ParserTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testParseArgs() {
		Parser parser = new Parser();
		// Success parsing log file path args
		String[] args1 = { "--accesslog=/path/to/file" };
		assertThat(parser.parseArgs(args1).values().size(), equalTo(1));

		// Success parsing query params args
		String[] args2 = { "--startDate=2017-01-01 13:00:00.324", "--duration=hourly", "--threshold=100" };
		assertThat(parser.parseArgs(args2).values().size(), equalTo(3));

		// Failed in parsing query params args
		String[] args3 = { "--startDate=2017-01-01 13:00:00.332", "--duration=hourly" };
		assertThat(parser.parseArgs(args3), not(3));
	}

	@Test
	public void testValidateParams() {
		Parser parser = new Parser();

		// Success validating log file path args
		String[] args1 = { "--accesslog=/path/to/file" };
		Map<String, String> params1 = parser.parseArgs(args1);
		assertTrue(parser.validateParams(params1));

		// Success validating query params args
		String[] args2 = { "--startDate=2017-01-01 13:00:00.342", "--duration=hourly", "--threshold=100" };
		Map<String, String> params2 = parser.parseArgs(args2);
		assertTrue(parser.validateParams(params2));
		
		// Failed in validating query params args
		String[] args3 = { "--startDate=2017-01-01.13:00:00.111", "--duration=weekly", "--threshold=100" };
		Map<String, String> params3 = parser.parseArgs(args3);
		assertFalse(parser.validateParams(params3));
	}

}
