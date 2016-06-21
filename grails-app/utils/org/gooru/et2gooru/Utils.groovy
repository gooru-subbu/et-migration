package org.gooru.et2gooru

import grails.util.Environment
import java.util.Date
import java.sql.Timestamp
import java.util.Calendar
import org.apache.commons.lang.time.DateUtils
import groovy.sql.Sql

class Utils {

	public static def sql = (Environment.isDevelopmentMode()) ?
							Sql.newInstance( 'jdbc:postgresql://localhost:5432/nucleus', 'nucleus', 'nucleus', 'org.postgresql.Driver' ) :
							Sql.newInstance( 'jdbc:postgresql://gooru-30-prod-postgresdb.c1ybherlivcq.us-west-1.rds.amazonaws.com/', 'nucleus', 'nucleus', 'org.postgresql.Driver' )
	
	public static def getDateWithoutTimezone() {
		long millis = DateUtils.truncate(new Date(), Calendar.MILLISECOND).getTime();
		Timestamp sq = new Timestamp(millis);  
		
		return sq;
	}
	
	public static def initWriteConnection() {
		return this.sql
	}
	
	public static def getWriteDBConnection() {
		return initWriteConnection()
	}
	
	public static def closeWriteConnection() {
		if (this.sql) this.sql.close()
	}
}