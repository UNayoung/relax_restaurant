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
		//String ur="jdbc:postgresql://127.0.0.1:5433/test";

    	String user="postgres";
    	//String password="***********";
    	String password="";


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
			stmt.executeUpdate("drop view RelaxRegion");
		}

		tables.close();

		
		stmt.executeUpdate("create table Region (zipcode int, siName varchar(10), sidoName varchar(10), primary key(zipcode))");
		stmt.executeUpdate("create table RelaxRestaurant (rSeq int, rName varchar(70), zipcode int, category varchar(10), isGMoney bool)");
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
		
		/*NodeList nList = doc.getElementsByTagName("row");

		PreparedStatement psmt = con.prepareStatement("INSERT INTO Region VALUES (?, ?, ?)");
		//서울특별시 O
		psmt.setInt(1, 121000); psmt.setString(2, "서울특별시");psmt.setString(3, "마포구");
		psmt.executeUpdate();
		psmt.setInt(1, 137000); psmt.setString(2, "서울특별시");psmt.setString(3, "서초구");
		psmt.executeUpdate();
		psmt.setInt(1, 122000); psmt.setString(2, "서울특별시");psmt.setString(3, "은평구");
		psmt.executeUpdate();
		psmt.setInt(1, 150000); psmt.setString(2, "서울특별시");psmt.setString(3, "영등포구");
		psmt.executeUpdate();
		psmt.setInt(1, 136000); psmt.setString(2, "서울특별시");psmt.setString(3, "성북구");
		psmt.executeUpdate();
		psmt.setInt(1, 152000); psmt.setString(2, "서울특별시");psmt.setString(3, "구로구");
		psmt.executeUpdate();
		psmt.setInt(1, 143000); psmt.setString(2, "서울특별시");psmt.setString(3, "광진구");
		psmt.executeUpdate();
		psmt.setInt(1, 110000); psmt.setString(2, "서울특별시");psmt.setString(3, "종로구");
		psmt.executeUpdate();
		psmt.setInt(1, 134000); psmt.setString(2, "서울특별시");psmt.setString(3, "강동구");
		psmt.executeUpdate();
		psmt.setInt(1, 132000); psmt.setString(2, "서울특별시");psmt.setString(3, "도봉구");
		psmt.executeUpdate();
		psmt.setInt(1, 120000); psmt.setString(2, "서울특별시");psmt.setString(3, "서대문구");
		psmt.executeUpdate();
		psmt.setInt(1, 131000); psmt.setString(2, "서울특별시");psmt.setString(3, "중량구");
		psmt.executeUpdate();
		psmt.setInt(1, 156000); psmt.setString(2, "서울특별시");psmt.setString(3, "동작구");
		psmt.executeUpdate();
		//부산광역시 O
		psmt.setInt(1, 609000); psmt.setString(2, "부산광역시");psmt.setString(3, "금정구");
		psmt.executeUpdate();
		psmt.setInt(1, 606000); psmt.setString(2, "부산광역시");psmt.setString(3, "영도구");
		psmt.executeUpdate();
		psmt.setInt(1, 604000); psmt.setString(2, "부산광역시");psmt.setString(3, "사하구");
		psmt.executeUpdate();
		psmt.setInt(1, 601000); psmt.setString(2, "부산광역시");psmt.setString(3, "동구");
		psmt.executeUpdate();
		psmt.setInt(1, 613000); psmt.setString(2, "부산광역시");psmt.setString(3, "수영구");
		psmt.executeUpdate();
		psmt.setInt(1, 612000); psmt.setString(2, "부산광역시");psmt.setString(3, "해운대구");
		psmt.executeUpdate();
		psmt.setInt(1, 607000); psmt.setString(2, "부산광역시");psmt.setString(3, "동래구");
		psmt.executeUpdate();
		psmt.setInt(1, 611000); psmt.setString(2, "부산광역시");psmt.setString(3, "연제구");
		psmt.executeUpdate();
		psmt.setInt(1, 614000); psmt.setString(2, "부산광역시");psmt.setString(3, "부산진구");
		psmt.executeUpdate();
		psmt.setInt(1, 617000); psmt.setString(2, "부산광역시");psmt.setString(3, "사상구");
		psmt.executeUpdate();
		psmt.setInt(1, 618000); psmt.setString(2, "부산광역시");psmt.setString(3, "강서구");
		psmt.executeUpdate();
		psmt.setInt(1, 616000); psmt.setString(2, "부산광역시");psmt.setString(3, "북구");
		psmt.executeUpdate();
		psmt.setInt(1, 619000); psmt.setString(2, "부산광역시");psmt.setString(3, "기장군");
		psmt.executeUpdate();
		psmt.setInt(1, 608000); psmt.setString(2, "부산광역시");psmt.setString(3, "남구");
		psmt.executeUpdate();
		psmt.setInt(1, 602000); psmt.setString(2, "부산광역시");psmt.setString(3, "서구");
		psmt.executeUpdate();
		psmt.setInt(1, 600000); psmt.setString(2, "부산광역시");psmt.setString(3, "중구");
		psmt.executeUpdate();
		//인천광역시 O
		psmt.setInt(1, 406000); psmt.setString(2, "인천광역시");psmt.setString(3, "연수구");
		psmt.executeUpdate();
		psmt.setInt(1, 403000); psmt.setString(2, "인천광역시");psmt.setString(3, "부평구");
		psmt.executeUpdate();
		psmt.setInt(1, 405000); psmt.setString(2, "인천광역시");psmt.setString(3, "남동구");
		psmt.executeUpdate();
		psmt.setInt(1, 905000); psmt.setString(2, "인천광역시");psmt.setString(3, "미추홀구");
		psmt.executeUpdate();
		psmt.setInt(1, 417000); psmt.setString(2, "인천광역시");psmt.setString(3, "강화군");
		psmt.executeUpdate();
		psmt.setInt(1, 400000); psmt.setString(2, "인천광역시");psmt.setString(3, "중구");
		psmt.executeUpdate();
		psmt.setInt(1, 407000); psmt.setString(2, "인천광역시");psmt.setString(3, "계양구");
		psmt.executeUpdate();
		psmt.setInt(1, 401000); psmt.setString(2, "인천광역시");psmt.setString(3, "동구");
		psmt.executeUpdate(); 
		psmt.setInt(1, 404000); psmt.setString(2, "인천광역시");psmt.setString(3, "서구");
		psmt.executeUpdate();
		//광주광역시 O
		psmt.setInt(1, 501000); psmt.setString(2, "광주광역시");psmt.setString(3, "동구");
		psmt.executeUpdate();
		psmt.setInt(1, 502000); psmt.setString(2, "광주광역시");psmt.setString(3, "서구");
		psmt.executeUpdate();
		psmt.setInt(1, 500000); psmt.setString(2, "광주광역시");psmt.setString(3, "북구");
		psmt.executeUpdate();
		psmt.setInt(1, 503000); psmt.setString(2, "광주광역시");psmt.setString(3, "남구");
		psmt.executeUpdate(); 
		psmt.setInt(1, 506000); psmt.setString(2, "광주광역시");psmt.setString(3, "광산구");
		psmt.executeUpdate();
		//대구광역시 O
		psmt.setInt(1, 711000); psmt.setString(2, "대구광역시");psmt.setString(3, "달성군");
		psmt.executeUpdate();
		psmt.setInt(1, 706000); psmt.setString(2, "대구광역시");psmt.setString(3, "수성구");
		psmt.executeUpdate();
		psmt.setInt(1, 704000); psmt.setString(2, "대구광역시");psmt.setString(3, "달서구");
		psmt.executeUpdate();
		psmt.setInt(1, 702000); psmt.setString(2, "대구광역시");psmt.setString(3, "북구");
		psmt.executeUpdate();
		psmt.setInt(1, 703000); psmt.setString(2, "대구광역시");psmt.setString(3, "서구");
		psmt.executeUpdate();
		psmt.setInt(1, 701000); psmt.setString(2, "대구광역시");psmt.setString(3, "동구");
		psmt.executeUpdate();
		psmt.setInt(1, 700000); psmt.setString(2, "대구광역시");psmt.setString(3, "중구");
		psmt.executeUpdate();
		psmt.setInt(1, 705000); psmt.setString(2, "대구광역시");psmt.setString(3, "남구");
		psmt.executeUpdate();
		//대전광역시 O
		psmt.setInt(1, 301000); psmt.setString(2, "대전광역시");psmt.setString(3, "중구");
		psmt.executeUpdate();
		psmt.setInt(1, 306000); psmt.setString(2, "대전광역시");psmt.setString(3, "대덕구");
		psmt.executeUpdate();
		psmt.setInt(1, 300000); psmt.setString(2, "대전광역시");psmt.setString(3, "동구");
		psmt.executeUpdate(); 
		psmt.setInt(1, 302000); psmt.setString(2, "대전광역시");psmt.setString(3, "서구");
		psmt.executeUpdate();
		psmt.setInt(1, 305000); psmt.setString(2, "대전광역시");psmt.setString(3, "유성구");
		psmt.executeUpdate();
		//울산광역시 O
		psmt.setInt(1, 689000); psmt.setString(2, "울산광역시");psmt.setString(3, "울주군");
		psmt.executeUpdate();
		psmt.setInt(1, 680000); psmt.setString(2, "울산광역시");psmt.setString(3, "남구");
		psmt.executeUpdate();
		psmt.setInt(1, 682000); psmt.setString(2, "울산광역시");psmt.setString(3, "동구");
		psmt.executeUpdate();
		psmt.setInt(1, 683000); psmt.setString(2, "울산광역시");psmt.setString(3, "북구");
		psmt.executeUpdate();
		psmt.setInt(1, 681000); psmt.setString(2, "울산광역시");psmt.setString(3, "중구");
		psmt.executeUpdate();
		//경기도 O
		psmt.setInt(1, 477000); psmt.setString(2, "경기도");psmt.setString(3, "가평군");
		psmt.executeUpdate();
		psmt.setInt(1, 411000); psmt.setString(2, "경기도");psmt.setString(3, "고양시");
		psmt.executeUpdate();
		psmt.setInt(1, 427000); psmt.setString(2, "경기도");psmt.setString(3, "과천시");
		psmt.executeUpdate();
		psmt.setInt(1, 423000); psmt.setString(2, "경기도");psmt.setString(3, "광명시");
		psmt.executeUpdate();
		psmt.setInt(1, 464000); psmt.setString(2, "경기도");psmt.setString(3, "광주시");
		psmt.executeUpdate();
		psmt.setInt(1, 471000); psmt.setString(2, "경기도");psmt.setString(3, "구리시");
		psmt.executeUpdate();
		psmt.setInt(1, 435000); psmt.setString(2, "경기도");psmt.setString(3, "군포시");
		psmt.executeUpdate();
		psmt.setInt(1, 415000); psmt.setString(2, "경기도");psmt.setString(3, "김포시");
		psmt.executeUpdate();
		psmt.setInt(1, 472000); psmt.setString(2, "경기도");psmt.setString(3, "남양주시");
		psmt.executeUpdate();
		psmt.setInt(1, 483000); psmt.setString(2, "경기도");psmt.setString(3, "동두천시");
		psmt.executeUpdate();
		psmt.setInt(1, 420000); psmt.setString(2, "경기도");psmt.setString(3, "부천시");
		psmt.executeUpdate();
		psmt.setInt(1, 461000); psmt.setString(2, "경기도");psmt.setString(3, "성남시");
		psmt.executeUpdate();
		psmt.setInt(1, 440000); psmt.setString(2, "경기도");psmt.setString(3, "수원시");
		psmt.executeUpdate();
		psmt.setInt(1, 429000); psmt.setString(2, "경기도");psmt.setString(3, "시흥시");
		psmt.executeUpdate();
		psmt.setInt(1, 425000); psmt.setString(2, "경기도");psmt.setString(3, "안산시");
		psmt.executeUpdate();
		psmt.setInt(1, 430000); psmt.setString(2, "경기도");psmt.setString(3, "안양시");
		psmt.executeUpdate();
		psmt.setInt(1, 482000); psmt.setString(2, "경기도");psmt.setString(3, "양주시");
		psmt.executeUpdate();
		psmt.setInt(1, 476000); psmt.setString(2, "경기도");psmt.setString(3, "양평군");
		psmt.executeUpdate();
		psmt.setInt(1, 486000); psmt.setString(2, "경기도");psmt.setString(3, "연천군");
		psmt.executeUpdate();
		psmt.setInt(1, 447000); psmt.setString(2, "경기도");psmt.setString(3, "오산시");
		psmt.executeUpdate();
		psmt.setInt(1, 449000); psmt.setString(2, "경기도");psmt.setString(3, "용인시");
		psmt.executeUpdate();
		psmt.setInt(1, 437000); psmt.setString(2, "경기도");psmt.setString(3, "의왕시");
		psmt.executeUpdate();
		psmt.setInt(1, 480000); psmt.setString(2, "경기도");psmt.setString(3, "의정부시");
		psmt.executeUpdate();
		psmt.setInt(1, 467000); psmt.setString(2, "경기도");psmt.setString(3, "이천시");
		psmt.executeUpdate();
		psmt.setInt(1, 413000); psmt.setString(2, "경기도");psmt.setString(3, "파주시");
		psmt.executeUpdate();
		psmt.setInt(1, 451000); psmt.setString(2, "경기도");psmt.setString(3, "평택시");
		psmt.executeUpdate();
		psmt.setInt(1, 487000); psmt.setString(2, "경기도");psmt.setString(3, "포천시");
		psmt.executeUpdate();
		psmt.setInt(1, 465000); psmt.setString(2, "경기도");psmt.setString(3, "하남시");
		psmt.executeUpdate();
		psmt.setInt(1, 445000); psmt.setString(2, "경기도");psmt.setString(3, "화성시");
		psmt.executeUpdate();
		psmt.setInt(1, 456000); psmt.setString(2, "경기도");psmt.setString(3, "안성시");
		psmt.executeUpdate();
		psmt.setInt(1, 469000); psmt.setString(2, "경기도");psmt.setString(3, "여주시");
		psmt.executeUpdate();
		//강원도 O
		psmt.setInt(1, 210000); psmt.setString(2, "강원도");psmt.setString(3, "강릉시");
		psmt.executeUpdate();
		psmt.setInt(1, 219000); psmt.setString(2, "강원도");psmt.setString(3, "고성군");
		psmt.executeUpdate();
		psmt.setInt(1, 240000); psmt.setString(2, "강원도");psmt.setString(3, "동해시");
		psmt.executeUpdate();
		psmt.setInt(1, 245000); psmt.setString(2, "강원도");psmt.setString(3, "삼척시");
		psmt.executeUpdate();
		psmt.setInt(1, 255000); psmt.setString(2, "강원도");psmt.setString(3, "양구군");
		psmt.executeUpdate();
		psmt.setInt(1, 230000); psmt.setString(2, "강원도");psmt.setString(3, "영월군");
		psmt.executeUpdate();
		psmt.setInt(1, 233000); psmt.setString(2, "강원도");psmt.setString(3, "정선군");
		psmt.executeUpdate();
		psmt.setInt(1, 269000); psmt.setString(2, "강원도");psmt.setString(3, "철원군");
		psmt.executeUpdate();
		psmt.setInt(1, 232000); psmt.setString(2, "강원도");psmt.setString(3, "평창군");
		psmt.executeUpdate();
		psmt.setInt(1, 209000); psmt.setString(2, "강원도");psmt.setString(3, "화천군");
		psmt.executeUpdate();
		psmt.setInt(1, 225000); psmt.setString(2, "강원도");psmt.setString(3, "횡성군");
		psmt.executeUpdate();
		psmt.setInt(1, 250000); psmt.setString(2, "강원도");psmt.setString(3, "홍천군");
		psmt.executeUpdate();
		psmt.setInt(1, 217000); psmt.setString(2, "강원도");psmt.setString(3, "속초시");
		psmt.executeUpdate();
		psmt.setInt(1, 200000); psmt.setString(2, "강원도");psmt.setString(3, "춘천시");
		psmt.executeUpdate();
		psmt.setInt(1, 252000); psmt.setString(2, "강원도");psmt.setString(3, "인제군");
		psmt.executeUpdate();
		psmt.setInt(1, 220000); psmt.setString(2, "강원도");psmt.setString(3, "원주시");
		psmt.executeUpdate();
		psmt.setInt(1, 215000); psmt.setString(2, "강원도");psmt.setString(3, "양양군");
		psmt.executeUpdate();
		//충청북도 O
		psmt.setInt(1, 395000); psmt.setString(2, "충청북도");psmt.setString(3, "단양군");
		psmt.executeUpdate();
		psmt.setInt(1, 380000); psmt.setString(2, "충청북도");psmt.setString(3, "충주시");
		psmt.executeUpdate();
		psmt.setInt(1, 360000); psmt.setString(2, "충청북도");psmt.setString(3, "청주시");
		psmt.executeUpdate();
		psmt.setInt(1, 368000); psmt.setString(2, "충청북도");psmt.setString(3, "증평군");
		psmt.executeUpdate();
		psmt.setInt(1, 376000); psmt.setString(2, "충청북도");psmt.setString(3, "보은군");
		psmt.executeUpdate();
		psmt.setInt(1, 369000); psmt.setString(2, "충청북도");psmt.setString(3, "음성군");
		psmt.executeUpdate();
		psmt.setInt(1, 390000); psmt.setString(2, "충청북도");psmt.setString(3, "제천시");
		psmt.executeUpdate();
		psmt.setInt(1, 367000); psmt.setString(2, "충청북도");psmt.setString(3, "괴산군");
		psmt.executeUpdate();
		psmt.setInt(1, 370000); psmt.setString(2, "충청북도");psmt.setString(3, "영동군");
		psmt.executeUpdate();
		psmt.setInt(1, 373000); psmt.setString(2, "충청북도");psmt.setString(3, "옥천군");
		psmt.executeUpdate();
		psmt.setInt(1, 365000); psmt.setString(2, "충청북도");psmt.setString(3, "진천군");
		psmt.executeUpdate();
		//충청남도 O
		psmt.setInt(1, 321000); psmt.setString(2, "충청남도");psmt.setString(3, "계룡시");
		psmt.executeUpdate();
		psmt.setInt(1, 314000); psmt.setString(2, "충청남도");psmt.setString(3, "공주시");
		psmt.executeUpdate();
		psmt.setInt(1, 312000); psmt.setString(2, "충청남도");psmt.setString(3, "금산군");
		psmt.executeUpdate();
		psmt.setInt(1, 320000); psmt.setString(2, "충청남도");psmt.setString(3, "논산시");
		psmt.executeUpdate();
		psmt.setInt(1, 343000); psmt.setString(2, "충청남도");psmt.setString(3, "당진군");
		psmt.executeUpdate();
		psmt.setInt(1, 355000); psmt.setString(2, "충청남도");psmt.setString(3, "보령시");
		psmt.executeUpdate();
		psmt.setInt(1, 323000); psmt.setString(2, "충청남도");psmt.setString(3, "부여군");
		psmt.executeUpdate();
		psmt.setInt(1, 356000); psmt.setString(2, "충청남도");psmt.setString(3, "서산시");
		psmt.executeUpdate();
		psmt.setInt(1, 325000); psmt.setString(2, "충청남도");psmt.setString(3, "서천군");
		psmt.executeUpdate();
		psmt.setInt(1, 336000); psmt.setString(2, "충청남도");psmt.setString(3, "아산시");
		psmt.executeUpdate();
		psmt.setInt(1, 340000); psmt.setString(2, "충청남도");psmt.setString(3, "예산군");
		psmt.executeUpdate();
		psmt.setInt(1, 330000); psmt.setString(2, "충청남도");psmt.setString(3, "천안시");
		psmt.executeUpdate();
		psmt.setInt(1, 345000); psmt.setString(2, "충청남도");psmt.setString(3, "청양군");
		psmt.executeUpdate();
		psmt.setInt(1, 357000); psmt.setString(2, "충청남도");psmt.setString(3, "태안군");
		psmt.executeUpdate();
		psmt.setInt(1, 350000); psmt.setString(2, "충청남도");psmt.setString(3, "홍성군");
		psmt.executeUpdate();
		//전라북도 O
		psmt.setInt(1, 576000); psmt.setString(2, "전라북도");psmt.setString(3, "김제시");
		psmt.executeUpdate();
		psmt.setInt(1, 580000); psmt.setString(2, "전라북도");psmt.setString(3, "정읍시");
		psmt.executeUpdate();
		psmt.setInt(1, 585000); psmt.setString(2, "전라북도");psmt.setString(3, "고창군");
		psmt.executeUpdate();
		psmt.setInt(1, 565000); psmt.setString(2, "전라북도");psmt.setString(3, "완주군");
		psmt.executeUpdate();
		psmt.setInt(1, 573000); psmt.setString(2, "전라북도");psmt.setString(3, "군산시");
		psmt.executeUpdate(); 
		psmt.setInt(1, 590000); psmt.setString(2, "전라북도");psmt.setString(3, "남원시");
		psmt.executeUpdate();
		psmt.setInt(1, 568000); psmt.setString(2, "전라북도");psmt.setString(3, "무주군");
		psmt.executeUpdate();
		psmt.setInt(1, 579000); psmt.setString(2, "전라북도");psmt.setString(3, "부안군");
		psmt.executeUpdate();
		psmt.setInt(1, 595000); psmt.setString(2, "전라북도");psmt.setString(3, "순창군");
		psmt.executeUpdate();
		psmt.setInt(1, 570000); psmt.setString(2, "전라북도");psmt.setString(3, "익산시");
		psmt.executeUpdate();
		psmt.setInt(1, 566000); psmt.setString(2, "전라북도");psmt.setString(3, "임실군");
		psmt.executeUpdate();
		psmt.setInt(1, 597000); psmt.setString(2, "전라북도");psmt.setString(3, "장수군");
		psmt.executeUpdate();
		psmt.setInt(1, 560000); psmt.setString(2, "전라북도");psmt.setString(3, "전주시");
		psmt.executeUpdate();
		psmt.setInt(1, 567000); psmt.setString(2, "전라북도");psmt.setString(3, "진안군");
		psmt.executeUpdate();
		//전라남도 O
		psmt.setInt(1, 529000); psmt.setString(2, "전라남도");psmt.setString(3, "장흥군");
		psmt.executeUpdate();
		psmt.setInt(1, 527000); psmt.setString(2, "전라남도");psmt.setString(3, "강진군");
		psmt.executeUpdate();
		psmt.setInt(1, 548000); psmt.setString(2, "전라남도");psmt.setString(3, "고흥군");
		psmt.executeUpdate();
		psmt.setInt(1, 516000); psmt.setString(2, "전라남도");psmt.setString(3, "곡성군");
		psmt.executeUpdate();
		psmt.setInt(1, 545000); psmt.setString(2, "전라남도");psmt.setString(3, "광양시");
		psmt.executeUpdate();
		psmt.setInt(1, 542000); psmt.setString(2, "전라남도");psmt.setString(3, "구례군");
		psmt.executeUpdate();
		psmt.setInt(1, 520000); psmt.setString(2, "전라남도");psmt.setString(3, "나주시");
		psmt.executeUpdate();
		psmt.setInt(1, 517000); psmt.setString(2, "전라남도");psmt.setString(3, "담양군");
		psmt.executeUpdate();
		psmt.setInt(1, 530000); psmt.setString(2, "전라남도");psmt.setString(3, "목포시");
		psmt.executeUpdate();
		psmt.setInt(1, 534000); psmt.setString(2, "전라남도");psmt.setString(3, "무안군");
		psmt.executeUpdate();
		psmt.setInt(1, 546000); psmt.setString(2, "전라남도");psmt.setString(3, "보성군");
		psmt.executeUpdate();
		psmt.setInt(1, 540000); psmt.setString(2, "전라남도");psmt.setString(3, "순천시");
		psmt.executeUpdate();
		psmt.setInt(1, 535000); psmt.setString(2, "전라남도");psmt.setString(3, "신안군");
		psmt.executeUpdate();
		psmt.setInt(1, 550000); psmt.setString(2, "전라남도");psmt.setString(3, "여수시");
		psmt.executeUpdate();
		psmt.setInt(1, 513000); psmt.setString(2, "전라남도");psmt.setString(3, "영광군");
		psmt.executeUpdate();
		psmt.setInt(1, 526000); psmt.setString(2, "전라남도");psmt.setString(3, "영암군");
		psmt.executeUpdate();
		psmt.setInt(1, 537000); psmt.setString(2, "전라남도");psmt.setString(3, "완도군");
		psmt.executeUpdate();
		psmt.setInt(1, 515000); psmt.setString(2, "전라남도");psmt.setString(3, "장성군");
		psmt.executeUpdate();
		psmt.setInt(1, 539000); psmt.setString(2, "전라남도");psmt.setString(3, "진도군");
		psmt.executeUpdate();
		psmt.setInt(1, 525000); psmt.setString(2, "전라남도");psmt.setString(3, "함평군");
		psmt.executeUpdate();
		psmt.setInt(1, 536000); psmt.setString(2, "전라남도");psmt.setString(3, "해남군");
		psmt.executeUpdate();
		psmt.setInt(1, 519000); psmt.setString(2, "전라남도");psmt.setString(3, "화순군");
		psmt.executeUpdate();
		//경상북도 O
		psmt.setInt(1, 719000); psmt.setString(2, "경상북도");psmt.setString(3, "성주군");
		psmt.executeUpdate();
		psmt.setInt(1, 718000); psmt.setString(2, "경상북도");psmt.setString(3, "칠곡군");
		psmt.executeUpdate();
		psmt.setInt(1, 716000); psmt.setString(2, "경상북도");psmt.setString(3, "군위군");
		psmt.executeUpdate();
		psmt.setInt(1, 780000); psmt.setString(2, "경상북도");psmt.setString(3, "경주시");
		psmt.executeUpdate();
		psmt.setInt(1, 757000); psmt.setString(2, "경상북도");psmt.setString(3, "예천군");
		psmt.executeUpdate();
		psmt.setInt(1, 764000); psmt.setString(2, "경상북도");psmt.setString(3, "영양군");
		psmt.executeUpdate();
		psmt.setInt(1, 790000); psmt.setString(2, "경상북도");psmt.setString(3, "포항시");
		psmt.executeUpdate();
		psmt.setInt(1, 717000); psmt.setString(2, "경상북도");psmt.setString(3, "고령군");
		psmt.executeUpdate();
		psmt.setInt(1, 740000); psmt.setString(2, "경상북도");psmt.setString(3, "김천시");
		psmt.executeUpdate();
		psmt.setInt(1, 763000); psmt.setString(2, "경상북도");psmt.setString(3, "청송군");
		psmt.executeUpdate();
		psmt.setInt(1, 742000); psmt.setString(2, "경상북도");psmt.setString(3, "상주시");
		psmt.executeUpdate();
		psmt.setInt(1, 730000); psmt.setString(2, "경상북도");psmt.setString(3, "구미시");
		psmt.executeUpdate();
		psmt.setInt(1, 712000); psmt.setString(2, "경상북도");psmt.setString(3, "경산시");
		psmt.executeUpdate();
		psmt.setInt(1, 745000); psmt.setString(2, "경상북도");psmt.setString(3, "문경시");
		psmt.executeUpdate();
		psmt.setInt(1, 760000); psmt.setString(2, "경상북도");psmt.setString(3, "안동시");
		psmt.executeUpdate();
		psmt.setInt(1, 766000); psmt.setString(2, "경상북도");psmt.setString(3, "영덕군");
		psmt.executeUpdate();
		psmt.setInt(1, 750000); psmt.setString(2, "경상북도");psmt.setString(3, "영주시");
		psmt.executeUpdate();
		psmt.setInt(1, 770000); psmt.setString(2, "경상북도");psmt.setString(3, "영천시");
		psmt.executeUpdate();
		psmt.setInt(1, 799000); psmt.setString(2, "경상북도");psmt.setString(3, "울릉군");
		psmt.executeUpdate();
		psmt.setInt(1, 767000); psmt.setString(2, "경상북도");psmt.setString(3, "울진군");
		psmt.executeUpdate();
		psmt.setInt(1, 714000); psmt.setString(2, "경상북도");psmt.setString(3, "청도군");
		psmt.executeUpdate();
		psmt.setInt(1, 769000); psmt.setString(2, "경상북도");psmt.setString(3, "의성군");
		psmt.executeUpdate();
		psmt.setInt(1, 755000); psmt.setString(2, "경상북도");psmt.setString(3, "봉화군");
		psmt.executeUpdate();
		//경상남도 O
		psmt.setInt(1, 641000); psmt.setString(2, "경상남도");psmt.setString(3, "창원시");
		psmt.executeUpdate();
		psmt.setInt(1, 627000); psmt.setString(2, "경상남도");psmt.setString(3, "밀양시");
		psmt.executeUpdate();
		psmt.setInt(1, 621000); psmt.setString(2, "경상남도");psmt.setString(3, "김해시");
		psmt.executeUpdate();
		psmt.setInt(1, 656000); psmt.setString(2, "경상남도");psmt.setString(3, "거제시");
		psmt.executeUpdate();
		psmt.setInt(1, 637000); psmt.setString(2, "경상남도");psmt.setString(3, "함안군");
		psmt.executeUpdate();
		psmt.setInt(1, 626000); psmt.setString(2, "경상남도");psmt.setString(3, "양산시");
		psmt.executeUpdate();
		psmt.setInt(1, 638000); psmt.setString(2, "경상남도");psmt.setString(3, "고성군");
		psmt.executeUpdate();
		psmt.setInt(1, 668000); psmt.setString(2, "경상남도");psmt.setString(3, "남해군");
		psmt.executeUpdate();
		psmt.setInt(1, 664000); psmt.setString(2, "경상남도");psmt.setString(3, "사천시");
		psmt.executeUpdate();
		psmt.setInt(1, 666000); psmt.setString(2, "경상남도");psmt.setString(3, "산청군");
		psmt.executeUpdate();
		psmt.setInt(1, 636000); psmt.setString(2, "경상남도");psmt.setString(3, "의령군");
		psmt.executeUpdate();
		psmt.setInt(1, 660000); psmt.setString(2, "경상남도");psmt.setString(3, "진주시");
		psmt.executeUpdate();
		psmt.setInt(1, 650000); psmt.setString(2, "경상남도");psmt.setString(3, "통영시");
		psmt.executeUpdate();
		psmt.setInt(1, 667000); psmt.setString(2, "경상남도");psmt.setString(3, "하동군");
		psmt.executeUpdate();
		psmt.setInt(1, 678000); psmt.setString(2, "경상남도");psmt.setString(3, "합천군");
		psmt.executeUpdate();
		psmt.setInt(1, 676000); psmt.setString(2, "경상남도");psmt.setString(3, "함양군");
		psmt.executeUpdate();
		//제주도 O
		psmt.setInt(1, 690000); psmt.setString(2, "제주특별자치도");psmt.setString(3, "제주시");
		psmt.executeUpdate();
		psmt.setInt(1, 697000); psmt.setString(2, "제주특별자치도");psmt.setString(3, "서귀포시");
		psmt.executeUpdate();
		//
		psmt.setInt(1, 908000); psmt.setString(2, "세종특별자시치");psmt.setString(3, "세종시");
		psmt.executeUpdate();*/
		
		
		System.out.println("create Region");

		//지역화폐 //GMoney table create
        stmt.executeUpdate("create table GMoney (sidoName varchar(30), rName varchar(70))");

        //File csv = new File("C:\\Users\\우나영\\Desktop\\데이터베이스\\지역화폐가맹점현황\\지역화폐가맹점현황_final.csv"); //파일 경로 설정
        String path = System.getProperty("user.dir");
        
        File csv = new File(path+"/지역화폐가맹점현황.csv");
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
		
		stmt.executeUpdate("create view RelaxRegion as "
				+ "select sidoName,rName,isGMoney from RelaxRestaurant natural join Region;");
		
		stmt.executeUpdate("create or replace function test()\n"
				+ "returns trigger as $$\n"
				+ "begin\n"
				+ "update RelaxRestaurant\n"
				+ "set isGMoney='TRUE'\n"
				+ "from GMoney,Region\n"
				+ "where RelaxRestaurant.rName=GMoney.rName and Region.sidoName=GMoney.sidoName "
				+ "and isGMoney='FALSE';\n"
				+ "return null;\n"
				+ "end;					\n"
				+ "$$\n"
				+ "language 'plpgsql';");
		
		stmt.executeUpdate("create trigger RelaxRegionupdate\n"
				+ "instead of update on RelaxRegion\n"
				+ "for each row execute procedure test();");
		
		stmt.executeUpdate("update RelaxRegion set isGMoney='TRUE'\n"
				+ "from GMoney\n"
				+ "where RelaxRegion.rName = GMoney.rName and RelaxRegion.sidoName=GMoney.sidoName;");
		
		


        
        System.out.println("Table 생성 완료");

    }
}