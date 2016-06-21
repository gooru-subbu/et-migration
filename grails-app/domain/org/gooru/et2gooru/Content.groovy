package org.gooru.et2gooru

import java.util.UUID
import net.kaleidos.hibernate.usertype.JsonbMapType
import net.kaleidos.hibernate.usertype.ArrayType
import org.gooru.et2gooru.Utils
import groovy.sql.Sql

class Content {
	UUID  id

	String title
	String url
	Date createdAt
	Date updatedAt
	UUID creatorId
	UUID modifierId
	UUID originalCreatorId
	UUID originalContentId
	UUID parentContentId
	Date publishDate
	String publishStatus
	String narration
	String description
	String contentFormat
	String contentSubformat
	Serializable answer
	Serializable metadata
	Serializable taxonomy
	Serializable hintExplanationDetail
	String thumbnail
	UUID courseId
	UUID unitId
	UUID lessonId
	UUID collectionId
	Short sequenceId
	Boolean isCopyrightOwner
	Serializable copyrightOwner
	Serializable info
	Boolean visibleOnProfile
	Serializable displayGuide
	Serializable accessibility
	Boolean isDeleted
	Serializable editorialTags
	Integer license
	String creatorSystem

	static mapping = {
		id generator : 'uuid2', type: 'pg-uuid'
		version false
	}

	static constraints = {
	}
	
	public saveIt(def etId, def etItemId) {
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.executeInsert( "insert into et_content(title, created_at, updated_at, creator_id, modifier_id, description, " +
        							   " content_format, content_subformat, answer, metadata, collection_id, sequence_id, " +
        							   " info, license, creator_system, et_item_id, et_id  ) " + 
            						   " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ",
            						  [this.title, 
            						   this.createdAt, 
            						   this.updatedAt, 
            						   this.creatorId, 
            						   this.modifierId, 
            						   this.description, 
            						   Sql.OTHER(this.contentFormat), 
            						   Sql.OTHER(this.contentSubformat), 
            						   Sql.OTHER(this.answer), 
            						   Sql.OTHER(this.metadata), 
            						   this.collectionId, 
            						   this.sequenceId, 
            						   Sql.OTHER(this.info), 
            						   this.license, 
            						   this.creatorSystem, 
            						   etItemId, 
            						   etId ]  ) 

		def id = keys[0][0]
		this.id = id 
    }    	
	
    public boolean isMigrated(def etId, def etItemId) {
		def sql = Utils.getWriteDBConnection()
		
        def keys = sql.rows( "select id, et_id, et_item_id from et_content  where et_id = ${etId} and et_item_id = ${etItemId}" )
		if (keys && keys.size() > 0) {
			this.id = keys[0].getAt(0)
			return true	
        }
        
		return false			
    }
	
}
