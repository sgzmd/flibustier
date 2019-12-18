DROP TABLE IF EXISTS  SequenceAuthor;

CREATE TABLE SequenceAuthor AS
SELECT SeqName, SeqId, group_concat(Author, ", ") as Authors FROM (
select 
	sn.SeqName, 
	s.SeqId, 
	an.FirstName || CASE when length(an.MiddleName) > 0 then " " || an.MiddleName  else "" end || " " || an.LastName || CASE when length(an.NickName) > 0 then " (" || an.MiddleName || ")"  else "" end  as Author
FROM
libavtor a, libavtorname an, libbook b, libseq s, libseqname sn
where a.AvtorId = an.AvtorId
and b.BookId = a.BookId
and s.BookId = b.BookId 
and sn.SeqId = s.SeqId
group by 1,2,3) 
GROUP BY 1,2;