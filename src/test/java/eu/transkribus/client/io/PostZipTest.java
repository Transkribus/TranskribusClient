package eu.transkribus.client.io;
//
//import java.io.File;
//import java.io.IOException;
//
//import javax.security.auth.login.LoginException;
//
//import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.dea.transcript.trp.client.connection.TrpServerConn;
//
//public class PostZipTest {
//	private static final Logger logger = LoggerFactory.getLogger(PostZipTest.class);
//	public static void main(String[] args) throws LoginException, IOException{
//		TrpServerConn conn = TrpServerConn.getInstance(TrpServerConn.SERVER_URIS[TrpServerConn.DEFAULT_URI_INDEX], args[0], args[1]);
//
//		final File zipFile = new File("/tmp/test.zip");
//		try{
//			conn.testPostTrpDoc(zipFile);
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		conn.logout();
//	}
//}
