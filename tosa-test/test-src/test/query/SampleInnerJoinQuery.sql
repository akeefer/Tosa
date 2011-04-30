SELECT * FROM Bar
INNER JOIN Foo ON Foo.Bar_id=Bar.id
            AND Foo.FirstName=:name