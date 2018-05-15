/*
 * coded by Yousef
 * 
 */

package common;
 

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class PatternDeal2 {

	public static void main(String[] args) {
	//	getAllSubSequence("c q;a b d c;e f g;f2 g3 g4;3 5 1;765.12",";");//152.23 232.22 322.00 V32.01",";");//"a;b c ; d; c d e", ";");
//		isPatternContained("a", "a", " ");
		List<String> list = new ArrayList<String>(Arrays.asList("c ; d, c ; e, c ; b, c ; c, c ; f, b ; a, b ; b, d ; a, d ; b, d ; c, d ; d, d ; e, d ; f, a ; b, e ; f, a ; c, e ; d, a ; a, e ; e, e ; b, e ; c, e ; a, b c, b e, b d, d e, f ; c, b f, f ; d, f ; e, d f, f ; f, f ; a, f ; b, a b, a d, a ; f, a c, a ; d, a f, c d, a ; e, a e, c f, c e, e f, b ; c, b ; d, b ; e, b ; f, c ; a".split(";|,")));
	
		for(String str: list)
			
			if(isPatternContainedAll(" b x b a d c ; a b ; a e f ; c d", str,";"))
					System.out.println(" contain  " + str);
			else
				System.out.println(" Not contain  " + str);

		
	}

	/**
	 * get all subsequence of the pattern, but don't contain itself
	 * 
	 * @param sequence
	 * @param delimiter
	 *          
	 * @return
	 */
	public static List<String> getAllSubSequence(String sequence,
			String delimiter) {
		
	//	StringBuffer buffer = new StringBuffer();
		
		if(sequence.length() == 0)
			return null;
		LinkedHashSet<String> subSequenceSet = new LinkedHashSet<String>();    // linkedhashset to preserve the order of insertion
		List<LinkedHashSet<String>> mdlsubSequenceSet = new ArrayList<LinkedHashSet<String>>();
		LinkedHashSet<String> mdlsubSetSubSequenceSet = new LinkedHashSet<String>();
		
		List<List<String>> mdlListCombine = new ArrayList<List<String>>();
		
		List<List<String>> mdlsubSequenceSetList = new ArrayList<List<String>>();


		// Extract itemsets from the given sequence
		List<String> itemsets = new ArrayList<String>(Arrays.asList(sequence.split(";"))); // split("\\s+|;")));
		itemsets.removeAll(Arrays.asList(null," "));    // remove any null item

		// generate contiguous subsequence
		if (itemsets.size() == 1) {
			List<String> itemset = new ArrayList<String>(Arrays.asList(itemsets.get(0).split("\\s+")));   // list of all items in the first  itemset
			itemset.removeAll(Arrays.asList(null," "));    // remove any null item
			if (itemset.size()>1) {
				// keep 1st item from itemset
				subSequenceSet=generateContigiousSeq(itemset);
				// drop 1st item from itemset
		//		itemset.remove(0);
		//		subSequenceSet.addAll(generateContigiousSeq(itemset));
				
		}
		}else {                        //   sequence has more than one itemset

			String fstseperator=" ; ";
			String lstseperator=" ; ";
			
			int seqSize=itemsets.size();
			// drop 1st item from 1st itemset and/or last item from last itemset
			List<String> itemset_fst = new ArrayList<String>(Arrays.asList(itemsets.get(0).split("\\s+")));   // all items in the 1st itemset
			itemset_fst.removeAll(Arrays.asList(null,""));    // remove any null items
		
			List<String> itemset_lst = new ArrayList<String>(Arrays.asList(itemsets.get(seqSize-1).split("\\s+")));   // all items in the last itemset
			itemset_lst.removeAll(Arrays.asList(null,""));    // remove any null items
			


			// generate the contagious subsequences for ist and last itemsets
			
			LinkedHashSet<String> sub1stItemsets= generateContigiousSeq1stLst(itemset_fst,true);
			LinkedHashSet<String> sublstItemsets= generateContigiousSeq1stLst(itemset_lst,false);
			
			// extract, if exists, the  middle itemsets [1,...,n-1]
			String itemsets_mdl=" ";

			if(seqSize>2){   // there exists a middle itemset(s)
				for(int j=1;j< seqSize-1;j++){
					itemsets_mdl+=itemsets.get(j)+" ";
					if(j!=seqSize-2)
						itemsets_mdl+=" ; ";
				}
				
				// for each new sequence, we need to elaborate all possible combination in the middle itemsets.
				// first extract all the middle itemsets from 1..n-1
				// then, apply Cartesian product the produce all possible middle-subsequences
			
//				System.out.println(" itemsets_mdl  " +itemsets_mdl );

				//subSequenceSet.add(new1stItemset+itemsets_mdl+itemsets.get(seqSize-1));
				for(int j=1;j<itemsets.size()-1;j++){
					// generate a possible sequence
					List<String> currItem = new ArrayList<String>(Arrays.asList(itemsets.get(j).split("\\s+")));   // list of all items in the first  itemset
	 //				System.out.println(" current itemset "+itemsets.get(j));
					currItem.removeAll(Arrays.asList(null,""));    // remove any null item
					mdlsubSetSubSequenceSet.clear();
					mdlsubSequenceSet.clear();
					if (currItem.size()>1) {    // find all subsequences from 
						// keep 1st item from itemset
						mdlsubSetSubSequenceSet.addAll(generateContigiousSeq(currItem));
						mdlsubSequenceSetList.add(new ArrayList<String>(mdlsubSetSubSequenceSet));
					}else
						mdlsubSequenceSetList.add(currItem);
				}	// end for			

				// Cartesian product
				mdlListCombine=computeCombinations(mdlsubSequenceSetList) ;
	 			
//				System.out.println(" mdlListCombine " +mdlListCombine);

				subSequenceSet.clear();
				
	// generate complete subsequences
				

				for(List<String> subSeq:mdlListCombine ){
					subSeq.removeAll(Arrays.asList(null,""));    // remove any null item
					// modify/map the resulted sequences into one String 
					String newSubSeq="";
					for(String str: subSeq){
						if (str.isEmpty())
							continue;
						newSubSeq+= str+" ; ";
					}
					
					if(newSubSeq.lastIndexOf(";")!=-1)
						newSubSeq=newSubSeq.substring(0,newSubSeq.lastIndexOf(";"));
					

					for(String fstitemset: sub1stItemsets){
						if(fstitemset.isEmpty())
							fstseperator="";
						else
							fstseperator=" ; ";
							
						for(String lstitemset: sublstItemsets){
							if(lstitemset.isEmpty())
								lstseperator="";
							else
								lstseperator="; ";

							// Original Sequence itself is not a part of its subsequences

	 	//				if(!(fstitemset+newSubSeq+lstseperator+lstitemset).replaceAll("\\s+","").equalsIgnoreCase(sequence.replaceAll("\\s+",""))) 
	 							subSequenceSet.add(fstitemset+fstseperator+newSubSeq+lstseperator+lstitemset);
	 						}
					}
				}
			}else{		// no middle itemsets.
				itemsets_mdl=" ";
				mdlsubSequenceSetList.clear();
				mdlListCombine.clear();
				String flstSeperator=" ; ";
				for(String fstitemset: sub1stItemsets){
					for(String lstitemset: sublstItemsets){
						if(fstitemset.isEmpty() || lstitemset.isEmpty())
							flstSeperator="";
						else 
							flstSeperator=" ; ";

 							subSequenceSet.add(fstitemset+flstSeperator+lstitemset);
 						}
				
			}
			}
//			itemsets_mdl=itemsets_mdl.substring(0,itemsets_mdl.length()-1);  // remove the last ";"


		}
		subSequenceSet.removeAll(Arrays.asList(null,""));    // remove any null item
		List<String> subSequences = new ArrayList<String>(subSequenceSet);
		// original sequence itself is not a part of the generated all subsequences
/*		for(String str:subSequences)
			System.out.println(" Result: " +str);
*/   		return subSequences.subList(1, subSequences.size());
	}
 
	
	public static <T> List<List<T>> computeCombinations(List<List<T>> lists) {
	    List<List<T>> currentCombinations = Arrays.asList(Arrays.asList());
	    for (List<T> list : lists) {
	        currentCombinations = appendElements(currentCombinations, list);
	    }
	    return currentCombinations;
	}
	
	
	public static <T> List<List<T>> appendElements(List<List<T>> combinations, List<T> extraElements) {
	    return combinations.stream().flatMap(oldCombination
	            -> extraElements.stream().map(extra -> {
	                List<T> combinationWithExtra = new ArrayList<>(oldCombination);
	                combinationWithExtra.add(extra);
	                return combinationWithExtra;
	            }))
	            .collect(Collectors.toList());
	}
	    

	/**
	 * @param items
	 * @param element_1 
	 * @return
	 */
	private static LinkedHashSet<String> generateContigiousSeq(List<String> itemset) {
		
		LinkedHashSet<String> currsubSequenceSet = new LinkedHashSet<String>();
		StringBuffer buffer = new StringBuffer();

        // drop one item for each iteration	
		ArrayList<String> element = new ArrayList<String>(itemset.subList(0, itemset.size())); 
		for (int j=0; j<itemset.size(); j++){
			element.remove(j);
			buffer.setLength(0);
			buffer.append(String.join(" ", element));
			currsubSequenceSet.add(buffer.substring(0, buffer.length() ));
			element = new ArrayList<String>(itemset.subList(0, itemset.size())); 
		
		}
		
//		2- drop 1st (last) item from ist (last) itemsets + any item 
			element = new ArrayList<String>(itemset.subList(1, itemset.size())); 

		buffer.setLength(0);
		buffer.append(String.join(" ", element));
		currsubSequenceSet.add(buffer.substring(0, buffer.length() ));

		
		for (int j=0; j<itemset.size()-1; j++){
			element.remove(j);
			buffer.setLength(0);
			buffer.append(String.join(" ", element));
			currsubSequenceSet.add(buffer.substring(0, buffer.length() ));
				element = new ArrayList<String>(itemset.subList(1, itemset.size())); 
		}

		
		return currsubSequenceSet;
	}

	/*
	 * flag:true for ist itemset, false for last itemset
	 * for first and last itemsets
	 * to generate the contagious subsequences for them
	 *  1- drop nothing at the first iteration, then one item for each iteration
	 *  2- drop 1st (last) item from ist (last) itemsets + any item 
	 */
	private static LinkedHashSet<String> generateContigiousSeq1stLst(List<String> itemset, Boolean flag) {
		
		LinkedHashSet<String> currsubSequenceSet = new LinkedHashSet<String>();
		StringBuffer buffer = new StringBuffer();

        // drop nothing at the first iteration, then one item for each iteration	
		ArrayList<String> element = new ArrayList<String>(itemset.subList(0, itemset.size())); 
		buffer.setLength(0);
		buffer.append(String.join(" ", element));
		currsubSequenceSet.add(buffer.substring(0, buffer.length() ));
	
		for (int j=0; j<itemset.size(); j++){
			element.remove(j);
			buffer.setLength(0);
			buffer.append(String.join(" ", element));
			currsubSequenceSet.add(buffer.substring(0, buffer.length() ));
			element = new ArrayList<String>(itemset.subList(0, itemset.size())); 
		
		}
		
//		2- drop 1st (last) item from ist (last) itemsets + any item 
		if(flag)
			element = new ArrayList<String>(itemset.subList(1, itemset.size())); 

		else
			element = new ArrayList<String>(itemset.subList(0, itemset.size()-1)); 

		buffer.setLength(0);
		buffer.append(String.join(" ", element));
		currsubSequenceSet.add(buffer.substring(0, buffer.length() ));

		
		for (int j=0; j<itemset.size()-1; j++){
			element.remove(j);
			buffer.setLength(0);
			buffer.append(String.join(" ", element));
			currsubSequenceSet.add(buffer.substring(0, buffer.length() ));
			if(flag)
				element = new ArrayList<String>(itemset.subList(1, itemset.size())); 

			else
				element = new ArrayList<String>(itemset.subList(0, itemset.size()-1)); 
		
		}

		return currsubSequenceSet;
	}

/*	public static List<String> getAllSubSequence(String pattern,
			String delimiter) {
		if(pattern.length() == 0)
			return null;
		HashSet<String> subSequenceSet = new HashSet<String>();
		String[] eles = pattern.split(delimiter);
		if (eles.length == 1) {
			//subSequenceSet.add(eles[0]);
		} else {
			for (int i = 0; i < eles.length; i++) {
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < eles.length; j++) {
					if (i == j)
						continue;
					buffer.append(eles[j] + delimiter);
				}
				subSequenceSet.add(buffer.substring(0, buffer.length() - 1));
			}
		}
		 System.out.println(pattern+":"+subSequenceSet.size());
		return new ArrayList<String>(subSequenceSet);
	}*/


	
	
	/**
	 * judge whether the pattern contain the subPattern
	 * 
	 * @param pattern
	 * @param subPattern
	 * @return
	 */
	public static boolean isPatternContained(String pattern, String subPattern,
			String delimiter) {
		if (subPattern.length() > pattern.length())
			return false;
		int fromIndex = -1;
		boolean flag = false;
		String[] eles = subPattern.split(delimiter);
		pattern = delimiter + pattern.trim() + delimiter;
		for (int i = 0; i < eles.length; i++) {
			String ch = delimiter + eles[i] + delimiter;
			fromIndex = pattern.indexOf(ch);
			if (fromIndex == -1) {
				flag = true;
				break;
			}
			pattern = pattern.substring(fromIndex + ch.length() - 1);
			// System.out.println('?' + pattern + '?');
		}
		if (flag)
			return false;
		else
			return true;
	}
	
	/**
	 * judge whether the pattern contain the subPattern
	 * 
	 * @param pattern
	 * @param subPattern
	 * @return
	 */
	public static boolean isPatternContainedAll(String sequence, String subSequence,
			String delimiter) {
		boolean flag=true;
		
		if (subSequence.length() > sequence.length())
			return false;
		List<String> itemsets = new ArrayList<String>(Arrays.asList(sequence.trim().split(";"))); // split("\\s+|;")));
		List<String> subItemsets = new ArrayList<String>(Arrays.asList(subSequence.trim().split(";"))); // split("\\s+|;")));

		itemsets.removeAll(Arrays.asList(null,""));    // remove any null item
		subItemsets.removeAll(Arrays.asList(null,""));    // remove any null item
		
		itemsets.replaceAll(String::trim);

		subItemsets.replaceAll(String::trim);
		int count=0;
		int tmp=0;
		int i=0;
		int j=0;
		
		while (i<subItemsets.size()){
			j=tmp;
			while(j<itemsets.size()){
				if(isPatternContained(itemsets.get(j).trim(), subItemsets.get(i).trim()," ")){
					count+=1;
					tmp=j+1;
					j=itemsets.size();
				}else{
					j+=1;
				}
			}
			i+=1;
		}
	if (count==subItemsets.size()){
		return true;
	
	}else{
			return false;
		}
	}
	
}
