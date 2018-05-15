package pfs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Node {
	private String name;
	private String seq;
	public ArrayList<Integer> nIndex;			//iejr: used for mark the item start position in this.seq
	private Node sibling;
	private Node child;
	private int count;
	private String totleName;
	private int level;
	private Node f_node;
	public static HashSet<String> itemSet = new HashSet<String>();

	public Node(String item) {
		this.name = item;
		this.sibling = null;
		this.child = null;
		this.count = 0;
		this.seq = null;
		this.nIndex = new ArrayList<Integer>();
		this.level = 0;
		this.totleName = "";
		this.f_node = null;
	}

	public Node(Node node) {
		this.name = node.name;
		this.sibling = node.sibling;
		this.child = node.child;
		this.count = 0;
		this.seq = node.seq;
		this.nIndex = new ArrayList<Integer> ( node.nIndex );
		this.level = node.level;
		this.totleName = node.totleName;
		this.f_node = node.f_node;
	}

	public void addChild(String item) {
		Node newNode = new Node(item);
		newNode.level = this.level + 1;
		String prefix = this.totleName;
		if (this.totleName.length() > 0)
			prefix += " ";
		newNode.totleName = prefix + item;
		newNode.f_node = this;
		this.addChild(newNode);
	}

	public void addChild(Node node) {
		if (this.child == null) {
			this.setChild(node);
		} else {
			Node temp = this.child;
			node.sibling = temp;
			this.child = node;
		}
		node.f_node = this;
	}

	public void addChildList(String[] items, int beginIndex) {
		Node node = this;
		while (beginIndex < items.length) {
			String item = items[beginIndex++];
			node.addChild(item);
			itemSet.add(item);
			node = node.getChild();
		}
	}

	public List<Node> getChildList() {
		List<Node> resultList = new ArrayList<Node>();
		Node t = this.child;
		while (t != null) {
			resultList.add(t);
			t = t.sibling;
		}
		return resultList;
	}

	public void buildTree(List<String> itemsList) {
		if (itemsList == null || itemsList.size() < 1)
			return;
		for (String items : itemsList) {
			String[] itemArray = items.trim().split("\\s+|;");
			itemArray=Arrays.stream(itemArray).filter(s -> (s != null && s.length() > 0)).toArray(String[]::new); 
			String[] itemArray1 = items.trim().split("\\s+");
			itemArray1=Arrays.stream(itemArray1).filter(s -> (s != null && s.length() > 0)).toArray(String[]::new); 

			int i = 0;
			int j=0;

			Node f_node = this;
			Node node = this;
 			while (node != null) {
 	
				f_node = node;
				node = node.getChild();
				boolean find = false;
				while (node != null) {
					// a b ; c and a b c are two different itemsets with similar parent=a 
					
					String subSeq= "";
					for(int k=0;k<=j;k++){
						subSeq+=itemArray1[k]+" ";
					}
					// check next item, if it is ; add this to the subSeq
					if(j<itemArray1.length-1)
						if(itemArray1[j+1].equals(";")){//items.charAt(j+2)==';'){
							subSeq+=itemArray1[j+1];
							j++;
 
						}
					
					if (node.getName().equals(itemArray[i]) && node.totleName.equals(subSeq.trim())) {
						++i;
						j=j+1;
						find = true;
						break;
					}
					node = node.getSibling();
				}
				if (!find) {
					f_node.addChildList(items,itemArray, itemArray1, i,j);
					break;
				}
				if (i >= itemArray.length)
					break;
			}
		}
	}
	
	
	/**
	 * New method for sequence itemsets
	 * @param items
	 * @param itemArray
	 * @param beginIndex
	 */
	private void addChildList(String items, String[] itemArray, String[] itemArray1, int i_Index, int j_Index) {

		Node node = this;

		while (i_Index < itemArray.length) {
			String item="";

			item=itemArray[i_Index];
			String subSeq= "";
			for(int k=0;k<=j_Index;k++) 
				subSeq+=itemArray1[k]+" ";

			// check next item, if it is ; add this to the subSeq
			if(j_Index<itemArray1.length-1)
				if(itemArray1[j_Index+1].equals(";")){//items.charAt(j+2)==';'){
					subSeq+=itemArray1[j_Index+1];
					j_Index++;
			}
			
			node.addChild(item, subSeq.trim());
			itemSet.add(item);    // keep track of all unique items in the tree
			node = node.getChild();
			i_Index++;
			j_Index++;
		}
	}

	/*
	 * new method to add child that preserves the semicolon
	 *  
	 */

		public void addChild(String item, String subSeq) {
			
			Node newNode = new Node(item);
			newNode.level = this.level + 1;
/*			String prefix = this.totleName;
			if (this.totleName.length() > 0)
				prefix += " ";
			newNode.totleName = prefix + item+suffix;*/
			newNode.totleName=subSeq;
			newNode.f_node = this;
			this.addChild(newNode);
		}
		
	/**
	 * visit the tree by level
	 * 
	 * @param sequence
	 */
	public void matchTree(String sequence) {
		this.seq = " " + sequence + " ";
		List<Node> quene = new ArrayList<Node>();
		quene.add(this);
		while (quene.size() > 0) {
			Node f_node = quene.remove(0);
			Node node = f_node.child;

			while (node != null) {
				int index = f_node.seq.indexOf(" " + node.name + " ");
				if (index != -1) {
					++node.count;
					node.seq = f_node.seq.substring(index + node.name.length()
							+ 1);
					quene.add(node);
				}
				node = node.sibling;
			}
		}
	}

	
	public void matchTreeN1(String sequence) {
		this.seq = " " + sequence + " ";
		List<Node> quene = new ArrayList<Node>();
		quene.add(this);
		while (quene.size() > 0) {
			Node f_node = quene.remove(0);
			Node node = f_node.child;

			while (node != null) {
	 			String nodeName=node.getName();
				if(node.getTotleName().endsWith(";"))
					nodeName+=" ;";
			//	else nodeName+=" ";
		 		int index = f_node.seq.indexOf(" " + nodeName + " ");
				if (index != -1) {
					++node.count;
					String subStr=f_node.seq.substring(index + nodeName.length()+ 1);
			 		
					if(subStr.trim().startsWith(";", 0))
						subStr=subStr.substring(2);
					
					node.seq = subStr;//f_node.seq.substring(index + nodeName.length()+ 1);
					quene.add(node);

				}
				node = node.sibling;
			}
		}
	}
	private int getCount(String seq) {
		int result = 0;
		String[] items = seq.split("\\s+");
		int i = 0;
		Node node = this;
		while (node != null && i < items.length) {
			node = node.child;
			boolean find = false;
			while (node != null) {
				if (node.getName().equals(items[i])) {
					++i;
					find = true;
					break;
				}
				node = node.sibling;
			}
			if (!find)
				break;
		}
		if (i == items.length)
			result = node.count;
		return result;
	}

	public HashMap<String, Integer> getSeqCount(List<String> candidateList) {
		HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
		if (candidateList != null && candidateList.size() > 0) {
			for (String candidate : candidateList) {
				resultMap.put(candidate, getCount(candidate));
			}
		}
		return resultMap;
	}

	/**
	 * only suit to the matched tree
	 * 
	 * @param level
	 * @return
	 */
	public HashMap<String, Integer> getSeqCount(int level) {
		HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
		List<Node> quene = new ArrayList<Node>();
		quene.add(this);
		while (quene.size() > 0) {
			Node f_node = quene.remove(0);
			Node node = f_node.child;
			while (node != null) {
				if (node.child == null && level == node.level)
					resultMap.put(node.totleName, node.count);
				else
					quene.add(node);
				// TODO clean
				node.count = 0;
				node = node.sibling;
			}
		}
		return resultMap;
	}

	/**
	 * only suit to the matched tree
	 * 
	 * @param level
	 * @return
	 */
	public HashMap<String, Integer> getSeqCount() {
		HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
		List<Node> quene = new ArrayList<Node>();
		quene.add(this);
		while (quene.size() > 0) {
			Node f_node = quene.remove(0);
			Node node = f_node.child;
			while (node != null) {
				if (node.child == null)
					resultMap.put(node.totleName, node.count);
				else
					quene.add(node);
				// TODO clean
				node.count = 0;
				node = node.sibling;
			}
		}
		return resultMap;
	}

	public HashMap<String, Integer> getSeqCount(String sequence, int level) {
		this.matchTree(sequence);
		HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
		List<Node> quene = new ArrayList<Node>();
		quene.add(this);

		while (quene.size() > 0) {
			Node f_node = quene.remove(0);
			Node node = f_node.child;
			while (node != null) {
				if (node.child == null && level == node.level) {
					resultMap.put(node.totleName, node.count);

				}else
					quene.add(node);
				// TODO clean
				node.count = 0;
				node = node.sibling;
			}
		}

		return resultMap;
	}

	/*
	 * I think return the count of leaves
	 * 
	 * 
	 */
	public HashMap<String, Integer> getSeqCountN1(String sequence, int level) {
		this.matchTreeN1(sequence);
		HashMap<String, Integer> resultMap = new HashMap<String, Integer>();
		List<Node> quene = new ArrayList<Node>();
		quene.add(this);
		while (quene.size() > 0) {
			Node f_node = quene.remove(0);
			Node node = f_node.child;
			while (node != null) {
				if (node.child == null && level == node.level) 
					resultMap.put(node.totleName, node.count);
				else
					quene.add(node);
				// TODO clean
				node.count = 0;
				node = node.sibling;
			}
		}
		return resultMap;
	}
	public Node getF_node() {
		return f_node;
	}

	public void setF_node(Node f_node) {
		this.f_node = f_node;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Node getSibling() {
		return sibling;
	}

	public void setSibling(Node sibling) {
		this.sibling = sibling;
	}

	public Node getChild() {
		return child;
	}

	public void setChild(Node child) {
		this.child = child;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getSeq() {
		return seq;
	}

	public void setSeq(String seq) {
		this.seq = seq;
	}

	public String getTotleName() {
		return totleName;
	}

	public void setTotleName(String totleName) {
		this.totleName = totleName;
	}

	public static Node constructTree(List<String> strList) {
		itemSet = new HashSet<String>();
		Node root = new Node("");
		root.buildTree(strList);
		return root;
	}

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("a ; b ; c");
		list.add("b ; c ; d");
 		list.add("b ; d ; a");
 		list.add("b ; d ; b");
 		
/*		list.add("123.45 678.90 ; abc.ef");
		list.add("123.45 ; 678.90 ; abc.ef");
 		list.add("X123.45 678.90 ; abc.ef");*/
		Random rand = new Random();
		StringBuffer strTest2 = new StringBuffer();
		for( int i = 0;i < 250;i++ ){
			int nRand = rand.nextInt(20)%20;
			strTest2.append(Integer.toString(nRand) + " ");
		}
		
		long begin = System.currentTimeMillis();
	 	String seq = "b ; c ; d";
	//	list.add(str);
		Node root = constructTree(list);
		
 
		System.out.println("build ok!");
		HashMap<String, Integer> countMap = null;
		for( int i = 0;i < 10000;i++ ){
			countMap = root.getSeqCount(strTest2.toString(), 9);
		}
		long end = System.currentTimeMillis();
		System.out.println("time: " + (end - begin));
		System.out.println("get ok!");
		for (String key : countMap.keySet())
			System.out.println(key + ": " + countMap.get(key));
	}

	
	
	public int getChildNum() {
		int result = 0;
		Node t = this.child;
		while (t != null) {
			++result;
			t = t.sibling;
		}
		return result;
	}

	/**
	 * get the leaf number in level k
	 * 
	 * @param level
	 * @return
	 */
	public int getLeafNum(int level) {
		int result = 0;
		Stack<Node> tempStack = new Stack<Node>();
		tempStack.push(this);
		while (tempStack.size() > 0) {
			Node node = tempStack.pop();
			Node child = node.child;
			if (child == null && node.level == level)
				++result;
			while (child != null) {
				tempStack.push(child);
				child = child.sibling;
			}
		}
		return result;
	}

	/**
	 * get the leaf number in level k
	 * 
	 * @param level
	 * @return
	 */
	public List<String> getLeafNodes() {
		List<String> resultList = new ArrayList<String>();
		Stack<Node> tempStack = new Stack<Node>();
		tempStack.push(this);
		while (tempStack.size() > 0) {
			Node node = tempStack.pop();
			Node child = node.child;
			if (child == null)
				resultList.add(node.totleName);
			while (child != null) {
				tempStack.push(child);
				child = child.sibling;
			}
		}
		return resultList;
	}

	/**
	 * get the item number in node's path
	 * 
	 * @return
	 */
	public int getItemNumInPath() {
		HashSet<String> curItemSet = new HashSet<String>();
		String[] items = this.totleName.trim().split(" ");
		for (String item : items)
			curItemSet.add(item);
		return curItemSet.size();
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
