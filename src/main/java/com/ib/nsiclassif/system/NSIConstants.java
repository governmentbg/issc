package com.ib.nsiclassif.system;

import com.ib.indexui.system.Constants;

/**
 * Константи за проекта nsiclassif
 *
 * @author unknown
 */
public class NSIConstants extends Constants {
	
	// Настройки за LDAP и тип логване  - сегашен вариант - прехвърлени са от DocuWork - ако се сменят тук трябва да се смени името на настройката в низа
	//*****************************************************************************************************************************************************************
	/** тил на логване в системата */
	public static final String	LOGIN_TYPE			= "DOCU_WORK_LOGIN_TYPE";
	/** тип на логване използвайки LDAP */
	public static final String	LOGIN_TYPE_LDAP		= "DOCU_WORK_LOGIN_TYPE_LDAP";
	/** тип на логване използвайки база данни */
	public static final String	LOGIN_TYPE_DATABASE	= "DOCU_WORK_TYPE_DATABASE";
	/** дефолтен домайн на потребителите, които влизат чрез LDAP протокол */
	public static final String	DEFAULT_LDAP_DOMAIN	= "DOCU_WORK_DEFAULT_LDAP_DOMAIN";
	//****************************************************************************************************************************************************************

	

	/** Код на класификация "Атрибути на позициите" */
	public static final int CODE_CLASSIF_POSITION_ATTRIBUTES = 108;
	
	/** Код на класификация "Тип на класификацията " */
	public static final int CODE_CLASSIF_CLASSIFICATION_TYPE = 121;
	
	/** Код на класификация "Класификационна единица " */
	public static final int CODE_CLASSIF_CLASSIFICATION_UNIT = 122;
	
	/** Код на класификация "Статус на позицията " */
	public static final int CODE_CLASSIF_POSITION_STATUS = 123;
	
	/** Код на класификация "Тип код на позицията " */
	public static final int CODE_CLASSIF_POSITION_CODE_TYPE = 124;
	
	/** Код на класификация "Статус на версия " */
	public static final int CODE_CLASSIF_VERSION_STATUS = 125;
	
	/** Код на класификация "Имена на класификационни нива " */
	public static final int CODE_CLASSIF_LEVEL_NAME = 129;
	
	/** Код на класификация "Тип на кода на класификационно ниво " */
	public static final int CODE_CLASSIF_LEVEL_CODE_TYPE = 130;
	
	/** Код на класификация "Класификационно семейство/подсемейство " */
	public static final int CODE_CLASSIF_CLASSIFICATION_FAMILY_SIMPLE = 131;
	
	/** Код на класификация "Служби и лица " */
	public static final int CODE_CLASSIF_CLASSIFICATION_SLUJBI_LICA = 136;	
	
	/** Код на класификация "Авторско право " */
	public static final int CODE_CLASSIF_CLASSIFICATION_AVTORSKO_PRAVO = 137;
	
	/** Код на класификация "Статус на кореспондираща таблица" */
	public static final int CODE_CLASSIF_CORESP_STATUS = 138;
	
	/** Код на класификация "Тип на кореспондираща таблица" */
	public static final int CODE_CLASSIF_CORESP_TYPE = 139;
	
	/** Код на класификация "Режим на свързване в кореспондираща таблица" */
	public static final int CODE_CLASSIF_CORESP_RELATION_TYPE = 140;
	
	/** Код на класификация "Тип на релация" */
	public static final int CODE_CLASSIF_RELATION_TYPE = 141;
	
	/** Код на класификация "Роля на лице от Служби и лица " */
	public static final int CODE_CLASSIF_CLASSIFICATION_ROLE_LICE= 142;
	
	/** Код на класификация "Тип на промяна на релация" */
	public static final int CODE_CLASSIF_CHANGE_TYPE = 143;
	
	/** Код на класификация "Мерни единици" */
	public static final int CODE_CLASSIF_UNITS= 144;
	
	/** Код на класификация "Вид на документа" */
	public static final int CODE_CLASSIF_DOC_TYPE= 146;
	
	/** Код на класификация "Класификационно семейство/подсемейство " */
	public static final int CODE_CLASSIF_CLASSIFICATION_FAMILY = 502;
	
	/** Код на класификация "Пояснение (Релации) " */
	public static final int CODE_CLASSIF_EXPLANATION = 512;
	
	/** Код на класификация "Търсене на релация - източник или цел " */
	public static final int CODE_CLASSIF_RELATION_SEARCH = 513;
		
	
	
	
	/** Статистическа класификация */
	public static final int	CODE_ZNACHENIE_JOURNAL_STAT_CLASSIF			= 91;
	
	/** Версия */
	public static final int	CODE_ZNACHENIE_JOURNAL_STAT_VERSION			= 92;
	
	/** Ниво */
	public static final int	CODE_ZNACHENIE_JOURNAL_STAT_LEVEL		= 93;
	
	/** Потребител към обект */
	public static final int	CODE_ZNACHENIE_JOURNAL_OBJECT_USER		= 94;
	
	/** Документ към обект */
	public static final int	CODE_ZNACHENIE_JOURNAL_OBJECT_DOC		= 95;
		
	/** Позиция */
	public static final int	CODE_ZNACHENIE_JOURNAL_POSITION		= 100;
	
	/** Кореспондираща таблица */
	public static final int	CODE_ZNACHENIE_JOURNAL_CORESP_TABLE		= 98;
	
	/** Релация */
	public static final int	CODE_ZNACHENIE_JOURNAL_RELATION		= 99;
	
	/** ГРУПА ПОТРЕБИТЕЛИ СПРАВКИ */
	public static final int	CODE_ZNACHENIE_GROUP_USER_SPRAVKI	= 286;
	
	
	// ТИП ВМЪКВАНЕ НА НОВА ПОЗИЦИЯ В СХЕМА 
	//*****************************************************************************************************************************************************************
	/** Вмъкване ПРЕДИ */
	public static final int	CODE_ZNACHENIE_INSERT_BEFORE		= 1;
	
	/** Вмъкване СЛЕД */
	public static final int	CODE_ZNACHENIE_INSERT_AFTER		= 2;
	
	/** Вмъкване КАТО ДЕТЕ */
	public static final int	CODE_ZNACHENIE_INSERT_AS_CHILD		= 3;
	//*****************************************************************************************************************************************************************
	
	
	
	/** Статус на версия - НЕЗАВЪРШЕНА */
	public static final int	CODE_ZNACHENIE_VER_STATUS_NEZAVARSHENA		= 4;
	/** "Статус на версия - действаща" */
	public static final int CODE_ZNACHENIE_VER_STATUS_ACTIVE = 1;
	
	/** Класификация 108 "Атрибути на позициите" */
	public static final int	CODE_ZNACHENIE_NACIONALNA = 7;
	public static final int	CODE_ZNACHENIE_MEJDUNARODNA = 8;
	
	/** Класификация 124 "Тип код на позицията" */
	public static final int	CODE_ZNACHENIE_LIPSVAST_COD = 3;
	

	/** Класификация 139 - тип на кор. таблица
	 * значение - Историческа 
	 * */	
	public static final int	CODE_ZNACHENIE_COR_TAB_HIST		= 1;
	
	
	
	

	/** Система за управление съдържанието на портала */
	// Системна класификация 2 - Информационни обекти (за журналиране)
		/** Портал секция */
		public static final int	CODE_ZNACHENIE_JOURNAL_PUBLICATION	= 96;
		/** Портал секция език*/
		public static final int	CODE_ZNACHENIE_JOURNAL_PUBLICATION_LANG	= 97;
		
	
		/**	 Код на системна класификация "Информационни секции на портала" */
		public static final int CODE_SYSCLASS_SECT_PUBL = 515;
			
		/**	 Код на системна класификация "Тип на секции" */
		public static final int CODE_SYSCLASS_PUBL_TYPE = 514;
		
		//Системна класификация "Тип на публикация" 
		/** код на значение Информационни Материали */
		public static final int CODE_ZNACHENIE_TYPE_PUBL_MATERIALI = 1;
		/** код на значение Изображения */
		public static final int CODE_ZNACHENIE_TYPE_PUBL_IMAGES = 2;
		/** код на значение Видео */
		public static final int CODE_ZNACHENIE_TYPE_PUBL_VIDEO = 3;
		
		/**	 Код на позициите на системна класификация "Информационни секции на портала" */
		/** код на значение Лого */
		public static final int CODE_ZNACHENIE_SECT_LOGO = 14;
		/** код на значение Хедър */
		public static final int CODE_ZNACHENIE_SECT_HEADER = 15;
		/** код на значение Футър */
		public static final int CODE_ZNACHENIE_SECT_FOOTER = 16;
		/** код на значение Информация */
		public static final int CODE_ZNACHENIE_SECT_INFORMATIA = 12;
		/** код на значение Атрибути на портала */
		public static final int CODE_ZNACHENIE_SECT_ATRIBUTI_PORTAL = 13;
		
		/** код на значение Условия за ползване */
		public static final int CODE_ZNACHENIE_SECT_TERMS_USE = 5;
		/** код на значение Контакти */
		public static final int CODE_ZNACHENIE_SECT_CONTACTS = 3;
		/** код на значение Връзки с институции и системи */
		public static final int CODE_ZNACHENIE_SECT_RELATIONS_INST_SYST = 10;
		/** код на значение Политика за личните данни  */
		public static final int CODE_ZNACHENIE_SECT_PRIVACY_POLICY = 6;
		/** код на значение Декларация за достъпност  */
		public static final int CODE_ZNACHENIE_SECT_DECLARATION_ACCESSIBILITY = 7;
		/** код на значение Бисквитки  */
		public static final int CODE_ZNACHENIE_SECT_COOKIES = 8;
		/** код на значение Лиценз  */
		public static final int CODE_ZNACHENIE_SECT_LICENSE = 9;
		/** код на значение Връзки  */
		public static final int CODE_ZNACHENIE_SECT_LINKS = 10;
		/** код на значение Помощ  */
		public static final int CODE_ZNACHENIE_SECT_HELP = 11;
		/** код на значение Казуси  */
		public static final int CODE_ZNACHENIE_SECT_CASES = 2;
		
		/** END Система за управление съдържанието на портала */
	
		// Системна класификация 4 - Бизнес роля
		/** Роля Класификационен експерт */
		public static final int	CODE_ZNACHENIE_ROLE_CLASS_EXP				= 2;
	
	/** */
	private NSIConstants() {
		super();
	}
}