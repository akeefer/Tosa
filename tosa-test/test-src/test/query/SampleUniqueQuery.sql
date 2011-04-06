SELECT * FROM Foo
WHERE
  UNIQUE (SELECT * FROM Bar
          WHERE Foo.Bar_id=Bar.id AND Bar.Misc=:barMisc)