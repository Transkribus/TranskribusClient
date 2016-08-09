package eu.transkribus.client.tools;

import eu.transkribus.client.connection.TrpServerConn;

public class HtrUroTrainTool {

	public static void main(String[] args){
		
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
//		final int colId = 1885;

		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], args[0], args[1])) {			
			
			
			//TEST Server!!!
//			final Integer[] docIds = {1192};
//			System.out.println(conn.runUroHtrTraining(
//					"Reichsgericht_v4", //netName
//					"200", //numEpochs
//					"2e-3", //learningRate
//					"no", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
			
			//PROD Server!!!
			final Integer[] docIds = {6191};
			System.out.println(conn.runUroHtrTraining(
					"South_Carolina_1720", //netName
					"200", //numEpochs
					"2e-3", //learningRate
					"no", //noise
					1000, //TrainSizePerEpoch
					docIds));
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
}
