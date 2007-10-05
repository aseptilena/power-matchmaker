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

public class SubstringByWordMungeStepTest extends TestCase {

	private SubstringByWordMungeStep step;
	
	private MungeStepOutput testInput;
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new SubstringByWordMungeStep();
	}
	public void testCallonNoOccurence() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdefg");
		step.setParameter(step.BEGIN_PARAMETER_NAME,"1");
		step.setParameter(step.END_PARAMETER_NAME,"3");
		step.connectInput(0, testInput);
		step.open();
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("bc",result);
	}

	public void testCallonMultipleOccurences() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abc bcd cde def efg fgh ghi");
		step.setParameter(step.BEGIN_PARAMETER_NAME,"1");
		step.setParameter(step.END_PARAMETER_NAME,"2");
		step.connectInput(0, testInput);
		step.open();
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("b c d e f g h",result);
	}

	public void testCallonMixedDelimiters() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abc   bcd \ncde def\n efg fgh\n\nghi");
		step.setParameter(step.DELIMITER_PARAMETER_NAME, " \n");
		step.setParameter(step.RESULT_DELIM_PARAMETER_NAME, ":");
		step.setParameter(step.BEGIN_PARAMETER_NAME,"1");
		step.setParameter(step.END_PARAMETER_NAME,"2");
		step.connectInput(0, testInput);
		step.open();
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("b:c:d:e:f:g:h",result);
	}
	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.setParameter(step.BEGIN_PARAMETER_NAME, "3");
		step.setParameter(step.END_PARAMETER_NAME, "9");
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