package org.gooru.et2gooru

class MasterBankItemTypes {
	Integer    id

	String itemName
	String itemDescription

	static hasMany = [masterBankItems: MasterBankItem]

	static mapping = {
    	datasource 'lookup'
		version false
        id column: 'id'
	}
}
