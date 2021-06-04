package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.bookkeeper.bookie.LedgerDescriptor;
import org.apache.bookkeeper.bookie.LedgerDescriptorImpl;
import org.apache.bookkeeper.bookie.SortedLedgerStorage;
import org.apache.bookkeeper.client.api.LedgerEntry;
import org.junit.Test;

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
		Long id = 4000L;
		ByteBuf ledger = LedgerDescriptor.createLedgerFenceEntry(id);
		srl.addEntry(ledger);
		int parameter = 5;
		assertEquals(5, parameter);
	}
}
