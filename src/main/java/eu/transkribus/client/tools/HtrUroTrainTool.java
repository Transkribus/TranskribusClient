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
//			TODO check with Gundram
			
//			final Integer[] docIds =  {4684, 4685, 4686, 4687, 4688, 4689, 4690, 4691, 4692, 4693, 4694,
//					4695, 4696, 4697, 4698, 4699, /* 4700,*/ 4701, 4702, 4703, 4704, 4705, 4706, 4707};
//			
//			//4700 is the test document
//			
//			System.out.println(conn.runUroHtrTraining(
//					"IO_Botany_v2", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
//			
//			
//			final Integer[] docIds =  {6459};
//			
//			System.out.println(conn.runUroHtrTraining(
//					"Bozen", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
//			

//			final Integer[] docIds =  {6527};
//			
//			System.out.println(conn.runUroHtrTraining(
//					"Frisch-Sklaverei", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
			
//			final Integer[] docIds = {771, 774, 936};
//			System.out.println(conn.runUroHtrTraining(
//					"Forrest_Collection", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
//			TODO check with Gundram
			
//			final Integer[] docIds = {6861};
//			System.out.println(conn.runUroHtrTraining(
//					"Resolutions_v1", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
			
//			final Integer[] docIds = {5471, 5244, 5245, 5246, 5252, 5254, 5256, 5258, 5260, 
//										5250, 5819, 5964, 6243, 6245, 6247};
//			
//			System.out.println(conn.runUroHtrTraining(
//					"Konzilsprotokolle_v1", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					docIds));
			
//			final Integer[] docIds = {7029, 7030, 7031, 7032, 7033, 7034, 7035, 7036, 7037, 7038, 7039};
//
//			System.out.println(conn.runUroHtrTraining(
//				"StAZH", //netName
//				"200", //numEpochs
//				"2e-3", //;1e-3", //learningRate
//				"both", //noise
//				1000, //TrainSizePerEpoch
//				"escher_v3.sprnn",
//				docIds));
			
//			final Integer[] docIds = {7268};
//			
//			System.out.println(conn.runUroHtrTraining(
//					"Cyrillic_20th_Century", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					null,
//					docIds));
			
//			final Integer[] docIds = {7441};
//			
//			System.out.println(conn.runUroHtrTraining(
//					"Speer", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					null,
//					docIds));
			
//			final Integer[] docIds = {6629};
//			
//			System.out.println(conn.runUroHtrTraining(
//					"Hyde_Reel_1_Session_2", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					null,
//					docIds));
			
//			final Integer[] docIds = {7706};
//			
//			System.out.println(conn.runUroHtrTraining(
//					"NB_Norway_Koren", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					null,
//					docIds));

//			final Integer[] docIds = {7854};
//			
//			System.out.println(conn.runUroHtrTraining(
//					"Egypt_diary", //netName
//					"200", //numEpochs
//					"2e-3", //;1e-3", //learningRate
//					"both", //noise
//					1000, //TrainSizePerEpoch
//					null,
//					docIds));
			
			final Integer[] docIds = {7996};
			
			System.out.println(conn.runUroHtrTraining(
					"Sutor", //netName
					"200", //numEpochs
					"2e-3", //;1e-3", //learningRate
					"both", //noise
					1000, //TrainSizePerEpoch
					null,
					docIds));
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
}
