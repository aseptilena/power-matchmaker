/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.util.List;

import junit.framework.TestCase;

public class StringSubstitutionMungeStepTest extends TestCase {

	private StringSubstitutionMungeStep step;
	
	private MungeStepOutput testInput;
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new StringSubstitutionMungeStep();
	}

	/**
	 * This tests the case where the target string is not present, the output
	 * should just be the same as before the call. 
	 */
	public void testCallonNoOccurence() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdefg");
		step.connectInput(0, testInput);
		step.setParameter(step.FROM_PARAMETER_NAME, "h");
		step.setParameter(step.TO_PARAMETER_NAME, "123");
		step.open();
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("abcdefg", result);
	}

	public void testCallonMultipleOccurences() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcABCabc");
		step.connectInput(0, testInput);
		step.setParameter(step.FROM_PARAMETER_NAME, "abc");
		step.setParameter(step.TO_PARAMETER_NAME, "123");
		step.open();
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("123ABC123", result);
	}

	/**
	 * This tests the case where the regular expression option is enabled.
	 */
	public void testCallonUsingRegex() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("aabfooaabfooabfoob");
		step.connectInput(0, testInput);
		step.setParameter(step.USE_REGEX_PARAMETER_NAME, "true");
		step.setParameter(step.FROM_PARAMETER_NAME, "a*b");
		step.setParameter(step.TO_PARAMETER_NAME, "-");
		step.open();
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("-foo-foo-foo-", result);
	}
	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.connectInput(0, testInput);
		step.open();
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals(null, result);
	}
	
	public void testCallonInteger() throws Exception {
		testInput = new MungeStepOutput<Integer>("test", Integer.class);
		testInput.setData(new Integer(1));
		try {
			step.connectInput(0, testInput);
			fail("UnexpectedDataTypeException was not thrown as expected");
		} catch (UnexpectedDataTypeException ex) {
			// UnexpectedDataTypeException was thrown as expected
		}
	}
}