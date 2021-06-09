package org.apache.bookkeeper.mytests;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.bookkeeper.bookie.EntryLogger;
import org.apache.bookkeeper.client.BookKeeper;
import org.apache.bookkeeper.client.LedgerHandle;
import org.apache.bookkeeper.conf.ServerConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.bookkeeper.conf.TestBKConfiguration;
import org.apache.bookkeeper.mytests.utils.SetupBK;
import java.util.Arrays;
import java.util.Collection;


/*
 * 
 * Un EntryLogger salva i suoi log files nelle directory specificate
 */
//@RunWith(Parameterized.class)
public class EntryLoggerTest extends SetupBK {
	
	private static String PSWD = "test-pswd";
	private static String CONT = "test-cont";

    private EntryLogger entryLogger;
    private ServerConfiguration baseConf = TestBKConfiguration.newServerConfiguration();
    private LedgerHandle ledgerHandle;
    private Long location;
    
    private Long ledgerID;
    private Long entryID;
    private Boolean validateEntry;
    private Boolean expected;

//    public EntryLoggerTest(Long ledgerID, Long entryID, Long location, Boolean validateEntry, Boolean expected) throws Exception {
//    	super();
//        this.ledgerID = ledgerID;
//        this.entryID = entryID;
//        this.location = location;
//        this.validateEntry = validateEntry;
//        this.expected = expected;
//    }
    
    public EntryLoggerTest() throws Exception {}

    private static Long testLong=312313L;
//    @Parameterized.Parameters
//    public static Collection<?> getParameters() {
//
//        return Arrays.asList(
//        		{}
////                new InternalReadEntryAux(-1L, -1L, -1L, true, false),
////                new InternalReadEntryAux(0L, 1L, 0L, false, false),
////                new InternalReadEntryAux(1L, 0L, 1L, false, false),
////                new InternalReadEntryAux(1234L, 1L, 0L, true, true),
////                new InternalReadEntryAux(1234L, 1L, 0L, false, true),// validateEntry:false->true for Jacoco branch coverage
////                new InternalReadEntryAux(1234L, 1L, 43L, false, false) // Added for Jacoco statement coverage
//                );
//    }

    @Test
    public void setUp() throws Exception {
        super.setUp();
        baseConf.setOpenFileLimit(1);
        File testFolder = new File("/tmp/bk-data/current");
        testFolder.mkdirs();

        entryLogger = new EntryLogger(baseConf);


//        if (ledgerID == 1234L) {
        // creo il ledger dove vado ad inserire le varie entry
        ledgerHandle = bkc.createLedger(BookKeeper.DigestType.CRC32, PSWD.getBytes());

            ByteBuf entry = Unpooled.buffer(1024);
            entry.writeLong(ledgerHandle.getId()); 	// ledger id
            entry.writeLong(entryID); 				// entry id
            entry.writeBytes("foo".getBytes());		// content

            
            location = entryLogger.addEntry(ledgerHandle.getId(), entry, true);
            System.out.println(location);
//            testEntity.setLedgerID(ledgerHandle.getId());
//            if(testEntity.getLocation()!=43L) {
//                testEntity.setLocation(location);
//            }
//        }


    }

    //@After
    public void tearDown() {
        File testFolder = new File("/tmp/bk-data/current");
        testFolder.delete();
    }

    //@Test
//    public void testMethod() throws Exception {
//        try {
//            ByteBuf testBuf = entryLogger.internalReadEntry(testEntity.getLedgerID(), testEntity.getEntryID(),
//                                                            testEntity.getLocation(), testEntity.getValidateEntry());
//            byte[] testByte = new byte[testBuf.capacity()];
//            testBuf.getBytes(0, testByte);
//            String testString = new String(testByte);
//            ledgerHandle.close();
//            Assert.assertEquals(testEntity.getExpected(), (testString.contains(new String("matteo"))));
//        } catch (NullPointerException | IOException | IllegalArgumentException e) {
//            e.printStackTrace();
//            Assert.assertEquals(testEntity.getExpected(), false);
//
//
//        }
//    }
}