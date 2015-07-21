package j1.lesson02;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;


public class TopicModel {
	private static String StopFolder = "./lib/mallet-2.0.7/stoplists/";
	
	public static Map<Integer,String[]> topic_modeling(String file, String entity,int size) throws Exception {

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		add_stoplist(entity);

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File(StopFolder+"en+.txt"), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequence2FeatureSequence() );

		InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File(connecter_stan.EntityTreeCsvFolder+file)), "UTF-8");
		instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
				3, 2, 1)); // data, label, name fields
		//System.out.println("inst::"+instances);
		// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		//  Note that the first parameter is passed as the sum over topics, while
		//  the second is the parameter for a single dimension of the Dirichlet prior.
		int numTopics = 5;
		ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and combine
		//  statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only, 
		//  for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(1000);
		model.estimate();


		//=====================================ここからNamed Entityの記述毎の推定====================================



		// Show the words and topics in the first instance
		Map<Integer,String[]> topic_words = new HashMap<Integer,String[]>();//(topic番号, topic単語)

		int i = 0;
		try{
			//Calendar now = Calendar.getInstance();
			FileWriter fw = new FileWriter(connecter_stan.TopicCsvFolder+"topic_"+entity+".csv", true);  //���P
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
			FileWriter topic_fw = new FileWriter(connecter_stan.TopicCsvFolder+entity+"_topic_words.csv", true);  //���P
			PrintWriter topic_pw = new PrintWriter(new BufferedWriter(topic_fw));

			while(i < size){//渡したツリーの数を全て読むまで

				// The data alphabet maps word IDs to strings
				Alphabet dataAlphabet = instances.getDataAlphabet();

				FeatureSequence tokens = (FeatureSequence) model.getData().get(i).instance.getData();
				LabelSequence topics = model.getData().get(i).topicSequence;

				Formatter out = new Formatter(new StringBuilder(), Locale.US);
				for (int position = 0; position < tokens.getLength(); position++) {
					out.format("%s ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));

				}
				//System.out.println(out);


				// Estimate the topic distribution of the first instance, 
				//  given the current Gibbs state.
				double[] topicDistribution = model.getTopicProbabilities(i);
				//System.out.println(topicDistribution.length);


				//�I�����b�Z�[�W����ʂɏo�͂���
				System.out.println("出力完了＠Mallet");



				// Get an array of sorted sets of word ID/count pairs
				ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
				// Show top 5 words in topics with proportions for the first document
				for (int topic = 0; topic < numTopics; topic++) {

					double pr_topic = topicDistribution[topic];
					out = new Formatter(new StringBuilder(), Locale.US);
					out.format("%d\t%.3f\t", topic, pr_topic);
					//トピック確率をCSVファイルにて取得
					pw.print(pr_topic+",");

					Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
					//1回目の時のみトピック単語を取得
					if(i == 0){
						int rank = 0;//トピック単語数 
						String[] words = new String[20];
						while (iterator.hasNext() && rank < 20) {
							IDSorter idCountPair = iterator.next();
							String str =  (dataAlphabet.lookupObject(idCountPair.getID())).toString();
							out.format("%s ", str);
							words[rank] = str;
							topic_pw.print(str+",");
							rank++;
						}
						//トピック単語を取得
						topic_words.put(topic,words);
						topic_pw.println();
					}

					//System.out.println(out);
					// �e�g�s�b�N�ԍ���topic�P����i�[

				}
				System.out.println();pw.println();

				i++;}

			pw.close();
			topic_pw.close();

		} catch (IOException ex) {
			//��O������
			ex.printStackTrace();
		}
		// Create a new instance with high probability of topic 0
		//        StringBuilder topicZeroText = new StringBuilder();
		//        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();
		//
		//        int rank = 0;
		//        while (iterator.hasNext() && rank < 20) {
		//            IDSorter idCountPair = iterator.next();
		//            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
		//            rank++;
		//        }
		//        System.out.println(topicZeroText);

		// Create a new instance named "test instance" with empty target and source fields.
		//        InstanceList testing = new InstanceList(instances.getPipe());    
		//        
		//        Formatter text = new Formatter(new StringBuilder(), Locale.US);   //�ǂݍ��ޕ���
		//        for (int position = 0; position < tokens.getLength(); position++) {     //�e�������X�y�[�X�łȂ�
		//            text.format("%s ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
		//        }
		//        
		//        
		//        testing.addThruPipe(new Instance(text.toString(), null, "test instance", null));//�w�K�������f����K�p���镶�H
		//        
		//        //System.out.println(testing);
		//        TopicInferencer inferencer = model.getInferencer();
		//       
		//        //getSampledDistribution(Instance instance,int numIterations,int thinning,int burnIn)
		//        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 100, 1, 5);
		//        int k = 0;
		//        while(k<testProbabilities.length){
		//        System.out.println(k+"\t" + testProbabilities[k]);
		//        k++;}


		return topic_words;
	}	


	public static void add_stoplist(String entity){
		File en_txt = new File(StopFolder+"en.txt");
		File add_txt = new File(StopFolder+"en+.txt");

		try {
			PrintWriter pw = new PrintWriter(new FileWriter(add_txt));
			if(checkBeforeReadfile(en_txt)){
				pw.println(entity.toLowerCase());
				BufferedReader br = new BufferedReader(new FileReader(en_txt));
				String str;

				while((str = br.readLine()) != null){	
					pw.println(str);
				}
				br.close();
			}
			pw.close();	



		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public static void main(String[] args) throws Exception{
		topic_modeling(args[0],args[1],5);
	}

}
