package j1.lesson02;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import j1.lesson02.organize_entity;

public class calculate_detail {
	private static int topic_num = 20;
	private static double Topic_Weight = 0.9;
	private static double Entity_Weight = 0.3;

	//全ての記事の部分木を取り出す
	public static Map<String, Map<String,Map<Integer,String[]>>> make_t_e(String[] file_name, Map<String,ArrayList<String>> entities){
		int i = 0,p =0;

		//<Named Entity, <file名, <行数, subtree>>>:出力
		Map<String, Map<String,Map<Integer,String[]>>> ent_file_subtree = new HashMap<String, Map<String,Map<Integer,String[]>>>();

		ArrayList<String> all_ents = new ArrayList<String>();//１記事中のentity

		//記事中のentityを重複を取り除いて纏める
		while(i < file_name.length){
			//1記事中のentity
			ArrayList<String> file_ents = entities.get(file_name[i]);
			int ii=0;
			//重複をチェック
			while(ii < file_ents.size()){
				String ent = file_ents.get(ii);
				//all_entsになければ
				if(all_ents.contains(ent) == false){
					all_ents.add(ent);			
				}
				ii++;
			}
			i++;
		}
		System.out.println("all_'" +all_ents);

		//重複を取り除く

		//System.out.println("all_" +all_ents);
		//各Named Entityの部分木
		while(p < all_ents.size()){
			String csv_ent = all_ents.get(p);
			Map<String,Map<Integer,String[]>> result_sent = new HashMap<String,Map<Integer,String[]>>();

			int shiki =0;

			while(shiki < file_name.length){
				Map<Integer, String[]> orgs = organize_entity.get_subsentense("re_"+file_name[shiki], csv_ent, shiki);
				//System.out.println("orgs:"+orgs);
				result_sent.put(file_name[shiki],orgs);
				//entity.txtが製作されている

				shiki++;
			}
						//System.out.println("@@@@@@" +csv_ent);
						//System.out.println("make  "+result_sent);
			ent_file_subtree.put(csv_ent,result_sent);
						//System.out.println("****" +ent_file_subtree);
			p++;
		}
		System.out.println("out of while" +ent_file_subtree);
		return ent_file_subtree;


	}


	public static double cal_det(Map<Integer,Double> at_s,Map<Integer,Double> ot_s){

		double detail = 0;
		//0でよいかな？
		int ii = 0; //トピック番号
		while(ii < topic_num){
			if(at_s.containsKey(ii)){//System.out.println("ii:"+ii);
				double a_score = at_s.get(ii);
				if(ot_s.containsKey(ii)){//共通のtopicならば
					double o_score = ot_s.get(ii);
					if(a_score < o_score){//詳細度の計算
						detail = detail - Math.log(zettai((a_score-o_score)/(a_score+o_score+1))+1);
					}else{
						detail = detail + Math.log(zettai((a_score-o_score)/(a_score+o_score+1))+1);
					}

				}else{
					detail = detail + Math.log(zettai((a_score)/(a_score+1))+1);//System.out.println("ii:"+detail);
				}


			}else{
				if(ot_s.containsKey(ii)){
					double o_score = ot_s.get(ii);
					detail = detail - Math.log(zettai((o_score)/(o_score+1))+1);//System.out.println("ii:"+detail);
				}



			}




			ii++;

		}
		//System.out.println("詳細度："+detail);
		return detail;

	}


	public static Map<Integer,Double> at_score_get(Map<Integer,String[]> at_dist, Map<Integer,String[]> top_term, Map<Integer,String[]> at_words,double weight){
		Map<Integer,Double> at_s = new HashMap<Integer,Double>(); //トピック毎のatのスコア
		int s = 0;
		//System.out.println("++++++++++++++weight:"+weight);
		//各記述のトピックスコアを見ていく
		while(s < at_dist.size()){
			String[] temp_scores = at_dist.get(s);//ｓ行目のトピックスコア
			int ss = 0;//トピック番号
			while(ss < temp_scores.length){
				//注目記述のトピックss番目の確率
				double sco = Double.valueOf(temp_scores[ss]);
				//注目記述の各トピック確率が重みよりも大きければ
				if(sco > weight){//System.out.println("ss++"+ss +"s;;"+s);
					//トピックssのtop_term
					String[] ss_top = top_term.get(ss);//System.out.println("ss_top"+change_box(ss_top));
					//元々の記述
					ArrayList<String> s_words = change_box(at_words.get(s));//System.out.println("smoto"+s_moto);
					int c = 0;
					//System.out.println("c=0の時？？::" +ss_top[0]);
					double new_at = 0;
					while(c < ss_top.length){//top_termをいくつ含むかを計算
						if(s_words.contains(ss_top[c])){
							new_at++;
							//System.out.println("@@@@@"+ss_top[c]);
						}
						c++;
					}//System.out.println("ss "+ss);
					if(at_s.get(ss) != null){
						double at_before = at_s.get(ss);
						at_s.put(ss, at_before+new_at);//前のatのスコアと合計
					}else{
						at_s.put(ss, new_at);
					}

				}else{//重みより小さければ
					if(at_s.containsKey(ss) == false){//まだ入力されていないならば####if文を入れなければssのスコアがリセットされる
						at_s.put(ss, 0.0);
					}
				}
				ss++;

			}			

			//System.out.println("::::::::next_sentense:::::::");
			s++;
		}


		return at_s;
	}

	public static double zettai(double value){
		if(value < 0){
			return -value;
		}
		else{
			return value;
		}

	}

	private static ArrayList<String> change_box(String[] hairetsu){
		int o = 0;//System.out.println(hairetsu);
		ArrayList<String> are = new ArrayList<String>();
		while(o < hairetsu.length && hairetsu != null){
			are.add(hairetsu[o]);//System.out.println(hairetsu[o]);
			o++;
		}
		return are;
	}







	public static Map<Integer,String[]> get_csv1(String file){//トピック確率のcsvを取る
		try{
			File csv = new File(file);

			if (checkBeforeReadfile(csv)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				Map<Integer,String[]> file_facters = new HashMap<Integer,String[]>();//行番号,文字・数位置等
				int i = 0;
				while((str = br.readLine()) != null){
					String strlow = str.toLowerCase();//System.out.println(change_box(str.split(",")));
					file_facters.put(i,strlow.split(","));//System.out.println("box  "+change_box(file_facters.get(i)));
					i++;
				}
				br.close();
				//System.out.println("file:"+file_facters);
				return file_facters;

			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		return null;

	}

	public static Map<Integer,String[]> get_csv2(String file){
		try{
			File csv = new File(file);

			if (checkBeforeReadfile(csv)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				Map<Integer,String[]> file_facters = new HashMap<Integer,String[]>();
				int i = 0;
				while((str = br.readLine()) != null){
					String strlow = str.toLowerCase();//System.out.println(change_box(str.split(",")));
					file_facters.put(i,strlow.split(",| "));//System.out.println("box  "+change_box(file_facters.get(i)));
					i++;
				}
				br.close();
				//System.out.println("file:"+file_facters);
				return file_facters;

			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		return null;

	}


	private static boolean checkBeforeReadfile(File file){
		if (file.exists()){
			if (file.isFile() && file.canRead()){
				return true;
			}
		}

		return false;
	}	



	public static Map<String, Double> dif_detailedness(String[] args, Map<String, ArrayList<String>> all_entities, ArrayList<String> cores) throws Exception{
		
		String origin = args[0]; //元記事
		//綺麗に出力するためのマップ
		Map<String, Double> print_detailed = new HashMap<String, Double>();

		//detailednessの初期化
		

		//<Named Entity, <file名, <行数, subtree>>>
		//System.out.println("kaisi");
		Map<String, Map<String,Map<Integer,String[]>>> topic_data = make_t_e(args,all_entities);

		System.out.println("aaaaaaaaa"+topic_data);
		double topic_weight = Topic_Weight;
		//while(topic_weight < 0.91){
				
		double entity_weight = Entity_Weight;
		
		//while(entity_weight < 0.91){
			//詳細度の値（ファイル名, 詳細度）
			Map<String, Double> detailedness = new HashMap<String,Double>();
			
			detailedness.put(origin,0.0);

			int j = 1;
			while(j < args.length){
				detailedness.put(args[j],0.0);
				j+=1;//####２かもしれない
			}

			//初期化終了
			System.out.println("初期化しました。"+detailedness);
			
		Set<String> entities = topic_data.keySet();
		Iterator<String> it = entities.iterator();
		//Named Entityのiterator
		//entityの重み
		


		
			//Named Entityごとに詳細の差を計算
			while(it.hasNext()){	
				String named_entity = it.next();
				Map<String, Map<Integer,String[]>> file_topics = topic_data.get(named_entity); // <file名, <行数, subtree>>
				Set<String> file_names = file_topics.keySet();
				//System.out.println( "file_topics: " + named_entity +"+" + file_topics);
				Iterator<String> it_file = file_names.iterator();
				int[] tree_count = new int[500];//各fileのNamed Entityのsubtree数
				int total_subtree = 0;
				int t = 0;

				////各fileの各Named Entityのsubtree数をゲット
				while(it_file.hasNext()){
					String S_file = it_file.next();
					if (file_topics.get(S_file) == null){
						tree_count[t] = 0;
					}else{
						Set<Integer> num_trees = (file_topics.get(S_file)).keySet();
						//System.out.println("num_tree: "+num_trees);
						tree_count[t] = num_trees.size();


						total_subtree += tree_count[t];

					}
					t++;
				}
				if(total_subtree < args.length ){ //全ての記事のサブツリー数がファイル数以下ならトピック生成しない
					//System.out.println("finish::::::::?");
					continue;

				}else{

					String topic_file_name = named_entity + ".csv";
					//System.out.println("name:"+topic_file_name);
					Map<Integer,String[]> topic_words = TopicModel.topic_modeling(topic_file_name,named_entity,total_subtree);//各entityのtopic確率のcsvを製作
					//Map<Integer,String[]> topic_words = get_csv1(connecter_stan.TopicCsvFolder+named_entity+"_topic_words.csv");//各entityのトピック単語を取得
					Map<Integer,String[]> topic_scores = get_csv1(connecter_stan.TopicCsvFolder+"topic_"+named_entity+".csv"); //各行のトピック確率
					//print_map(topic_words);
					//print_map(topic_scores);
					Set<String> file_names2 = file_topics.keySet();//ファイル毎のサブツリー
					Iterator<String> it_file2 = file_names2.iterator();
					Map<String,Map<Integer,Double>> at_scores = new HashMap<String,Map<Integer,Double>>();//各行のatスコア
					int file_num = 0; //subtreeの通し番号
					int total_before = 0; //今まで読み込んだtopic数を保存


					//各ファイルのat_scoreを取り出し
					while(it_file2.hasNext()){
						String S2 = it_file2.next();
						Map<Integer,String[]> each_file_words = file_topics.get(S2);//各ファイルのサブツリー
						Map<Integer, String[]> each_file_topic = new HashMap<Integer,String[]>();//各ファイルのトピック確率＜ツリー, トピック確率＞
						int tree_num = 0;//file番号

						while(tree_num < tree_count[file_num]){//tree_countは各ファイルのsubtree数
							//System.out.println("num:"+tree_num+" count:"+tree_count[file_num]);

							//System.out.println("each topic::"+topic_scores.get(tree_num + tree_count[file_num-1]));
							each_file_topic.put(tree_num, topic_scores.get(tree_num + total_before)); //今まで読み込んだもの以降のトピック

							tree_num++;
						}

						//各ファイルのトピック確率を取得完了
						//print_map(each_file_topic);
						//System.out.println("++++++++++++++topic_weight:"+topic_weight);
						at_scores.put(S2, at_score_get(each_file_topic, topic_words, each_file_words,topic_weight));

						//file_num xの各トピックのat_scoreを格納
						total_before+=tree_count[file_num];
						file_num++;

					}
					//System.out.println(at_scores);
					
			
					
					Set<String> file_keys = at_scores.keySet();
					Iterator<String> it_file_at = file_keys.iterator();
					Map<Integer, Double> ori_score_each = at_scores.get(origin);//元記事のat_score

					//元記事のat_scoreをデータベースへ
					import_newsDB.import_topic_score(origin, ori_score_each, named_entity);
					
					//file毎に詳細の差を出す
					while(it_file_at.hasNext()){

						String file_S = it_file_at.next();
						if(file_S.equals(origin) == false){//元記事でなければ
							//System.out.print(file_S);
							Map<Integer, Double> at_score_each = at_scores.get(file_S);
							
							//トピックのスコア（at）をデータベースへ
							import_newsDB.import_topic_score(file_S, at_score_each, named_entity);
							
							//System.out.println(" +++ "+at_score_each);
							double detail_each = cal_det(at_score_each, ori_score_each);//file_Sの詳細の差を計算
							//entityで場合分け
							if(cores.contains(named_entity)){
								//System.out.println("c_weight:"+entity_weight);
								detailedness.put(file_S, detailedness.get(file_S)+(entity_weight*detail_each));//detailednessを更新
								//detailedness.put(file_S, detailedness.get(file_S)+(detail_each));
							}else{
								//System.out.println("weight:"+entity_weight);
								detailedness.put(file_S, detailedness.get(file_S)+((1-entity_weight)*detail_each));
								//detailedness.put(file_S, detailedness.get(file_S)+(detail_each));
							}
						}
					}

				}
			}

			//データベースに格納
			import_newsDB.entry_measure(detailedness, "details");
			System.out.println("詳細度:"+detailedness);
			//positive_score.print_score(detailedness, args, "detailedness");
			String most_file = null;

			//detailedness最大を取り出す
//			Set<String> keys = detailedness.keySet();
//			Iterator<String> itt = keys.iterator();
//			double max = 0;
//			while(itt.hasNext()){
//				String SS = itt.next();
//				if(SS.equals(origin)==false){
//					double each_det = detailedness.get(SS);
//					//keyをファイル名＋重みで表現
//					String print_key = SS + String.valueOf(entity_weight);
//					print_detailed.put(print_key, each_det);
//					if(max < each_det){
//						max = each_det;
//						most_file = SS;
//					}
//				}
//
//			}
			//csvへ出力
			System.out.println("weight:::::"+entity_weight);
			return detailedness;
			
			//entity_weight+=0.1;}
			
			//-----------重み計算用--------------

//		try {
//			FileWriter fw = new FileWriter("/Users/admin/Documents/workspace/a_measure.clean/ent_detailedness/ent_"+connecter_stan.event+"_result.csv", true);  //���P
//			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
//		
//			for (int id = 1; id < args.length; id++) {
//
//				String print_file  = args[id];
//				//各重み毎に取り出し出力
//				if(print_file.equals(origin) == false){
//				for (double i = 0.10; i < 0.91; i+=0.10) {
//					//keyを作成
//					String file_wei = print_file + String.valueOf(i);
//					Double score = print_detailed.get(file_wei);
//					pw.print(file_wei+","+score+",");
//				}
//				pw.println();
//				}
//
//			}
//			pw.println("topic="+topic_weight);
//			pw.close();
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		//topic_weight+=0.10;}//return most_file;
	}

	private static void print_map(Map<Integer,String[]> map_san){

		Set<Integer> keys = map_san.keySet();
		Iterator<Integer> it = keys.iterator();
		while(it.hasNext()){
			int i = it.next();
			String[] xx = map_san.get(i);
			int ii =0;
			while(ii < xx.length){
				System.out.print(" "+xx[ii]);
				ii++;
			}
			System.out.println();
		}

	}

}