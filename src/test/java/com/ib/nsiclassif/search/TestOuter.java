package com.ib.nsiclassif.search;

import org.junit.Test;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.ib.nsiclassif.db.dto.TreeHolder;
import com.ib.nsiclassif.db.dto.TreeHolderLang;
import com.ib.system.exceptions.DbErrorException;



public class TestOuter {
	
	
	@Test
	public void testOuterTree() { 
		
		
		try {
			
			Integer lang = 1;
			
			TreeSearch tcs = new TreeSearch();
			tcs.init();
			
			DefaultTreeNode tree = tcs.buildTree("IE", "Измерителни единици", lang);
			
			
			System.out.println(tree.getChildCount());
			
			for (TreeNode child : tree.getChildren()) {				
				TreeHolder thC = (TreeHolder) child.getData();
				TreeHolderLang lMap = thC.getLangMap().get(lang);
				if (lMap != null) {
					System.out.println(lMap.getIdent() + " - " + lMap.getName());
				}else {
					System.out.println("N/A - N/A");
				}
				for (TreeNode vChild : child.getChildren()) {
					TreeHolder thV = (TreeHolder) vChild.getData();
					TreeHolderLang lMapV = thV.getLangMap().get(lang);
					if (lMapV != null) {
						System.out.println("\t" + lMapV.getIdent() + " - " + lMapV.getName());
					}else {
						System.out.println("\tN/A - N/A");
					}
					for (TreeNode tChild : vChild.getChildren()) {
						TreeHolder thT = (TreeHolder) tChild.getData();
						TreeHolderLang lMapT = thT.getLangMap().get(lang);
						if (lMapT != null) {
							System.out.println("\t\t" + lMapT.getIdent() + " - " + lMapT.getName());
						}else {
							System.out.println("\t\tN/A - N/A");
						}
					}
				}
				
			}
			
			
		} catch (DbErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}