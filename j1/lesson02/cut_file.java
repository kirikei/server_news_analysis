package j1.lesson02;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class cut_file {
	public static void combine_files(String ori, String a, String res){//元記事と関連記事の合成
		try{
			File ori_file = new File(connecter_stan.ArticleFolder + ori);	//元記事
			File a_file = new File(connecter_stan.ArticleFolder + a);		//関連記事
			File com_file = new File(connecter_stan.ArticleFolder + res);		//合成後の記事

			PrintWriter pw = new PrintWriter(new FileWriter(com_file));

			if (checkBeforeReadfile(ori_file) ){
				BufferedReader br = new BufferedReader(new FileReader(ori_file));

				String str;
				while((str = br.readLine()) != null){	
					pw.println(str);
				}
				br.close();
				pw.println();
			}
			if(checkBeforeReadfile(a_file)){
				BufferedReader br2 = new BufferedReader(new FileReader(a_file));

				String str2;
				while((str2 = br2.readLine()) != null){	
					pw.println(str2);
				}
				br2.close();      		  
			}
			pw.close();

		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}

	}

	public static boolean checkBeforeReadfile(File file){
		if (file.exists()){
			if (file.isFile() && file.canRead()){
				return true;
			}
		}
		System.out.println(file + " は見つかりませんでした。");
		return false;
	}

	public static void while_combine(String[] files){//上記メソッドをwhileで回す
		int i = 1;
		while(i < files.length){
			combine_files(files[0], files[i], files[0]+"_"+files[i]);
			i++;
		}
	}
	//$,|を除外する
	public static void replace_escape(String file){//元記事と関連記事の合成
		ArrayList<String> rows = new  ArrayList<String>();
		try{
			File replace_file = new File(connecter_stan.ArticleFolder + file);	//元記事
			//File result_file = new File("next.txt");

			if (checkBeforeReadfile(replace_file) ){
				BufferedReader br = new BufferedReader(new FileReader(replace_file));	      
				String str;
				while((str = br.readLine()) != null){
					str = str.replace("|-", "bar-");
					str = str.replace("$-", "dol-");
					str = str.replace("/", "_");
					str = str.replace("'", "_");//SQLに対応するため
					str = str.replace("\t", " ");//タブによるバグを防ぐ
					str = str.replace("*-", "Ast-");
					rows.add(str);
				}
				br.close();
			}
			PrintWriter pw = new PrintWriter(new FileWriter(replace_file));
			int i = 0;
			while(i < rows.size()){
				pw.println(rows.get(i));
				i++;
			}
			System.out.println("完了しました");
			pw.close();

		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}

	}
	//replace_escapeを全てに適用
	public static void all_replace_esc(String[] files){
		replace_escape("re_"+files[0]);
		int i = 1;
		while(i < files.length){
			replace_escape("re_"+files[i]);
			replace_escape("re_"+files[0]+"_"+files[i]);
			i++;
		}
	}

	
	public static void main(String[] args){
		replace_escape(args[0]);
	}


}
