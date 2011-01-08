uses java.lang.System
uses java.io.File

function detectAardvark() {
  var path = System.getenv( "PATH" )
  print( path )
  if( path != null )
  {
    for( s in path.split( File.pathSeparator ) )
    {
      if( new File( s, "vark" ).exists() )
      {
        return
      }
    }
  }
  print( "Aardvark was not found on your path.  Ronin uses Aardvark to package and run applications.\n" +
         "Please download it from http://vark.github.com")
}

detectAardvark()