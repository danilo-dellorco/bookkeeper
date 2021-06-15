package org.apache.bookkeeper.mytests;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

import org.apache.bookkeeper.bookie.BufferedChannel;
import org.apache.bookkeeper.mytests.parameters.BufferedChannelReadParameters;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@RunWith(Parameterized.class)
public class BufferedChannelReadTest {

	private static final String TMP_DIR = "testTemp";
	private static final String TMP_FILE = "BfcReadFile";
	
	private int testNum;


	private int fileSize;
	private int startIndex;
	private int readLength;
	private int buffSize;
	
    private FileChannel fileChannel;
    private BufferedChannel bufferedChannel;
    private byte[] randomBytes;


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public BufferedChannelReadTest(BufferedChannelReadParameters testInputs) {
    	this.testNum = testInputs.getTestNum();
    	this.fileSize = testInputs.getFileSize();
    	this.startIndex = testInputs.getStartIndex();
    	this.readLength = testInputs.getReadLenght();
    	this.buffSize = testInputs.getBuffSize();
    	if (testInputs.getExpectedException() != null) {
    		expectedException.expect(testInputs.getExpectedException());
    	}
    }



    @Parameterized.Parameters
    public static Collection<BufferedChannelReadParameters> getTestParameters() {
        List<BufferedChannelReadParameters> testInputs = new ArrayList<>();
        
        // File Size / Start Index / Read Length / Buffer Size / Expected Exception
        testInputs.add(new BufferedChannelReadParameters(0, -1, 0, 0, 0, NegativeArraySizeException.class));
        testInputs.add(new BufferedChannelReadParameters(1, 0, -1, 0, 0, ArrayIndexOutOfBoundsException.class));
        testInputs.add(new BufferedChannelReadParameters(2, 0, 0, -1, 0, IllegalArgumentException.class));
        testInputs.add(new BufferedChannelReadParameters(2, 0, 0, 0, -1, IllegalArgumentException.class));
        testInputs.add(new BufferedChannelReadParameters(2, 0, 0, 0, 0, null));
        testInputs.add(new BufferedChannelReadParameters(4, 1, 0, 2, 1, IOException.class));
        testInputs.add(new BufferedChannelReadParameters(4, 3, 0, 2, 3, null));
        testInputs.add(new BufferedChannelReadParameters(5, 3, 1, 2, 3, null));
        testInputs.add(new BufferedChannelReadParameters(7, 5000, 2000, 3000, 1500, null));
        testInputs.add(new BufferedChannelReadParameters(8, 5000, 2000, 3001, 1500, IOException.class));
        return testInputs;
    }

    @BeforeClass
    static public void configureEnvironment() {
    	
		// Se non esiste, creo la directory tmp in cui verrà inserito il file di log
		if (!Files.exists(Paths.get(TMP_DIR))) {
			File tmp = new File(TMP_DIR);
			tmp.mkdir();
		}
    }

    
    @Before
    public void configure() throws IOException {
    	String debug = String.format("\n\n================ Test [%d] ================ ", testNum);
    	System.out.println(debug);
    	System.out.println("file size: " + this.fileSize);
    	System.out.println("read length: " + this.readLength);
    	System.out.println("start index: " + this.startIndex);
    	System.out.println("==========================================");
    	
    	generateRandomFile(this.fileSize);
    	Path filePath = Paths.get(TMP_DIR,TMP_FILE);
        this.fileChannel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        this.fileChannel.position(this.fileChannel.size());
    }

    
	@After
	public void clear() throws IOException {
		// Chiudo canali e file aperti soltanto se sono stati effettivamente aperti
		if (expectedException==null) {
			this.bufferedChannel.close();
			this.fileChannel.close();
		}
	}
    
	// Cancello la directory contenente i file temporanei
	@AfterClass
	public static void clearEnvironment() {
		File directory = new File(TMP_DIR);
		String[] entries = directory.list();
		for(String s: entries){
		    File currentFile = new File(directory.getPath(),s);
		    currentFile.delete();
		}
		directory.delete();
	}
	
    @Test
    public void ReadTest() throws Exception {

    	UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
    	this.bufferedChannel = new BufferedChannel(allocator, this.fileChannel, this.buffSize);
    	
        ByteBuf readDestBuff = Unpooled.buffer();
        
        // Imposto la dimensione del buffer di destinazione della read pari al numero di bytes che voglio leggere
        readDestBuff.capacity(readLength);

        // Read ritorna il numero di byte letti dal bufferedChannel
        int numReadBytes = this.bufferedChannel.read(readDestBuff, this.startIndex,this.readLength);
        System.out.println("Eseguita lettura | Num Bytes letti: " + numReadBytes);
        
        // Ottengo l'array dei bytes letti tramite il buffer di destinazione passato alla read
        byte[] bytesRead = readDestBuff.array(); 
        System.out.println("Bytes Letti: " + Arrays.toString(bytesRead));
        
        
        int numBytesExpected = 0;
        if (this.fileSize - this.startIndex >= this.readLength) {
        	numBytesExpected = this.readLength;
        }
        else {
        	numBytesExpected =  this.randomBytes.length - this.startIndex - this.readLength;
        }
        
        System.out.println("numBytesExpected: " + numBytesExpected);
        byte[] expectedBytes = Arrays.copyOfRange(this.randomBytes, this.startIndex, this.startIndex + numBytesExpected);
        System.out.println("BytesExpected: "+Arrays.toString(expectedBytes));

        Assert.assertEquals(Arrays.toString(expectedBytes), Arrays.toString(bytesRead));
    }
    
	private void generateRandomFile(int size) throws IOException {
		// Genero un numero random di bytes e li inserisco dentro l'array 'bytes'
		this.randomBytes = new byte[size];
		Random rd = new Random();
		rd.nextBytes(randomBytes);
		
		// Scrivo i bytes generati all'interno del File Generato
        FileOutputStream fileStream = new FileOutputStream(TMP_DIR+"/"+TMP_FILE);
        fileStream.write(this.randomBytes);
        fileStream.close();

        System.out.println("Bytes Generati: "+Arrays.toString(randomBytes));
	}


}
