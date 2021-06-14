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
import java.io.FileNotFoundException;
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
	private static final int FILE_SIZE = 3;
	
	private static final int READ_LENGHT = FILE_SIZE;
	private static final int BUFF_SIZE = 5;
	private static final int START_INDEX = 0;

    private FileChannel fileChannel;
    private BufferedChannel bufferedChannel;
    private byte[] randomBytes;


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    public <TestInput> BufferedChannelReadTest(TestInput testInput) {}



    @Parameterized.Parameters
    public static Collection<BufferedChannelReadParameters> getTestParameters() {
        List<BufferedChannelReadParameters> testInputs = new ArrayList<>();
        
        testInputs.add(new BufferedChannelReadParameters(FILE_SIZE,BUFF_SIZE,READ_LENGHT));
        // TODO aggiungere altri test case
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
    	generateRandomFile(FILE_SIZE);
    	Path filePath = Paths.get(TMP_DIR,TMP_FILE);
        this.fileChannel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        System.out.println("File Channel Position: " + fileChannel.position());
        
        // Sposto la posizione del fileChannel alla fine del fileChannel
        this.fileChannel.position(this.fileChannel.size());
        System.out.println("File Channel Position: " + fileChannel.position());
    }

    
	@After
	public void clear() throws IOException {
		// Chiudo canali e file aperti soltanto se sono stati effettivamente aperti
		if (expectedException==null) {
			this.bufferedChannel.close();
		}
		this.fileChannel.close();
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
	
//	/*
    @Test
    public void ReadTest() throws Exception {
    	System.out.println("INIZIATO READ TEST");
        ByteBuf readBuf = Unpooled.buffer();
        UnpooledByteBufAllocator allocator = UnpooledByteBufAllocator.DEFAULT;
        
        this.bufferedChannel = new BufferedChannel(allocator, this.fileChannel, BUFF_SIZE);
        System.out.println("ISTANZIATO BUFFERED CHANNEL");
        

        // Read ritorna il numero di byte letti dal bufferedChannel
        int numReadBytes = this.bufferedChannel.read(readBuf, START_INDEX,FILE_SIZE);
        System.out.println("Eseguita lettura | Num Bytes letti: " + numReadBytes);
        
        // Ottengo i bytes letti come i soli bytes nel buffer di lettura non nulli
        byte[] readBuffArray = readBuf.array();
        byte[] bytesReaded = Arrays.copyOfRange(readBuffArray,0,numReadBytes);
        System.out.println("Bytes Letti: " + Arrays.toString(bytesReaded));
        
        int numBytesExpected = (this.randomBytes.length - START_INDEX);
        byte[] expectedBytes = Arrays.copyOfRange(this.randomBytes, START_INDEX, START_INDEX + numBytesExpected);

        Assert.assertEquals(Arrays.toString(expectedBytes), Arrays.toString(bytesReaded));
    }
//    */
    
	private void generateRandomFile(int size) throws IOException {
		// Genero un numero random di bytes e li inserisco dentro l'array 'bytes'
		this.randomBytes = new byte[size];
		Random rd = new Random();
		rd.nextBytes(randomBytes);
		
		// Scrivo i bytes generati all'interno del ByteBuf
        FileOutputStream fileStream = new FileOutputStream(TMP_DIR+"/"+TMP_FILE);
        fileStream.write(this.randomBytes);
        fileStream.close();

        System.out.println("Bytes Generati: "+Arrays.toString(randomBytes));
	}


}
