import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SafeStore {
    private static String getTagValue(String tag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        if(nValue == null)
            return null;
        return nValue.getNodeValue();
    }

    public static void main(String[] args) throws IOException, SQLException, ParserConfigurationException, SAXException {

    	String ur="jdbc:postgresql://localhost/";
//		String ur="jdbc:postgresql://127.0.0.1:5433/test";

    	String user="postgres";
    	String password="*****";
//    	String password="1234";


		String city;
		int num;
		String si, sido, sigun, sigungu, type, name;

		Scanner scan = new Scanner(System.in);

		Connection con;
		
		try 
	      {
	         // connection establish 하기
	         con = DriverManager.getConnection(ur, user, password);
	           
	      }
	      catch (SQLException e) 
	      {
	         System.out.println("Connection Failed Check output console");
	         e.printStackTrace();
	         return; 
	      }
	      
	      if (con != null)
	      {
	         System.out.println("Connection Success");
	      } 
	      else 
	      {
	         System.out.println("Failed to make conn");
	      }
	      
        Statement stmt = con.createStatement();
        
        DatabaseMetaData dbm = con.getMetaData();
		
		ResultSet tables=dbm.getTables(null, null, "relaxrestaurant", null);
		
		// 이미 table이 있다면 Drop
		if(tables.next()) {
			stmt.executeUpdate("drop table RelaxRestaurant cascade");
		}
		
		tables=dbm.getTables(null, null, "restaurantdetail", null);
		
		if(tables.next()) {
			stmt.executeUpdate("drop table RestaurantDetail");
		}
		
		tables=dbm.getTables(null, null, "gmoney", null);
		if(tables.next()) {
			stmt.executeUpdate("drop table GMoney");
		}

		tables = dbm.getTables(null, null, "region", null);
		if(tables.next()) {
			stmt.executeUpdate("drop table Region");
		}
		
		tables = dbm.getTables(null, null, "relaxregion", null);
		if(tables.next()) {
			stmt.executeUpdate("drop table RelaxRegion cascade");
		}

		tables.close();

		
		stmt.executeUpdate("create table Region (zipcode int, siName varchar(10), sidoName varchar(10), primary key(zipcode))");
		stmt.executeUpdate("create table RelaxRestaurant (rSeq int, rName varchar(70), zipcode int , category varchar(10), isGMoney bool, primary key(rSeq))");
		stmt.executeUpdate("create table RestaurantDetail (rSeq int references RelaxRestaurant(rSeq), address varchar(100), addressDetail varchar(100), telephone varchar(20))");

        String api = "http://211.237.50.150:7080/openapi/2584402a923e0249609a30aa68a450f4c22cf3d2a8af71ca5d1f0b32662c7af6/xml/Grid_20200713000000000605_1/1/1000";

        URL url = new URL(api);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
        Document doc = dBuilder.parse(api);

        doc.getDocumentElement().normalize();
        
        //전체 안심식당 수
		int totalCnt=Integer.parseInt(getTagValue("totalCnt",doc.getDocumentElement()));


		for(int i=1; i<=totalCnt; i=i+1000) {
			
			//1000개씩 가져
			String api2 = "http://211.237.50.150:7080/openapi/2584402a923e0249609a30aa68a450f4c22cf3d2a8af71ca5d1f0b32662c7af6/xml/Grid_20200713000000000605_1/"
			+Integer.toString(i)+"/"+Integer.toString(i+999);
			
			System.out.println(api2);
			URL url2 = new URL(api2);
			HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
			
			conn2.setRequestMethod("GET");
			
			DocumentBuilderFactory dbFactoty2= DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder2 = dbFactoty2.newDocumentBuilder();
			Document doc2 = dBuilder.parse(api2);
			
			doc2.getDocumentElement().normalize();
			
			NodeList nList = doc2.getElementsByTagName("row");
						
			PreparedStatement psmt = con.prepareStatement("INSERT INTO RelaxRestaurant VALUES (?, ?, ?, ?, false)");
			PreparedStatement psmt2 = con.prepareStatement("INSERT INTO RestaurantDetail VALUES (?, ?, ?, ?)");
			PreparedStatement psmt3 = con.prepareStatement("INSERT INTO Region VALUES (?, ?, ?)");

			//db저장
			for(int temp = 0; temp < nList.getLength(); temp++){
				Node nNode = nList.item(temp);
				if(nNode.getNodeType() == Node.ELEMENT_NODE){
					
					Element eElement = (Element) nNode;
					
					psmt.setInt(1, Integer.parseInt(getTagValue("RELAX_SEQ", eElement)));
					psmt.setString(2, getTagValue("RELAX_RSTRNT_NM", eElement));
					psmt.setInt(3, Integer.parseInt(getTagValue("RELAX_ZIPCODE", eElement)));
					psmt.setString(4, getTagValue("RELAX_GUBUN_DETAIL", eElement));
					psmt.executeUpdate();
					
					psmt2.setInt(1, Integer.parseInt(getTagValue("RELAX_SEQ", eElement)));
					psmt2.setString(2, getTagValue("RELAX_ADD1", eElement));
					psmt2.setString(3, getTagValue("RELAX_ADD2", eElement));
					psmt2.setString(4, getTagValue("RELAX_RSTRNT_TEL", eElement));
					psmt2.executeUpdate();

					boolean flag = false;
					int result=0;

					city = getTagValue("RELAX_SIDO_NM", eElement);
					num = Integer.parseInt(getTagValue("RELAX_ZIPCODE", eElement));
					String a1 ="select count(*) from Region where zipcode = " + "'"+num+"'";

					PreparedStatement p1 = con.prepareStatement(a1);
					ResultSet r1 = p1.executeQuery();

					while(r1.next()) {
						result=r1.getInt(1);

						if(result > 0) {
							flag=true;
						}
					}

					if(flag == false) {
						psmt3.setInt(1, Integer.parseInt(getTagValue("RELAX_ZIPCODE", eElement)));
						psmt3.setString(2, getTagValue("RELAX_SI_NM", eElement));
						psmt3.setString(3, getTagValue("RELAX_SIDO_NM", eElement));
						psmt3.executeUpdate();
					}

					
				}	
			}
			
		}
		System.out.println("create RelaxRestaurant");
		System.out.println("create RestaurantDetail");
		System.out.println("create Region");

        //지역화폐 //GMoney table create
        stmt.executeUpdate("create table GMoney (sidoName varchar(30), rName varchar(70))");

//        File csv = new File("C:\\Users\\우나영\\Desktop\\데이터베이스\\지역화폐가맹점현황\\지역화폐가맹점현황_final.csv"); //파일 경로 설정
        String path = System.getProperty("user.dir");

        File csv = new File(path+"/지역화폐가맹점현황.csv");
//        File csv = new File(path+"/지역화폐가맹점현황.csv");
        BufferedReader br = new BufferedReader(new FileReader(csv));
        String line = "";

        PreparedStatement psmt_region = con.prepareStatement("INSERT INTO GMoney VALUES (?, ?)");

        while ((line = br.readLine()) != null) {
            String[] token = line.split(",");
            String temp = token[3].replaceAll("\\\"",""); //따옴표 제거
            //db insert
            if(temp.length() >= 6) {
                if (temp.substring(0,6).equals("일반휴게음식")) {
                    psmt_region.setString(1, token[1].replaceAll("\\\"", "")); //따옴표 제거
                    psmt_region.setString(2, token[2].replaceAll("\\\"", ""));
                    psmt_region.executeUpdate();
                }
            }
        }
        br.close();
		System.out.println("create GMoney");
		
		
//        stmt.executeUpdate("create table RelaxRegion as\n"
//        		+ "select * from RelaxRestaurant natural join Region;");
//
//		stmt.executeUpdate("update RelaxRegion "
//				+ "set isGMoney='TRUE'\n"
//				+ "from GMoney\n"
//				+ "where RelaxRegion.rName like '%'||GMoney.rName||'%' and RelaxRegion.sidoName=GMoney.sidoName;");
		
		stmt.executeUpdate("create table RelaxRegion as "
				+ "select * from RelaxRestaurant natural join Region;");
		
		stmt.executeUpdate("create or replace function test()\n"
				+ "returns trigger as $$\n"
				+ "begin\n"
				+ "update RelaxRestaurant\n"
				+ "set isGMoney='TRUE'\n"
				+ "where rSeq in (select rSeq from RelaxRegion where isGMoney='TRUE'); "
				+ "return null;\n"
				+ "end;					\n"
				+ "$$\n"
				+ "language 'plpgsql';");
		
		stmt.executeUpdate("create trigger RelaxRegionupdate\n"
				+ "after update on RelaxRegion\n"
				+ "for each row execute procedure test();");
		
		stmt.executeUpdate("update RelaxRegion set isGMoney='TRUE'\n"
				+ "from GMoney\n"
				+ "where RelaxRegion.rName = GMoney.rName and RelaxRegion.sidoName=GMoney.sidoName;");
		
		System.out.println("update RelaxRestaurant");
		


        System.out.println("Table 생성 완료");

    }
}