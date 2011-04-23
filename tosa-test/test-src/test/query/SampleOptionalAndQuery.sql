SELECT * FROM Bar
WHERE
  OPTIONAL( Misc = :misc1 ) AND
  OPTIONAL( Misc = :misc2 ) AND
  OPTIONAL( Misc = :misc3 )