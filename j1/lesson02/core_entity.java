package j1.lesson02;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import j1.lesson02.word_hit;


public class core_entity {
	public static ArrayList<String> check_core(String arg[], Map<String, ArrayList<String>> entities){
		int i = 0;
		double t = 0.002;//doreDegreeのしきい値

		Map<String,Double> ent_tf_score = new HashMap<String,Double>();//�ŏI�I��entity���Ƃ�tf�̃X�R�A�̑S�Ă̕����̍��Z
		Map<String,Double> score_temp = new HashMap<String,Double>();//各記事のentityのtfスコア
		ArrayList<String> ent0 = new ArrayList<String>();
		Set<String> ent_keys = entities.keySet();
		Iterator<String> ent_it = ent_keys.iterator();

		//各記事のEntityをent_iへ
		while(ent_it.hasNext()){
			ArrayList<String> ent_i = entities.get(ent_it.next());
			score_temp = tf_ent(ent_i,count_word(arg[i]), arg[0], arg[i]); //記事iのent_iのtf値を計算
			Set<String> keys = score_temp.keySet();	
			Iterator<String> itt = keys.iterator();

			//各文書のentityのtf値を合算
			while(itt.hasNext()){
				String Se = itt.next();
				Double new_score = score_temp.get(Se);
				//既にスコアが存在するなら
				if(ent_tf_score.containsKey(Se)){

					Double score = ent_tf_score.get(Se);
					ent_tf_score.put(Se,score + new_score);
				}else{
					ent_tf_score.put(Se,new_score);
				}


			}

			//各記事中の重複するentityの排除
			int ee = 0;
			ArrayList<String> ent0_dash =new ArrayList<String>();
			while(ent_i.size() > ee){
				String entsama = ent_i.get(ee);//ent_iに含まれるentityを取り出す
				if(ent0_dash.contains(entsama) == false){
					ent0_dash.add(entsama);
				}
				ee++;
			}
			ent0.addAll(ent0_dash);//ent_0には各記事のentityが入る
			i++;
		}
		System.out.println("tf score all::"+ent_tf_score);


		//------文書頻度の計算--------
		int u = 0;
		Map<String,Integer> entities_count = new HashMap<String,Integer>();

		//entityを一つずつ検索
		while(u < ent0.size()){
			String ent_C = ent0.get(u);
			//初めて見るentityならば
			if(entities_count.get(ent_C) == null){
				entities_count.put(ent_C,1);		

			}
			else{
				entities_count.put(ent_C,entities_count.get(ent_C)+1);
				//System.out.println("aaaaaa");
			}
			u++;
		}
		//System.out.println(entities_count);
		ArrayList<String> core_entities = new ArrayList<String>();
		Set<String> ent_key = entities_count.keySet();
		Iterator<String> it = ent_key.iterator();

		//core entityの計算
		while(it.hasNext()){
			String S = it.next();
			try {
				double p = entities_count.get(S);//文書頻度
				double tf = ent_tf_score.get(S); //tfの平均
				double CoreDegree = (p*tf / (arg.length));//Coredegree!!
				if(CoreDegree > t){
					core_entities.add(S);
					//System.out.println(S + " =>" + CoreDegree);
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				continue;
				
			}
		}
		System.out.println("core entity: "+core_entities);
		return core_entities;


	}


	//各記事内のword数を計算
	public static int count_word(String file1){
		try{
			File file = new File(connecter_stan.ArticleFolder +file1);

			if (checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				String str1 = "";
				int count_file = 0;
				while((str = br.readLine()) != null){
					str1 += str + " ";
				}
				br.close();
				String[] words = str1.split(" ");
				count_file = words.length;
				System.out.println(file1 + ":" +count_file);
				return count_file;

			}else{
				System.out.println("ファイルが見つからないか開けません。");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}

		return 0;



	}

	//tf値の計算
	public static Map<String,Double> tf_ent(ArrayList<String> ents, int file_size, String ori_file, String a_file){
		Map<String, Double> tf_ent = new HashMap<String, Double>();
		int i = 0;
		Map<String, String> organizer = new HashMap<String, String>();
		//照応解析したデータを用いる
		//元記事のときは
		if(ori_file.equals(a_file)){
			organizer = word_hit.organize_entity("re_"+ori_file);	
		}else{
			organizer = word_hit.organize_entity("re_"+ ori_file +"_"+a_file);
		}
		
		//a_fileのoverlap_remove処理をしていないentity
		ArrayList<String> overlap_ents = word_hit.entity_get("re_"+a_file);
		//System.out.println("over:" + overlap_ents);
		
		//entsが存在すれば加算
		while(i < overlap_ents.size()){
			String entsan = overlap_ents.get(i);
			
			//照応解析で対応していたら
			if(organizer.containsKey(entsan)){
				//解析後のentityへ変換
				String first_ent = organizer.get(entsan);
				
			}
			if(tf_ent.get(entsan) != null){
				double val = tf_ent.get(entsan);
				tf_ent.put(entsan, val+1.0);}
			else{
				tf_ent.put(entsan, 1.0);
			}
			i++;
		}
		System.out.println("tf_ent1:"+tf_ent);
		Set<String> key = tf_ent.keySet();
		Iterator<String> it = key.iterator();

		while(it.hasNext()){
			String S = it.next();
			tf_ent.put(S, tf_ent.get(S)/file_size);
		}
		System.out.println("tf_ent2:"+tf_ent);
		return tf_ent;

	}


	private static boolean checkBeforeReadfile(File file){
		if (file.exists()){
			if (file.isFile() && file.canRead()){
				return true;
			}
		}
		return false;
	}




	public static void main(String[] args){
		String[] files = {"0.txt","1.txt"};
		Map<String, ArrayList<String>> entities = word_hit.file_entities(files);
		System.out.println("entities:"+entities);
		check_core(files,entities);
	}
}


