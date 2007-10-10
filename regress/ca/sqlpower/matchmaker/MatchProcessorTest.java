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

package ca.sqlpower.matchmaker;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResult;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.util.MMTestUtils;
import ca.sqlpower.sql.SPDataSource;

public class MatchProcessorTest extends TestCase {

	private static final Logger logger = Logger.getLogger(MatchProcessor.class);
	
	private MatchProcessor matcher;
	private SQLTable resultTable;
	private MatchPool pool;
	
	private SourceTableRecord dup1;
	private SourceTableRecord dup2;
	private SourceTableRecord dup3;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		SPDataSource dataSource = DBTestUtil.getHSQLDBInMemoryDS();
		SQLDatabase db = new SQLDatabase(dataSource);
		Connection con = db.getConnection();
		MMTestUtils.createResultTable(con);
		TestingMatchMakerSession session = new TestingMatchMakerSession();
		session.setConnection(con);
		Match match = new Match();
		match.setSession(session);
		resultTable = db.getTableByName(null, "pl", "match_results");
		match.setResultTable(resultTable);
		
		List<String> testStrings = new ArrayList<String>();
		testStrings.add("A");
		testStrings.add("B");
		testStrings.add("C");
		
		List<BigDecimal> testNumbers = new ArrayList<BigDecimal>();
		testNumbers.add(BigDecimal.valueOf(0));
		testNumbers.add(BigDecimal.valueOf(1));
		testNumbers.add(BigDecimal.valueOf(2));
		
		List<Boolean> testBooleans = new ArrayList<Boolean>();
		testBooleans.add(Boolean.TRUE);
		testBooleans.add(Boolean.FALSE);
		
		List<Date> testDates = new ArrayList<Date>();
		testDates.add(new Date(0));
		testDates.add(new Date(Long.MAX_VALUE/2));
		testDates.add(new Date(Long.MAX_VALUE));

		List<MungeResult> results = new ArrayList<MungeResult>();

		int key = 0;
		
		// Add test data
		for (String s : testStrings) {
			for (BigDecimal n: testNumbers) {
				for (Boolean b: testBooleans) {
					for (Date d: testDates) {
						MungeResult result = new MungeResult();
						MungeStepOutput<String> out1 = new MungeStepOutput<String>("string", String.class);
						out1.setData(s);
						MungeStepOutput<BigDecimal> out2 = new MungeStepOutput<BigDecimal>("decimal", BigDecimal.class);
						out2.setData(n);
						MungeStepOutput<Boolean> out3 = new MungeStepOutput<Boolean>("boolean", Boolean.class);
						out3.setData(b);
						MungeStepOutput<Date> out4 = new MungeStepOutput<Date>("date", Date.class);
						out4.setData(d);
						MungeStepOutput[] outputs = {out1, out2, out3, out4};
						
						result.setMungedData(outputs);
						SourceTableRecord source = new SourceTableRecord(session, match, Integer.valueOf(key));
						key++;
						result.setSourceTableRecord(source);
						
						results.add(result);
					}
				}
			}
		}
		
		// Add duplicate data
		MungeResult result = new MungeResult();
		MungeStepOutput<String> out1 = new MungeStepOutput<String>("string", String.class);
		out1.setData("A");
		MungeStepOutput<BigDecimal> out2 = new MungeStepOutput<BigDecimal>("decimal", BigDecimal.class);
		out2.setData(BigDecimal.valueOf(0));
		MungeStepOutput<Boolean> out3 = new MungeStepOutput<Boolean>("boolean", Boolean.class);
		out3.setData(Boolean.TRUE);
		MungeStepOutput<Date> out4 = new MungeStepOutput<Date>("date", Date.class);
		out4.setData(new Date(0));
		MungeStepOutput[] outputs = {out1, out2, out3, out4};
		result.setMungedData(outputs);
		dup1 = new SourceTableRecord(session, match, Integer.valueOf(key));
		key++;
		result.setSourceTableRecord(dup1);
		results.add(result);
		
		result = new MungeResult();
		out1 = new MungeStepOutput<String>("string", String.class);
		out1.setData("B");
		out2 = new MungeStepOutput<BigDecimal>("decimal", BigDecimal.class);
		out2.setData(BigDecimal.valueOf(1));
		out3 = new MungeStepOutput<Boolean>("boolean", Boolean.class);
		out3.setData(Boolean.FALSE);
		out4 = new MungeStepOutput<Date>("date", Date.class);
		out4.setData(new Date(Long.MAX_VALUE/2));
		outputs = new MungeStepOutput[4];
		outputs[0] = out1;
		outputs[1] = out2;
		outputs[2] = out3;
		outputs[3] = out4;
		result.setMungedData(outputs);
		dup2 = new SourceTableRecord(session, match, Integer.valueOf(key));
		key++;
		result.setSourceTableRecord(dup2);
		results.add(result);
		
		result = new MungeResult();
		out1 = new MungeStepOutput<String>("string", String.class);
		out1.setData("C");
		out2 = new MungeStepOutput<BigDecimal>("decimal", BigDecimal.class);
		out2.setData(BigDecimal.valueOf(2));
		out3 = new MungeStepOutput<Boolean>("boolean", Boolean.class);
		out3.setData(Boolean.TRUE);
		out4 = new MungeStepOutput<Date>("date", Date.class);
		out4.setData(new Date(Long.MAX_VALUE));
		outputs = new MungeStepOutput[4];
		outputs[0] = out1;
		outputs[1] = out2;
		outputs[2] = out3;
		outputs[3] = out4;
		result.setMungedData(outputs);
		dup3 = new SourceTableRecord(session, match, Integer.valueOf(key));
		key++;
		result.setSourceTableRecord(dup3);
		results.add(result);
		
		
		logger.debug("Here's the data we're testing on:");
		for (MungeResult r: results) {
			logger.debug(r);
		}
		
		Collections.shuffle(results);

		MungeProcess process = new MungeProcess();
		process.setParentMatch(match);
		pool = new MatchPool(match);
		matcher = new MatchProcessor(pool, process, results);
	}
	
	public void testCall() throws Exception {
		matcher.call();
		Set<PotentialMatchRecord> pmrs = pool.getPotentialMatches();
		assertEquals(3, pmrs.size());
		
		for (PotentialMatchRecord pmr: pmrs) {
			SourceTableRecord lhs = pmr.getOriginalLhs();
			SourceTableRecord rhs = pmr.getOriginalRhs();
			
			if (lhs != dup1 && lhs != dup2 && lhs != dup3 &&
					rhs != dup1 && rhs != dup2 && rhs != dup3) {
				logger.debug("An unexpected duplicate was found:");
				logger.debug("LHS: " + lhs);
				logger.debug("RHS: " + rhs);
				fail("An unexpected Potential Match was found");
			}
		}
	}
}