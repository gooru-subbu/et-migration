package org.gooru.et2gooru

class MetadataReference {

	Date createdAt
	Date updatedAt
	String format
	String label
	Serializable info
	Boolean isVisible
	Short sequenceId

	static mapping = {
		id generator: "assigned"
		version false
	}

	static constraints = {
		label maxSize: 2000
		info nullable: true
	}
}
