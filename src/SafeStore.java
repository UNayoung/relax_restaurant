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

        stmt.executeUpdate("create table RelaxRestaurant (rSeq int, rName varchar(30), zipcode int, category varchar(10), isGMoney bool)");
        String api = "http://211.237.50.150:7080/openapi/2584402a923e0249609a30aa68a450f4c22cf3d2a8af71ca5d1f0b32662c7af6/xml/Grid_20200713000000000605_1/1/1000";

        URL url = new URL(api);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        DocumentBuilderFactory dbFactoty = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactoty.newDocumentBuilder();
        Document doc = dBuilder.parse(api);

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("row");

        PreparedStatement psmt = con.prepareStatement("INSERT INTO RelaxRestaurant VALUES (?, ?, ?, ?, false)");

        for(int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if(nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;
                psmt.setInt(1, Integer.parseInt(getTagValue("RELAX_SEQ", eElement)));
                psmt.setString(2, getTagValue("RELAX_RSTRNT_NM", eElement));
                psmt.setInt(3, Integer.parseInt(getTagValue("RELAX_ZIPCODE", eElement)));
                psmt.setString(4, getTagValue("RELAX_GUBUN_DETAIL", eElement));
                psmt.executeUpdate();
            }	// for end
        }	// if end

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