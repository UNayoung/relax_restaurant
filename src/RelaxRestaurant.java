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
		query_store = "select rName, sidoName,rSeq from Relaxrestaurant natural join Region where rName like ? order by rName";
		PreparedStatement psmt_store = con.prepareStatement(query_store);

		psmt_store.clearParameters();
		psmt_store.setString(1, store);

	
		int i=1;
		String rName[]=new String[9999];
		String sidoName[]=new String[9999];
		int rSeq[]=new int[9999];
		
		ResultSet result_store = psmt_store.executeQuery();

			if(result_store!=null) {
				while (result_store.next()) {
					
					rName[i] = result_store.getString("rName");
					sidoName[i] = result_store.getString("sidoname");
					rSeq[i]=result_store.getInt("rSeq");
					System.out.println("No"+i+". "+rName[i] + "\t" + sidoName[i]);
				i=i+1;
				
				}
				
				System.out.println("상세 정보를 원하는 식당의 번호를 입력하세요");
				
				int num=scan.nextInt();
				
				String query="select * from RestaurantDetail natural join RelaxRestaurant "
						+ "where rSeq= ? ";
				
				PreparedStatement p= con.prepareStatement(query);
				p.setInt(1, rSeq[num]);

				ResultSet r = p.executeQuery();
				
				while (r.next()) {
			
					System.out.println(r.getString("rName")+" "+r.getString("address")+" "+r.getString("addressdetail")+" "
							+r.getString("telephone")+" ");
					
					if(r.getBoolean("isGMoney")==true) {
						System.out.println(r.getString("rName")+"은(는)"+"경기도 지역화폐가맹점입니다.");
					}
					else {
						System.out.println(r.getString("rName")+"은(는)"+"경기도 지역화폐가맹점이 아닙니다.");

					}
					
				}
			}
			else {
				System.out.println("검색하신 식당은 안심식당이 아닙니다.");
			}
			
		

	}
	
	

}
