package org.gooru.et2gooru

import java.util.UUID
import net.kaleidos.hibernate.usertype.JsonbMapType
import net.kaleidos.hibernate.usertype.ArrayType
import groovy.sql.Sql
import org.gooru.et2gooru.Utils

class Collection {
	UUID id

	UUID courseId
	UUID unitId
	UUID lessonId
	String title
	Date createdAt
	Date updatedAt
	UUID ownerId
	UUID creatorId
	UUID modifierId
	UUID originalCreatorId
	UUID originalCollectionId
	Serializable parentCollectionId
	Short sequenceId
	Date publishDate
	String publishStatus
	String format
	String thumbnail
	String learningObjective
	Serializable collaborator
	Serializable metadata
	Serializable taxonomy
	String url
	Boolean loginRequired
	Serializable setting
	String grading
	Boolean visibleOnProfile
	Boolean isDeleted
	Integer license
	String creatorSystem

	static mapping = {
		id generator : 'uuid2', type: 'pg-uuid'
		version false
	}

	static constraints = {
		courseId nullable: true
		unitId nullable: true
		lessonId nullable: true
		title maxSize: 1000
		originalCreatorId nullable: true
		originalCollectionId nullable: true
		parentCollectionId nullable: true
		sequenceId nullable: true
		publishDate nullable: true
		publishStatus nullable: true
		thumbnail nullable: true, maxSize: 2000
		learningObjective nullable: true, maxSize: 20000
		collaborator nullable: true
		metadata nullable: true
		taxonomy nullable: true
		url nullable: true, maxSize: 2000
		loginRequired nullable: true
		setting nullable: true
		grading nullable: true
		license nullable: true
		creatorSystem nullable: true
	}
	
	public saveIt(def etId) {
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.executeInsert( "insert into et_collection(title, created_at, updated_at, owner_id, creator_id, modifier_id, format, metadata, setting, grading, license, creator_system, et_id  ) " + 
            						  "values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
            						  [this.title, this.createdAt, this.updatedAt, this.ownerId, this.creatorId, this.modifierId, Sql.OTHER(this.format), Sql.OTHER(this.metadata), Sql.OTHER(this.setting), Sql.OTHER(this.grading), this.license, this.creatorSystem, etId]  ) 

		def id = keys[0][0]
		this.id = id 
    }    	
	
    public boolean isMigrated(def etId) {
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.firstRow( "select id, et_id from et_collection  where et_id = ${etId}" )
		if (keys) {
			this.id = keys.id
			return true	
        }
        
		return false			
    }
}
