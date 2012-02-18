SELECT * FROM Foo
WHERE
  EXISTS (SELECT * FROM Bar
          WHERE Foo.Bar_id=Bar.id AND Bar.Misc=:barMisc)