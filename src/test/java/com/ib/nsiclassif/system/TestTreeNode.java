package com.ib.nsiclassif.system;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import javax.persistence.Query;

import org.junit.Test;
import org.primefaces.model.TreeNode;

import com.ib.nsiclassif.db.dao.PositionSDAO;
import com.ib.nsiclassif.db.dto.PositionS;
import com.ib.system.ActiveUser;
import com.ib.system.db.JPA;

public class TestTreeNode {
	
	
	
	@Test
	public void testTreeNode() {
		
		PositionSDAO dao = new PositionSDAO(ActiveUser.DEFAULT);
		
		try {
			
			Integer vesrionId = 10;
			
			
			String sql = " from PositionS where versionId = :ViD order by levelNumber";
			
			
			
			Query q = JPA.getUtil().getEntityManager().createQuery(sql);
			q.setParameter("ViD", vesrionId);
			
			ArrayList<PositionS> scheme = (ArrayList<PositionS>) q.getResultList();
			
			TreeNode node = dao.loadTreeData3(scheme, null, false, false, null, null);
			printNode(node, "");
			
			 
			
				
		
		
		
		} catch (Exception e) {			
			e.printStackTrace();
			fail();;
		}
	}
	
	
	public static void printNode(TreeNode node, String otmestvane) {
		if (node != null && node.getData() != null) {
			PositionS sItem = (PositionS) node.getData();
			System.out.println(otmestvane + sItem.getCode() + " "  + sItem.getCodeFull());
		}
		if (node.getChildren() != null) {
			for (TreeNode child : node.getChildren()) {
				printNode(child, otmestvane+ "\t");
			}
		}
		
	}
	

}
