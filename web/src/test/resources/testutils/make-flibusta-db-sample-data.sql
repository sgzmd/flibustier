-- SQLite
-- 34145

.mode insert libseq
select * from libseq where seqid  = 34145;

.mode insert libseqname
select * from libseqname where seqid = 34145;

.mode insert libbook
select * from libbook where bookId in (select BookId from libseq where seqid  = 34145);

.mode insert libavtor
select * from libavtor where bookId in (select BookId from libseq where seqid  = 34145);

.mode insert libavtorname
select * from libavtorname where AvtorId IN (select AvtorId from libavtor where bookId in (select BookId from libseq where seqid  = 34145));