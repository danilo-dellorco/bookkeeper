package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.bookkeeper.bookie.EntryKey;
import org.apache.bookkeeper.bookie.LedgerDescriptor;
import org.apache.bookkeeper.bookie.LedgerDescriptorImpl;
import org.apache.bookkeeper.bookie.SortedLedgerStorage;
import org.apache.bookkeeper.client.api.LedgerEntry;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.primitives.Bytes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

public class ProvaLancioTest {
	
	public ProvaLancioTest () {
		// Costruttore
	}
	
	@Test
	public void dummyTest() throws IOException {
		SortedLedgerStorage srl = new SortedLedgerStorage();
		System.out.println(srl);
		Long id = 4000L;
		//ByteBuf ledger = LedgerDescriptor.createLedgerFenceEntry(id);
		EntryKey entry = new EntryKey();
		//System.out.println(ledger);
		System.out.println(entry);
		
		int parameter = 5;
		assertEquals(5, parameter);
	}
}
