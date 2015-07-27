package j1.lesson02;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import j1.lesson02.tree_change;
import j1.lesson02.organize_entity.*;

public class positive_score {
	private static double entity_weight = 0.8;

	//センテンス毎にentityのツリーを取り出す＋entityの感情語スコアを取り出す
	public static Map<String,Double> positive(String file, ArrayList<String> entities){
		String file1 = "re_"+ file;
		//System.out.println("aaa");
		Map<Integer, ArrayList<String>> trees = organize_entity.get_tree(file1);//センテンス毎のツリー
		//System.out.println("trees::"+trees);
		Map<Integer, Map<String, Double>> wordscores = word_hit.POS_score_get(file1);//センテンス毎の感情語とスコア
		Map<Integer,ArrayList<String>> verbs = organize_entity.get_verb(file1);//センテンス毎の動詞
		Map<String,Double> entity_score = new HashMap<String,Double>();//entityの感情語スコア
		int sentense_num = 1;//1???

		//センテンス毎に回す(Sentence１からなので<=)
		while(sentense_num <= trees.size()){ 

			try{
				//センテンス毎の部分木,動詞,感情語を取り出す
				ArrayList<String> treekun = trees.get(sentense_num);
				ArrayList<String> focus_verb = verbs.get(sentense_num);
				//System.out.println("verb:"+focus_verb);
				ArrayList<String> focus_tree = tree_change.tree_c_m(treekun,focus_verb);//System.out.println("tree:"+focus_tree);
				Map<String,Double> focus_score = wordscores.get(sentense_num);//System.out.println("score:"+focus_score);
				ArrayList<String> senti_words = new ArrayList<String>();

				//センテンス中に感情語が含まれるなら
				if( focus_score != null && focus_score.size() > 0 ){
					Set<String> keys = focus_score.keySet();

					Iterator<String> it = keys.iterator();
					//senti_wordにセンテンスの全ての感情語を挿入
					while(it.hasNext()){
						String S = it.next();
						senti_words.add(S);
					}
					//System.out.println(senti_words);
					int i = 0;

					//senti_word１つずつを見ていく
					while(i< senti_words.size()){

						String s_word = senti_words.get(i);   //部分木中の感情語
						//System.out.println("s_word::"+s_word+" senti_words::"+senti_words);
						int ii = 0;int ent_num = -1;
						int index = organize_entity.check_in_list(focus_tree,s_word); //部分木中の感情語のインデックス
						//System.out.println("focus::"+focus_tree+",index::"+index);
						
							String start_node = focus_tree.get(index);  //感情語から開始
						
						int flag = 0; //２回連続で終点にたどり着いたら脱出する
						//感情語とentityの位置関係を検索
						while(true){
							String f_facter = tree_change.first_facter(start_node);
							//System.out.println("start_node:::"+f_facter);

							//動詞に行き着くか、ルートに行き着いたら=>感情語とのパス中にentityが現れなかったら
							if(organize_entity.check_in_list_b(focus_verb, f_facter) != -1 || f_facter.equals("ROOT-0"))
							{	//System.out.println("Start_node>>>>"+start_node);

								ArrayList<Integer> check_list = new ArrayList<Integer>();
								if(ii == 0){//ii=-1(見つからない場合)の代入を防ぐ
									check_list.add(0);}
								else{
									check_list.add(ii); //インデックスをcheck_listへ
								}
								ArrayList<String> sub_tree = organize_entity.find_dep(check_list, start_node, focus_tree);	
								//System.out.println("sub****" + sub_tree);
								int cou = 0;
								//entityを含むノード番号
								ArrayList<Integer> ents_in_tree = new ArrayList<Integer>();

								//sub_tree内のエンティティを含むノード番号をents_in_treeに格納
								while(sub_tree.size() > cou){
									String finder = tree_change.second_facter(sub_tree.get(cou));
									//entityが存在するなら
									if(organize_entity.check_in_list_b(entities,finder) != -1){
										ents_in_tree.add(cou);
									}

									cou++;
								}//System.out.println("ents_in_tree::: "+ents_in_tree);

								int c = 0;


								while(ents_in_tree.size() > c){//System.out.println("while��");
									Integer get_ent_num = ents_in_tree.get(c);

									//entityが含まれるノードをstartに代入, ここから親をさかのぼって検索
									String start = sub_tree.get(get_ent_num);
									int aa = 0;
									while(true){
										//順番に部分木を見ていく
										String mawa = sub_tree.get(aa);

										//現在見ているノードが終点に着いたら
										if(mawa == start_node){
											ent_num = organize_entity.check_in_list_b(entities,sub_tree.get(get_ent_num));
											String ent = entities.get(ent_num);//発見したentity
											Double score = focus_score.get(s_word);//部分木に含まれる感情語のスコア

											//既にentのスコアが記述されているとき
											if(entity_score.containsKey(ent)){
												//スコアを足し合わせて新たに格納
												Double new_score = entity_score.get(ent) + score;
												entity_score.put(ent, new_score);

												//entのスコアがまだ記述されていないとき
											}else{
												//そのまま格納
												entity_score.put(entities.get(ent_num),score);
											}
											break;	
										}
										//System.out.println("mawa++++++++++++"+mawa);
										if(tree_change.second_facter(mawa).equals(tree_change.first_facter(start))){
											start = mawa;//付け替え（親ノードへ）
											if(ents_in_tree.contains(aa)){//aa(現在見ているノード番号)が部分木内のentityならば
												System.out.println("親にエンティティが含まれます。");
												break;
											}

										}



										aa++;
										if(sub_tree.size() == aa){
											aa=0;
										}
									}


									c++;	
								}

								break;
							}

							//パス中にentityに遭遇したら
							//System.out.println("entities:"+entities);
							if((ent_num = organize_entity.check_in_list_b(entities,f_facter))!= -1){
								String ent = entities.get(ent_num);
								Double score = focus_score.get(s_word);

								if(entity_score.containsKey(ent)){//既にentのスコアが記述されている場合
									//合計したスコアを格納
									Double new_score = entity_score.get(ent) + score;
									entity_score.put(ent, new_score);
									//System.out.println("******in find entity_1 " + ent);

								}else{//ent記述されていない場合は
									entity_score.put(entities.get(ent_num),score);//System.out.println("%%%%%%"+start_node);
								}
								break;
							}
							//どの場合にも引っかからなかった場合
							String find_node = focus_tree.get(ii);//次のノードへ
							String s_f_facter = tree_change.second_facter(find_node);//次のノードの子と同じならば
							//System.out.println("sf--------->"+s_f_facter);
							if(s_f_facter.equals(f_facter)){
								//付け替える
								start_node = find_node;//System.out.println("ccc");
								flag = 0;
							}else{
								if(flag > focus_tree.size()){
									break;
								}
								flag++;
							}

							ii++;
							if(ii == focus_tree.size()){
								ii = 0;
							}
						}

						i++;
					}


				}
			}catch (Exception e){
				sentense_num++;	
				continue;
				
			}



			sentense_num++;	
		}

		System.out.println("entity_score:"+ entity_score);
		import_newsDB.import_positive_score(file, entity_score);
		return entity_score;


	}
	//記事間のentity毎の擁護度の差を計算
	public static double cal_pos(Map<String,Double> ori_score, String a_file,ArrayList<String> a_entities, ArrayList<String> cores){
		System.out.println("a_entities:"+a_entities);
		Map<String, Double> a_score = positive(a_file,a_entities);

		double w = entity_weight;
		//while(w < 0.91){
		double wori = 1-w;
		double sup = 0;
		double osups = 0;
		double asups = 0;
		Set<String> keys = a_score.keySet();
		Iterator<String> it = keys.iterator();

		//関連記事のpolarityの計算において
		while(it.hasNext()){
			String S = it.next();//System.out.println("S::"+ S);
			double a_sup = a_score.get(S);

			//元記事と共通するentityならば
			if(ori_score.containsKey(S)){
				double o_sup = ori_score.get(S);
				if(cores.contains(S)){//core entityかどうか？
					asups = asups + wori*calculate_detail.zettai(a_sup-o_sup);	//System.out.println("kara/wc::"+ sup);
				}else{
					asups = asups + w*calculate_detail.zettai(a_sup-o_sup);	//System.out.println("kara/w::"+ sup);
				}
			}
			else{
				if(cores.contains(S)){
					asups = asups + calculate_detail.zettai(wori*a_sup);	//System.out.println("kara/wc::"+ sup);
				}else{
					asups = asups + calculate_detail.zettai(w*a_sup);	//System.out.println("kara/w::"+ sup);
				}
			}
		}

		Set<String> o_keys = ori_score.keySet();	
		Iterator<String> o_it = o_keys.iterator();

		//元記事のpolarityの計算において
		while(o_it.hasNext()){
			String So = o_it.next();//System.out.println("S::"+ So);
			double o_sup2 = ori_score.get(So);

			//比較記事と共通するentityならば
			if(a_score.containsKey(So) == false){
				if(cores.contains(So)){//core entityに含まれるならば
					osups = osups + calculate_detail.zettai(wori*o_sup2);	//System.out.println("kara/wc::"+ sup);
				}else{
					osups = osups + calculate_detail.zettai(w*o_sup2);	//System.out.println("kara/w::"+ sup);
				}
			}
		}

		sup = asups+osups;

		System.out.println("w="+w+"での"+a_file+"のpolarityは"+ sup + "です。");
		//			try {
		//				//出力先の決定
		//				FileWriter fw = new FileWriter("/Users/admin/Documents/workspace/a_measure.clean/rel_experiment/pol_"+connecter_stan.event+"_result.csv", true);  //���P
		//				PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
		//				pw.print(calculate_detail.zettai(sup));
		//				pw.print(",");
		//
		//				pw.close();
		//
		//				//終了メッセージを出力
		//				System.out.println("出力が完了しました。");
		//
		//			} catch (IOException ex) {
		//				//例外時処理
		//				ex.printStackTrace();
		//			}
		//w +=0.1;}
		return sup;

	}







	public static Map<String,Double> start_positive(String[] args, Map<String, ArrayList<String>> entity_list, ArrayList<String> core_entities){
		//polarityの計算を全てのファイルに
		double score = 0;
		Map<String, Double> scores = new HashMap<String, Double>();
		String most_file = null;//最も値の大きなファイル
		//System.out.println(entity_list);
		//元記事のentityの極性
		ArrayList<String> ori_entities = entity_list.get(args[0]);
		Map<String,Double> ori_score = positive(args[0], ori_entities);

		int q =1;

		//関連記事毎に見ていく
		while(q < args.length){
			ArrayList<String> entities = entity_list.get(args[q]);
			//args[q]と元記事間の擁護度の差を計算

			double next =cal_pos(ori_score,args[q], entities, core_entities);
			double moto_rel = calculate_rel_div.cal_rel_com(entities, ori_entities);
			double polarity_score = next * moto_rel;
			scores.put(args[q], polarity_score);
			if(next > score){//最も値の大きいファイルの抽出
				score = next;
				most_file = args[q];
			}
			q++;


		}
		//print_score(scores, args, "polarity");
		import_newsDB.entry_measure(scores,"polarities");
		return scores;
	}

	//各尺度のスコアを出力する関数 scores:スコア, args : 入力ファイル, attribute : 尺度の属性（ファイルの名前）
	public static void print_score(Map<String, Double> scores, String[] args, String attribute){
		try {
			//出力
			FileWriter fw = new FileWriter("/Users/admin/Documents/workspace/server_news_analysis/result.csv", true);  //���P
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
			pw.println("["+args[0]+"]");
			for (int j = 1; j < args.length; j++) {			
				pw.print(args[j]+",");
				pw.println(scores.get(args[j]));

			}
			pw.close();	
			System.out.println("print_score::出力完了しました。");

		} catch (IOException ex) {
			//例外時処理
			ex.printStackTrace();
		}
	}



}
