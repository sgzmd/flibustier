CREATE TABLE IF NOT EXISTS "libseqname" (
	"SeqId"	integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	"SeqName"	varchar(254) NOT NULL DEFAULT '' UNIQUE
);
CREATE TABLE IF NOT EXISTS "libseq" (
	"BookId"	integer NOT NULL,
	"SeqId"	integer NOT NULL,
	"SeqNumb"	integer NOT NULL,
	"Level"	integer NOT NULL DEFAULT '0',
	"Type"	integer NOT NULL DEFAULT '0',
	PRIMARY KEY("BookId","SeqId")
);
CREATE TABLE IF NOT EXISTS "libbook" (
	"BookId"	integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	"FileSize"	integer NOT NULL DEFAULT '0',
	"Time"	timestamp NOT NULL DEFAULT current_timestamp,
	"Title"	varchar(254) NOT NULL DEFAULT '',
	"Title1"	varchar(254) NOT NULL,
	"Lang"	char(3) NOT NULL DEFAULT 'ru',
	"LangEx"	integer NOT NULL DEFAULT '0',
	"SrcLang"	char(3) NOT NULL DEFAULT '',
	"FileType"	char(4) NOT NULL,
	"Encoding"	varchar(32) NOT NULL DEFAULT '',
	"Year"	integer NOT NULL DEFAULT '0',
	"Deleted"	char(1) NOT NULL DEFAULT '0',
	"Ver"	varchar(8) NOT NULL DEFAULT '',
	"FileAuthor"	varchar(64) NOT NULL,
	"N"	integer NOT NULL DEFAULT '0',
	"keywords"	varchar(255) NOT NULL,
	"md5"	binary(32) NOT NULL UNIQUE,
	"Modified"	timestamp NOT NULL DEFAULT '2009-11-29 05:00:00',
	"pmd5"	char(32) NOT NULL DEFAULT '',
	"InfoCode"	integer NOT NULL DEFAULT '0',
	"Pages"	integer NOT NULL DEFAULT '0',
	"Chars"	integer NOT NULL DEFAULT '0',
	UNIQUE("Deleted","BookId")
);
CREATE TABLE IF NOT EXISTS "libavtorname" (
	"AvtorId"	integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	"FirstName"	varchar(99) NOT NULL DEFAULT '',
	"MiddleName"	varchar(99) NOT NULL DEFAULT '',
	"LastName"	varchar(99) NOT NULL DEFAULT '',
	"NickName"	varchar(33) NOT NULL DEFAULT '',
	"uid"	integer NOT NULL DEFAULT '0',
	"Email"	varchar(255) NOT NULL,
	"Homepage"	varchar(255) NOT NULL,
	"Gender"	char(1) NOT NULL DEFAULT '',
	"MasterId"	integer NOT NULL DEFAULT '0'
);
CREATE TABLE IF NOT EXISTS "libavtor" (
	"BookId"	integer NOT NULL DEFAULT '0',
	"AvtorId"	integer NOT NULL DEFAULT '0',
	"Pos"	integer NOT NULL DEFAULT '0',
	PRIMARY KEY("BookId","AvtorId")
);
CREATE INDEX IF NOT EXISTS "idx_libavtorname_MasterId" ON "libavtorname" (
	"MasterId"
);
CREATE INDEX IF NOT EXISTS "idx_libavtorname_uid" ON "libavtorname" (
	"uid"
);
CREATE INDEX IF NOT EXISTS "idx_libavtorname_Homepage" ON "libavtorname" (
	"Homepage"
);
CREATE INDEX IF NOT EXISTS "idx_libavtorname_email" ON "libavtorname" (
	"Email"
);
CREATE INDEX IF NOT EXISTS "idx_libavtorname_LastName" ON "libavtorname" (
	"LastName"
);
CREATE INDEX IF NOT EXISTS "idx_libavtorname_FirstName" ON "libavtorname" (
	"FirstName"
);
CREATE INDEX IF NOT EXISTS "idx_libavtor_iav" ON "libavtor" (
	"AvtorId"
);
CREATE INDEX IF NOT EXISTS "idx_libseq_SeqId" ON "libseq" (
	"SeqId"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_LangDel" ON "libbook" (
	"Deleted",
	"Lang"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_FileTypeDel" ON "libbook" (
	"Deleted",
	"FileType"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_Title1" ON "libbook" (
	"Title1"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_N" ON "libbook" (
	"N"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_FileAuthor" ON "libbook" (
	"FileAuthor"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_FileSize" ON "libbook" (
	"FileSize"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_Lang" ON "libbook" (
	"Lang"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_FileType" ON "libbook" (
	"FileType"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_Deleted" ON "libbook" (
	"Deleted"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_Year" ON "libbook" (
	"Year"
);
CREATE INDEX IF NOT EXISTS "idx_libbook_Title" ON "libbook" (
	"Title"
);
