package org.gooru.et2gooru

import java.util.UUID
import net.kaleidos.hibernate.usertype.JsonbMapType
import net.kaleidos.hibernate.usertype.ArrayType
import org.gooru.et2gooru.Utils
import groovy.sql.Sql

class UserDemographic {

    UUID id

	String firstname
	String lastname
	String emailId
	def   createdAt
	def   updatedAt
	Map metadata
	
	static mapping = {
		id generator : 'uuid2', type: 'pg-uuid'
		metadata type: JsonbMapType
		version false
	}

	static constraints = {
		firstname nullable: true, maxSize: 100
		lastname nullable: true, maxSize: 100
		emailId nullable: true, maxSize: 256, unique: true
		metadata nullable: true
	}
	
	public saveIt(def etId, boolean bInsertItem){
		def sql = Utils.getWriteDBConnection()
		def keys	
		
		if (this.id) {
        	keys = sql.executeInsert( "insert into et_user_demographic(firstname, lastname, email_id, created_at, updated_at, user_category, et_id, id, et_insert_flag   ) " + 
            						  "values (?,?,?,?,?,?,?,?,?)",
            						  [this.firstname, this.lastname, this.emailId, this.createdAt, this.updatedAt, Sql.OTHER('teacher'), etId, this.id, bInsertItem]  ) 
		} else {
        	keys = sql.executeInsert( "insert into et_user_demographic(firstname, lastname, email_id, created_at, updated_at, user_category, et_id, et_insert_flag   ) " + 
            						  "values (?,?,?,?,?,?,?,?)",
            						  [this.firstname, this.lastname, this.emailId, this.createdAt, this.updatedAt, Sql.OTHER('teacher'), etId, bInsertItem]  ) 
			def id = keys[0][0]
			this.id = id 
		}		
    }
    
    public boolean isMigrated(def etId) {
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.firstRow( "select id, et_id from et_user_demographic  where et_id = ${etId}" )
		if (keys) {
			this.id = keys.id
			return true	
        }
        
		return false			
    }
	
}
