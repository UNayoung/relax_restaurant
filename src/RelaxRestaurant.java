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

		System.out.println("2. �ȽɽĴ� �˻� �� �����ϼ̽��ϴ�.");
		System.out.println("�˻��� ���ϴ� �ȽɽĴ��� �Է��ϼ���.");
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
				
				System.out.println("�� ������ ���ϴ� �Ĵ��� ��ȣ�� �Է��ϼ���");
				
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
						System.out.println(r.getString("rName")+"��(��)"+"��⵵ ����ȭ�󰡸����Դϴ�.");
					}
					else {
						System.out.println(r.getString("rName")+"��(��)"+"��⵵ ����ȭ�󰡸����� �ƴմϴ�.");

					}
					
				}
			}
			else {
				System.out.println("�˻��Ͻ� �Ĵ��� �ȽɽĴ��� �ƴմϴ�.");
			}
			
		

	}
	
	

}
