/**
 *   Modified the candidate sequences generation
 *   a;b  with a;c will generate: a;bc , a;b;c and a;c;b
 * 
 */

/*
 * coded by
 * 		* Shengzhi
 * modified/updated by: 
 *      * Yousef
 *      * Wenrui
 */

package gsp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import common.PatternDeal2;
import common.StrUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GSP {
	public static int ThreadNum = 4;
	private BufferedReader r;
	private FileWriter w;
	
	private final static class FuncCombinePattern implements Callable<List<List<String>>> {
		List<String> AllPatternList;
		String curPattern;
		int curPos;
		int numPattern;
		
		public FuncCombinePattern(List<String> patternList, String str1, int strpos, int numCand) {
			this.AllPatternList = patternList;
			this.curPattern = str1;
			this.curPos = strpos;
			this.numPattern = numCand;
		}
		@Override
		public List<List<String>> call() throws Exception {
			List<List<String>> JoinResult = new ArrayList<List<String>>();
			for (int j = 0; j < AllPatternList.size(); j++) {
				String str2 = AllPatternList.get(j);
				JoinResult.add(joinPattern(curPattern, str2, curPos, j, numPattern));
			}
			return JoinResult;
		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		//
		String sDataset ="CCS";
		int    dbSize   = 29405;
		double correspondThreshold=0.008;
		double threshold = dbSize * correspondThreshold;
		System.out.println("  threshold " +threshold );
		String src = ".\\dataset\\" + sDataset + "\\" + sDataset + ".dat";
		
		String dest = ".\\dataset\\" + sDataset + "\\" + sDataset + "_" +  correspondThreshold + "_-1_E_1_gspNew_May_28.txt";

		GSP gsp4 = new GSP();
		gsp4.initWriter(dest);

		long begin = System.currentTimeMillis();
		gsp4.callGSP(src, dest,threshold);
		long end = System.currentTimeMillis();
		System.out.println("Time consuming: " + (end - begin));
		gsp4.closeWriter();
		System.out.println("Done~");
	}

	private void callGSP(String src, String dest,  double threshold) throws InterruptedException, ExecutionException {
		List<String> k_patternList = firstScan(src, threshold);
		k_patternList.sort(null);
		int L = 1;
 		while (k_patternList.size() > 0) {
			System.out.println("mining frequent " + ++L + "-sequence...");

	 	k_patternList = countCandidate(src,
	 			generateCandidate(k_patternList,L), threshold);
 		}
	}

	/**
	 * 
	 * @param src
	 * @param length
	 * @param threshold
	 * @return
	 */
	public List<String> get_frequent_k_seq_List(String src, int length,
			double threshold, String dest1) throws InterruptedException, ExecutionException {
		int L = 1;
		List<String> k_patternList = firstScan(src, threshold);
	 	while (L < length && k_patternList.size() > 0) {
			System.out.println("generate..."+L);
			k_patternList = countCandidate(src,
					generateCandidate(k_patternList,L), threshold);
			L+=1;
			if (L == length)
				return k_patternList;
	 	}
		return k_patternList;
	}

	/**
	 * the first scan of the data set
	 * 
	 * @param src
	 * @return the 1-item sequence pattern list
	 */
	private List<String> firstScan(String src, double threshold) {
		Map<String, Integer> countMap = new TreeMap<String, Integer>();
		List<String> resultList = new ArrayList<String>();
		initReader(src);
		try {
			String seq = null;
			while ((seq = r.readLine()) != null) {
				seq = seq.trim();
				if (seq.length() < 1) 
					continue;
				Set<String> itemOccuredList = new HashSet<String>(            
						Arrays.asList(seq.split("\\s+|;")));  
				itemOccuredList.removeAll(Arrays.asList(null,"")); 
				
				for (String item : itemOccuredList) {
					if (!item.isEmpty()){
						if (!countMap.containsKey(item))  
							countMap.put(item, 1);
						  else  
							countMap.put(item, countMap.get(item) + 1);
					}
				}
			}
			r.close();
			for (String item : countMap.keySet()) {
				if (countMap.get(item) >= threshold) {
					resultList.add(item);
					write(item + ":" + countMap.get(item));// +
					}
			}
			System.out.println(countMap.size() + "," + resultList.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultList;
	}

	
	/**
	 * count the frequency of the candidate pattern sequence
	 * 
	 * @param src
	 * @param candidateList
	 * @param threshold
	 * @return pattern sequence
	 */
	public List<String> countCandidate(String src, List<String> candidateList,
			double threshold) {
		System.out.println("count candidate...");
		initReader(src);
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		List<String> resultList = new ArrayList<String>();
		try {
			String seq = null;
			while ((seq = r.readLine()) != null) {
				seq = seq.trim();
				if (seq.length() < 1)// || StrUtil.strLen(seq, " ") > limit)//
					continue;

				for (String candidate : candidateList) {
					if (PatternDeal2.isPatternContainedAll(seq, candidate, ";")) {
						if (countMap.containsKey(candidate))  
							countMap.put(candidate, countMap.get(candidate) + 1);
						  else  
							countMap.put(candidate, 1);
					}
				}
			}
			r.close();
			for (String candidate : countMap.keySet()) {
				if (countMap.get(candidate) >= threshold) {
					resultList.add(candidate);
					write(candidate + ":" + countMap.get(candidate));  
				}
			}
			System.out.println("Candidate size: " + candidateList.size()
					+ ", frequent size: " + resultList.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultList;
	}

	/**
	 * generate candidate (L+1)-pattern sequence by L-pattern sequence
	 * 
	 * @param patternList
	 * @param dest1 
	 * @param patternLen
	 * @return
	 */
	public List<String> generateCandidate(List<String> patternList, int L) throws InterruptedException, ExecutionException {
		System.out.println("generate " +L+ " candidate...");
		HashSet<String> resultSet = new HashSet<String>();
		//String[][] Results = new String[patternList.size()][patternList.size()];
		//List<List<String>> Results = new ArrayList<List<String>>();
		ExecutorService pool = Executors.newFixedThreadPool(ThreadNum);
		Future<List<List<String>>>[] joinedArray = new Future[patternList.size()];

		patternList.sort(String::compareToIgnoreCase);

		for (int i = 0; i < patternList.size(); i++) {
			String str1 = patternList.get(i);
			joinedArray[i] = pool.submit(new FuncCombinePattern(patternList, str1, i, L));
			/*for (int j = 0; j < patternList.size(); j++) {
				String str1 = patternList.get(i);
				String str2 = patternList.get(j);
				List<String> candidate = joinPattern(str1, str2, i, j,L);

				if(!candidate.contains(null)){
					for (String canValue: candidate){
						if (canValue != null && judgeCandidate(canValue, patternList, L)) 
							resultSet.add(canValue);
 					}
				}
			}*/						
		}
		for (int i = 0; i < patternList.size(); i++) {
			//Results.add(joinedArray[i].get());
			for (int j = 0; j < patternList.size(); j++) 
				if (!joinedArray[i].get().get(j).contains(null)) {
					List<String> curCand = joinedArray[i].get().get(j);
					for (String canValue: curCand) {
						if (canValue != null && judgeCandidate(canValue, patternList, L))
							resultSet.add(canValue);
					}
				}
		}
		pool.shutdown();
	//	System.out.println("resultSet " +L+ " resultSet..."+resultSet);

		return new ArrayList<String>(resultSet);
	}

	
	private static List<String> joinPattern(String str1, String str2, int i1, int j1, int l) {
		Boolean flag=false;
		// to store the joining result
	//	List<String> str=new ArrayList<String>();
		HashSet<String> str = new HashSet<String>();
		// this case only for frequent 1-sequence

		// clean the data entry		
		str1=str1.replaceAll("\\s+", " ");
		str2=str2.replaceAll("\\s+", " ");
		str1=str1.replaceFirst("^\\s*", "");
		str2=str2.replaceFirst("^\\s*", "");
		if(str1.endsWith(" "))
			str1=str1.substring(0, str1.length()-1);		
		if(str2.endsWith(" "))
			str2=str2.substring(0, str2.length()-1);
		if (l==2){//2|| (str1.split(";").length == 1 && str2.split(";").length == 1 && str1.trim().length()==1 && str2.trim().length()==1 )) {
			str.add(str1 + " ; " + str1);   // a;a
			if(i1!=j1){
//			str.add(0,str1 + " ; " + str1);   // a;a
				str.add(str1 + " ; " + str2);   // a;b
		//	str.add(str2 + " ; " + str1);   // b;a
		//	str.add(str2 + " ; " + str2);   // b;b
			}			
			if(i1<j1) //i!=j)//i<j)
				if(str1.compareTo(str2)<0)
					str.add(str1 + " "+ str2);        // (ab) same itemset
				else 
					str.add(str2 + " "+ str1);        // (ab) same itemset
			return new ArrayList<String>(str);
		}

		// for L>2
		//////////     locate and drop the 1st item in the 1st encounter set / last item in the last encounterset		
		List<String> fstItemsets = new ArrayList<String>(Arrays.asList(str1.trim().split(" ; "))); // split("\\s+|;")));
		fstItemsets.removeAll(Arrays.asList(null," "));    // remove any null item
		//	fstItemsets.replace(Arrays.asList("\\s+", " "));
		List<String> lstItemsets = new ArrayList<String>(Arrays.asList(str2.trim().split(" ; "))); // split("\\s+|;")));
		lstItemsets.removeAll(Arrays.asList(null," "));    // remove any null item

		// 1st itemset
		List<String> fstItemset = new ArrayList<String>(Arrays.asList(fstItemsets.get(0).trim().split("\\s+")));   // list of all items in the first  itemset
		fstItemset.removeAll(Arrays.asList(null,""));    // remove any null item
		// lst itemset
		List<String> lstItemset = new ArrayList<String>(Arrays.asList(lstItemsets.get(lstItemsets.size()-1).trim().split("\\s+")));   // list of all items in the first  itemset
		lstItemset.removeAll(Arrays.asList(null,""));    // remove any null item
			
		///
		// consider the case of: a;b and a;c to produce: a;b;c ,  a;c;b  and a; bc
		if(i1!=j1){
			if(fstItemsets.get(0).trim().equals(lstItemsets.get(0).trim()) && fstItemsets.size()>1 && lstItemsets.size()>1 ){
				// combine the str1(2nd to last) with str2(2nd to last)
				String subString=str1.substring(str1.indexOf(" ; ")+2).trim()+" "+str2.substring(str2.indexOf(" ; ")).trim();
				List<String> subItemsets = new ArrayList<String>(Arrays.asList(subString.trim().split(" ; "))); // split("\\s+|;")));
				subItemsets.removeAll(Arrays.asList(null,""));    // remove any null item
				List<String> subItemsets_str1 = new ArrayList<String>(Arrays.asList(str1.substring(str1.indexOf(" ; ")+2).trim().split(" ; "))); // split("\\s+|;")));
				subItemsets_str1.removeAll(Arrays.asList(null,""));    // remove any null item
				List<String> subItemsets_str2 = new ArrayList<String>(Arrays.asList(str2.substring(str2.indexOf(" ; ")+2).trim().split(" ; "))); // split("\\s+|;")));
				subItemsets_str2.removeAll(Arrays.asList(null,""));    // remove any null item

	 			// a; bc
				// no duplication is allowed
				Set<String> subItemsets_1 = new TreeSet<String>(Arrays.asList(subString.trim().split("\\s+|;"))); // split("\\s+|;")));
				subItemsets_1.remove("\\s+");//.removeAll(Arrays.asList(null,""));    // remove any null item
	 			if(subItemsets_1.size()>1){
	 				// convert set to list then sorted
	 				List<String> subItemsets_2 = new ArrayList<String>(); // split("\\s+|;")));
	 				subItemsets_2.addAll(subItemsets_1);
					subItemsets_2.removeAll(Arrays.asList(null,""));    // remove any null item
	 				Collections.sort(subItemsets_2);
		// 			System.out.println(" substr :" + subItemsets_2.stream().map(Object::toString).collect(Collectors.joining(" ")));
				
		 			// make sure not to generate a candidate >l+1
	 				if(subItemsets_2.size()<=l)
	 					str.add(fstItemsets.get(0).trim()+" ; "+subItemsets_2.stream().map(Object::toString).collect(Collectors.joining(" ")));
	 				else{  // 
	 					for(int p=0;p<subItemsets_2.size();p++){	 						
		 				String tmpResult="";
	 						for(int q=0;q<subItemsets_2.size();q++){
		 						if(p!=q)
		 							tmpResult+=subItemsets_2.get(q)+" ";		 						 						
		 					}
		 					str.add(fstItemsets.get(0).trim()+" ; "+tmpResult.trim());
	 					}
	 				}
	 	//			System.out.println(" str :" + str);
	 			}

	 			/// a;b;c  , a;c;b
	 			for(String tmp1: subItemsets_str1){	 				
	 				for(String tmp2: subItemsets_str2){
		 				List<String> tmpItemset_2 = new ArrayList<String>(Arrays.asList(tmp2.trim().split("\\s+"))); // split("\\s+|;")));
		 				tmpItemset_2.removeAll(Arrays.asList(null,""));    // remove any null item
		 				String tmpResult=fstItemsets.get(0).trim()+" ; "+tmp1+" ; " + tmp2;
		 				if(StrUtil.strLen1(tmpResult, ";")!=l+1) 
			 				for(String tmp3: tmpItemset_2){
	//		 					System.out.println(" str2 " + fstItemsets.get(0).trim()+" ; "+tmp1+" ; " + tmp3); 
			 					str.add(fstItemsets.get(0).trim()+" ; "+tmp1+" ; " + tmp3);
			 				}
		 				else
		 					str.add(tmpResult);
		 				List<String> tmpItemset_1 = new ArrayList<String>(Arrays.asList(tmp1.trim().split("\\s+"))); // split("\\s+|;")));
		 				tmpItemset_1.removeAll(Arrays.asList(null,""));    // remove any null item
		 				tmpResult=fstItemsets.get(0).trim()+" ; "+tmp2+" ; " + tmp1;
		 				if(StrUtil.strLen1(tmpResult, ";")!=l+1) 
			 				for(String tmp4: tmpItemset_1){
			 					str.add(fstItemsets.get(0).trim()+" ; "+tmp2+" ; " + tmp4);
			 				}
		 				else
		 					str.add(tmpResult);
	 					}	 						
	 				}
	 			}
	 		}

		// drop the 1st item
		// permute all the possible strings in 1st and lst itemset		
		// return all possible permutations of 1st itemset
		List<String> subStr_fst = new ArrayList<String>();
		List<String> subStr_Lst = new ArrayList<String>();
 		
		if(fstItemset.size()>1){
//			flag=true; 
 			ArrayList<ArrayList<String>> permList_fst =  permuteUnique(fstItemset);
 			for(List<String> permItemsets: permList_fst ){
 				permItemsets.remove(0);
 				String tmp="";
 				for(int i=0; i<permItemsets.size();i++)
 					tmp+=permItemsets.get(i).trim()+" ";
 				subStr_fst.add(tmp);
 	 		}		
 		}else 
 			subStr_fst.add(0,"");
 		if(lstItemset.size()>1){   //  remove an item from the last itemset 
			flag=true; 
			ArrayList<ArrayList<String>> permList_Lst = permuteUnique(lstItemset);
			for(List<String> permItemsets: permList_Lst ){
	 			permItemsets.remove(0);
				String tmp="";
				for(int i=0; i<permItemsets.size();i++)
					tmp+=permItemsets.get(i).trim()+" ";
				subStr_Lst.add(tmp);
	 		}		
	 	}else 
	 		subStr_Lst.add(0,"");

 		fstItemsets.remove(0);
	 	String mdlSubStr1="";
		if(!fstItemsets.isEmpty())
			mdlSubStr1=fstItemsets.stream().map(Object::toString).collect(Collectors.joining(" ; "));
		lstItemsets.remove(lstItemsets.size()-1);   // drop the last itemset	
		String mdlSubStr2="";	
		if(!lstItemsets.isEmpty())
			mdlSubStr2=lstItemsets.stream().map(Object::toString).collect(Collectors.joining(" ; "));		
		boolean equal=false;
		String mdlDelimietr_1="";
		if(!mdlSubStr1.isEmpty())
			mdlDelimietr_1=" ; ";
		String mdlDelimietr_2="";
		if(!mdlSubStr2.isEmpty())
			mdlDelimietr_2=" ; ";
		for(int i=0; i<subStr_fst.size();i++){
			if(!equal){
				String curSubStr1="";
				String curSubStr2="";
				for(int j=0; j<subStr_Lst.size();j++){
					if(!subStr_fst.get(i).trim().isEmpty())
						curSubStr1=subStr_fst.get(i).trim()+mdlDelimietr_1+mdlSubStr1;
					else
						curSubStr1=mdlSubStr1;					
					if(!subStr_Lst.get(j).trim().isEmpty())
						curSubStr2= mdlSubStr2+mdlDelimietr_2 + subStr_Lst.get(j).trim();
					else
						curSubStr2= mdlSubStr2;
					if(curSubStr1.trim().equals(curSubStr2.trim())){
						int index1=str1.lastIndexOf(" ; ");
						int index2=str2.lastIndexOf(" ; ");
						if(index1==-1 && index2==-1){  // only one long sequence with one itemset
							Set<String> subRes_x = new TreeSet<String>(Arrays.asList(str1.split("\\s+")));
							subRes_x.addAll(Arrays.asList(str2.split("\\s+")));////lstItemset.get(lstItemset.size()-1).trim()));
							str.add(subRes_x.stream().map(Object::toString).collect(Collectors.joining(" ")));
//								System.out.println(" str.add(): Res1 " +subRes_x.stream().map(Object::toString).collect(Collectors.joining(" ")));
						}else if(index1==-1 && index2!=-1)
							str.add(str1+" ; " + lstItemset.stream().map(Object::toString).collect(Collectors.joining(" ")));
						else if(index1!=-1 && index2==-1)
							str.add(str1.substring(0,str1.lastIndexOf(" ; ")+3)+lstItemset.stream().map(Object::toString).collect(Collectors.joining(" ")));
						else {
							if(lstItemset.size()<=1)
								str.add( str1+" ; "+lstItemset.get(lstItemset.size()-1));
							else{
								Set<String> subRes_y = new TreeSet<String>(Arrays.asList(str1.trim().substring(str1.lastIndexOf(" ; ")+3,str1.length()).split("\\s+")));
								subRes_y.addAll(Arrays.asList(lstItemset.get(lstItemset.size()-1).trim()));
								str.add( str1.substring(0,str1.lastIndexOf(" ; ")+3)+subRes_y.stream().map(Object::toString).collect(Collectors.joining(" ")));
							}
						}		
						equal=true;	
						str.removeAll(Arrays.asList(null,""));    // remove any null item
						break;
					}
				}
			}			
	//			if(!equal){
			if(str.isEmpty()){
				str.clear();
				str.add(null);
			}
		}
//			
 // 				for(String strx: str)
 //						System.out.println(" str " + strx);
  		for (Iterator<String> i = str.iterator(); i.hasNext();) {
  			String currStr = i.next();
  			if(StrUtil.strLen1(currStr, ";")<l)
	 			i.remove();
 		}
 	 	return new ArrayList<String>(str);//.stream().filter(string ->string.replaceAll("\\s+|;"," ").split("\\s+").length==l).collect(Collectors.toList())); 	
	}

	/**
	 * 
	 * permuteUnique method to find all the possible permutation of a given String
	 */

	public static ArrayList<ArrayList<String>> permuteUnique(List<String> str) {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		returnList.add(new ArrayList<String>());
	 
		for (int i = 0; i < str.size(); i++) {
			Set<ArrayList<String>> currentSet = new HashSet<ArrayList<String>>();
			for (List<String> l : returnList) {
				for (int j = 0; j < l.size() + 1; j++) {
					l.add(j, str.get(i));
					ArrayList<String> T = new ArrayList<String>(l);
					l.remove(j);
					currentSet.add(T);
				}
			}
			returnList = new ArrayList<ArrayList<String>>(currentSet);
		}
	 
		return returnList;
	}


	/**
	 * judge whether all the k-1 subsequence of candidate pattern sequence are
	 * contained by the k-1 pattern sequence set
	 * 
	 * @param candidate
	 * @param patternList
	 * @return
	 */
	
	
	private boolean judgeCandidate(String candidate, List<String> patternList , int L) {

		List<String> elements = PatternDeal2.getAllSubSequence(candidate, ";");//"\\s+|;");
		
		elements.removeAll(Arrays.asList(null," "));    // remove any null item
	//	candidate.add(candidate.removeIf(u -> u.length() <= L+1));

		List<String> filtered = elements.stream().filter(string ->string.replaceAll("\\s+|;"," ").split("\\s+").length>=L-1).collect(Collectors.toList());

		int count=0;
		int i=0;
		int j=0;
		boolean flag=false;
		while(i<filtered.size()) {
			while(j<patternList.size()){
				if(patternList.get(j).trim().equals(filtered.get(count).trim()) ){
					count+=1;
					j=patternList.size()+1;	

				}else
					j+=1;
			}
			if (count==filtered.size()){
				flag=true;
				break;
			}else if(count==0){
				flag=false;
				count=filtered.size();
				break;
			}else if(count>0 && count<filtered.size()){ 
				j=0;
			    i+=1;
			}
		}
		return flag;
	}

	private void initReader(String src) {
		try {
			r = new BufferedReader(new FileReader(src));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void initWriter(String dest) {
		try {
			w = new FileWriter(dest);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private void write(String content) {
		try {
			w.write(content + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	private void closeWriter() {
		try {
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
