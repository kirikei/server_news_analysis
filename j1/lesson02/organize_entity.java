package j1.lesson02;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class organize_entity {

	public static Map<Integer,String[]> get_subsentense(String file, String nomal_entity,int number){
		Map<Integer,ArrayList<String>> subtrees = new HashMap<Integer,ArrayList<String>>();
		Map<Integer,ArrayList<String>> treesan = get_tree(file);
		Map<Integer,ArrayList<String>> verbs = get_verb(file);//System.out.println("bbb");
		ArrayList<String> sub_tree = new ArrayList<String>();
		int p = 1,t = 0;
		String[] ent_case = nomal_entity.split(" ");
		String entity = ent_case[ent_case.length-1];
		while(p < treesan.size()){
			//System.out.println(p);
			//System.out.println("aaa");
			ArrayList<String> word_tree = tree_change.tree_c_m(treesan.get(p),verbs.get(p)); //System.out.println("word_tree:"+word_tree);
			if((t = check_in_list(word_tree,entity)) != -1){//entityの含まれる木が存在するなら
				String str = word_tree.get(t); //entityの存在するword_treeの要素をstrに格納
				int i = 0,q=0;	//System.out.println(str);//
				int flag = 0; //２回以上終点についたらbreak
				while(true){
					//entityの部分木を抽出する
					String i_facter = word_tree.get(i);
					String is_facter = tree_change.second_facter(i_facter);
					//System.out.println("str "+str);
					if((check_in_list_b(verbs.get(p),tree_change.first_facter(str)) != -1) || tree_change.first_facter(str).equals("ROOT-0")){//str�̑��v�f�������Ȃ�
						//System.out.println(str);
						break;

					}
					if(tree_change.first_facter(str).equals(is_facter)){//�؂������̂ڂ�

						//System.out.println(verbs.get(p));
						str = i_facter;//
						q = i;//System.out.println(q);
						flag = 0;
						
					}else{
						if(flag>word_tree.size()){//ツリーを全部読み込んでも存在しないなら
							break;
						}
						flag++;//見つからなかった時のツリーを読み込んだ数
						
					}
					i++;
					//

					if(i == word_tree.size()){
						i = 0;
					}

				}


				String root_v =str;
				//System.out.println(root_v);

				ArrayList<Integer> through_checker = new ArrayList<Integer>();
				if(q==0){
					through_checker.add(t);
				}
				else{through_checker.add(q);
				}
				//System.out.println(through_checker);
				sub_tree = find_dep(through_checker,root_v,word_tree);
				//System.out.println("sub_tree::" + sub_tree);

				//(governor, dependency)から単語の列へ変換
				subtrees.put(p, make_sentense(sub_tree));
				
				ArrayList<String> dummy = new ArrayList<String>();
				sub_tree = dummy;



			}

			p++;


		}

		//System.out.println(subtrees);
		
		//出力するサブツリー
		Map<Integer,String[]> result_subtree = new HashMap<Integer, String[]>();//�ڍדx�̌v�Z�ŗp���镔����

		if(subtrees.size() > 0){


			try {
				//ファイルへ書き出し
				FileWriter fw = new FileWriter(connecter_stan.EntityTreeCsvFolder+ nomal_entity + ".csv", true);  //���P
				PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

				int key = 0;int num_rs =0;
				ArrayList<String> sentense = new ArrayList<String>();
				ArrayList<Integer> itemIdList = new ArrayList<Integer>(subtrees.keySet());
						
				while(key < p){//���e���w�肷��
					if(itemIdList.indexOf(key) != -1){
						//１センテンス毎に取り出し
						sentense = subtrees.get(key);
						int sent_num = 0;
						pw.print(number+",");
						String[] sent_rs = new String[sentense.size()];
						while(sent_num < sentense.size()){
							String str = sentense.get(sent_num);
							pw.print(str);
							pw.print(" ");
							//pw_lda.print(str+" ");
							sent_rs[sent_num] = str;
							sent_num++;
						}
						pw.println();
						//pw_lda.println();
						result_subtree.put(num_rs,sent_rs);
						num_rs++;
					}
					key++;
				}


				//�t�@�C���ɏ����o��
				pw.close();
				//pw_lda.close();

				//�I�����b�Z�[�W����ʂɏo�͂���
				//System.out.println("部分木："+result_subtree);
				return result_subtree;

			} catch (IOException ex) {
				
				ex.printStackTrace();
				return null;
			}
		}

		else{//System.out.println("subtree��null�ł��B");
			return null;}
	}




	private static ArrayList<String> make_sentense(ArrayList<String> list){//(governor, dependency)の形から単語の列へ変換
		int list_num = 0;
		ArrayList<String> list_a =new ArrayList<String>();
		ArrayList<String> result_list =new ArrayList<String>();
		while(list_num < list.size()){
			String ff = tree_change.first_facter(list.get(list_num));
			String sf = tree_change.second_facter((list.get(list_num)));
			//もし重なるものが無ければリストへ格納
			if(list_a.indexOf(ff) == -1){
				list_a.add(ff);
			}
			if(list_a.indexOf(sf) == -1){
				list_a.add(sf);
			}
			list_num++;
		}
		//System.out.println(result_list);
		list_num = 0;
		String[] result = list_a.toArray(new String[list_a.size()]);
		while(list_num < result.length){
			int index = result[list_num].lastIndexOf("-");
			if(index != -1){
				String word = result[list_num].substring(0,index);
				result[list_num] = word;}
			list_num++;

		}
		int s = 0;
		while(s < result.length){
			if(result[s].equals("ROOT") == false){
				result_list.add(result[s]);}
			s++;
		}

		return result_list;

	}

	public static int check_in_list_bubun(ArrayList<String> list, String str){//list�v�f��str��������v���邩
		int cou = 0;
		while(cou < list.size()){
			if(list.get(cou).matches(".*" + str +".*")){
				//System.out.println(cou);
				return cou;

			}
			cou++;	
		}
		//System.out.println(-1);	
		return -1;

	}





	public static int check_in_list(ArrayList<String> list, String str){//list�v�f��str��������v���邩
		int cou = 0;
		try {
			while(cou < list.size()){
			if(list.get(cou).matches(".*" + str +".*")){
				//System.out.println(cou);
				return cou;

			}
			cou++;	
		}
		} catch (Exception e) {
			// TODO: handle exception
			return -1;
		}
		
		//System.out.println(-1);	
		return -1;

	}

	public static int check_in_list_b(ArrayList<String> list, String str){//list�v�f��str�ɕ�����v���邩�ǂ���
		int cou = 0;
		try {
			while(cou < list.size()){
			if(str.matches(".*" + list.get(cou) +".*")){
				//System.out.println(cou);
				return cou;

			}
			cou++;	
		}
			
		} catch (Exception e) {
			// TODO: handle exception
			return -1;
		}
		
		//System.out.println(-1);	
		return -1;

	}



//sentenceを読んで関係構文木（aaa-1, bbb-2）を抽出する
	public static Map<Integer,ArrayList<String>> get_tree(String args){
		try{
			File file = new File(connecter_stan.ArticleFolder + args);

			if (cut_file.checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				int num = 1;
				//1 sentenceのツリー
				ArrayList<String> trees = new ArrayList<String>();
				//ファイル全体のツリー
				Map<Integer,ArrayList<String>> text_tree = new HashMap<Integer,ArrayList<String>>();

				//Listへ入れる時のフラグ
				boolean subflag = false;
				boolean flag = false;
				str = br.readLine();//1行目は無視
				while((str = br.readLine()) != null){
					
					if(str.matches("\\(ROOT")){
						subflag = true;//���̍s�ȉ������o��

					}
					
					//空白行を読んだら				
					if(str.length()==0 && subflag){
						flag = true;
					}

					//SentenceかConference setを読んだら（終了条件）
					if(str.matches("Sentence" + " #.*") ||str.matches("Coreference set" + ".*")){
						flag = false;
						subflag = false;
						text_tree.put(num, trees);//Mapに追加
						num++;
						//Coreference setと1回マッチしたら読み終わり
						if(str.matches("Coreference set" + ".*")){
							break;
						}

						//treeの初期化
						ArrayList<String> dummy = new ArrayList<String>();
						trees = dummy;
					}



				if(flag && str.length() > 0 && str.matches(".*" + "-" + "\\d.*")){
					trees.add(str);
					//System.out.println(str);

				}
				//取り出す条件（ピリオドノードを読んだら）
//				if(str.matches(".*" +"\\(\\. .*?\\)" + ".*")){
//					flag = true;//���̍s�ȉ������o��
//
//				}


			}


				//System.out.println("text: "+text_tree);
				br.close();
				return text_tree;

			}else{
				System.out.println("出力が完了しました。");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}

		return null;
	}
	//動詞を取り出す
	public static Map<Integer,ArrayList<String>> get_verb(String args){
		try{
			File file = new File(connecter_stan.ArticleFolder + args);

			if (cut_file.checkBeforeReadfile(file)){
				BufferedReader br = new BufferedReader(new FileReader(file));
				String str;
				int num = 1;
				ArrayList<String> trees = new ArrayList<String>();
				Map<Integer,ArrayList<String>> checker = new HashMap<Integer,ArrayList<String>>();
				boolean flag = false;
				while((str = br.readLine()) != null){
					if(str.matches(".*" +"\\(\\. .*?\\)" + ".*")||str.length()==0){
						flag = false;

						checker.put(num, trees);
						num++;

						ArrayList<String> dummy = new ArrayList<String>();
						trees = dummy;
					}



					if(flag && str.length() > 0){
						if (str.matches(".*" + "VB" + ".*")){
							String regex = "(.*VB.*? )(.*?)(\\))(.*)";
							Pattern p = Pattern.compile(regex);
							Matcher m = p.matcher(str);
							if(m.find()){
								//System.out.println(m.group(2));
								trees.add(m.group(2));	
							}
						}	



					}

					if(str.matches("\\(ROOT")){
						flag = true;//���̍s�ȉ������o��

					}


				}
				//System.out.println("aaa");
				//System.out.println(checker);
				br.close();
				return checker;

			}else{
				System.out.println("ファイルが見つかりません。");
			}
		}catch(FileNotFoundException e){
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
		return null;



	}

	public static void main(String[] args){
		int a = 1;
		while(a < args.length){
			get_subsentense(args[a],args[0],a);
			a++;
		}
	}



	public static ArrayList<String> find_dep(ArrayList<Integer> check_list, String root, ArrayList<String> trees){
		ArrayList<Integer> stack = new ArrayList<Integer>(); 
		ArrayList<String> result_a = new ArrayList<String>();
		int ii = 0,pp=0;
		result_a.add(root);
		while(pp < trees.size()){
			if(check_list.indexOf(pp) == -1 && tree_change.first_facter(trees.get(pp)).equals(tree_change.first_facter(root))){
				stack.add(pp);//���򂷂�m�[�h��T��
				//System.out.println(pp);
			}
			pp++;
		}

		while(ii < trees.size()){//root_v����q���Ȃ��Ď��o��
			//sub_tree.add(root_v);
			if(check_list.indexOf(ii) == -1){//��x���������m�[�h�̌����������

				String ff = tree_change.first_facter(trees.get(ii));

				if(ff.equals(tree_change.second_facter(root))){
					//System.out.println(ff + ":" +  tree_change.first_facter(root_v));
					root = trees.get(ii);
					result_a.add(root);//System.out.println(root_v);
					check_list.add(ii);//���������m�[�h�͋L��
					ii = 0;pp=0;//������x����
					while(pp < trees.size()){
						if(check_list.indexOf(pp) == -1 && tree_change.first_facter(trees.get(pp)).equals(tree_change.first_facter(root)) && stack.indexOf(pp) == -1){
							stack.add(pp);//���򂷂�m�[�h��T��
							//System.out.println(pp);
						}
						pp++;
					}
				}

			}

			ii++;
			if(ii == trees.size() && stack.size() > 0){
				root = trees.get(stack.get(0));//stack��O����root��
				check_list.add(stack.get(0));//���������m�[�h�͋L��
				stack.remove(0); //���o�����m�[�h�͏���
				result_a.add(root);
				ii=0;//������x�����瑀��

			}
		}

		ArrayList<String> result = new ArrayList<String>();
		int doub = 0;
		while(doub < result_a.size()){
			String kkk = result_a.get(doub);
			if(result.contains(kkk) == false){
				result.add(kkk);
			}
			doub++;
		}


		return result;




	}

}


