package com.ib.nsiclassif;

import java.io.File;
import java.io.IOException;


import org.junit.Test;

import com.ib.system.utils.FileUtils;

public class Delme {

	@Test
	public  void delme1() {
		try {
			FileUtils.writeBytesToFile("/home/krasig/MyApps/Tmp/алабала.txt", "aaaaaaaaaaa".getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
