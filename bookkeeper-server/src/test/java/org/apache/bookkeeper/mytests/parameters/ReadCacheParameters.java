package org.apache.bookkeeper.mytests.parameters;

import io.netty.buffer.ByteBuf;

public class ReadCacheParameters {
	
	int ledgerId;
	int entryId;
	ByteBuf entry;
	Class<? extends Exception> expectedException;

	// Costruttore
	public ReadCacheParameters(int ledgerId, int entryId, ByteBuf entry, Class<? extends Exception> expectedException) {
		this.ledgerId = ledgerId;
		this.entryId = entryId;
		this.entry = entry;
		this.expectedException = expectedException;
	}
	
	
	/*
	 * Getters & Setters
	 */
	public int getLedgerId() {
		return ledgerId;
	}

	public void setLedgerId(int ledgerId) {
		this.ledgerId = ledgerId;
	}

	public int getEntryId() {
		return entryId;
	}

	public void setEntryId(int entryId) {
		this.entryId = entryId;
	}

	public ByteBuf getEntry() {
		return entry;
	}

	public void setEntry(ByteBuf entry) {
		this.entry = entry;
	}

	public Class<? extends Exception> getExpectedException() {
		return expectedException;
	}

	public void setExpectedException(Class<? extends Exception> expectedException) {
		this.expectedException = expectedException;
	}

}
