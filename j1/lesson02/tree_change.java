package j1.lesson02;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class tree_change {
	public static ArrayList<String> tree_c_m(ArrayList<String> treekun,ArrayList<String> verbs){
	
//	      File file = new File(args[0]);
//
//	      if (checkBeforeReadfile(file)){
//	        BufferedReader br = new BufferedReader(new FileReader(file));
//	        String str;
	        int i = treekun.size();
	        String[] t_facters = treekun.toArray(new String[i]);;
//	        while((str = br.readLine()) != null){
//	        	 if (str.matches(".*" + "\\(" + ".*?\\-.*"+ ",.*?\\-.*\\)")){
//	        		 t_facters[i] = str;
//	        		 i++;
//	        	 }
//	        }
//	       
//	        while((str = br.readLine()) != null){
//	        	t_facters[i] = str;
//	        	i++;
//	        }
//	        br.close();
	        int n = 0;
	        	       
	        while(n < i){
	        	//System.out.println(t_facters[n]);
	        	String fact = br_type(t_facters[n]);
	        	if(fact == null){
	        		System.out.println("関係が存在しません。");
	        	}
	        	else{
	        switch(fact){
	        case "conj_and" :
	        case "conj_or" : 
	        	int c = 0;
	        	String cf = first_facter(t_facters[n]);
	        	String cs =  second_facter(t_facters[n]);
	        	while(c < i){
	        		if(second_facter(t_facters[c]).equals(cf)){
	        			t_facters[n] = t_facters[n].replaceAll(cf,first_facter(t_facters[c]));
	        			t_facters[n] = t_facters[n].replaceAll(br_type(t_facters[n]),"conj'");
	        			if(organize_entity.check_in_list(verbs,cf) != -1 && organize_entity.check_in_list(verbs,cs) != -1){
	        				int cc = 0;
	        				while(cc < t_facters.length){
	        					String ccf = first_facter(t_facters[cc]);
	        					String ccs =first_facter(t_facters[cc]);
	        					if(ccf == cf && ccs != cs){
	        						t_facters[cc] = t_facters[cc].replaceAll(ccf, ccs);
	        						//System.out.println(t_facters[cc]);
	        						
	        					}
	        					cc++;
	        				}
	        			}
	        			
	        		}
	        		
	        		c++;
	        		
	        	}
	        	break;
	        	
	        case "appos":
	        	int a = 0;
	        	String af = first_facter(t_facters[n]);
	        	String as = second_facter(t_facters[n]);
	        	while(a < i){
	        		//System.out.println(t_facters[a]);
	        		//System.out.println(af);
	        		//System.out.println(as);
	        		if(second_facter(t_facters[a]).equals(af)){
	        			t_facters[n] = t_facters[n].replaceAll(af,first_facter(t_facters[a]));
	        			t_facters[n] = t_facters[n].replaceAll(br_type(t_facters[n]),"appos'");
	        			if(organize_entity.check_in_list(verbs,af) != -1 && organize_entity.check_in_list(verbs,as) != -1){
	        				int aa = 0;
	        				while(aa < t_facters.length){
	        					String aaf = first_facter(t_facters[aa]);
	        					String aas =first_facter(t_facters[aa]);
	        					if(aaf == af && aas != as){
	        						t_facters[aa] = t_facters[aa].replaceAll(aaf, aas);
	        						//System.out.println(t_facters[aa]);
	        						
	        					}
	        					aa++;
	        				}
	        			}
	        		}
	        		a++;
	        	}
	        	break;
	        	
	        case "rcmod":
	        	String fr = first_facter(t_facters[n]);
	        	String sr = second_facter(t_facters[n]);
	        	if(organize_entity.check_in_list(verbs,sr) != -1){
	        	t_facters[n] = t_facters[n].replaceAll(sr,"__tmp__");
	        	t_facters[n] = t_facters[n].replaceAll(fr,sr);
	        	t_facters[n] = t_facters[n].replaceAll("__tmp__",fr);
	        	t_facters[n] = t_facters[n].replaceAll(br_type(t_facters[n]),"rcmod'");
	        	}
	        	break;	
	        	
	        case "cop":
	        	String fc = first_facter(t_facters[n]);
	        	String sc = second_facter(t_facters[n]);
	        	if(organize_entity.check_in_list(verbs,sc) != -1){
	        	t_facters[n] = t_facters[n].replaceAll(sc,"__tmp__");
	        	t_facters[n] = t_facters[n].replaceAll(fc,sc);
	        	t_facters[n] = t_facters[n].replaceAll("__tmp__",fc);
	        	t_facters[n] = t_facters[n].replaceAll(br_type(t_facters[n]),"cop'");
	        	}
	        	break;
	        	
	        	default:
	        		break;
	       
	       
	        
	        
	        }
	        n++;
	        	}
	        }
	        int aa = 0;
	        while(aa < i){
	        	//System.out.println(t_facters[aa]);
	        	aa++;
	        	
	        	}
	        ArrayList<String> list = new ArrayList<String>();
	        int k = 0;
	        while(k < i){
	        	list.add(t_facters[k]);
	        	k++;
	        }
	        //System.out.println("aaa");
			return list;
	}
	        
	        
	 
		
	  
	
	public static String first_facter(String p_tree){
		//ファクターが1,000等カンマを含む時注意！
		String regex = "(.+?\\()(.+)(, .+)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(p_tree);
		String b_facter = null;
		if(m.find()){
			b_facter = m.group(2);
		}
		return b_facter;
	}
	
	
		public static String second_facter(String p_tree){
			
			String regex = "(.+?, )(.+)(\\))";
			Pattern p2 = Pattern.compile(regex);
			Matcher m2 = p2.matcher(p_tree);
			String s_facter = null;
			if(m2.find()){
				
				s_facter = m2.group(2);//System.out.println("aaa");
			}

			return s_facter;
			
		}		
		
		private static String br_type(String p_tree){
			String regex = "(.+?)(\\(.+)";
			Pattern pt = Pattern.compile(regex);
			Matcher mt = pt.matcher(p_tree);
			String b_type = null;
			if(mt.find()){
				b_type = mt.group(1);
			}
			return b_type;
		}
		

	
}
