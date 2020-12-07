import java.sql.*;
import java.util.Scanner;
import java.io.*;

public class App {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		//기능
		
				//String ur="jdbc:postgresql://127.0.0.1:5433/test";
				String ur="jdbc:postgresql://localhost/";

				String user="postgres";
				//String password="***********";
				String password="";
				Connection con = null;

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

				Scanner scan = new Scanner(System.in);

				System.out.println("안심식당 정보 제공 서비스");
				System.out.println("--------------------------------------");
				System.out.println("기능");
				System.out.println("1. 지역 및 음식점 종류별 안심식당 정보 탐색");
				System.out.println("2. 안심식당 검색");
				System.out.println("3. 상세 정보 제공 (+지역화폐 가맹점 유무)");
				int function_choice = scan.nextInt();
				if(function_choice == 1)
					try {
						
							Region region = new Region();
							region.main(con);
							
						
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else if (function_choice == 2) {
					try {
						
						RelaxRestaurant rstrnt=new RelaxRestaurant();
						rstrnt.main(con);
						
					
					} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
					
				}
				else if (function_choice == 3) {
				}
	}

}
