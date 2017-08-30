/**
 * 
 */
package com.heliosapm.io.qreactor.common;

/**
 * @author nwhitehead
 *
 */
public enum SerFormat {

	
//	Serializable objects, though this is to be avoided as it is not efficient
//
//	Externalizable objects is preferred if you wish to use standard Java APIs.
//
//	byte[] and String
//
//	Marshallable; a self describing message which can be written as YAML, Binary YAML, or JSON.
//
//	BytesMarshallable which is low-level binary, or text encoding.
//
//  ByteBuf
	
	SERIALIZABLE,
	BYTES,
	STRING,
	MARSHALLABLE,
	BYTESMARSHALLABLE,
	BYTEBUF;
	
	
}
