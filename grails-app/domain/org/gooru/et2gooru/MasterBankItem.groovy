package org.gooru.et2gooru

class MasterBankItem {
	Integer    id

	Integer ticketType
	Integer teacherUid
	String title
	String subTitle
	Byte statusDeleted
	Date addedDate
	Date updatedDate
	BigDecimal ticketDifficultyRating
	String optimizeDevice
	String pollTimeDuration
	String pollDuration
	Integer standardCommonCoreGuid
	Integer searchable
	Integer isScored
	String displayMode

	                  
	static mapping = {
    	datasource 'lookup'
		version false
        id column: 'id'
	}
}
