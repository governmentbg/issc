package com.ib.nsiclassif.system;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.ib.nsiclassif.db.dto.Classification;
import com.ib.nsiclassif.db.dto.ClassificationLang;
import com.ib.system.ObjectsDifference;

public class TestDifference {
	
	
	@Test
	public void testCompare() {
		Classification c1 = new Classification();
		
		c1.setClassType(1);
		c1.setClassUnit(2);
		c1.setFamily(3);
		
		ClassificationLang l1_1 = new  ClassificationLang();
		l1_1.setId(1);
		l1_1.setArea("Area");
		l1_1.setLang(2);
		l1_1.setClassif(c1);
		c1.getLangMap().put(2, l1_1);
		
		
		Classification c2 = new Classification();
		c2.setClassType(1);
		c2.setClassUnit(3);
		c2.setFamily(14);
		
		ClassificationLang l1_2 = new  ClassificationLang();
		l1_2.setId(1);
		l1_2.setArea("AreaNew");
		l1_2.setDescription("descr");
		l1_2.setLang(2);
		l1_2.setClassif(c2);
		c2.getLangMap().put(2, l1_2);
		
		ClassificationLang l2_2 = new  ClassificationLang();
		l2_2.setId(2);
		l2_2.setArea("Oblast");
		l2_2.setLang(1);
		l2_2.setClassif(c2);
		c2.getLangMap().put(1, l2_2);
		
		
		ObjectComparator comparator = new ObjectComparator(new Date(), new Date(), new SystemData());
		List<ObjectsDifference> diffs = comparator.compare(c1, c2);
		for (ObjectsDifference diff : diffs) {
			System.out.println(diff.getFieldName() + "\t" + diff.getOldVal() +  "\t" + diff.getNewVal());
		}
	}

}
