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

import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.sql.SPDataSource;


public class SQLServerCleanseTest extends AbstractCleanseTest {
	protected String getFullTableName() {
		return "MM_TEST.MM_TEST.CLEANSE_TEST";
	}
	
	protected SPDataSource getDS() {
		return DBTestUtil.getSqlServerDS();
	}

	protected void createTables() throws Exception {

		//Drop the old tables
		String sql = "DROP TABLE " + getFullTableName();
		execSQL(con,sql);
		
		// Creates the source table
		sql = "CREATE TABLE " + getFullTableName() + " (" +
			"\n ID NUMERIC NOT NULL PRIMARY KEY," +
			"\n COL_STRING VARCHAR(20) NULL," +
			"\n COL_DATE DATETIME NULL," +
			"\n COL_NUMBER NUMERIC NULL)";
		execSQL(con,sql);
		
		sourceTable = db.getTableByName("MM_TEST", "MM_TEST", "CLEANSE_TEST");
		project.setSourceTable(sourceTable);
        project.setSourceTableIndex(sourceTable.getPrimaryKeyIndex());
		
	}
	

}
