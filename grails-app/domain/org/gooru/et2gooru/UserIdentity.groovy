package org.gooru.et2gooru

import java.util.UUID
import groovy.sql.Sql
import net.kaleidos.hibernate.usertype.JsonbMapType
import net.kaleidos.hibernate.usertype.ArrayType
import org.gooru.et2gooru.Utils

class UserIdentity {
    Long id

	UUID userId
	String referenceId
	String emailId
	UUID clientId
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
		
		println "Save IT : ${this.userId}"
		
        def keys = sql.executeInsert( "insert into et_user_identity(login_type, provision_type, email_id, reference_id, user_id, email_confirm_status, status, created_at, updated_at, client_id, et_id, et_insert_flag  ) " + 
            						  "values (?,?,?,?,?,?,?,?,?,?,?,?)",
            						  [Sql.OTHER(this.loginType), Sql.OTHER(this.provisionType), this.emailId, this.referenceId, this.userId, this.emailConfirmStatus, Sql.OTHER(this.status), this.createdAt, this.updatedAt, this.clientId, etId, bInsertItem  ]  ) 
    }
    
    public updateIt(def etId){
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.executeUpdate( "update et_user_identity set reference_id = ? where id = ?",
            						  [this.referenceId, this.id]  ) 
    }
    
    public boolean isMigrated(def etId) {
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.rows( "select user_id, et_id from et_user_identity  where et_id = ${etId}" )
		if (keys && keys.size() > 0) {
			this.userId = keys[0].getAt(0)
			return true
        }
        
		return false			
    }
    
}
