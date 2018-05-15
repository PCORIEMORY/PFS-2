package pfs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import common.PatternDeal2;
import common.StrUtil;

public class SmartTruncating2 {
	int nCnt;
	public static int ThreadNum = 4;
	
	private final class writeHashMap implements Callable<HashMap<String, Integer>> {
		String curRow;
		int curFI;
		int curLimit;
		HashMap<String, Integer> cntMap;
		Node rt;
		List<String> CandList;
		
		public writeHashMap(String row, int fi, int limit, Node root, List<String> candidateList, HashMap<String, Integer> countMap) {
			curRow = row;
			curFI = fi;
			curLimit = limit;
			cntMap = countMap;
			rt =root;
			CandList= candidateList;
		}
		@Override
		public HashMap<String, Integer> call() throws Exception {
			List<Node> preKLevelNode = new ArrayList<Node>();
			Node subTree = generateSubTreeN1(rt, curRow, preKLevelNode, curFI);
			if(1 == curFI || StrUtil.strLen1(curRow.trim(), ";")<=curLimit){
				HashMap<String, Integer> tempMap = subTree.getSeqCountN1(curRow, curFI);
				for (String key : tempMap.keySet()) {
					if (cntMap.containsKey(key)) {
						cntMap.put(key,
								cntMap.get(key) + tempMap.get(key));
					} else {
						cntMap.put(key, tempMap.get(key));
					}
				}
			} else {
				HashMap<String, Node> preKLevelMap = new HashMap<String, Node>();
				for (Node t : preKLevelNode) {
					preKLevelMap.put(t.getTotleName(), t);
				}

				HashMap<String, Integer> mNoUse = new HashMap<String, Integer>();
				curRow = truncate(subTree, preKLevelNode, preKLevelMap, curLimit, mNoUse, curFI);
			}
			// ͳ�Ƴ���
			for (String candidate : CandList) {
				if (StrUtil.strContainN1(curRow, candidate, ";")) {//if (StrUtil.strContain(row, candidate, " ")) {
					if (cntMap.containsKey(candidate)) {
						cntMap.put(candidate, cntMap.get(candidate) + 1);
					} else {
						cntMap.put(candidate, 1);
					}
				}
			}
			return cntMap;
		}
	}
	public Map<String, Integer> smartCount(List<String> candidateList,
			String src, int limit, int fi) throws InterruptedException, ExecutionException {
		HashMap<String, Integer> countMap = new HashMap<String, Integer>();
		// generate CT tree based on Ck
		Node root = Node.constructTree(candidateList);
		HashSet<String> itemOccured = Node.itemSet;
		List<String> Buffer = new ArrayList<String>();
		ExecutorService pool = Executors.newFixedThreadPool(ThreadNum);
		Future<HashMap<String,Integer>> cMap = null;

		try {
			BufferedReader r = new BufferedReader(new FileReader(src));
			String row = null;
			nCnt = 0;
			while ((row = r.readLine()) != null) {
				nCnt++;
				row = row.trim();
				if (row.length() < 1)
					continue;
 	    	 	if(StrUtil.strLen1(row.trim(), ";")>limit)
 					row = shortRecord(row, itemOccured, fi);
				if (row.length() < 1)
					continue;
				Buffer.add(row);
			}
			r.close();
			for (int i = 0; i < Buffer.size(); i++) {
				row = Buffer.get(i);
				cMap = pool.submit(new writeHashMap(row, fi, limit, root, candidateList, countMap));
			}
			countMap = cMap.get();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pool.shutdown();
		return countMap;
	}

	/**
	 * truncate the sequence, and count the candidate by the way
	 * 
	 * @param preKLevelNode
	 * @param preKLevelMap
	 * @param limit
	 * @param countMap
	 * @param loop 
	 * @return
	 */
	private String truncate(Node root, List<Node> preKLevelNode,
			HashMap<String, Node> preKLevelMap, int limit,
			HashMap<String, Integer> countMap, int level) {
		
		List<String> candidateList = new ArrayList<String>();
		// c-nodes
		for (Node node : preKLevelNode) {
			node = node.getChild();
			while (node != null) {
				candidateList.add(node.getTotleName());
				node = node.getSibling();
			}
		}

		String result = " ";
		// greedy growth
		// keep adding items to seq till reach limit length
 		while (StrUtil.strLen1(result.trim(), ";" )< limit) {//.strLen(result, "\\s+|;") < limit) {
			if (preKLevelMap.size() == 0)
				break;
			List<Node> tmpSequence = getAllContainedSeq(result, preKLevelMap);

			if (tmpSequence.size() == 0) {
 				
				// get all the candidate sequences who have the largest number of children
				String maxCandidate = getMaxCandidateN1(root, preKLevelNode,
						candidateList, level);
 			//	System.out.println(">>>  maxCandidate " + maxCandidate);
				
				if (StrUtil.strLen1(result.trim() + " ; " + maxCandidate.trim(), ";") > limit)
					break;

				candidateList.remove(maxCandidate);
				// and get the child of k-1 node who can generate most k-1
				// nodes' children
				if (countMap.containsKey(maxCandidate))
					countMap.put(maxCandidate, countMap.get(maxCandidate) + 1);
				else
					countMap.put(maxCandidate, 1);
			//	System.out.println(" countMap " + countMap );

				int spaceIndex = maxCandidate.lastIndexOf(" ");
				String preKstr = maxCandidate.substring(0, spaceIndex);
				String lastItem = maxCandidate.substring(spaceIndex + 1);
	/*			System.out.println("  maxCandidate " + maxCandidate);
				System.out.println("  spaceIndex " + spaceIndex);
				
				System.out.println("  preKstr " + preKstr);
				System.out.println("  lastItem " + lastItem);
		*/		
				
				if(preKLevelMap.get(preKstr)!=null){
						Node preNode = preKLevelMap.get(preKstr);  // check here Yousef for Set_ICD9DX
					if(preNode.getChild()!= null){
						Node child = preNode.getChild();
		//				if(child != null)     // ADDED TO FIX THE NULLPOINTER EXCEPTIONS ERROR
							if (child.getName().equals(lastItem))
								preNode.setChild(child.getSibling());
							else {
								while (child.getSibling() != null) {
									if (child.getSibling().getName().equals(lastItem)) {
										child.setSibling(child.getSibling().getSibling());
										break;
									} else
										child = child.getSibling();
								}
							}
					}
				}
				result = StrUtil.strAdd(result, maxCandidate, " ; ");
				continue;
			} else {

				// get the most frequent item occurred in k-1 nodes' children
				HashMap<String, Integer> itemCountMap = new HashMap<String, Integer>();
				HashMap<String, List<Node>> itemSequenceMap = new HashMap<String, List<Node>>();
				for (Node t : tmpSequence) {
					Node child = t.getChild();
					while (child != null) {
						String childName = child.getName();
						if (!itemCountMap.containsKey(childName))
							itemCountMap.put(childName, 1);
						else
							itemCountMap.put(childName,
									itemCountMap.get(childName) + 1);
						if (!itemSequenceMap.containsKey(childName)) {
							List<Node> temp = new ArrayList<Node>();
							temp.add(t);
							itemSequenceMap.put(childName, temp);
						} else {
							List<Node> temp = itemSequenceMap.get(childName);
							temp.add(t);
							itemSequenceMap.put(childName, temp);
						}
						child = child.getSibling();
					}
				}
				String maxItem = null;
				int maxValue = 0;
				for (String key : itemCountMap.keySet()) {
					if (itemCountMap.get(key) > maxValue) {
						maxValue = itemCountMap.get(key);
						maxItem = key;
					}
				}
				if (maxItem != null) {

					List<Node> maxPreNodeList = itemSequenceMap.get(maxItem);
					// update candidates count
					for (Node maxPreNode : maxPreNodeList) {
						String temp = StrUtil.strAdd(maxPreNode.getTotleName(),
								maxItem, " ; ");
						candidateList.remove(temp);
						if (countMap.containsKey(temp))
							countMap.put(temp, countMap.get(temp) + 1);
						else
							countMap.put(temp, 1);
					}
					// update preKLevelMap
					result = StrUtil.strAdd(result, maxItem, " ; ");
					
					updatePreKLevelMap(preKLevelMap, preKLevelNode,
							itemSequenceMap.get(maxItem), maxItem);
				} else
					break;
			}
		}

		return result;
	}


	private String getMaxCandidateN1(Node root, List<Node> preKLevelNode,
			List<String> candidateList, int level) {
		int maxChileNum = 0;
		List<String> maxCandidateList = new ArrayList<String>();

		for (String candidate : candidateList) {
			int curChildNum = 0;
			List<Node> quene = new ArrayList<Node>();
			root.setSeq(candidate);
//			for(Node n : root.getChildList())
//				System.out.println(" node --> " + n.getTotleName() + " children # " + n.getChildNum() + " lvl " + n.getLevel());
			
			quene.add(root);
			while (quene.size() > 0) {
				Node temp = quene.remove(0);
			
				if (temp.getLevel() == level - 1 ) {
					curChildNum += temp.getChildNum();
				} else {
					
					Node tempChild = temp.getChild();
					while (tempChild != null) {
						String fstr = " " + temp.getSeq() + " ";
						String childName=tempChild.getName();
						if(tempChild.getTotleName().endsWith(";"))
							childName+=" ;";
	
						int index = fstr.indexOf(" " + childName+ " ");

						if (index != -1) {
							String substr = fstr.substring(
									index + childName.length() + 1)
									.trim();
							if(substr.trim().startsWith(";",0))
								substr=substr.substring(2);

							tempChild.setSeq(substr.trim());
							quene.add(tempChild);
						
						}
						tempChild = tempChild.getSibling();
					}
				}
			}
			

			if (curChildNum > maxChileNum) {
				maxChileNum = curChildNum;
				maxCandidateList = new ArrayList<String>();
				maxCandidateList.add(candidate);
			} else if (curChildNum == maxChileNum) {
				maxCandidateList.add(candidate);
			}
			
		}
		
		//System.out.println(" maxCandidateList "+ maxCandidateList);
		if (maxCandidateList.size() == 1)
			return maxCandidateList.get(0);
		else {
			int maxItemNum = 0;
			String maxStr = null;
			for (String maxCandidate : maxCandidateList) {
				int curItemNum = getCandidateItemNumN1(maxCandidate);
				
				if (curItemNum > maxItemNum) {
					maxItemNum = curItemNum;
					maxStr = maxCandidate;
//					maxStr = maxCandidate + " ; ";

				}
			}
			
			return maxStr;
		}
	}

	private String getMaxCandidateN2(Node root, List<Node> preKLevelNode,
			List<String> candidateList, int level) {
		int maxChileNum = 0;
		List<String> maxCandidateList = new ArrayList<String>();
		System.out.println("  candidateList " + candidateList);

		for (String candidate : candidateList) {
			System.out.println("  candidate " + candidate);
			int curChildNum = 0;
			List<Node> quene = new ArrayList<Node>();
			root.setSeq(candidate);
			for(Node n : root.getChildList())
				System.out.println(" node --> " + n.getTotleName() + " children # " + n.getChildNum() + " lvl " + n.getLevel());
		
			String canPrefix="";
			quene.add(root);
			while (quene.size() > 0) {
				Node temp = quene.remove(0);
				//System.out.println(" temp  "+temp.getTotleName() );
				
				if (temp.getLevel() == level - 1) {
					System.out.println(" >>temp  "+temp.getTotleName() + " lvl " + temp.getLevel()  + " child# " + temp.getChildNum());
					curChildNum += temp.getChildNum();
				} else {
					
					Node tempChild = temp.getChild();
					while (tempChild != null) {
						System.out.println(" temp  "+temp.getTotleName() + " lvl " + temp.getLevel()  + " child#" + temp.getChildNum() + " tempChild.getName() " + tempChild.getTotleName());

						String fstr = " " + temp.getSeq() + " ";
						String childName=tempChild.getName();
						if(tempChild.getTotleName().endsWith(";"))
							childName+=" ;";
						System.out.println(" childName " + childName);
						
						int index = fstr.indexOf(" " + childName+ " ");

						if (index != -1) {
							String substr = fstr.substring(
									index + childName.length() + 1)
									.trim();
							if(substr.trim().startsWith(";",0))
								substr=substr.substring(2);

							tempChild.setSeq(substr.trim());
							quene.add(tempChild);
							System.out.println(" add tempChild " + tempChild.getTotleName() + " child # "+tempChild.getChildNum());
						
						}
						tempChild = tempChild.getSibling();
					}
				}
			}
			

			if (curChildNum > maxChileNum) {
				maxChileNum = curChildNum;
				maxCandidateList = new ArrayList<String>();
				maxCandidateList.add(candidate);
			} else if (curChildNum == maxChileNum) {
				maxCandidateList.add(candidate);
			}
			System.out.println(" maxCandidateList " + maxCandidateList);
			System.out.println(" curChildNum " + curChildNum);

			System.out.println(" maxChileNum " + maxChileNum);
			
		}
		if (maxCandidateList.size() == 1)
			return maxCandidateList.get(0);
		else {
			int maxItemNum = 0;
			String maxStr = null;
			for (String maxCandidate : maxCandidateList) {
				int curItemNum = getCandidateItemNumN1(maxCandidate);

				if (curItemNum > maxItemNum) {
					maxItemNum = curItemNum;
					maxStr = maxCandidate + " ; ";
				}
			}
			if(maxStr.endsWith(";"))
				maxStr=maxStr.substring(0, maxStr.length()-1);
			
			return maxStr;
		}
	}

	
	private int getCandidateItemNumN1(String candidate) {
	//	String[] items = candidate.split("\\s+|;");
		List<String> itemSet = new LinkedList<String>(Arrays.asList(candidate.trim().split("\\s+|;")));
 		itemSet.removeAll(Arrays.asList("",null));//  removeAll(Arrays.asList("", null));
		return itemSet.size();
	}
	

	private void updatePreKLevelMap(HashMap<String, Node> preKLevelMap,
			List<Node> preKLevelNode, List<Node> list, String item) {
		for (Node node : list) {
			String str = node.getTotleName();
			Node child = node.getChild();
			if (child != null && child.getName().equals(item))
				node.setChild(child.getSibling());
			else {
				while (child.getSibling() != null) {
					if (child.getSibling().getName().equals(item)) {
						child.setSibling(child.getSibling().getSibling());
						break;
					}
					child = child.getSibling();
				}
			}
			if (node.getChild() != null)
				preKLevelMap.put(str, node);
			else {
				preKLevelNode.remove(node);
				preKLevelMap.remove(str);
			}
		}
	}

	private List<Node> getAllContainedSeq(String result,
			HashMap<String, Node> preKLevelMap) {
		List<Node> resultList = new ArrayList<Node>();

		for (String str : preKLevelMap.keySet()){
			if (StrUtil.strContainN1(result, str, ";"))//strContain(result, str, ";")) {
				resultList.add(preKLevelMap.get(str));
		}

		return resultList;
	}

	/**
	 * generate all contained preKLevelNode
	 * 
	 * @param node
	 * @param row
	 * @param preKLevelNode
	 * @return
	 */
	private Node generateSubTree(Node node, String row,
			List<Node> preKLevelNode, int k) {
		List<Node> quene1 = new ArrayList<Node>();
		List<Node> quene2 = new ArrayList<Node>();
		Node result = new Node(node);
		result.setChild(null);
		result.setSibling(null);
////
		String newRow="";
		
		List<String> itemsets = new ArrayList<String>(Arrays.asList(row.trim().split(";"))); // split("\\s+|;")));
		itemsets.removeAll(Arrays.asList(null,""));    // remove any null item

		for(String item: itemsets){
			List<String> itemset = new ArrayList<String>(Arrays.asList(item.trim().split("\\s+")));   // list of all items in the first  itemset
			itemset.removeAll(Arrays.asList(null,""));    // remove any null item
			newRow += item.trim() + " ; ";
			}
		 //	System.out.println( " item " + item + " itemset.size()" +itemset.size()); 
	 //	System.out.println( " newRow " + newRow + " ||| itemsets" +itemsets); 

		newRow = newRow.substring(0, newRow.length()-2);
		// 	System.out.println( "  after remove ;  newRow " + newRow ); 


		node.setSeq(row);//"a; b ; d f");//row);
		quene1.add(node);
		quene2.add(result);
		while (quene1.size() > 0) {
			Node t1 = quene1.remove(0);
			Node t2 = quene2.remove(0);

		/*	System.out.println("  t1 " + t1.getName() + " , getSeq(): " + t1.getSeq() );
			System.out.println("  t2 " + t2.getName() + " , getSeq(): " + t2.getSeq() );
*/
 

			Node child = t1.getChild();
			while (child != null) {
				String f_str = " " + t1.getSeq() + " ";
				int index = f_str.indexOf(" " + child.getName() + " ");
				if (index != -1) {
					String subStr = f_str.substring(
							index + child.getName().length() + 1).trim();
					child.setSeq(subStr);
					Node c_temp = new Node(child);
					c_temp.setChild(null);
					c_temp.setSibling(null);
					t2.addChild(c_temp);
					quene1.add(child);
					quene2.add(c_temp);
				}
				child = child.getSibling();
			}
		}
 //	}
		// get k-1 level nodes
		quene2.clear();
		quene2.add(result);
		while (quene2.size() > 0) {
			Node t = quene2.remove(0);
			Node child = t.getChild();
			while (child != null) {
				if (child.getLevel() == k - 1 && child.getChild() != null)
					preKLevelNode.add(child);
				quene2.add(child);
				child = child.getSibling();
			}
		}
		return result;
	}

	
	private static Node generateSubTreeN(Node node, String row,
			List<Node> preKLevelNode, int k) {
		
		System.out.println(" row  " + row );
		List<Node> quene1 = new ArrayList<Node>();
		List<Node> quene2 = new ArrayList<Node>();
		Node result = new Node(node);
		result.setChild(null);
		result.setSibling(null);
		
		node.setSeq(row);
		quene1.add(node);
		quene2.add(result);
		while (quene1.size() > 0) {
			Node t1 = quene1.remove(0);
			Node t2 = quene2.remove(0);
		 
			Node child = t1.getChild();
	
			while (child != null) {

				String f_str = " " + t1.getSeq() + " ";
				int index = f_str.indexOf(" " + child.getName() + " ");
				

				if (index != -1) {
					// Extract substring from index+1 till the end of this string 
					
					String subStr = f_str.substring(
							index + child.getName().length() + 1).trim();
				
					child.setSeq(subStr);
					// Then add this node to the new subtree
					
					Node c_temp = new Node(child);
					c_temp.setChild(null);
					c_temp.setSibling(null);
					t2.addChild(c_temp);
					quene1.add(child);
					quene2.add(c_temp);
				}
				// move to the next subTree/child
				child = child.getSibling();

			}
		}
		// get k-1 level nodes
		quene2.clear();
		quene2.add(result);
		while (quene2.size() > 0) {
			Node t = quene2.remove(0);
			Node child = t.getChild();
			while (child != null) {
			// 	System.out.println(" >>>>>>>>>>>>>>>child "+ child.getName() + " totName " + child.getTotleName() + "  level " + child.getLevel() + " #child "+ child.getChildNum());

				if (child.getLevel() == k - 1 && child.getChild() != null){
					preKLevelNode.add(child);
					System.out.println(" preKLevelNode added "+ child.getName() + " totName " + child.getTotleName() + " child "+ child.getChild().getTotleName() );

				}
				quene2.add(child);
				child = child.getSibling();
			}
		}
//	}
		return result;
	}

	private static Node generateSubTreeN1(Node node, String row,
			List<Node> preKLevelNode, int k) {
		
		List<Node> quene1 = new ArrayList<Node>();
		List<Node> quene2 = new ArrayList<Node>();
		Node result = new Node(node);
		result.setChild(null);
		result.setSibling(null);
		
		node.setSeq(row);
		quene1.add(node);
		quene2.add(result);
		
		while (quene1.size() > 0) {
			Node t1 = quene1.remove(0);
			Node t2 = quene2.remove(0);
		 
			Node child = t1.getChild();
			while (child != null) {

				String f_str = " " + t1.getSeq() + " ";
				String childName=child.getName();
				if(child.getTotleName().endsWith(";"))
					childName+=" ;";
				
				int index = f_str.indexOf(" " + childName + " ");

				if (index != -1) {
					// Extract substring from index+1 till the end of this string 
					String subStr = f_str.substring(index + childName.length() + 1).trim();
		
					if(subStr.trim().startsWith(";", 0)){
						subStr=subStr.substring(2);
					
					}
			//		System.out.println("  subStr " + subStr);

					child.setSeq(subStr.trim());
					// Then add this node to the new subtree
					
					
		//			System.out.println("   " + );
					Node c_temp = new Node(child);
					c_temp.setChild(null);
					c_temp.setSibling(null);
					t2.addChild(c_temp);
					quene1.add(child);
					quene2.add(c_temp);
				}
				// move to the next subTree/child
				child = child.getSibling();

			}
		}
		// get k-1 level nodes
		quene2.clear();
		quene2.add(result);
		while (quene2.size() > 0) {
			Node t = quene2.remove(0);
			Node child = t.getChild();
			while (child != null) {
			//	System.out.println(" child  " + child.getTotleName() + " lvl " + child.getLevel() );
				if (child.getLevel() == k - 1 && child.getChild() != null){
					preKLevelNode.add(child);
				//	System.out.println(" >>>>>chosen  " + child.getTotleName() );

				}
				quene2.add(child);
				child = child.getSibling();
			}
		}
//	}
		return result;
	}
	public static String shortRecord(String row, HashSet<String> itemOccured,
			int fi) {
	 	List<String> itemsets= new ArrayList<String>(Arrays.asList(row.trim().split("\\s+"))); // it was ";"
 	 	itemsets.removeAll(Arrays.asList(null,""));

 		LinkedList<String> items = new LinkedList<String>();
		items.addAll(itemsets);
 	
		items = oneShortN1(items, itemOccured, fi);
		//System.out.println(" after oneShort >>>>  " + items );
	
		items = twoShort(items, fi);
	
		//items = threeShort(items, fi);
		String result = "";
//		result = StrUtil.strAdd(result, items, 1, ";");
		result= items.stream().map(Object::toString).collect(Collectors.joining(""));

		return result;
	}
	
	/*
	 * Modified by Yousef
	 *  remove the irrelevant items
	 * 
	 */

	private static LinkedList<String> oneShort1(LinkedList<String> items,
 			HashSet<String> itemOccured, int fi) {
	LinkedList<String> temp = new LinkedList<String>();
	String pre = "";
	if(!items.isEmpty())
	for (String item : items) {
		if(item.trim().equals(";") && !pre.trim().equals(";")){
				temp.add(item);
				pre=";";
				continue;
		}else if (!item.trim().equals(";"))
				if(isContains(item,itemOccured)){
					temp.add(item);
					pre=item.substring(0);
		}
	}
	return temp;
}

	private static LinkedList<String> oneShortN1(LinkedList<String> items,
 			HashSet<String> itemOccured, int fi) {
	LinkedList<String> temp = new LinkedList<String>();
	String pre = " ";
	int count = 0;
	String last = " ";

	if(!items.isEmpty())
	for (String item : items) {
//		if(item!=null)
		if(item.equals(";") && !last.trim().equals(";")){
				temp.add(item);
				last=item.trim();
				continue;
		}else if (!isContains(item,itemOccured)){
					continue;
					
		}else if (pre == null || !pre.equals(item)) {
			temp.add(item);
			pre = item;
			last=item.trim();

			count = 1;

		} else {
			if (count < fi){
				temp.add(item);
				last=item.trim();
			}
			++count;
		}
	}
	return temp;
}
	
	private static LinkedList<String> oneShort(LinkedList<String> items,
 			HashSet<String> itemOccured, int fi) {
	LinkedList<String> temp = new LinkedList<String>();
	String pre = null;
	int count = 0;
	if(!items.isEmpty())
	for (String item : items) {
//		if(item!=null)
	if(!temp.isEmpty()){
			if(item.equals(";") && !temp.getLast().equals(";")){
				temp.add(item);
				continue;
			}
		}else if(item.equals(";")){
				temp.add(item);
				continue;

	//	if(item.equals(";") &&	!temp.getLast().equals(";")){// && !temp.get(temp.size()-1).equals(";")){
			// before insert, check whether last item in linkedlist is ";" or not ";"
//			temp.add(item);
//			continue;

		//////////////////////   Issue Here. i need to modify this
		}else if //(!isContains(item,itemOccured))
			//continue;
				 (!itemOccured.contains(item))
					continue;
				
/*		boolean flag=false;
		for(String str: itemOccured)
			if(PatternDeal2.isPatternContained(str.trim(), item.trim()," "))
				flag=true;
		if(!flag)
		
		*/
		else if (pre == null || !pre.equals(item)) {
			temp.add(item);
			pre = item;
			count = 1;

		} else {
			if (count < fi){
				temp.add(item);
				
			}
			++count;
		}
	}
	return temp;
}
	
	/*
	 * 
	 * New method to check whether an item is occurring in a HashSet
	 */
private static boolean isContains(String item, HashSet<String> itemOccured){
	boolean flag=false;
	for(String str: itemOccured)
		if(PatternDeal2.isPatternContained(str.trim(), item.trim()," "))
			flag=true;

	return flag;
}



	private static LinkedList<String> twoShort(LinkedList<String> items, int fi) {
		String seq = items.stream().map(Object::toString).collect(Collectors.joining(" "));
	//	System.out.println("  s "+ s);
		
		items = new LinkedList<String>(Arrays.asList(seq.split(";")));
		items.removeAll(Arrays.asList(null,""));    // remove any null items
		LinkedList<String> temp = new LinkedList<String>();
		int count = 0;
		String curr="";
		
		for (String item : items) {

			if (curr.isEmpty()){ 
				curr=item;
				count =1;
			}else if (curr.trim().equals(item.trim()))  
				count+= 1;
			 else if (!curr.trim().equals(item.trim())){ 
				curr=item;
				count= 1;
			}
			if(count<=fi){ 
				temp.add(item);
				temp.add(";");
				
				curr=item;
			}else 
 				curr=item;
			
		}
		if(!temp.isEmpty())
			if(temp.getLast().trim().equals(";"))
				temp.removeLast();
		return temp;
	}

	private static LinkedList<String> threeShort(LinkedList<String> items,
			int fi) {
		LinkedList<String> temp = new LinkedList<String>();
		ArrayList<String> t = new ArrayList<String>();
		int count = 0;
		int index = 0;
		for (String item : items) {
			if (t.size() <= 1) {
				temp.add(item);
				t.add(item);
			} else if (t.size() == 2) {
				if (!(t.get(0).equals(item) && t.get(1).equals(item))) {
					t.add(item);
					count = 1;
				}
				temp.add(item);
			} else {
				if (t.get(index).equals(item)) {
						temp.add(item);
						++index;
						if (index == 3) {
							++count;
							if( count > fi ){
								temp.removeLast();
								temp.removeLast();
								temp.removeLast();
							}
							index = 0;
						}
				} else {
					temp.add(item);
					t.remove(0);
					t.add(item);
					index = 0;
					count = 1;
				}
			}
		}
		return temp;
	}
	
	public static void main(String[] args) {
		String itemocc="427.89, 427.1, 465.9, V43.1, 461.9, 459.81, 250.02, 367.4, 250.00, V17.3, V67.09, 781.2, 785.1, 786.05, V76.11, 427.81, 785.2, 433.10, V76.12, 729.5, 785.6, 367.1, 729.1, 593.9, 724.3, 724.4, 724.2, 365.01, 477.9, 786.09, 794.31, 473.9, 607.84, 280.9, V05.3, 585.3, 401.9, 585.9, V17.49, 783.21, 789.00, 585.6, 466.0, 274.9, 493.90, 530.81, 365.11, V45.81, V45.82, 278.00, 428.0, 490, 443.9, 401.1, 424.0, V06.1, 424.1, 346.90, V45.89, 278.01, 733.90, 379.21, 496, V14.8, 780.2, V14.5, V03.82, 780.4, 784.0, 412, 403.90, 403.91, 788.1, 788.41, 724.5, 285.9, 455.0, V70.0, V72.31, V67.00, 427.31, 723.1, 276.51, 787.01, V58.11, 414.00, 429.9, 787.02, 429.3, 796.2, 584.9, 780.52, V12.54, 478.19, 414.01, 722.52, V72.83, V72.81, 314.00, 425.4, 719.41, V64.2, V58.66, V58.65, 599.0, 311, V58.67, 719.49, 625.9, V76.51, V58.61, 719.47, 719.46, 719.45, 300.00, 564.00, 787.91, 380.4, 553.3, 729.81, V58.69, 780.60, 366.10, 244.9, V10.3, V14.0, V14.2, 793.19, 790.29, 786.59, 733.00, V76.43, 786.50, 787.20, 211.3, 276.8, 714.0, 238.2, 375.15, 780.79, 272.4, 692.9, 272.2, V58.44, 782.0, V16.3, 174.9, 272.0, 782.1, 327.23, 782.3, 600.00, 786.2, 366.16, E928.9, 305.1, V15.82, 616.10, 724.02, 721.3, 721.0, 268.9, 627.2, 715.90, 462, V04.81, 338.29, 715.96, 562.10, 715.16";
		
		
		
		HashSet<String> itemOccured = new HashSet<String>(Arrays.asList(itemocc.split("\\s+")));
			
		int fi = 7;
		String str="465.9 786.2 ; 250.00 ; 790.5 ; 250.00 272.4 530.81 782.0 ; 250.00 477.9 530.81 788.30";
		String stra="465.9 786.2 ; , 250.00 ; , 790.5 ; , 250.00 272.4 530.81 782.0 ; , 250.00 477.9 530.81 788.30";
	
		LinkedList<String> items = new LinkedList<String>(Arrays.asList(str.trim().split("\\s+")));
		List<String>  candidateList=new ArrayList<String>(Arrays.asList(stra.trim().split(",")));
		
		Node root = Node.constructTree(candidateList);
		 
 
		
	//	System.out.println("  itemset " + root.itemSet + " items " + root.ge);
	
		//	LinkedList<String> items, 		HashSet<String> itemOccured, int fi) {
		LinkedList<String> result = new LinkedList<String>();
		System.out.println("  itemOccured " + itemOccured);
		System.out.println("  items " +items );
		
		result=oneShort(items, itemOccured, 7);
 		
		System.out.println("  result " +result );
		if(result.getLast().equals(";"))
			result.removeLast();
		result=twoShort(result, 7);
 		
		System.out.println("  result " +result );
		
		String result1 = "";
		String s = result.stream().map(Object::toString).collect(Collectors.joining(""));

		System.out.println("  result1 " +result1 );
		System.out.println("  s " +s );

		

}
}
