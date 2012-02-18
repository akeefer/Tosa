SELECT * FROM Bar
WHERE
  OPTIONAL( Misc = :misc1 ) OR
  OPTIONAL( Misc = :misc2 ) OR
  OPTIONAL( Misc = :misc3 )