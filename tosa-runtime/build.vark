
function clean() {
  file( "target" ).deleteRecursively()
}

function compile() {
  var classesDir = file("target/classes")
  classesDir.mkdirs()
  Ant.javac( :srcdir = classpath( file("src") ),
             :destdir = classesDir,
             :classpath = pom().dependencies(COMPILE, :additionalDeps = {{
                                              :GroupId = "org.gosu-lang.gosu", :ArtifactId = "gosu-core", :Version = "0.9-SNAPSHOT"
                                             }}).Path,
             :debug = true,
             :includeantruntime = false)
}

@Depends( {"compile"} )
function test() {
  throw "Not yet implemented"
}
