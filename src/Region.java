import java.sql.*;
import java.util.Scanner;


public class Region {
	

	public static void main(Connection con) throws SQLException {
		// TODO Auto-generated method stub

        DatabaseMetaData dbm = con.getMetaData();
		ResultSet tables=dbm.getTables(null, null, "counter", null);
		Statement stmt = con.createStatement();
		
		tables = dbm.getTables(null, null, "counter", null);
		if(tables.next()) {
			stmt.executeUpdate("drop view Counter");
		}
		tables = dbm.getTables(null, null, "safestore", null);
		if(tables.next()) {
			stmt.executeUpdate("drop view SafeStore");
		}
		tables.close();
		

		Scanner scan = new Scanner(System.in);

		String si, sido, sigun, sigungu, type;
		
		String name[]=new String[9999];
		int rSeq[]=new int[9999];
		
	
			System.out.println("1. 지역 및 음식점 종류별 안심식당 정보 탐색 을 선택하셨습니다.");
			
			String CounterViewWithRollUp = "create view Counter as" + "\n" +
										   "select siName, sidoName, category, count(*)" + "\n" +
										   "from RelaxRestaurant natural join Region" + "\n" +
										   "group by rollup(siName, sidoName, category)" + "\n" +
										   "order by siName, sidoName, category";
			stmt.executeUpdate(CounterViewWithRollUp);
			int totalCount=0;
			String v1 = "select count from Counter where siName is null and sidoName is null and category is null;";
			
			PreparedStatement pp1 = con.prepareStatement(v1);
			ResultSet rr1 = pp1.executeQuery();
			
			while(rr1.next()) {
				totalCount=rr1.getInt(1);
			}
			
			System.out.printf("*****전국 음식점 개수: %d개***** \n", totalCount);
			//System.out.println("[시도명]");
			String a2 ="select distinct siName from Region";
			PreparedStatement p2 = con.prepareStatement(a2);
			ResultSet r2 = p2.executeQuery();
			
			while(r2.next()) {
				si=r2.getString(1);
				
				System.out.println(si);
			}
			System.out.println("\n");
			System.out.println("원하는 시도명을 입력하세요.");
			sido= scan.next();
			
			System.out.println("\n");
			
			String v2 = "select count from Counter where siName="+ "'"+sido+"'" +"and sidoName is null and category is null;";
			
			PreparedStatement pp2 = con.prepareStatement(v2);
			ResultSet rr2 = pp2.executeQuery();
			
			while(rr2.next()) {
				totalCount=rr2.getInt(1);
			}
			
			System.out.printf("*****%s 음식점 개수: %d개***** \n", sido ,totalCount);
			//System.out.println("[시군구명]");
			String a3 ="select distinct sidoName from Region where siName = "+ "'"+sido+"'";
			PreparedStatement p3 = con.prepareStatement(a3);
			ResultSet r3 = p3.executeQuery();
			
			while(r3.next()) {
				sigun=r3.getString(1);
				
				System.out.println(sigun);
			}

			System.out.println("\n");
			System.out.println("원하는 시군구명을 입력하세요.");
			sigungu= scan.next();

			System.out.println("\n");
			
			String v3 = "select count from Counter where siName="+ "'"+sido+"'" +"and sidoName="+ "'"+sigungu+"'" +"and category is null;";
			
			PreparedStatement pp3 = con.prepareStatement(v3);
			ResultSet rr3 = pp3.executeQuery();
			
			while(rr3.next()) {
				totalCount=rr3.getInt(1);
			}
			
			System.out.printf("*****%s %s 음식점 개수: %d개***** \n", sido , sigungu, totalCount);
			//System.out.println("[음식 종류]");
			System.out.println("일식         서양식         한식         중식");
			
			System.out.println("\n");
			System.out.println("원하는 음식 종류를 입력하세요.");
			type= scan.next();
			
			System.out.println("\n");
			
			String v4 = "select count from Counter where siName="+ "'"+sido+"'" +"and sidoName="+ "'"+sigungu+"'" +"and category = "+ "'"+type+"'";
			
			PreparedStatement pp4 = con.prepareStatement(v4);
			ResultSet rr4 = pp4.executeQuery();
			
			while(rr4.next()) {
				totalCount=rr4.getInt(1);
			}
			
			String SafeStoreView ="create view SafeStore as" +"\n" +
		   			  			  "select rName, rSeq" +"\n" +
		   			  			  "from RelaxRestaurant natural join Region" +"\n" + 
		   			  			  "where siName = "+ "'"+sido+"'"+"and sidoName = "+ "'"+sigungu+"'"+ "and category = "+ "'"+type+"'";
			stmt.executeUpdate(SafeStoreView);

			String a4 ="select rName,rSeq from SafeStore";
			PreparedStatement p4 = con.prepareStatement(a4);
			ResultSet r4 = p4.executeQuery();	
			
			System.out.printf("*********["+ sido+ "-"+sigungu+"-"+type +"] 안심식당 목록 : %d개********* \n", totalCount);
			int i=1;
			if(r4!=null) {
				while(r4.next()) {
					name[i]=r4.getString(1);
					rSeq[i]=r4.getInt(2);
					
					System.out.println("No."+i+" "+name[i]);
					i=i+1;
				}
			}
			if(i!=1) {
				System.out.println("상세 정보를 원하는 식당의 번호를 입력하세요");
				
				int num=scan.nextInt();
				
				String query="select * from RestaurantDetail natural join RelaxRegion "
						+ "where rSeq= ? ";
				
				PreparedStatement p= con.prepareStatement(query);
				p.setInt(1, rSeq[num]);

				ResultSet r = p.executeQuery();
				if(r!=null) {
					while (r.next()) {
						
						if(r.getString("addressdetail")==null) {
							System.out.println(r.getString("rName")+" "+r.getString("address")+" "+r.getString("telephone")+" ");
						}
						else {
							System.out.println(r.getString("rName")+" "+r.getString("address")+" "+r.getString("addressdetail")+" "
									+r.getString("telephone")+" ");
						}
						
						if(r.getBoolean("isGMoney")==true) {
							System.out.println(r.getString("rName")+"은(는)"+"경기도 지역화폐가맹점입니다.");
						}
						else {
							System.out.println(r.getString("rName")+"은(는)"+"경기도 지역화폐가맹점이 아닙니다.");

						}

					}
				}
			}
			else {
				System.out.println("해당 카테고리의 안심식당 정보가 없습니다.");

			}
			
			
			
			

			
			while(true) {
				
				System.out.println("안심식당 정보 제공 서비스");
				System.out.println("--------------------------------------");
				System.out.println("1. 다시 검색하기");
				System.out.println("2. 종료");
				int choice=scan.nextInt();
				
				if(choice==1) {
					App app = new App();
					try {
						app.main(null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(choice==2){
					System.out.println("프로그램을 종료합니다.");
					
					return;
				}
				else {
					System.out.println("잘못된 입력입니다.");
				}
			}
			
			
	

	}

}
