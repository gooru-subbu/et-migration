package org.gooru.et2gooru

import java.util.UUID
import groovy.sql.Sql
import net.kaleidos.hibernate.usertype.JsonbMapType
import net.kaleidos.hibernate.usertype.ArrayType
import org.gooru.et2gooru.Utils

class UserIdentity {
    Long id

	String userId
	String referenceId
	String emailId
	String clientId
	String loginType
	String provisionType
	Boolean emailConfirmStatus
	String status
	Date createdAt
	Date updatedAt

	static mapping = {
		id generator: "assigned"
		version false
	}

	static constraints = {
	}
	
	public saveIt(def etId, boolean bInsertItem){
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.executeInsert( "insert into et_user_identity(login_type, provision_type, email_id, reference_id, user_id, email_confirm_status, status, created_at, updated_at, client_id, et_id, et_insert_flag  ) " + 
            						  "values (?,?,?,?,?,?,?,?,?,?,?,?)",
            						  [ Sql.OTHER(this.loginType), 
            						  	Sql.OTHER(this.provisionType), 
            						  	this.emailId, 
            						  	this.referenceId, 
            						  	UUID.fromString(this.userId), 
            						  	this.emailConfirmStatus, 
            						  	Sql.OTHER(this.status), 
            						  	this.createdAt, 
            						  	this.updatedAt, 
            						  	UUID.fromString(this.clientId), 
            						  	etId, 
            						  	bInsertItem  ]  ) 
    }
    
    public boolean isMigrated(def etId) {
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.firstRow( "select user_id, et_id from et_user_identity  where et_id = ${etId}" )
		if (keys) {
			this.userId = keys.user_id
			return true
        }
        
		return false			
    }
    
}
