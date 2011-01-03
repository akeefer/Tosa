uses java.io.File
uses java.lang.System
uses gw.vark.Aardvark
uses org.apache.tools.ant.types.Path

//=======================================================================
// Change to gosu distro home
//=======================================================================

var ghVar = System.getenv( "GOSU_HOME" )
if(ghVar == null) {
  Ant.fail( :message = "Please define the GOSU_HOME system variable" )
}
var gosuHome = file( ghVar )

//=======================================================================
//
//=======================================================================

var tosaHome = file( "." )

function clean() {
  file( "tosa/build" ).deleteRecursively()
}

function build() {
  buildModule( file("tosa"), classpath().withFileset( gosuHome.file( "jars" ).fileset() ), "tosa.jar" )
}

private function buildModule(root : File, cp : Path, jarName : String) {
  var classesDir = root.file( "build/classes" )
  classesDir.mkdirs()
  Ant.javac( :srcdir = path(root.file("src")),
             :destdir = classesDir,
             :classpath = cp,
             :debug = true,
             :includeantruntime = false)
  Ant.copy( :filesetList = {root.file( "src" ).fileset( :excludes = "**/*.java") },
            :todir = classesDir )
  Ant.jar( :destfile = root.file( "build/${jarName}" ),
           :manifest = root.file( "src/META-INF/MANIFEST.MF" ).exists() ? root.file( "src/META-INF/MANIFEST.MF" ) : null,
           :basedir = classesDir )
}