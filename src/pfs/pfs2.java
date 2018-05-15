/*
 * PFS^2:  Parallel Version
 * Last updated: 01/29/2018
 */

package pfs;

import gsp.GSP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.mathworks.toolbox.javabuilder.MWException;
import common.Computer;
import common.Distribution;
import computeVar.ComputeVar;
import common.Constract;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author zyli
 * modified/updated by:
 * 		 Yousef & Wenrui
 */
public class pfs2 {
	private static List<String> superFrequentList = new ArrayList<String>();
	private static List<String> globleFrequentList = new ArrayList<String>();
	private static double minsup;
	private static int dbSize;
	private static int sampleSize;
	private static int limitLen;
	private static String prefix;
	private static double lower;//  
	private static int maxFreSeqLen;
	private static ComputeVar computeVar;
	
	public static int ThreadNum = 4;
	public final static class FuncGenNegList implements Callable<String> {
		Map<String, Integer> cntMap;
		String curPat;
		double curEst;
		double Threshold;
		
		public FuncGenNegList(Map<String, Integer> Count, String candidate, double estSupport, double support_small) {
			cntMap = Count;
			curPat = candidate;
			curEst = estSupport;
			Threshold = support_small;
		}
		@Override
		public String call() throws Exception {
			String neg = null;
			if (cntMap.containsKey(curPat))
				curEst += cntMap.get(curPat);
			if (curEst >= Threshold)
				neg = curPat;
			return neg;
		}
	}
	
    private final static class FuncGenPosList implements Callable<Double> {
    	Map<String, Integer> cntMap;
    	String curStr;
    	double Prob;
    	int NegListNum;
    	String destFile;
    	
    	public FuncGenPosList(Map<String, Integer> countMap, String candidate, double pr2, int countNum, String dest) {
    		cntMap = countMap;
    		curStr = candidate;
    		Prob = pr2;
    		NegListNum = countNum;
    		destFile = dest;  
    	}
		@Override
		public Double call() throws Exception {
			// TODO Auto-generated method stub
			int fre = cntMap.get(curStr);
			double noisy;
			double support; 
			do {
				noisy = Distribution.laplace(Prob, NegListNum);
				support = fre + noisy;
			}while (support > dbSize);


			if (support >= minsup * dbSize) {
				write(destFile, curStr + ":" + fre +":" +(long) support);
			} else {
				support = -1;
			}
			return support;
		}
    }
  
	public static void main(String[] args) throws MWException, InterruptedException, ExecutionException {
		List<Double> minSupValues = Arrays.asList(0.03);
		List<Double> epsilonValues = Arrays.asList(1.0);
		List<Integer> maxfreSeqlength = new ArrayList<>(Arrays.asList(5,7,9,11,13,15));//,10,15));//5,7,9,11,13,15));//5,6)); //,6,7,8,9,10,11,12,13,14,15,16,17,18));
		List<Integer> limitLength = new ArrayList<>(Arrays.asList(1,3,5,7,9,11,13,15));//,17,19));//,7,9,11,13));//5,6));//25,50,75,100,125,150,175,200));//10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190));//new ArrayList<>(Arrays.asList(3,5,7,9,11,13,15,17,19,21,22,24));
		List<Integer> loopValues = new ArrayList<>(Arrays.asList(1,2,3,4,5));//,4,5,6,7,8,9,10));//3,6,9,12,15,18,21,24));//25,50,75,100,125,150,175,200));//10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,160,170,180,190));//new ArrayList<>(Arrays.asList(3,5,7,9,11,13,15,17,19,21,22,24));

		for (Double correspondminSup : minSupValues)
		{
			for (Double epsilonValue : epsilonValues)
			{
				superFrequentList.clear();
				globleFrequentList.clear();				 	
				for (Integer freSeq : maxfreSeqlength) 
				{
					for (Integer lmLength : limitLength)
					{
						superFrequentList.clear();
						globleFrequentList.clear();			
						for (Integer loopValue : loopValues)
						{							 
							superFrequentList.clear();
							globleFrequentList.clear();
			 //			System.out.println(" No epsilonValue "+epsilonValue + " correspondminSup "+correspondminSup);	
							computeVar = new ComputeVar();
							pfs2 mspx = new pfs2();
							String dataset ="Sequence_CCSDX";//"Sequence_ICD9DX_Sorted";//"SyntheticData_10";//"Sequence_ICD9DX_Sorted";//  "SyntheticData";//"Sequence_ICD9DX_Sorted";//"Sequence_CCSDX";//"Sequence_ICD9DX";//"Set_ICD9DX";//"Sequence_ICD9DX";//"test_ICD9DX";//"Primary_ICD9DX";//"Primary_Multi_CCSDX";//"Primary_CCSDX";//"Primary_ICD9DX";//"Primary_CCSDX";//"Primary_ICD9DX";//"Primary_CCSDX";//"Primary_CCSDX";//"Primary_ICD9DX";//"Multi_CCS_PR";//"CCS_PR";//"ICD9_PR";//"Primary_ICD9DX";//"Primary_Multi_CCSDX";//"Primary_CCSDX";//"Primary_Multi_CCSDX";//"Primary_CCSDX";//"Primary_Multi_CCSDX";//"Primary_CCSDX";//"Primary_Multi_CCSDX";//"Primary_CCSDX";//"Primary_Multi_CCSDX";//"Primary_CCSDX";//"Primary_Multi_CCSDX";//"Primary_CCSDX";//"Primary_Multi_CCSDX";//"Primary_CCSDX";//"Primary_Multi_CCSDX";//"Primary_CCSDX"; //"Primary_ICD9DX"; //"Primary_CCSDX"; ////"Primary_CCS";//"Primary_ICD9";//"CCS_SingleDataSet" ;//"CCS_Multi";//"CCS_Single"; //"ICD9";//"ICD9_3Levels";//"ICD9";//"House";
						
							maxFreSeqLen =freSeq;
							limitLen =lmLength;//10;//9;//3;//6; //30;
							dbSize =299445;//10001;//299445;//10001;//299445;//5;//299445;//299567;//299445;//40358;//299445;//52511;//36180; //43357; //61008; //62994;//61008; //40986;
							sampleSize = (int) Math.ceil((dbSize / (double) (maxFreSeqLen - 1)));
							minsup = correspondminSup;//0.009; //01;//0.00004917387;// 0.01;// 0.00004917387;//0.02;//0.00004917387;  //0.38;
							lower = 0.2;
							int epsilon=epsilonValue.intValue();//2;//4;//6;//8;//10;//18;//16;//14;//12;
							double e1 =epsilonValue *10/100;//0.1;//0.02;//0.04;//0.06;//0.08;//0.1;//0.18;// 0.16;//0.14;//0.14;//0.12;//0.1;//0.3;//0.2;  // 0.3;  // 0.1;
							double e2 =epsilonValue *45/100;//0.45;//0.09;//0.18;//0.27;//0.36;//0.45;//0.81;//0.72;//0.63;//0.63;//0.54;//0.45;//1.35;//0.90; // 1.35; // 0.45;
							double e3 =epsilonValue *45/100;//0.45;//0.09;//0.18;//0.27;//0.36;//0.45;//0.81;//0.72;//0.63;//0.54;//0.45;//1.35;//0.90; // 1.35; // 0.45;
			
							System.out.println("correspondminSup: "+correspondminSup+" epsilonValue: " + epsilon +" maxFreSeqLen: "+maxFreSeqLen + " limitLen : "+lmLength + " e1 =" +e1 +" e2 =" + e2 +" e3 =" +e3 );
		
 							////////////////////////////////////////////////////////////////////////////
							String src = ".\\dataset\\" + dataset + "\\" + dataset + ".dat";
							String dict = ".\\dataset\\" +dataset+"\\"+ dataset+"_dictionary.dat";//".\\dataset\\" + dataset + "\\"+ dataset+"_dictionary" + ".dat";
							String dest = ".\\dataset\\" +dataset+"\\" + dataset + "Loop_" +loopValue+"_"+ minsup +"_L_"+limitLen+"_E_"+epsilonValue +"_m_"+maxFreSeqLen + "_newMspx.txt"; // ".\\dataset\\" + dataset + "\\" + dataset + "_" + minsup +"_L_"+limitLen+"_E_"+epsilon +"_m_"+maxFreSeqLen + "_newMspx.txt";
							String correctSeqPath = ".\\dataset\\" +dataset+"\\" + dataset + "_" + minsup + "_-1_gsp.txt"; // ".\\dataset\\" + dataset + "\\" + dataset + "_" + minsup + "_-1_gsp.txt";
							prefix = ".\\dataset\\" + dataset + "\\sample\\" + freSeq+"\\";							// sample  

						
							System.out.println("sample begin...");
 							long stime = System.currentTimeMillis();
						    SampleDataBase.sampleDataBase(src, prefix, maxFreSeqLen - 1);
							long etime = System.currentTimeMillis();
							System.out.println("Sample database, run time: " + (etime-stime) + "ms");
							System.out.println("sample done~");
							//  
						   	mspx.newInitial_phase(dict,src, dest, e1);
							
		 					System.out.println("initial phase done~");
							// super maximal frequent sequence set
						  	List<String> superMFS = mspx.button_up_phase(src, dest, e2, e3);
					 	 	System.out.println("frequent: " + superMFS.size());
					 		System.out.println("button up phase done~");
						  //	Constract.constract(correctSeqPath, dest);
							System.out.println("Done dest" + dest );
						}
					}
				}
			}
		}
	}
	
	/**
	 * @param src
	 * @param dest
	 * @param e1
	 */
	private void newInitial_phase(String dict,String src, String dest, double e1) {
				
		File file = new File(dest);
		try {
			if (file.exists())
				file.delete();
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			superFrequentList = updateCountDictData(dict,src, dest, e1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (superFrequentList != null)
			globleFrequentList.addAll(superFrequentList);
		return;
	}


	private HashMap<String, Integer> readDictData(String dict) {
		
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		
		// Upload the data from the dictionary 
		try {
			BufferedReader r = new BufferedReader(new FileReader(dict));
		//	 r.readLine();
			String seq;// = null;
			while ((seq = r.readLine()) != null) { 
				seq = seq.replaceAll("\"", "");
				seq=seq.replaceAll("\\s+","");
				countMap.put(seq, 0);
		}
			r.close();		
	
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return countMap;
	}		
	


	private List<String> updateCountDictData(String dict,String src, String dest, double privacy) throws FileNotFoundException, IOException {
		List<String> resultList = new ArrayList<String>();
		
		HashMap<String, Integer>  missingSeq = new HashMap<String, Integer>();
		HashMap<String, Integer>  countMap1 = readDictData(dict);

		BufferedReader reader = new BufferedReader(new FileReader(src));
		int nMaxSeqLen = 1;
      //  reader.readNext(); // skip the header
        String seq=null;
        while ((seq = reader.readLine()) != null) {
    		Set<String> itemsets = new HashSet<String>((Arrays.asList(seq.split("\\s+|;"))));
    		itemsets.removeAll(Collections.singleton(""));
    	//	System.out.println(" itemsets " + itemsets);

		 	if (itemsets.size() < 1)
				continue;
			else if( itemsets.size() > nMaxSeqLen ){
				nMaxSeqLen = itemsets.size();
			}
		//	Set<String> itemOccuredList = randomTrun( ccs_code );
		
			for (String item : itemsets) {
				if (!countMap1.containsKey(item)) {//if (!countMap.containsKey(item)) {
					 if(!missingSeq.containsKey(item)){
					 	missingSeq.put(item, 1);
        			}else{
        				missingSeq.put(item, missingSeq.get(item) + 1);
        			}
        		}else {
        			countMap1.put(item, countMap1.get(item) + 1);//countMap.put(item, countMap.get(item) + 1);
        		}
       		}
       }
        reader.close();
  		int sensitivity = Math.min( countMap1.size(), limitLen);
		System.out.println( "The sensitivity is:" + sensitivity );
		for (String item : countMap1.keySet()) {
			double noisy;
			double support; 
 	 		do {
				noisy = Distribution.laplace(privacy, sensitivity);
				support =Math.abs( noisy + countMap1.get(item));
 	 		}while (support >dbSize);
		// 	System.out.println(" item : "+ item + " : " + countMap1.get(item) + " : " + support);
			
			if (support >= minsup * dbSize) {
				resultList.add(item);
				write(dest, item + ":" + countMap1.get(item) +":" +(long) support);
			}
		}
		
		System.out.println("total number of sequences in dictionary " + countMap1.size());
		System.out.println("total number of sequences in frequent 1- sequences " + resultList.size());
	 	if(missingSeq.size()>0){
		 	System.out.println("There are unmatching "+ missingSeq.size()+ " sequences in  the dBase");
			for (String item : missingSeq.keySet()) 
				System.out.println("....missing seq " + item + ": "+ missingSeq.get(item)+ "times");
	 	}
	return resultList;
    }
	
	private HashSet<String> randomTrun( String sSubOneSeq ){
		
		HashSet<String> countSet = new HashSet<String>();
		
		List<String> lSubOneSeq = Arrays.asList(sSubOneSeq.split("\\s+"));
		
		for( String seq : lSubOneSeq ){
			if( countSet.size() >= limitLen ){
				break;
			}
			
			countSet.add( seq );
		}
		
		return countSet;
	}

	/**
	 * get a superset of all frequent sequence
	 * 
	 * @param src
	 * @param sample
	 */
	private List<String> button_up_phase(String src, String dest, double pr1,
			double pr2) throws InterruptedException, ExecutionException {
		List<String> candidateList;
		int L = 2;
		while (superFrequentList.size() > 0 && L <= maxFreSeqLen) {
			System.out.println("generating frequent-" + L + " sequence~");
			long stime = System.currentTimeMillis();
			candidateList = generateCandidateSeqList(superFrequentList, L);
			long etime = System.currentTimeMillis();
			System.out.println("Generated candidate seq list, run time: " + (etime-stime) + "ms");
			if (candidateList.size() < 1)
				break;
			/*String sampleAddr ="";
			// 50% of the dataset for L=2
			if(L==2)
				sampleAddr = prefix + "slice_" + L + "_.txt";
			else
				sampleAddr = prefix + "slice_" + L + ".txt";*/
			String sampleAddr = prefix + "slice_" + L + ".txt";
		//	System.out.println("   L = " + " sampleAddr " + sampleAddr);
			stime = System.currentTimeMillis();
			superFrequentList = generateFrequentSeqList(src, sampleAddr, dest, candidateList, L, pr1, pr2 / (L - 1));
			etime = System.currentTimeMillis();
			System.out.println("Generated frequent seq list, run time: " + (etime-stime) + "ms");

			if (superFrequentList != null)
				globleFrequentList.addAll(superFrequentList);
			++L;
		}
		return globleFrequentList;
	}

	/**
	 *  
	 * 
	 * @param patternList
	 * @param patternLen
	 * @return
	 */
	private List<String> generateCandidateSeqList(List<String> patternList, int L) throws InterruptedException, ExecutionException {
		GSP gsp = new GSP();
		return gsp.generateCandidate(patternList, L);
	}

	/**
	 *  
	 * 
	 * @param originalDB
	 * @param sampleDB
	 * @param candidateSeqList
	 * @return
	 */
	private List<String> generateFrequentSeqList(String originalDB,
			String sampleDB, String dest, List<String> candidateSeqList,
			int fi, double pr1, double pr2) throws InterruptedException, ExecutionException {
		//  
		SmartTruncating2 st = new SmartTruncating2();
		Map<String, Integer> countMap = st.smartCount(candidateSeqList, sampleDB, limitLen, fi);

		List<String> positiveCandidateList = new ArrayList<String>();
		List<String> negativeCandidateList = new ArrayList<String>();
 //
		int sensitivity = Math.min(candidateSeqList.size(), Distribution.calculateFCT(limitLen, fi));
		
		System.out.println( "the sensitivity is: " + sensitivity );
		
		double support_small = Computer.computeVar2(computeVar, sampleSize, pr1, sensitivity, minsup, 0.5 - lower);
		System.out.println("small support: " + support_small);
		ExecutorService pool = Executors.newFixedThreadPool(ThreadNum);
		Future<String>[] negativeList = new Future[candidateSeqList.size()];
		String[] negResult = new String[candidateSeqList.size()];
		/*for (String candidate : candidateSeqList) {
			// TODO noisy add
			double estimateSupport = Distribution.laplace(pr1, sensitivity);
			if (countMap.containsKey(candidate))
				estimateSupport += countMap.get(candidate);
			if (estimateSupport >= support_small)
				negativeCandidateList.add(candidate);
			
		}*/
		for (int i = 0; i < candidateSeqList.size(); i++) {
			double estimateSupport = Distribution.laplace(pr1, sensitivity);
			String candidate = candidateSeqList.get(i);
			negativeList[i] = pool.submit(new FuncGenNegList(countMap,candidate, estimateSupport, support_small));
		}
		for (int i = 0; i < candidateSeqList.size(); i++) {
			negResult[i] = negativeList[i].get();
			if (negResult[i] != null)
				negativeCandidateList.add(negResult[i]);
		}
		pool.shutdown();
		System.out.println("negative size: " + negativeCandidateList.size()
				+ " candidate: " + candidateSeqList.size());
		

		int countNum = negativeCandidateList.size();
		
		countMap = countSequenceListFrequence(originalDB, negativeCandidateList, fi);
		// noisy add
		/*for (String candidate : negativeCandidateList) {

			int fre = countMap.get(candidate);

			double noisy;
			double support; 
			do {
				noisy = Distribution.laplace(pr2, countNum);
				support = fre + noisy;
			}while (support >dbSize);
			 
			
			//double noisy = Distribution.laplace(pr2, countNum);
			//double support = fre + noisy;

			if (support >= minsup * dbSize) {
				positiveCandidateList.add(candidate);
				//write(dest, candidate + ":" + support);
				write(dest, candidate + ": " + fre + ": "+ (long) support);
			//	write(dest, candidate + ": " + support);
				
			}
		}*/
		ExecutorService pool2 = Executors.newFixedThreadPool(ThreadNum);
		Future<Double>[] supportList = new Future[countNum];
		for (int i = 0; i < countNum; i++) {
			String candidate = negativeCandidateList.get(i);
			supportList[i] = pool2.submit(new FuncGenPosList(countMap, candidate, pr2, countNum, dest));
		}
		for (int i = 0; i < countNum; i++) {
			double curSupport = supportList[i].get();
			if (curSupport >= minsup * dbSize) {
				String candidate = negativeCandidateList.get(i);
				positiveCandidateList.add(candidate);
			}
		}
		pool2.shutdown();
		System.out.println("the frequent sequence number: " + positiveCandidateList.size());
		return positiveCandidateList;
	}

	/**
 	 * 
	 * @param src
	 * @param sequenceList
	 */
	private HashMap<String, Integer> countSequenceListFrequence(String src,
			List<String> sequenceList, int fi) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(src));
			String seq = null;
			Node root = new Node("");
			root.buildTree(sequenceList);
			while ((seq = r.readLine()) != null) {
				seq = seq.trim();
				if (seq.length() < 1)
					continue;
				root.matchTreeN1(seq);
			}
			r.close();
			return root.getSeqCount(fi);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

		
	private static void write(String dest, String seq) {
		try {
			FileWriter w = new FileWriter(dest, true);
			w.write(seq + "\r\n");
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}