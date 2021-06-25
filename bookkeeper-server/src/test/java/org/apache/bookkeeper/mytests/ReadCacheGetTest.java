package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.bookkeeper.bookie.storage.ldb.ReadCache;
import org.apache.bookkeeper.mytests.parameters.ReadCacheParameters;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;


@RunWith(Parameterized.class)
public class ReadCacheGetTest {
	static ReadCache cache;
	
	// Cache settings
	public static int ENTRY_SIZE = 1024;					// Ogni entry viene ha dimensione 1024 byte
	public static int MAX_ENTRIES = 10;						// Ogni cache può contenere al massimo 10 entry
	public static int CACHE_SIZE = ENTRY_SIZE*MAX_ENTRIES;	// Massimo numero di byte che può contenere l'entry
	
	// Test parameters
	long ledgerId;
	long entryId;
	ByteBuf expectedResult;
	
	public static final ByteBuf ENTRY = Unpooled.wrappedBuffer(new byte[ENTRY_SIZE]);;
	public static final ByteBuf ENTRY_NULL = null;
	public static final long ENTRY_ID = 1;
	public static final long LEDGER_ID = 1;
	
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	public ReadCacheGetTest(ReadCacheParameters input) {
		this.ledgerId = input.getLedgerId();
		this.entryId = input.getEntryId();
		if (input.getExpectedException()!=null) {
			expectedException.expect(input.getExpectedException());
		}
		else {
			this.expectedResult = input.getExpectedResult();
		}
	}
	
    @Parameters
    public static Collection<ReadCacheParameters> getParameters() {
		
		List<ReadCacheParameters> testInputs = new ArrayList<>();
		
		// ledgerId / entryId / entry
		testInputs.add(new ReadCacheParameters(ENTRY_ID,LEDGER_ID,ENTRY));
		testInputs.add(new ReadCacheParameters(ENTRY_ID+1,ENTRY_ID+1,ENTRY_NULL));
		testInputs.add(new ReadCacheParameters(1,-1,ENTRY_NULL));
		testInputs.add(new ReadCacheParameters(-1,1,IllegalArgumentException.class));
		
		return testInputs;
    }

	
	
	// Configuro l'ambiente di esecuzione istanziando una nuova ReadCache ed inserendo un'entry da ottenere
	@Before
	public void configure() {
		UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
		cache = new ReadCache(allocator,CACHE_SIZE);
		cache.put(LEDGER_ID, ENTRY_ID, ENTRY);
	}
	
	
	// Chiudo la cache dopo aver eseguito tutti i test case
	@AfterClass
	public static void closeCache() {
		cache.close();
	}
	
	@Test
	public void getTest() {
		ByteBuf actualValue = cache.get(ledgerId, entryId);
		
		ByteBuf expectedValue = this.expectedResult;
		assertEquals(expectedValue, actualValue);			// Verifichiamo che il valore ottenuto dal get è uguale a quello inserito con il put durante la configurazione
	}
}
