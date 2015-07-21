package j1.lesson02;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class calculate_rel_div {
	private static double Cov_weight = 0.1;
	private static double Rel_weight = 0.7;
	//暫定　観点の差＊イベント関連度を用いて観点の差を定義
	
	//関連度
	public static double cal_rel(ArrayList<String> a_entities, ArrayList<String> ori_ents,ArrayList<String> cores, String file){
		System.out.println("ent"+ a_entities);
		double al = 0.1;
		double iv_data = cal_rel_ev(a_entities, cores);
		double com_data = cal_rel_com(a_entities, ori_ents);

		try {
			FileWriter fw = new FileWriter("/Users/admin/Documents/workspace/a_measure.clean/rel_result.csv", true);  //���P
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
			pw.print(file+",");
			while(al <0.91){
				double res = iv_data*al+com_data*(1-al);
				System.out.println("iv"+iv_data);
				System.out.println("com"+com_data);
				System.out.println("al="+al+"の関連度は"+ res +"です");

				pw.print(res+",");
				al+=0.1;
			}
			pw.print(","+iv_data);
			pw.print(","+com_data);
			pw.println();
			pw.close();

		} catch (IOException ex) {
			ex.printStackTrace();
			return 0;
		}

		return iv_data;
	}

	//イベント関連度
	public static double cal_rel_ev(ArrayList<String> a_entities, ArrayList<String> cores){
		int i = 0;
		double iv_data = 0;
		double regular_eve_rel = 0;
		//System.out.println("aaaaa"+a_entities);
		//System.out.println("ccccc"+cores);
		while(i < cores.size()){
			String core_fact = cores.get(i);
			if(a_entities.contains(core_fact)){
				//System.out.println("rcore:"+core_fact);
				iv_data++;

			}

			i++;
		}
		
		int core_size = cores.size();
		if(core_size != 0){
			//コアエンティティで割ることで正規化
			regular_eve_rel = iv_data / cores.size();
		}else{
			regular_eve_rel = 0;
		}
		System.out.println("@@@@commmmmm:"+regular_eve_rel);
		return regular_eve_rel;

	}

	//元記事関連度
	public static double cal_rel_com(ArrayList<String> a_entities, ArrayList<String> ori_ents){
		int p=0;
		//System.out.println("com_ent***"+a_entities+"ori"+ori_ents);
		double com_data = 0;
		while(p < a_entities.size()){
			if(ori_ents.contains(a_entities.get(p))){
				com_data++;
				//System.out.println("rori:"+a_entities.get(p));
			}
			p++;
		}
		
		//２記事の共通集合数を計算
		Set<String> com_list = new HashSet<String>();
		com_list.addAll(a_entities);
		com_list.addAll(ori_ents);
		
		System.out.println("@@@@commmmmm"+com_data+" com_list:"+com_list);
		System.out.println(com_data / com_list.size());
		return com_data / com_list.size();
	}

	//発散度
	public static double cal_div(ArrayList<String> a_entities, ArrayList<String> ori_entities, ArrayList<String> cores, String file_name){

		int i = 0;
		//最終結果
		double result = 0;
		double art_data = 0.0;//
		//重み
		double be = Cov_weight;
		double wc = 1;
		double w = 1;

		//元記事関連度
		//double relevance = cal_rel_com(a_entities, ori_entities);

				//関連記事のエンティティを全て読む
				while(i < a_entities.size()){
					String a_fact = a_entities.get(i);
					//元記事に含まれるか？
					if(ori_entities.contains(a_fact) == false){

						if(cores.contains(a_fact)){		//core entityならば
							art_data = art_data + wc;//System.out.println("wc");
						}else{
							art_data = art_data + w;//System.out.println("w");
						}
					}
					i++;
				}
				int p = 0;
				double ev_data = 0;

				result = art_data;//発散度を計算
				System.out.println("be="+ be + "発散度：" + result);

		return result;
	}

	public static Map<String, Double> start_div(String[] args, Map<String, Double> ev_relevance, Map<String,ArrayList<String>> entities, ArrayList<String> core_entities){

		String most_file = null;//最大の記事
		//出力データ
		Map<String, Double> div_scores = new HashMap<String, Double>();
//		Map<String, Double> div_scores2 = new HashMap<String, Double>();
		//元記事のNamed Entityのリスト
		ArrayList<String> ori_entities = entities.get(args[0]);

		//関連記事との観点の差を計算していく
		for (int i = 1; i < args.length; i++) {
			String file_name = args[i];
			ArrayList<String> a_ent = entities.get(file_name);
			//観点の差
			double next = cal_div(a_ent, ori_entities, core_entities, file_name);
			//イベント関連度を掛ける
			double rel = ev_relevance.get(file_name);
			div_scores.put(file_name, next*rel);
			//プレーン
			//div_scores2.put(file_name, next);
			System.out.println("ss:"+file_name);
		}
		//csvへ出力
		positive_score.print_score(div_scores, args, "coverage");
		import_newsDB.entry_measure(div_scores, "Coverages");
		//positive_score.print_score(div_scores2, args, "coverage");
		System.out.println("div_score 出力完了="+div_scores);

		return div_scores;

	}


	//正規化したイベント関連度をデータベースへ＋出力
	public static Map<String, Double> start_rel(String[] args, ArrayList<String> core_entities, Map<String,ArrayList<String>> entities){

		
		Map<String, Double> rel_scores = new HashMap<String, Double>();

		ArrayList<String> ori_entities = entities.get(args[0]);

		for (int i = 1; i < args.length; i++) {
			String file_name = args[i];
			double next = cal_rel_ev(entities.get(file_name),core_entities);
			rel_scores.put(file_name, next);
			System.out.println("ss:"+file_name);

		}

		import_newsDB.entry_rel(rel_scores);
		System.out.println("出力完了");
		return rel_scores;

	}
	public static void main(String[] args){
		ArrayList<String> list = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		list.add("a");
		list2.add("a");
		list.add("b");
		list2.add("c");
		list.add("d");
		list2.add("d");
		cal_rel_ev(list, list2);
		
	}

}