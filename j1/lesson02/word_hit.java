package j1.lesson02;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import j1.lesson02.SentiWordNetDemoCode;

public class word_hit {	
	//entityを取り出すメソッド
	public static Map<String,ArrayList<String>> file_entities(String[] files){
		Map<String,ArrayList<String>> result_entities = new HashMap<String,ArrayList<String>>();
		ArrayList<String> one_entities = new ArrayList<String>(); //各記事毎のエンティティリスト

		//1記事目は個別に
		one_entities = entity_get("re_"+files[0]);
		one_entities = list_overlap_remove(one_entities);
		result_entities.put(files[0], entity_arrange(one_entities,null));
		int k = 1;
		while(k < files.length){
			ArrayList<String> a_entities = entity_get("re_"+files[k]);
			a_entities=list_overlap_remove(a_entities);
			//0.txt_i.txtを用いる
			result_entities.put(files[k], entity_arrange(a_entities,"re_"+files[0]+"_"+files[k]));
			k++;
		}

		return result_entities;
	}


	public static ArrayList<String> entity_get(String text_file1){
		ArrayList<String> entities = new ArrayList<String>();
		try{
			File file = new File(connecter_stan.ArticleFolder + text_file1);

			if (cut_file.checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				String[] strs;

				String[] ents = new String[500];
				int s = 0,h=0;

				//re_.txtを一行ずつ見ていく
				while((str = br.readLine()) != null){

					//品詞の行に来たら
					if (str.matches(".*" + "Text=" + ".*")){
						//単語毎に切り出す
						strs = str.split("] ");
						int i=0;	
						String memo = null;

						while(i < strs.length){
							String regex = "(\\[Text=)(.*?)( C.*?NamedEntityTag=)(.*)";
							Pattern p = Pattern.compile(regex);
							//System.out.println("aaa");
							Matcher m1 = p.matcher(strs[i]);

							//NamedEntityTagを持つ単語を見つけたならば
							if(m1.find()){// ;

								if(m1.group(4).startsWith("PERSON") ||m1.group(4).startsWith("ORGANIZATION") ||m1.group(4).startsWith("LOCATION")){
									//System.out.println(m1.group(2) + " : " + m1.group(4));
									//System.out.println(h);
									if(ents[h] != null){
										ents[h] = ents[h] + " " + m1.group(2); //P,O,Lが連続で現れた時
									}
									else{//初めて現れたとき
										ents[h] = m1.group(2);}
									memo = m1.group(4);

								}		
								else{
									if(ents[h] != null){
										//P,O,Lでなく, ents内がnullでないとき, entを進める
										h++;
									}
								}

							}
							i++;

						}

						//配列->リストへ
						while(s < h ){//&& ents[s] != null){// &&
							//System.out.println("ents "+ents[s]);	        	 
							entities.add(ents[s]);


							s++;   
						}
					}
				}
				//System.out.println("w_entities:"+entities);

				br.close();
			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		return entities;
	}

	public static ArrayList<String> entity_arrange(ArrayList<String> entities, String text_file2){
		ArrayList<String> result_entities = new ArrayList<String>();
		Map<String,String> ent_map = new HashMap<String,String>(); //organizeしたentityを入れる
		Map<String,String> check_ent = new HashMap<String,String>();//keyとvalueを逆にしてentityの同期に使用

		//text_file2がnullのとき、即ち元記事のentity抽出だったとき
		if(text_file2 == null){	
			//System.out.println(entities);
			return entities;

		}else{

			//元記事とのentityの同期	
			ent_map = organize_entity(text_file2);
			//System.out.println(ent_map);

			int ii = 0;
			//記事のentityを見ていく
			while(ii < entities.size()){
				String checker = entities.get(ii);
				
				//矢印先の座標をvalueとする
				String value = ent_map.get(checker);
				
				//valueがなければ
				if(value == null){
					result_entities.add(checker);
				}
				//System.out.println(value);
				else{
					String getkun = check_ent.get(value);
					if(getkun == null){
						check_ent.put(value, checker);
						result_entities.add(checker);
					}else{
						result_entities.add(check_ent.get(value));
						//System.out.println("txt=="+ text_file2 +" get "+getkun);
					}


				}
				ii++;

			}//System.out.println("result:::" + result_entities);
			return result_entities;
		}
	}

	
	//照応解析
	public static Map<String,String> organize_entity(String arg){	
		try{
			File file = new File(connecter_stan.ArticleFolder + arg);

			if (cut_file.checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				Map<String,String> organizer = new HashMap<String,String>();
				while((str = br.readLine()) != null){
					//照応解析の行に来たら	
					if (str.matches(".*?" + "->" + ".*?" + "->" + ".*")){
						//矢印の前と先を取り出す
						String regex = "(.*?-> )(.*?)(, that.*: \")(.*?)(\".*)";
						Pattern p = Pattern.compile(regex);
						Matcher m = p.matcher(str);
						if(m.find()){
							organizer.put(m.group(4),m.group(2));
						}
					}		
				}	
				

				//System.out.println(organizer.keySet());

				br.close();
				return organizer;

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

	//感情語のスコアを取り出す	
	public static Map<Integer, Map<String, Double>> POS_score_get(String text_file1){
		Map<Integer,Map<String,Double>> sentense_wordscores = new HashMap<Integer,Map<String,Double>>();//�e�Z���e���X���Ƃ�wordscore

		try{
			File file = new File(connecter_stan.ArticleFolder + text_file1);

			if (cut_file.checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str1;
				String[] strs1;
				int sent_num = 0;
				boolean frag = false; //sentenceを拾ったらtrueになる

				while((str1 = br.readLine()) != null){

					Map<String,String> POSs = new HashMap<String,String>();//品詞とその品詞の種類を格納
					Map<String,Double> wordscores = new HashMap<String,Double>();	//品詞とそのスコアを格納
					if(str1.matches("Sentence" + " #.*")){
						String str_text = "";
						sent_num++;

						while(str1.matches("\\(ROOT") == false && str1.matches("\\(X" + ".*") == false){
							str1 = br.readLine();
							//System.out.println("str1 : "+str1+" " + str1.matches("\\(X" + ".*"));
							if (str1.matches(".*" + "Text=" + ".*")){
								str_text = str_text+str1;
								//System.out.println("str_text"+str_text);
							}
							//System.out.println("str1 : "+str1+" " + str1.matches("\\(ROOT"));
						}
						//System.out.println("str_text"+str_text);
						strs1 = str_text.split("] ");
						int i=0;

						while(i < strs1.length){
							String regex = "(\\[Text=)(.*?)( C.*?)(PartOfSpeech=)(.*?)( .*?)(NamedEntityTag=)(.*)";
							Pattern p = Pattern.compile(regex);
							//System.out.println("aaa");
							Matcher m2 = p.matcher(strs1[i]);
							//
							//SentiWordNetで得られる値を見つける
							if(m2.find()){// ;
								if((m2.group(8)).equals("O")){//NamedEntityの場合
									String text = m2.group(2);
									String pos = m2.group(5);
									if(pos.startsWith("V")){//動詞の場合
										POSs.put(text,"v");
									}
									else if(pos.startsWith("J")){//形容詞の場合
										POSs.put(text, "a");
									}
									else if(pos.startsWith("N")){//名詞の場合
										POSs.put(text, "n");
									}


								}

							}
							i++;

						}

						if(POSs.size() > 0){//一つでも感情語があれば

							for(String key : POSs.keySet()){
								String data = POSs.get(key);

								//System.out.println(key);
								//SentiWordNetDemoCode sentiwordnet = new SentiWordNetDemoCode("SentiWordNet_3.0.0_20130122.txt");
								double word_score = connecter_stan.sentiwordnet.extract(key, data);
								if(word_score != 0){
									//System.out.println(key + " #" + data +" " + word_score+" sent "+sent_num);
									wordscores.put(key, word_score);
								}

							}//System.out.println(wordscores);
							sentense_wordscores.put(sent_num, wordscores);
						}
						}
				}
				System.out.println(sentense_wordscores);
				return sentense_wordscores;

			}else{
				System.out.println("ファイルが見つからないか開けません");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
			return sentense_wordscores;
		}catch(IOException e){
			System.out.println(e);
			return sentense_wordscores;
		}
		return sentense_wordscores;
	}
	
	
//	public static Map<Integer, Map<String, Double>> POS_score_get(String text_file1){
//		Map<Integer,Map<String,Double>> sentense_wordscores = new HashMap<Integer,Map<String,Double>>();//�e�Z���e���X���Ƃ�wordscore
//		try{
//			File file = new File(connecter_stan.ArticleFolder + text_file1);
//
//			if (cut_file.checkBeforeReadfile(file)){
//				BufferedReader br = new BufferedReader(new FileReader(file));
//				String str1;
//				String[] strs1;
//				int sent_num = 0;
//
//				while((str1 = br.readLine()) != null){
//					Map<String,String> POSs = new HashMap<String,String>();//品詞とその品詞の種類を格納
//					Map<String,Double> wordscores = new HashMap<String,Double>();	//品詞とそのスコアを格納
//
//					if (str1.matches(".*" + "Text=" + ".*")){
//						strs1 = str1.split("] ");
//						int i=0;	
//						sent_num++;
//						while(i < strs1.length){
//							String regex = "(\\[Text=)(.*?)( C.*?)(PartOfSpeech=)(.*?)( .*?)(NamedEntityTag=)(.*)";
//							Pattern p = Pattern.compile(regex);
//							//System.out.println("aaa");
//							Matcher m2 = p.matcher(strs1[i]);
//							//
//							//SentiWordNetで得られる値を見つける
//							if(m2.find()){// ;
//								if((m2.group(8)).equals("O")){//NamedEntityの場合
//									String text = m2.group(2);
//									String pos = m2.group(5);
//									if(pos.startsWith("V")){//動詞の場合
//										POSs.put(text,"v");
//									}
//									else if(pos.startsWith("J")){//形容詞の場合
//										POSs.put(text, "a");
//									}
//									else if(pos.startsWith("N")){//名詞の場合
//										POSs.put(text, "n");
//									}
//
//
//								}
//
//							}
//							i++;
//
//						}
//
//
//
//					}
//					if(POSs.size() > 0){
//
//						for(String key : POSs.keySet()){
//							String data = POSs.get(key);
//
//							//System.out.println(key);
//							//SentiWordNetDemoCode sentiwordnet = new SentiWordNetDemoCode("SentiWordNet_3.0.0_20130122.txt");
//							double word_score = connecter_stan.sentiwordnet.extract(key, data);
//							if(word_score != 0){
//								//System.out.println(key + " #" + data +" " + word_score);
//								wordscores.put(key, word_score);
//							}
//
//						}//System.out.println(wordscores);
//						sentense_wordscores.put(sent_num, wordscores);
//					}
//				}
//				System.out.println(sentense_wordscores);
//				return sentense_wordscores;
//
//			}else{
//				System.out.println("ファイルが見つからないか開けません");
//			}
//		}catch(FileNotFoundException e){
//			System.out.println(e);
//		}catch(IOException e){
//			System.out.println(e);
//		}
//		return sentense_wordscores;
//	}

	//リストの重複を取り除く
	public static ArrayList<String> list_overlap_remove(ArrayList<String> list){
		ArrayList<String> result_list = new ArrayList<String>();
		int i = 0;
		while(i < list.size()){
			if(result_list.indexOf(list.get(i)) == -1){
				result_list.add(list.get(i));
			}
			i++;
		}
		
		return result_list;
	}
	
	public static void main(String args[]) throws IOException{	
		POS_score_get(args[0]);

	}
}