package eu.transkribus.client.connection;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.dea.fimgstoreclient.FimgStoreGetClient;
import org.dea.fimgstoreclient.beans.FimgStoreTxt;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpWordgraph;
import eu.transkribus.core.util.HtrUtils;

public class TrpServerConnWordgraphTest {
	public static void main(String[] args){
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
		
//		final int docId = 62;
		final int docId = 287;
		final int pageNr = 1;

		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], args[0], args[1])) {			
			List<TrpWordgraph> wgList = conn.getWordgraphs(2, docId, pageNr);
			
			TrpWordgraph w = wgList.get(3);
			
			FimgStoreGetClient getter = new FimgStoreGetClient(w.getnBestUrl());
			
			FimgStoreTxt nBest = getter.getTxt(w.getnBestKey());
			
			System.out.println(nBest.getText());
			
			String[][] matrix = HtrUtils.getnBestMatrixUpvlc(nBest.getText(), false);
			
			for(String[] s1 : matrix){
				for(String s2 : s1){
					System.out.print(s2 + "\t");
				}
				System.out.print("\n");
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
