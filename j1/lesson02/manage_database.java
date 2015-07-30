package j1.lesson02;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class manage_database {

	public static void create_view(){
		try {
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(import_newsDB.Database_path,import_newsDB.UserName, import_newsDB.Pass); 
			System.out.println("create_view: "+"connected");
			Statement st = conn.createStatement();
			String exist_query = "select count(*) from pg_class where relname = 'current_news_views'";
			ResultSet rs = st.executeQuery(exist_query);
			rs.next();
			if(rs.getInt(1) == 0){
				rs.close();
				st.close();
				Statement st1 = conn.createStatement();

				String create_query = "create view current_news_views as " +
						"select aid, title, summary, link, media, image, category, pid, pubdate, analyzed_at" +
						" from newsarticles where analyzed_at between (select max(analyzed_at) from newsarticles)" +
						"+ '-1 day' and (select max(analyzed_at) from newsarticles)";
				st1.executeQuery(create_query);
				st1.close();
			}else{

				rs.close();
				st.close();
			}
			conn.close(); 


		} catch (ClassNotFoundException e) {
			System.out.println(e);

		} catch (SQLException e) { 
			System.out.println("sql:"+ e);
		}


	}


	public static void change_userscore(String now){
		try {
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(import_newsDB.Database_path, import_newsDB.UserName, import_newsDB.Pass); 
			System.out.println("delete_userscore: "+"connected");
			Statement st = conn.createStatement();
			
			String exist_query = "delete from user_scores where aid not in (select aid from current_news_views)";
			st.executeUpdate(exist_query);
			st.close();
			System.out.println("user_scores中：viewにない記事の消去 "+ now);
			
			//user_scoreへ解析したスコアを挿入
			String insert_query = "insert into user_scores(aid, uuid, link, pid, p_score, c_score, d_score) " +
					"select current_news_views.aid, uuid, link, current_news_views.pid, polarities.score, coverages.score, details.score" +
					" from uuid_tables, current_news_views, polarities, coverages, details " +
					"where current_news_views.aid = polarities.aid " +
					"and current_news_views.aid = coverages.aid " +
					"and current_news_views.aid = details.aid " +
					"and current_news_views.analyzed_at = (timestamp '"+now+"')";
			//時間の比較は文字列の比較でなく、timestamp型に直して比較
			
			Statement st1 = conn.createStatement();
			st1.executeUpdate(insert_query);
			System.out.println("SQL : "+insert_query);
			
			st1.close();
			conn.close();

		} catch (ClassNotFoundException e) {
			System.out.println(e);

		} catch (SQLException e) { 
			System.out.println("sql:"+ e);
		}


	}
	
	public static void delete_mini_art(){
		try {
			Class.forName("org.postgresql.Driver");
			Connection conn = DriverManager.getConnection(import_newsDB.Database_path, import_newsDB.UserName, import_newsDB.Pass); 
			System.out.println("delete_userscore: "+"connected");
			Statement st = conn.createStatement();
			
			String exist_query = "delete from newsarticles where length(text) < 300";
			st.executeUpdate(exist_query);
			st.close();
			conn.close();

		} catch (ClassNotFoundException e) {
			System.out.println(e);

		} catch (SQLException e) { 
			System.out.println("sql:"+ e);
		}


	}
	
	
	public static void main(String[] args){
		change_userscore("2015-07-01 11:54:54");
	}
}
