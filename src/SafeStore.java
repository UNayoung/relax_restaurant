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

//    	String ur="jdbc:postgresql://localhost/";
		String ur="jdbc:postgresql://127.0.0.1:5433/test";

    	String user="postgres";
//    	String password="***********";
    	String password="1234";


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

					
				}	
			}
			
		}
		System.out.println("create RelaxRestaurant");
		System.out.println("create RestaurantDetail");



		NodeList nList = doc.getElementsByTagName("row");

		PreparedStatement psmt = con.prepareStatement("INSERT INTO Region VALUES (?, ?, ?)");
		psmt.setInt(1, 395000); psmt.setString(2, "충청북도");psmt.setString(3, "단양군");
		psmt.executeUpdate();
		psmt.setInt(1, 380000); psmt.setString(2, "충청북도");psmt.setString(3, "충주시");
		psmt.executeUpdate();
		psmt.setInt(1, 576000); psmt.setString(2, "전라북도");psmt.setString(3, "김제시");
		psmt.executeUpdate();
		psmt.setInt(1, 580000); psmt.setString(2, "전라북도");psmt.setString(3, "정읍시");
		psmt.executeUpdate();
		psmt.setInt(1, 529000); psmt.setString(2, "전라남도");psmt.setString(3, "장흥군");
		psmt.executeUpdate();
		psmt.setInt(1, 406000); psmt.setString(2, "인천광역시");psmt.setString(3, "연수구");
		psmt.executeUpdate();
		psmt.setInt(1, 403000); psmt.setString(2, "인천광역시");psmt.setString(3, "부평구");
		psmt.executeUpdate();
		psmt.setInt(1, 405000); psmt.setString(2, "인천광역시");psmt.setString(3, "남동구");
		psmt.executeUpdate();
		psmt.setInt(1, 689000); psmt.setString(2, "울산광역시");psmt.setString(3, "울주군");
		psmt.executeUpdate();
		psmt.setInt(1, 908000); psmt.setString(2, "세종특별자시치");psmt.setString(3, "세종시");
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
		psmt.setInt(1, 609000); psmt.setString(2, "부산광역시");psmt.setString(3, "금정구");
		psmt.executeUpdate();
		psmt.setInt(1, 606000); psmt.setString(2, "부산광역시");psmt.setString(3, "영도구");
		psmt.executeUpdate();
		psmt.setInt(1, 711000); psmt.setString(2, "대구광역시");psmt.setString(3, "달성군");
		psmt.executeUpdate();
		psmt.setInt(1, 706000); psmt.setString(2, "대구광역시");psmt.setString(3, "수성구");
		psmt.executeUpdate();
		psmt.setInt(1, 501000); psmt.setString(2, "광주광역시");psmt.setString(3, "동구");
		psmt.executeUpdate();
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
		psmt.setInt(1, 641000); psmt.setString(2, "경상남도");psmt.setString(3, "창원시");
		psmt.executeUpdate();
		psmt.setInt(1, 627000); psmt.setString(2, "경상남도");psmt.setString(3, "밀양시");
		psmt.executeUpdate();
		psmt.setInt(1, 621000); psmt.setString(2, "경상남도");psmt.setString(3, "김해시");
		psmt.executeUpdate();
		psmt.setInt(1, 656000); psmt.setString(2, "경상남도");psmt.setString(3, "거제시");
		psmt.executeUpdate();
		psmt.setInt(1, 413000); psmt.setString(2, "경기도");psmt.setString(3, "파주시");
		psmt.executeUpdate();
		psmt.setInt(1, 467000); psmt.setString(2, "경기도");psmt.setString(3, "이천시");
		psmt.executeUpdate();
		psmt.setInt(1, 480000); psmt.setString(2, "경기도");psmt.setString(3, "의정부시");
		psmt.executeUpdate();
		psmt.setInt(1, 482000); psmt.setString(2, "경기도");psmt.setString(3, "양주시");
		psmt.executeUpdate();
		psmt.setInt(1, 425000); psmt.setString(2, "경기도");psmt.setString(3, "안산시");
		psmt.executeUpdate();
		psmt.setInt(1, 440000); psmt.setString(2, "경기도");psmt.setString(3, "수원시");
		psmt.executeUpdate();
		psmt.setInt(1, 415000); psmt.setString(2, "경기도");psmt.setString(3, "김포시");
		psmt.executeUpdate();
		psmt.setInt(1, 423000); psmt.setString(2, "경기도");psmt.setString(3, "광명시");
		psmt.executeUpdate();
		psmt.setInt(1, 210000); psmt.setString(2, "강원도");psmt.setString(3, "강릉시");
		psmt.executeUpdate();
		psmt.setInt(1, 250000); psmt.setString(2, "강원도");psmt.setString(3, "홍천군");
		psmt.executeUpdate();
		psmt.setInt(1, 217000); psmt.setString(2, "강원도");psmt.setString(3, "속초시");
		psmt.executeUpdate();
		psmt.setInt(1, 200000); psmt.setString(2, "강원도");psmt.setString(3, "춘천시");
		psmt.executeUpdate();
		psmt.setInt(1, 360000); psmt.setString(2, "충청북도");psmt.setString(3, "청주시");
		psmt.executeUpdate();
		psmt.setInt(1, 368000); psmt.setString(2, "충청북도");psmt.setString(3, "증평군");
		psmt.executeUpdate();
		psmt.setInt(1, 585000); psmt.setString(2, "전라북도");psmt.setString(3, "고창군");
		psmt.executeUpdate();
		psmt.setInt(1, 565000); psmt.setString(2, "전라북도");psmt.setString(3, "완주군");
		psmt.executeUpdate();
		psmt.setInt(1, 905000); psmt.setString(2, "인천광역시");psmt.setString(3, "미추홀구");
		psmt.executeUpdate();
		psmt.setInt(1, 680000); psmt.setString(2, "울산광역시");psmt.setString(3, "남구");
		psmt.executeUpdate();
		psmt.setInt(1, 110000); psmt.setString(2, "서울특별시");psmt.setString(3, "종로구");
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
		psmt.setInt(1, 301000); psmt.setString(2, "대전광역시");psmt.setString(3, "중구");
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
		psmt.setInt(1, 506000); psmt.setString(2, "광주광역시");psmt.setString(3, "광산구");
		psmt.executeUpdate();
		psmt.setInt(1, 502000); psmt.setString(2, "광주광역시");psmt.setString(3, "서구");
		psmt.executeUpdate();
		psmt.setInt(1, 500000); psmt.setString(2, "광주광역시");psmt.setString(3, "북구");
		psmt.executeUpdate();
		psmt.setInt(1, 503000); psmt.setString(2, "광주광역시");psmt.setString(3, "남구");
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
		psmt.setInt(1, 637000); psmt.setString(2, "경상남도");psmt.setString(3, "함안군");
		psmt.executeUpdate();
		psmt.setInt(1, 626000); psmt.setString(2, "경상남도");psmt.setString(3, "양산시");
		psmt.executeUpdate();
		psmt.setInt(1, 420000); psmt.setString(2, "경기도");psmt.setString(3, "부천시");
		psmt.executeUpdate();
		psmt.setInt(1, 464000); psmt.setString(2, "경기도");psmt.setString(3, "광주시");
		psmt.executeUpdate();
		psmt.setInt(1, 447000); psmt.setString(2, "경기도");psmt.setString(3, "오산시");
		psmt.executeUpdate();
		psmt.setInt(1, 456000); psmt.setString(2, "경기도");psmt.setString(3, "안성시");
		psmt.executeUpdate();
		psmt.setInt(1, 476000); psmt.setString(2, "경기도");psmt.setString(3, "양평군");
		psmt.executeUpdate();
		psmt.setInt(1, 477000); psmt.setString(2, "경기도");psmt.setString(3, "가평군");
		psmt.executeUpdate();
		psmt.setInt(1, 487000); psmt.setString(2, "경기도");psmt.setString(3, "포천시");
		psmt.executeUpdate();
		psmt.setInt(1, 411000); psmt.setString(2, "경기도");psmt.setString(3, "고양시");
		psmt.executeUpdate(); //85개
		psmt.setInt(1, 252000); psmt.setString(2, "강원도");psmt.setString(3, "인제군");
		psmt.executeUpdate();
		psmt.setInt(1, 220000); psmt.setString(2, "강원도");psmt.setString(3, "원주시");
		psmt.executeUpdate();
		psmt.setInt(1, 215000); psmt.setString(2, "강원도");psmt.setString(3, "양양군");
		psmt.executeUpdate();
		psmt.setInt(1, 445000); psmt.setString(2, "경기도");psmt.setString(3, "화성시");
		psmt.executeUpdate();
		psmt.setInt(1, 471000); psmt.setString(2, "경기도");psmt.setString(3, "구리시");
		psmt.executeUpdate();
		psmt.setInt(1, 451000); psmt.setString(2, "경기도");psmt.setString(3, "평택시");
		psmt.executeUpdate();
		psmt.setInt(1, 437000); psmt.setString(2, "경기도");psmt.setString(3, "의왕시");
		psmt.executeUpdate();
		psmt.setInt(1, 469000); psmt.setString(2, "경기도");psmt.setString(3, "여주시");
		psmt.executeUpdate();
		psmt.setInt(1, 430000); psmt.setString(2, "경기도");psmt.setString(3, "안양시");
		psmt.executeUpdate();
		psmt.setInt(1, 429000); psmt.setString(2, "경기도");psmt.setString(3, "시흥시");
		psmt.executeUpdate(); //95개
		psmt.setInt(1, 472000); psmt.setString(2, "경기도");psmt.setString(3, "남양주시");
		psmt.executeUpdate();
		psmt.setInt(1, 435000); psmt.setString(2, "경기도");psmt.setString(3, "군포시");
		psmt.executeUpdate();
		psmt.setInt(1, 376000); psmt.setString(2, "충청북도");psmt.setString(3, "보은군");
		psmt.executeUpdate();
		psmt.setInt(1, 369000); psmt.setString(2, "충청북도");psmt.setString(3, "음성군");
		psmt.executeUpdate();
		psmt.setInt(1, 390000); psmt.setString(2, "충청북도");psmt.setString(3, "제천시");
		psmt.executeUpdate(); //100개

		//Region DB 저장
		for(int temp = 0; temp < nList.getLength(); temp++){
			Node nNode = nList.item(temp);
			if(nNode.getNodeType() == Node.ELEMENT_NODE){

				Element eElement = (Element) nNode;

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
					psmt.setInt(1, Integer.parseInt(getTagValue("RELAX_ZIPCODE", eElement)));
					psmt.setString(2, getTagValue("RELAX_SI_NM", eElement));
					psmt.setString(3, getTagValue("RELAX_SIDO_NM", eElement));
					psmt.executeUpdate();
				}

			}
		}
		System.out.println("create Region");

        //지역화폐 //GMoney table create
        stmt.executeUpdate("create table GMoney (sidoName varchar(30), rName varchar(70))");

//        File csv = new File("C:\\Users\\우나영\\Desktop\\데이터베이스\\지역화폐가맹점현황\\지역화폐가맹점현황_final.csv"); //파일 경로 설정
        String path = System.getProperty("user.dir");
        
        File csv = new File(path+"/지역화폐가맹점현황.csv");
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