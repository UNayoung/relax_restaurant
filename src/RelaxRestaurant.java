import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class RelaxRestaurant {
	
	public static void main(Connection con) throws SQLException {
		// TODO Auto-generated method stub
		Scanner scan = new Scanner(System.in);

	
		System.out.println("2. 안심식당 검색 을 선택하셨습니다.");
		System.out.println("검색을 원하는 안심식당을 입력하세요.");
		String store = scan.nextLine();
		store += "%";
		String query_store = null;
		query_store = "select rName, sidoName,rSeq from RelaxRegion where rName like ? order by rName";
		PreparedStatement psmt_store = con.prepareStatement(query_store);

		psmt_store.clearParameters();
		psmt_store.setString(1, store);
	
		int i=1;
		String rName[]=new String[9999];
		String sidoName[]=new String[9999];
		int rSeq[]=new int[9999];
		
		ResultSet result_store = null;
		
		result_store=psmt_store.executeQuery();
//		System.out.println(result_store);
		
			if(result_store!=null) {
				
				while (result_store.next()) {

					rName[i] = result_store.getString("rName");
					sidoName[i] = result_store.getString("sidoname");
					rSeq[i]=result_store.getInt("rSeq");
					System.out.println("No"+i+". "+rName[i] + "\t" + sidoName[i]);
				i=i+1;
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
								if(r.getString("telephone")==null) {
									System.out.println(r.getString("rName") + " " + r.getString("address") + " ");
								}
								else {
									System.out.println(r.getString("rName") + " " + r.getString("address") + " " + r.getString("telephone") + " ");
								}
							}
							else {
								if(r.getString("telephone")==null) {
									System.out.println(r.getString("rName") + " " + r.getString("address") + " " + r.getString("addressdetail") + " ");
								}
								else {
									System.out.println(r.getString("rName") + " " + r.getString("address") + " " + r.getString("addressdetail") + " "
											+ r.getString("telephone") + " ");
								}
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
					System.out.println("검색하신 식당은 안심식당이 아닙니다.");
				}
				
				
			}
			
			
			while(true) {
				
				System.out.println("\n안심식당 정보 제공 서비스");
				System.out.println("--------------------------------------");
				System.out.println("1. 다시 검색하기");
				System.out.println("2. 종료");
				int choice=scan.nextInt();
				
				if(choice==1) {
					App app = new App();
					try {
						app.main(null);
						return ;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else if(choice==2){
					System.out.println("프로그램을 종료합니다.");

					System.exit(0);
				}
				else {
					System.out.println("잘못된 입력입니다.");
				}
			}
			
			


	}
	
	

}
