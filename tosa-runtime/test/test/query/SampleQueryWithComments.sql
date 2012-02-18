-- test comment
SELECT Bar.* FROM Bar --end of line comment
JOIN Foo ON Foo.Bar_id=Bar.id
            AND Foo.FirstName=:name --end of file comment