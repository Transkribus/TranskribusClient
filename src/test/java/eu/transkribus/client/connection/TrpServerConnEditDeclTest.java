package eu.transkribus.client.connection;

import java.util.List;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.EdFeature;
import eu.transkribus.core.model.beans.EdOption;

public class TrpServerConnEditDeclTest {
	public static void main(String[] args){
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
		
		
		final int colId = 2;
		final int docId = 69;

		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[1], args[0], args[1])) {			
//			List<TrpDocMetadata> docs = conn.getAllDocs(colId);
			List<EdFeature> features = conn.getEditDeclFeatures(colId);
			
			EdFeature newFeat = new EdFeature();
			newFeat.setColId(null);
			newFeat.setTitle("ein neues Feature");
			EdOption option = new EdOption();
			option.setText("eine neue Option");
			newFeat.getOptions().add(option);
			
			conn.postEditDeclFeature(colId, newFeat);
			
			
//			features.get(0).getOptions().get(0).setSelected(true);
//			List<EdFeature> editDecl = new ArrayList<>(1);
//			editDecl.add(features.get(0));
			
//			conn.postEditDecl(colId, docId, editDecl);
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
