SELECT * FROM ForNumericTests
WHERE
  1 = ABS(Number - :var)