SELECT *
FROM Bar
WHERE
  Misc = ALL (SELECT Misc FROM Bar WHERE Misc='misc')