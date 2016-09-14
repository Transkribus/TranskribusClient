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
//			final Integer[] docIds = {6191};
//			System.out.println(conn.runUroHtrTraining(
//					"South_Carolina_1720", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
			
//			final Integer[] docIds = {6068, 6072, 6259};
//			System.out.println(conn.runUroHtrTraining(
//					"GEO_1-3_v2", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
			
//			final Integer[] docIds =  {4684, 4685, 4686, 4687, 4688, 4689, 4690, 4691, 4692, 4693, 4694,
//					4695, 4696, 4697, 4698, 4699, /* 4700,*/ 4701, 4702, 4703, 4704, 4705, 4706, 4707};
//			
//			//4700 is the test document
//			
//			System.out.println(conn.runUroHtrTraining(
//					"IO_Botany_v1", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
			
			final Integer[] docIds =  {6459};
			
			//4700 is the test document
			
			System.out.println(conn.runUroHtrTraining(
					"Bozen", //netName
					"200", //numEpochs
					"2e-3", //;1e-3", //learningRate
					"both", //noise
					1000, //TrainSizePerEpoch
					docIds));
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
}
