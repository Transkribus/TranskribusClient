package eu.transkribus.client.tools;

import eu.transkribus.client.connection.TrpServerConn;

public class HtrTrainTool {

	public static void main(String[] args){
		
		if(args.length != 2){
			throw new IllegalArgumentException("No credentials");
		}
//		final int colId = 1885;

		try (TrpServerConn conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0], args[0], args[1])) {			
			
//			System.out.println(conn.runHtr(colId, 101, "Reichsgericht1"));
			

//			final Integer[] docIds = {84};
//			System.out.println(conn.runHtrTraining("Bozen_HS37a", docIds));

//			final Integer[] docIds = {821};
//			System.out.println(conn.runHtrTraining("Reichsgericht_Training", docIds));
			
//			final Integer[] docIds = {771, 774, 936, 937};
//			System.out.println(conn.runHtrTraining("Forrest_Collection_3", docIds));

//			final Integer[] docIds = {902};
//			System.out.println(conn.runHtrTraining("Marine_Lives", docIds));
			
//			final Integer[] docIds = {26};
//			System.out.println(conn.runHtrTraining("Bentham_2", docIds));
			
//			final Integer[] docIds =  {2721}; //Schiller 62
//			final Integer[] docIds = {2723}; //Goethe 58
			//final int docId = 2724; //Goethe-Schreiber 9
//			final int colId = 639;
//			System.out.println(conn.runHtrTraining("Goethe", docIds));
//			final Integer[] docIds =  {2721, 2723};
//			System.out.println(conn.runHtrTraining("Goethe_Schiller", docIds));
			
			//445 Fuchs Gabelsberger Steno 
//			final Integer[] docIds =  {445};
//			System.out.println(conn.runHtrTraining("Fuchs", docIds));
			
//			final Integer[] docIds = {2794};
//			System.out.println(conn.runHtrTraining("Reichsgericht_artificial_2", docIds));
			
//			System.out.println(conn.runHtr(2, 904, "Zwettl_30"));
//			System.out.println(conn.runHtr(2, 822, "Reichsgericht_Training"));
//			System.out.println(conn.runHtr(344, 1119, "Forrest_Collection_3"));
//			1119
			
//			final Integer[] docIds = {2020};
//			System.out.println(conn.runHtrTraining("Kerr_Collection_1", docIds));
//			System.out.println(conn.runHtr(495, 2020, "Kerr_Collection_1"));
			
//			final Integer[] docIds =  {2444};
//			System.out.println(conn.runHtrTraining("Konzilsprotokolle_1", docIds));
			
//			final Integer[] docIds =  {917, 2002};
//			System.out.println(conn.runHtrTraining("WrDiarium_Farbe", docIds));
			
//			final Integer[] docIds =  {2009, 2010};
//			System.out.println(conn.runHtrTraining("WrDiarium_BW", docIds));
			
//			final Integer[] docIds =  {917, 2009, 2010, 2011, 2012};
//			System.out.println(conn.runHtrTraining("WrDiarium", docIds));
			
//			final Integer[] docIds =  {2808, 2809, 2810};
//			System.out.println(conn.runHtrTraining("Wieland", docIds));
			
//			final Integer[] docIds =  {2683};
//			System.out.println(conn.runHtrTraining("Kochbuch_Binder", docIds));
			
//			final Integer[] docIds =  {3678, 3679, 3680};
//			System.out.println(conn.runHtrTraining("Konzilsprotokolle_2", docIds));
			
//			final Integer[] docIds = {3686, 3687, 3695, 3696, 3697, 3705, 3736, 
//					3744, 3781, 3821, 3824, 3826, 3827, 3872, 3904, 3905, 3920};
//			System.out.println(conn.runHtrTraining("Wydemann", docIds));
			
//			final Integer[] docIds = {3840};
//			System.out.println(conn.runHtrTraining("Just_Anton", docIds));
			
//			final Integer[] docIds = {3667, 3668};
//			System.out.println(conn.runHtrTraining("Schweinfurth_Georg", docIds));
			
//			final Integer[] docIds = {3675, 3676, 3849};
//			System.out.println(conn.runHtrTraining("Nuernberger_Briefbuecher", docIds));
			
//			final Integer[] docIds = {4658};
//			System.out.println(conn.runHtrTraining("NAF_504_v1", docIds));
			
//			final Integer[] docIds = {4865, 4973};
//			System.out.println(conn.runHtrTraining("GeoIII", docIds));
			
//			final Integer[] docIds =  {3963, 3964, 4158, 4159, 4160, 4161, 3678, 3679, 3680};
//			System.out.println(conn.runHtrTraining("Konzilsprotokolle_3", docIds));
			
//			final Integer[] docIds =  {4684, 4685, 4686, 4687, 4688, 4689, 4690, 4691, 4692, 4693, 4694,
//					4695, 4696, 4697, 4698, 4699, 4700, 4701, 4702, 4703, 4704, 4705, 4706, 4707};
//			System.out.println(conn.runHtrTraining("IO_Botany_2", docIds));
			
			final Integer[] docIds =  {286};
			System.out.println(conn.runHtrTraining("Frisch-Sklaverei_2", docIds));
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
}
