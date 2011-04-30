SELECT * FROM Bar
LEFT OUTER JOIN Foo ON Foo.Bar_id=Bar.id
            AND Foo.FirstName=:name