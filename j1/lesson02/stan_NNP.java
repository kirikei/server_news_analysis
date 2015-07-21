package j1.lesson02;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class stan_NNP {
		public static void main(String args[]){
		    try{
		      File file = new File(args[0]);

		      if (checkBeforeReadfile(file)){
		        BufferedReader br = new BufferedReader(new FileReader(file));
		        String str;
		        String[] strs;
		        ArrayList<String> entities = new ArrayList<String>();
		        while((str = br.readLine()) != null){
		        		
		        int i=0	;

		         if (str.matches(".*" + "Text=" + ".*")){
		          strs = str.split("] ");
		          	while(i < strs.length){
		          		if (strs[i].matches(".*" + "NamedEntityTag=PERSON") || strs[i].matches(".*" + "NamedEntityTag=ORGANIZASION") || strs[i].matches(".*" + "NamedEntityTag=LOCATION")){
		          			String[] strs1;
		          			strs1 = strs[i].split(" ");

		          			int index = strs1[0].indexOf("=");

		          			String entry_entity = strs1[0].substring(index+1);
		          			if (entities.indexOf(entry_entity) == -1){
		          			entities.add(strs1[0].substring(index+1));
		          			}
		          		}
		          			i++;
		          	}
		         }
		         else{
		        	 //System.out.println("str is not [ ]");
		         }
		        }
		        System.out.println(entities);
		        
		        br.close();
		 	     }else{
		 	    	 System.out.println("�t�@�C����������Ȃ����J���܂���");
		 	     }
		    }catch(FileNotFoundException e){
			      System.out.println(e);
		    }catch(IOException e){
		      System.out.println(e);
		    }
		  }

		  private static boolean checkBeforeReadfile(File file){
		    if (file.exists()){
		      if (file.isFile() && file.canRead()){
		        return true;
		      }
		    }

		    return false;
		  }
		}
