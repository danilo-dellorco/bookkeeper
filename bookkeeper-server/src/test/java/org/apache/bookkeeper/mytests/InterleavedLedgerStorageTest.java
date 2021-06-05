package org.apache.bookkeeper.mytests;

import java.io.IOException;

import org.apache.bookkeeper.bookie.EntryKey;
import org.apache.bookkeeper.bookie.InterleavedLedgerStorage;
import org.apache.bookkeeper.client.LedgerHandle;
import org.apache.bookkeeper.client.api.LedgerEntry;
import org.apache.bookkeeper.client.impl.LedgerEntryImpl;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import junit.*;

public class InterleavedLedgerStorageTest {
	
	/*
 	@Override
    public long addEntry(ByteBuf entry) throws IOException {
        long ledgerId = entry.getLong(entry.readerIndex() + 0);
        long entryId = entry.getLong(entry.readerIndex() + 8);
        long lac = entry.getLong(entry.readerIndex() + 16);

        processEntry(ledgerId, entryId, entry);

        ledgerCache.updateLastAddConfirmed(ledgerId, lac);
        return entryId;
    }
	 */
	
	public InterleavedLedgerStorageTest() {
	}
	
	
	@Test
	public void addEntryTest() throws IOException {
		InterleavedLedgerStorage storage = new InterleavedLedgerStorage();
		System.out.println(storage);
		long ledgerId;
		long entryId;
        ByteBuf entry = Unpooled.buffer(128);
        	entry.writeBoolean(true);

        storage.addEntry(entry);
	}
}
