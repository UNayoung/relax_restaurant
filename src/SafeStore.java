import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
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
         String user="postgres";
         String password="***********";

        Connection con = DriverManager.getConnection(ur,user,password);
        Statement stmt = con.createStatement();
        
        DatabaseMetaData dbm = con.getMetaData();
		
		ResultSet tables=dbm.getTables(null, null, "relaxrestaurant", null);
		
		if(tables.next()) {
			stmt.executeUpdate("drop table RelaxRestaurant");
		}
		
		tables=dbm.getTables(null, null, "restaurantdetail", null);
		
		if(tables.next()) {
			stmt.executeUpdate("drop table RestaurantDetail");
		}
		
		tables=dbm.getTables(null, null, "gmoney", null);
		if(tables.next()) {
			stmt.executeUpdate("drop table GMoney");
		}
		tables.close();
		

        stmt.executeUpdate("create table RelaxRestaurant (rSeq int, rName varchar(50), zipcode int, category varchar(10), isGMoney bool)");
		stmt.executeUpdate("create table RestaurantDetail (rSeq int, address varchar(100), addressDetail varchar(100), telephone varchar(20))");

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
			
			//db저
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

					
				}	
			}
			
		}

        //지역화폐 //GMoney table create
        stmt.executeUpdate("create table GMoney (sidoName varchar(30), rName varchar(70))");

        File csv = new File("C:\\Users\\우나영\\Desktop\\데이터베이스\\지역화폐가맹점현황\\지역화폐가맹점현황_final.csv"); //파일 경로 설정
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
    }
}