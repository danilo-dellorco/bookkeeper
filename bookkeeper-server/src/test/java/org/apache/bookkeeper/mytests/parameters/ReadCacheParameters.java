package org.apache.bookkeeper.mytests.parameters;

import io.netty.buffer.ByteBuf;

public class ReadCacheParameters {
	
	long ledgerId;
	long entryId;
	ByteBuf expectedResult;
	Class<? extends Exception> expectedException;

	// Costruttore
	public ReadCacheParameters(long ledgerId, long entryId, Class<? extends Exception> expectedException) {
		this.ledgerId = ledgerId;
		this.entryId = entryId;
		this.expectedException = expectedException;
	}
	
	// Costruttore
	public ReadCacheParameters(long ledgerId, long entryId, ByteBuf expectedResult) {
		this.ledgerId = ledgerId;
		this.entryId = entryId;
		this.expectedResult = expectedResult;
	}
	
	
	/*
	 * Getters & Setters
	 */
	public long getLedgerId() {
		return ledgerId;
	}

	public void setLedgerId(long ledgerId) {
		this.ledgerId = ledgerId;
	}

	public long getEntryId() {
		return entryId;
	}

	public void setEntryId(long entryId) {
		this.entryId = entryId;
	}

	public Class<? extends Exception> getExpectedException() {
		return expectedException;
	}

	public void setExpectedException(Class<? extends Exception> expectedException) {
		this.expectedException = expectedException;
	}

	public ByteBuf getExpectedResult() {
		return expectedResult;
	}
	
	

}
