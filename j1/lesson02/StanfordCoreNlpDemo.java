package j1.lesson02;

import java.io.*;
import java.util.*;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;
import j1.lesson02.import_newsDB;


public class StanfordCoreNlpDemo {

	public static void stan(String[] args) throws IOException {
		PrintWriter out;
		if (args.length > 1) {
			out = new PrintWriter(connecter_stan.ArticleFolder + args[1]);
		} else {
			out = new PrintWriter(System.out);
		}
		PrintWriter xmlOut = null;
		if (args.length > 2) {
			xmlOut = new PrintWriter(connecter_stan.ArticleFolder + args[2]);
		}

		StanfordCoreNLP pipeline = new StanfordCoreNLP();
		Annotation annotation;
		if (args.length > 0) {
			annotation = new Annotation(IOUtils.slurpFileNoExceptions(connecter_stan.ArticleFolder + args[0]));
		} else {
			annotation = new Annotation("Japan said depressed US and Japan talked with he which is growing rapidly.");
		}

		pipeline.annotate(annotation);
		pipeline.prettyPrint(annotation, out);

		if (xmlOut != null) {
			pipeline.xmlPrint(annotation, xmlOut);
		}


		OutputStream output = new ByteArrayOutputStream();
		pipeline.xmlPrint(annotation, output);
		String xmlResult = output.toString();
		// System.out.println(xmlResult);

		try {
			Builder builder = new Builder();
			Document doc = builder.build(xmlResult, null);
			System.out.println(doc.getChildCount());
		} catch (ParsingException e) {
			e.printStackTrace();
		}


		int i = 0;
		// An Annotation is a Map and you can get and use the various analysis individually.
		// For instance, this gets the parse tree of the first sentence in the text.
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		//System.out.println(sentences);
		if (sentences != null && sentences.size() > 0) {
			while(i < sentences.size()){
				CoreMap sentence = sentences.get(i);

				Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
				//out.println();
				//out.println("The first sentence parsed is:");
				//tree.indexLeaves();
				// System.out.println(tree);

				i++;
			}
		}
	}

	public static void start_stan1(String[] database) throws IOException{
		int i = 0; 
		//re_aid.txtの生成
		while(i < database.length){
			String[] files = {database[i],"re_"+database[i]};
			//ファイルの存在をチェック、存在すればstanに渡さない
			File file = new File(connecter_stan.ArticleFolder+"re_"+database[i]);
			if(file.exists() == false){
				try{
				stan(files);
				}catch(Exception e){
					continue;
				}
			}
			i++;

		}
		int j = 1;
		//re_0.txt_aid.txtの生成
		while(j < database.length){
			String[] files = {database[0]+"_"+database[j],"re_"+database[0]+"_"+database[j]};
			File file = new File(connecter_stan.ArticleFolder+"re_"+database[0]+"_"+database[j]);
			if(file.exists() == false){
				try{
				stan(files);
				}catch(Exception e){
					continue;
				}
			}
			j++;

		}	
	}

	public static void start_stan2(String[] database) throws IOException{

		int j = 0;
		while(j < database.length){
			String[] files = {database[j+1],database[j]};
			stan(files);
			j+=2;

		}



	}
	public static void main(String[] args) throws IOException{
		stan(args);
	}

}



