package org.apache.bookkeeper.mytests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.apache.bookkeeper.bookie.storage.ldb.ReadCache;

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
public class ReadCacheTest {
	static ReadCache cache;
	
	// Cache settings
	public static int ENTRY_SIZE = 1024;					// Ogni entry viene ha dimensione 1024 byte
	public static int MAX_ENTRIES = 10;						// Ogni cache può contenere al massimo 10 entry
	public static int CACHE_SIZE = ENTRY_SIZE*MAX_ENTRIES;	// Massimo numero di byte che può contenere l'entry
	public static UnpooledByteBufAllocator ALLOCATOR = UnpooledByteBufAllocator.DEFAULT;
	
	// Types of entries
	public static ByteBuf valid_entry;
	public static ByteBuf illegal_entry;
	public static ByteBuf null_entry;
	
	// Test parameters
	int ledgerId;
	int entryId;
	ByteBuf entry;
	Class<? extends Exception> expectedException;
	
	public ReadCacheTest(int ledgerId, int entryId, ByteBuf entry, Class<? extends Exception> expectedException) {
		this.ledgerId = ledgerId;
		this.entryId = entryId;
		this.entry = entry;
		this.expectedException = expectedException;
	}

	// Permette di specificare l'eccezione attesa.
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	
	
	// Configuro l'ambiente di esecuzione istanziando una nuova ReadCache prima di ogni test.
	@Before
	public void configure() {
		cache = new ReadCache(ALLOCATOR,CACHE_SIZE);
	}
	
	// Chiudo la cache dopo aver eseguito tutti i test case
	@AfterClass
	public static void closeCache() {
		cache.close();
	}
	
    @Parameters
    public static Collection<Object[]> data() {
    	
		// Setup of the entries
		valid_entry = Unpooled.wrappedBuffer(new byte[ENTRY_SIZE]);
		illegal_entry = Unpooled.wrappedBuffer(new byte[CACHE_SIZE+1]);
		null_entry = null;
		
        return Arrays.asList(new Object[][] {
                {1,0,valid_entry,null},
                {0,1,illegal_entry,IndexOutOfBoundsException.class},
                {1,-1,null_entry,NullPointerException.class},
                {-1,1,valid_entry,IllegalArgumentException.class}
        });
    }
    
    
    
	@Test
	public void putTest() {
		System.out.println("______________PUT TEST______________");
		System.out.println(String.format("ledger: %d\nentry: %d\n", ledgerId,entryId));
		
		if (expectedException != null) {
			exceptionRule.expect(expectedException);
		}
		cache.put(ledgerId, entryId, entry);

		assertEquals(1, cache.count()); 				// Vediamo che abbiamo esattamente un'entry
		assertEquals(ENTRY_SIZE, cache.size()); 		// Vediamo che la dimensione totale è data dalla dimensione dell'unica entry aggiunta
	}

	@Test
	public void getTest() {
        if(expectedException != null) {
        	exceptionRule.expect(expectedException);
        }
		
        cache.put(ledgerId, entryId, entry);
		ByteBuf expectedValue = entry;
		ByteBuf actualValue = cache.get(ledgerId, entryId);
		assertEquals(expectedValue, actualValue);			// Verifichiamo che il valore ottenuto dal get è uguale a quello inserito con il put
		
		actualValue = cache.get(ledgerId+1, entryId+1);
		assertEquals(null,actualValue);						// Verifichiamo che il get su un'entry non presente in cache ritorna null
	}
}
