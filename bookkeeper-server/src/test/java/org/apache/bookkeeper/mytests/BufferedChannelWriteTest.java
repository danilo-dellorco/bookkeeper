package org.apache.bookkeeper.mytests;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import tools.DebugTools;

import org.apache.bookkeeper.bookie.BufferedChannel;
import org.apache.bookkeeper.mytests.parameters.BufferedChannelWriteParameters;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.primitives.Bytes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

/**
 * Write all the data in src to the {@link FileChannel}. Note that this function
 * can buffer or re-order writes based on the implementation. These writes will
 * be flushed to the disk only when flush() is invoked.
 *
 * @param src The source ByteBuffer which contains the data to be written.
 * @throws IOException if a write operation fails.
 */

@RunWith(Parameterized.class)

public class BufferedChannelWriteTest {
	
	private static Random rand = new Random();
	
	private static final String TMP_DIR = "tmp/";
	private static final String LOG_FILE = "BfcWriteFile.log";
	
	private static final int WRITE_BUF_SIZE = 2;
	private static final int CHANNEL_BUF_SIZE = 4;

	private static Path logPath;
	private Integer writeBufSize;

	private Integer buffChanCapacity;
	private Integer initialPos; // Posizione assoluta per riprendere l'operazione di write successiva

	/*
	 * if unpersistedBytesBound is non-zero value, then after writing to
	 * writeBuffer, it will check if the unpersistedBytes is greater than
	 * unpersistedBytesBound and then calls flush method if it is greater.
	 *
	 * It is a best-effort feature, since 'forceWrite' method is not synchronized
	 * and unpersistedBytes is reset in 'forceWrite' method before calling
	 * fileChannel.force
	 */
	// private Long unpersistedBytesBound; // Se è pari a zero controlla che
	// unpersistedBytes è maggiore di unpersistedBytesBound ed invoca flush

	private FileChannel fc; // Canale per leggere, scrivere e manipolare i file.
	private BufferedChannel bufferedChannel; // Offre un livello di bufferizzazione prima di scrivere effettivamente sul
												// FileChannel
	private ByteBuf srcBuffer; // ByteBuffer sorgente che contiene i dati che saranno scritti nel
								// BufferedChannel
	private byte[] bytes;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	public BufferedChannelWriteTest(BufferedChannelWriteParameters testInput) {
		this.writeBufSize = testInput.getWriteBufSize();
		this.buffChanCapacity = testInput.getBuffChanCapacity();
		if (testInput.getExpectedException() != null) {
			this.expectedException.expect(testInput.getExpectedException());
		}
		// this.unpersistedBytesBound = testInput.getUnpersistedBytesBound();
		this.initialPos = 0;

	}

	@Parameterized.Parameters
	public static Collection<BufferedChannelWriteParameters> getParameters() {
		List<BufferedChannelWriteParameters> testInputs = new ArrayList<>();

		testInputs.add(new BufferedChannelWriteParameters(true, WRITE_BUF_SIZE, CHANNEL_BUF_SIZE, null));
		// TODO aggiungere altri parametri

		return testInputs;
	}

	@BeforeClass
	static public void configure() {
		logPath = Paths.get(TMP_DIR, LOG_FILE);

		// Se non esiste, creo la directory tmp in cui verrà inserito il file di log
		if (!Files.exists(Paths.get(TMP_DIR))) {
			File tmp = new File(TMP_DIR);
			tmp.mkdir();
		}

		// Cancello il file di log nel caso fosse ancora presente da esecuzioni precedenti
		if (Files.exists(Paths.get(TMP_DIR, LOG_FILE))) {
			File testFile = new File(TMP_DIR, LOG_FILE);
			testFile.delete();
		}
	}

	@Before
	public void setup() throws IOException {
		
		FileChannel fileChannel = FileChannel.open(logPath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);	// Apre o Crea il file e ritorna il canale R/W associato
		this.fc = fileChannel;
		this.fc.position(this.fc.size());
		this.bytes = new byte[WRITE_BUF_SIZE];
		
		// Genero un numero random di bytes e li inserisco dentro l'array 'bytes'
		Random rd = new Random();
		rd.nextBytes(this.bytes);
		
		// Scrivo i bytes generati randomicamente all'interno del buffer di input srcBuffer
		this.srcBuffer = createEntryFromBytes(this.bytes);
		System.out.println("Bytes random generati (srcBuffer): "+Arrays.toString(this.bytes));
	}

	@After
	public void tearDown() {
		// Cancella il file di test
		try {
			this.fc.close();
			// Cancella il file di test se esiste
			if (Files.exists(logPath)) {
				File testFile = new File(logPath.toString());
				//testFile.delete();	// TODO cancellare il file al termine dell'operazione ora lo tengo commentato per debugging
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test(timeout = 1000)
	public void bufChWrTest() throws Exception {
		UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
		bufferedChannel = new BufferedChannel(allocator, fc, buffChanCapacity);
		
		// A questo punto ho soltanto un array random di byte dentro this.bytes e in srcBuffer dovrei aver scritto la stessa quantità di byte
		
		// Scrivo tutti i byte di srcBuffer all'interno del bufferedChannel
		bufferedChannel.write(srcBuffer);

		// Check if the write was made correctly
		Integer numBytesInWriteBuff = 0;
		Integer numBytesInFileCh = 0;
		numBytesInWriteBuff = this.writeBufSize;	// Ottengo il numero di bytes nel buffer di scrittura come la dimensione del writeBuffer

		// Creo un array di bytes per inserire i bytes dentro il buffer
		byte[] bytesInWriteBuf = new byte[numBytesInWriteBuff];
		
		// Scrivo i bytes nel WriteBuffer in un array di bytes bytesInWriteBuf
		this.bufferedChannel.writeBuffer.getBytes(0, bytesInWriteBuf);
		
		System.out.println("Bytes presenti nel WriteBuffer: "+Arrays.toString(bytesInWriteBuf));
		

		// Otengo i byte che dovrebbero essere nel WriteBuf come i bytes random generati togliendo il numero di bytes che sono in writeBuf
		byte[] expectedBytes = Arrays.copyOfRange(this.bytes, this.bytes.length - numBytesInWriteBuff, this.bytes.length);
		System.out.println("Bytes attesi che siano nel WriteBuffer: "+Arrays.toString(expectedBytes));

		Assert.assertEquals("Error",Arrays.toString(expectedBytes),Arrays.toString(bytesInWriteBuf));

		ByteBuffer buff = ByteBuffer.allocate(numBytesInFileCh);

		this.fc.position(this.initialPos);
		this.fc.read(buff);
		byte[] bytesInFileCh = buff.array();

		expectedBytes = Arrays.copyOfRange(this.bytes, 0, numBytesInFileCh);
		Assert.assertEquals("Error", Arrays.toString(expectedBytes), Arrays.toString(bytesInFileCh));

		// Controllo se la posizione è corretta
		Assert.assertEquals(this.writeBufSize + this.initialPos, this.bufferedChannel.position);
	}
	
	
	private static ByteBuf createEntryFromBytes(byte[] bytes) {
		ByteBuf bb = Unpooled.buffer();
		bb.writeBytes(bytes);
		return bb;
	}
}
