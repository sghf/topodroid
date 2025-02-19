/* @file Archiver.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid survey archiver
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDFile;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.Enumeration;
import java.util.zip.DataFormatException;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.Inflater;
import java.util.zip.ZipFile;

// import java.nio.charsets.StandardCharsets; // API-19

import java.util.List;
// import java.util.Locale;
// import java.util.ArrayList;

// import android.content.Context;
// import android.content.Intent;

public class Archiver
{
  // private final TopoDroidApp mApp;
  private static final int BUF_SIZE = 4096;
  private byte[] data; // = new byte[ BUF_SIZE ];

  String zipname;

  public Archiver( ) // TopoDroidApp app
  {
    // mApp = app;
    data = new byte[ BUF_SIZE ];
  }

  private boolean addEntry( ZipOutputStream zos, File name, String filepath )
  {
    if ( name == null || ! name.exists() ) return false;
    boolean ret = false;
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "zip add file " + name.getPath() );
      bis = new BufferedInputStream( TDFile.getFileInputStream( filepath ), BUF_SIZE );
      ZipEntry entry = new ZipEntry( name.getName() );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
        zos.write( data, 0, cnt );
      }
      zos.closeEntry( );
      ret = true;
    } catch (FileNotFoundException e ) {
      TDLog.Error( "File Not Found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { /* ret = false; */ }
    }
    // Log.v( "DistoX-ZIP", "zip add file " + name.getPath() + " return " + ret );
    return ret;
  }


  private boolean addEntry( ZipOutputStream zos, String subdir, String filename )
  {
    // Log.v( "DistoX-ZIP", "zip add entry: " + subdir + " " + filename );
    boolean ret = false;
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "zip add file " + name.getPath() );
      bis = new BufferedInputStream( TDFile.getMSinput( subdir, filename, "application/octet-stream" ), BUF_SIZE );
      ZipEntry entry = new ZipEntry( filename );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
        zos.write( data, 0, cnt );
      }
      zos.closeEntry( );
      ret = true;
    } catch (FileNotFoundException e ) {
      TDLog.Error( "File Not Found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { /* ret = false; */ }
    }
    // Log.v( "DistoX-ZIP", "zip add file " + name.getPath() + " return " + ret );
    return ret;
  }

  private void addOptionalEntry( ZipOutputStream zos, File name, String filepath )
  {
    if ( name == null || ! name.exists() ) return;
    // Log.v( "DistoX-ZIP", "zip optional file " + name.getPath() );
    BufferedInputStream bis = null;
    try { 
      // TDLog.Log( TDLog.LOG_IO, "zip add file " + name.getPath() );
      bis = new BufferedInputStream( TDFile.getFileInputStream( filepath ), BUF_SIZE );
      ZipEntry entry = new ZipEntry( name.getName() );
      int cnt;
      zos.putNextEntry( entry );
      while ( (cnt = bis.read( data, 0, BUF_SIZE )) != -1 ) {
        zos.write( data, 0, cnt );
      }
      zos.closeEntry( );
    } catch (FileNotFoundException e ) {
      TDLog.Error( "File Not Found " + e.getMessage() );
    } catch ( IOException e ) {
      TDLog.Error( "IO exception " + e.getMessage() );
    } finally {
      if ( bis != null ) try { bis.close(); } catch (IOException e ) { }
    }
  }

  private void addDirectory( ZipOutputStream zos, File dir, String dirname )
  {
    if ( ! dir.exists() ) return;
    // Log.v( "DistoX-ZIP", "zip add dir " + dir.getPath() );
    File[] files = dir.listFiles();
    if ( files != null ) {
      for ( File file : files ) { // listFiles MAY NullPointerException
        if (file.isFile()) addOptionalEntry(zos, file, file.getPath() ); 
      }
    }
  }

  /** used to compress shapefiles
   * @param zipdir    zip folder
   * @param zipname   zip-filename
   * @param subdir    files subdirectory
   * @param filenames file names
   * @return true if successful
   */
  public boolean compressFiles( String zipdir, String zipname, String subdir, List< String > filenames )
  {
    // here zipname is the full absolute zipfile path
    // Log.v("DistoX", "ZIP-compress files to " + zipdir + " " + zipname );
    ZipOutputStream zos = null;
    boolean ret = true;
    try {
      zos = new ZipOutputStream( new BufferedOutputStream( TDFile.getMSoutput( zipdir, zipname, "application/octet-stream" ) ) );
      for ( String filename : filenames ) {
        // the file.getPath() is the full absolute file path
        // Log.v("DistoX", "ZIP-compress add file " + file.getPath() );
        ret &= addEntry( zos, subdir, filename );
      }
      // for ( File file : files ) TDFile.deleteFile( file );
    } catch ( FileNotFoundException e ) {
    } catch ( IOException e ) {
      // FIXME
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { 
        ret = false;
        // TDLog.Error("ZIP compress close error");
      }
    }
    return ret;
  }

  public boolean compressFiles( OutputStream os, String subdir, List< String > filenames )
  {
    // here zipname is the full absolute zipfile path
    // Log.v("DistoX", "ZIP-compress files to " + zipdir + " " + zipname );
    ZipOutputStream zos = null;
    boolean ret = true;
    // try {
      zos = new ZipOutputStream( new BufferedOutputStream( os ) );
      for ( String filename : filenames ) {
        // the file.getPath() is the full absolute file path
        Log.v("DistoX", "ZIP-compress files: add " + subdir + " " + filename );
        ret &= addEntry( zos, subdir, filename );
      }
      // for ( File file : files ) TDFile.deleteFile( file );
    // } catch ( FileNotFoundException e ) {
    // } catch ( IOException e ) {
      // FIXME
    // } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { 
        ret = false;
        // TDLog.Error("ZIP compress close error");
      }
    // }
    return ret;
  }

  // @param zipname  compressed zip file
  // @param lib      symbol library
  // @param dirpath  (external files) folder for the symbol files
  private boolean compressSymbols( File zipfile, SymbolLibrary lib, String dirpath )
  {
    if ( lib == null ) return false;
    if ( ! (TDFile.getExternalDir( dirpath )).exists() ) return false;
    // Log.v("DistoX", "ZIP symbols zip " + zipfile.getPath() );
    List< Symbol > symbols = lib.getSymbols();
    ZipOutputStream zos = null;
    try { 
      zos = new ZipOutputStream( new BufferedOutputStream( new FileOutputStream( zipfile ) ) );
      for ( Symbol symbol : symbols ) {
        if ( symbol.mEnabled ) {
          String filename = symbol.getThName();
          // THERION-U: filename = Symbol.deprefix_u( filename );
          String filepath = dirpath + "/" + filename;
          // Log.v("DistoX", "ZIP symbols compress " + dirpath + " " + filepath );
          addOptionalEntry( zos, TDFile.getExternalFile( dirpath, filename ), filepath );
        }
      }
    } catch ( FileNotFoundException e ) {
      return false;
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { TDLog.Error("ZIP-symbol close error"); }
    }
    return true;
  }

  // @param zin      compressed input stream
  // @param dirpath  (external files) folder for the symbol files
  // @param prefix   symbol prefix in the database config table
  // @return true is a symbol has been uncompressed
  static private boolean uncompressSymbols( InputStream zin, String dirpath, String prefix )
  {
    if ( ! (TDLevel.overExpert && TDSetting.mZipWithSymbols ) ) return false;
    boolean ret = false;
    // Log.v("DistoX", "ZIP-uncompress symbol dirpath " + dirpath + " prefix " + prefix );
    File tempfile = TDFile.getExternalFile( null, "tmp.zip" );
    FileOutputStream fout = null;
    int c;
    byte[] sbuffer = new byte[4096];
    try { 
      fout = new FileOutputStream( tempfile );
      while ( ( c = zin.read( sbuffer ) ) != -1 ) fout.write(sbuffer, 0, c);
      fout.close();
      fout = null;
      // uncompress symbols zip
      ZipEntry sze;
      FileInputStream fis = new FileInputStream( tempfile );
      ZipInputStream szin = new ZipInputStream( fis );
      while ( ( sze = szin.getNextEntry() ) != null ) {
        // String symbolfilename = dirpath + sze.getName();
        // File symbolfile = TDFile.getExternalFile( symbolfilename );
        File symbolfile = TDFile.getExternalFile( dirpath, sze.getName() );
        // Log.v("DistoX", "ZIP try to uncompress symbol " + dirpath + " " + sze.getName() );
        if ( ! symbolfile.exists() ) { // don't overwrite
          // Log.v("DistoX", "ZIP-uncompress symbol " + symbolfile.getPath() );
          // FileOutputStream sfout = TDFile.getFileOutputStream( symbolfilename ); // uncompress symbols zip
          FileOutputStream sfout = new FileOutputStream( symbolfile ); 
          while ( ( c = szin.read( sbuffer ) ) != -1 ) sfout.write(sbuffer, 0, c);
          sfout.close();
          ret = true;
          // add symbol to library and enable it
        }
        szin.closeEntry();
        // need to get the thname from the file
        // FileInputStream sfis = TDFile.getFileInputStream( symbolfilename );
        FileInputStream sfis = new FileInputStream( symbolfile );
        BufferedReader br = new BufferedReader( new InputStreamReader( sfis, "UTF-8" /* StandardCharsets.UTF_8 */ ) ); // String iso = "UTF-8";
        String line;
        while ( (line = br.readLine()) != null ) {
          line = line.trim();
          if ( line.startsWith("th_name") ) {
            String th_name = line.substring(8).trim();
            // Log.v("DistoX-ZIP", "enable " + th_name );
            TopoDroidApp.mData.setSymbolEnabled( prefix + th_name, true );
            break;
          }
        }
        sfis.close();
      }
      fis.close();
    } catch ( FileNotFoundException e1 ) { Log.v( "DistoX-ZIP", "File Not Found " + e1.getMessage() );
    } catch ( IOException e2 ) { Log.v( "DistoX-ZIP", "I/O exception " + e2.getMessage() );
    } finally {
      TDFile.deleteFile( tempfile );
    }
    return ret;
  }


  boolean archive( TopoDroidApp mApp )
  {
    if ( TDInstance.sid < 0 ) return false;
    DataHelper app_data = TopoDroidApp.mData;
    // if ( app_data == null ) return false;
    
    // File temp = null;
    String survey = TDInstance.survey;
    boolean ret = true;

    zipname = TDPath.getSurveyZipFile( survey );
    TDPath.checkPath( zipname );
    // TDLog.Log( TDLog.LOG_IO, "zip export file: " + zipname );
    // Log.v( "DistoX-ZIP", "zip export file: " + zipname + " pre " + ret );

    ZipOutputStream zos = null;
    try {
      String pathname;
      zos = new ZipOutputStream( new BufferedOutputStream( TDFile.getFileOutputStream( zipname ) ) );

      pathname = TDPath.getManifestFile( ); // The first entry must be the manifest 
      mApp.writeManifestFile();
      ret &= addEntry( zos, TDFile.getTopoDroidFile(pathname), pathname );
      // Log.v("DistoX-ZIP", "archive post-manifest returns " + ret );

      pathname = TDPath.getSqlFile( );
      app_data.dumpToFile( pathname, TDInstance.sid );
      ret &= addEntry( zos, TDFile.getTopoDroidFile(pathname), pathname );
      // Log.v("DistoX-ZIP", "archive post-sqlite returns " + ret );

      pathname = TDPath.getSurveyNoteFile( survey );
      addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );

      if ( TDLevel.overExpert && TDSetting.mZipWithSymbols ) {
        File file = TDFile.getExternalFile( null, "points.zip" );
        if ( compressSymbols( file, BrushManager.getPointLib(), TDPath.getSymbolPointDirname() ) )  {
          addOptionalEntry( zos, file, "points.zip" );
        }
        file = TDFile.getExternalFile( null, "lines.zip" );
        if ( compressSymbols( file, BrushManager.getLineLib(), TDPath.getSymbolLineDirname() ) )  {
          addOptionalEntry( zos, file, "lines.zip" );
        }
        file = TDFile.getExternalFile( null, "areas.zip" );
        if ( compressSymbols( file, BrushManager.getAreaLib(), TDPath.getSymbolAreaDirname() ) )  {
          addOptionalEntry( zos, file, "areas.zip" );
        }
      }

/* FIXME_SKETCH_3D *
      List< Sketch3dInfo > sketches  = app_data.selectAllSketches( TDInstance.sid, TDStatus.NORMAL );
      for ( Sketch3dInfo skt : sketches ) {
        ret &= addEntry( zos, TDFile.getTopoDroidFile( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
      sketches  = app_data.selectAllSketches( TDInstance.sid, TDStatus.DELETED );
      for ( Sketch3dInfo skt : sketches ) {
        ret &= addEntry( zos, TDFile.getTopoDroidFile( TDPath.getSurveySketchOutFile( survey, skt.name ) ) );
      }
/* END_SKETCH_3D */

      List< PlotInfo > plots  = app_data.selectAllPlots( TDInstance.sid, TDStatus.NORMAL );
      for ( PlotInfo plt : plots ) {
        pathname = TDPath.getSurveyPlotTdrFile( survey, plt.name ); // N.B. plot file CAN be missing
        addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
        // pathname = TDPath.getSurveyPlotTh2File( survey, plt.name );
        // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
        // pathname = TDPath.getSurveyPlotDxfFile( survey, plt.name );
        // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
        // pathname = TDPath.getSurveyPlotSvgFile( survey, plt.name );
        // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
        // pathname = TDPath.getSurveyPlotXviFile( survey, plt.name );
        // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
        // pathname = TDPath.getSurveyPlotPngFile( survey, plt.name );
        // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
        // pathname = TDPath.getSurveyPlotTnlFile( survey, plt.name );
        // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
        // if ( plt.type == PlotType.PLOT_PLAN ) {
        //   pathname = TDPath.getSurveyCsxFile( survey, plt.name );
        //   addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
        // }
        // pathname = TDPath.getSurveyPlotShzFile( survey, plt.name );
	// addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      }

      // try overview exports
      //   pathname = TDPath.getSurveyPlotTh2File( survey, "p" );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      //   pathname = TDPath.getSurveyPlotDxfFile( survey, "p" );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      //   pathname = TDPath.getSurveyPlotSvgFile( survey, "p" );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      //   pathname = TDPath.getSurveyPlotXviFile( survey, "p" );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      //   pathname = TDPath.getSurveyPlotShzFile( survey, "p" ); // zipped shp
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );

      //   pathname = TDPath.getSurveyPlotTh2File( survey, "s" );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      //   pathname = TDPath.getSurveyPlotDxfFile( survey, "s" );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      //   pathname = TDPath.getSurveyPlotSvgFile( survey, "s" );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      //   pathname = TDPath.getSurveyPlotXviFile( survey, "s" );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      //   pathname = TDPath.getSurveyPlotShzFile( survey, "s" ); // zipped shp
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );

      plots  = app_data.selectAllPlots( TDInstance.sid, TDStatus.DELETED );
      for ( PlotInfo plt : plots ) {
        pathname = TDPath.getSurveyPlotTdrFile( survey, plt.name );
        addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      }

      pathname = TDPath.getSurveyPhotoDir( survey );
      addDirectory( zos, TDFile.getTopoDroidFile( pathname ), pathname );

      pathname = TDPath.getSurveyAudioDir( survey );
      addDirectory( zos, TDFile.getTopoDroidFile( pathname ), pathname );

      // pathname = TDPath.getSurveyThFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyCsvFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyCsxFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyCaveFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyCavFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyDatFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyDxfFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyGrtFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyGtxFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyKmlFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyJsonFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyPltFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyShzFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveySrvFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveySurFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveySvxFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyTroFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyTrbFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );
      // pathname = TDPath.getSurveyTopFile( survey );
      // addOptionalEntry( zos, TDFile.getTopoDroidFile( pathname ), pathname );


      // ret = true;
    } catch ( FileNotFoundException e ) {
      // Log.v("DistoX-ZIP", e.getMessage() );
      ret = false;
    } catch ( IOException e ) {
      // FIXME
    } finally {
      if ( zos != null ) try { zos.close(); } catch ( IOException e ) { TDLog.Error("ZIP close error"); }
      TDFile.deleteFile( TDPath.getSqlFile() );
    }
    // Log.v("DistoX-ZIP", "archive returns " + ret );
    return ret;
  }

  // the zip inputstream must be aligned to the entry
  private static int decompressEntry( InputStream zin, ZipEntry ze, FileOutputStream fout )
  {
    // int csize = (int)ze.getCompressedSize(); // cannot rely on sizes attributes
    // int dsize = (int)ze.getSize();
    // Log.v("DistoX", "decompress entry: " + ze.getName() );
    byte[] data = new byte[ 4096 ];
    int size = 0;
    try {
      int c;
      while ( ( c = zin.read( data ) ) != -1 ) {
        fout.write(data, 0, c);
        size += c;
      }
      // Inflater decompresser = new Inflater();
      // decompresser.setInput( data, 0, csize );
      // buffer = new byte[ dsize ];
      // size = decompresser.inflate( buffer );
      // decompresser.end();
    } catch ( IOException e ) {
      Log.e("DistoX", "decompress entry: " + e.getMessage() );
      return -1;
    }
    // Log.v("DistoX", "decompress entry: size " + size );
    return size;
  }
    

  public int unArchive( TopoDroidApp mApp, String filename, String surveyname, boolean force )
  {
    boolean sql_success = false;
    int ok_manifest = -2;
    String pathname;
    DataHelper app_data = TopoDroidApp.mData;
    try {
      // byte buffer[] = new byte[36768];
      // byte[] buffer = new byte[4096];

      ZipEntry ze;
      TDLog.Log( TDLog.LOG_ZIP, "unzip " + filename );
      ZipFile zip = new ZipFile( filename );
      ze = zip.getEntry( "manifest" );
      if ( ze == null ) return -2;
      pathname = TDPath.getManifestFile( );
      // TDPath.checkPath( pathname );
      FileOutputStream fout = TDFile.getFileOutputStream( pathname );
      InputStream zin = zip.getInputStream( ze );
      int msize = decompressEntry( zin, ze, fout );
      fout.close();
      if ( msize > 0 ) {
        // int c; while ( ( c = is.read( mbuffer ) ) != -1 ) fout.write(mbuffer, 0, c);
        ok_manifest = TopoDroidApp.checkManifestFile( pathname, surveyname  ); // this sets surveyname
      }
      TDFile.deleteFile( pathname );
      TDLog.Log( TDLog.LOG_ZIP, "un-archived manifest " + ok_manifest );
      if ( ok_manifest < 0 ) {
        if ( ! force ) {
          zip.close();
          return ok_manifest;
        }
      }
      TDLog.Log( TDLog.LOG_ZIP, "unzip file " + filename + " survey " + surveyname );
      // byte buffer[] = new byte[36768];
      // byte[] buffer = new byte[4096];

      int nr_entry = 0;
      for ( Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>)zip.entries(); entries.hasMoreElements(); ) {
        ze = entries.nextElement();
        ++ nr_entry;
        if ( ze.isDirectory() ) {
          TDFile.makeTopoDroidDir( TDPath.getDirFile( ze.getName() ) );
        } else if ( ze.getName().equals( "manifest" ) ) {
          // skip
        } else {
          zin = zip.getInputStream( ze );
          TDLog.Log( TDLog.LOG_ZIP, "Zip entry " + nr_entry + " \"" + ze.getName() + "\"" );
          boolean sql = false;
          pathname = null;
          if ( ze.getName().equals( "survey.sql" ) ) {
            pathname = TDPath.getSqlFile();
            sql = true;
          // } else if ( ze.getName().endsWith( TDPath.CSV ) ) {
          //   pathname = TDPath.getCsvFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.CSX ) ) {
          //   pathname = TDPath.getCsxFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.CAVE ) ) {
          //   pathname = TDPath.getCaveFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.CAV ) ) {
          //   pathname = TDPath.getCavFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.DAT ) ) {
          //   pathname = TDPath.getDatFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.GRT ) ) {
          //   pathname = TDPath.getGrtFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.GTX ) ) {
          //   pathname = TDPath.getGtxFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.DXF ) ) {
          //   pathname = TDPath.getDxfFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.KML ) ) {
          //   pathname = TDPath.getKmlFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.JSON ) ) {
          //   pathname = TDPath.getJsonFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.PLT ) ) {
          //   pathname = TDPath.getPltFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.PNG ) ) {
          //   pathname = TDPath.getPngFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.SRV ) ) {
          //   pathname = TDPath.getSrvFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.SVG ) ) {
          //   pathname = TDPath.getSvgFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.SUR ) ) {
          //   pathname = TDPath.getSurFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.SVX ) ) {
          //   pathname = TDPath.getSvxFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.SHZ ) ) {
          //   pathname = TDPath.getShzFile( ze.getName() );

          // } else if ( ze.getName().endsWith( TDPath.TH ) ) {
          //   pathname = TDPath.getThFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.TH2 ) ) {
          //   pathname = TDPath.getTh2File( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.TNL ) ) {
          //   pathname = TDPath.getTnlFile( ze.getName() );
	  // /* FIXME_SKETCH_3D *
          // } else if ( ze.getName().endsWith( TDPath.TH3 ) ) {
          //   pathname = TDPath.getTh3File( ze.getName() );
	  // * END_SKETCH_3D */
          } else if ( ze.getName().endsWith( TDPath.TDR ) ) {
            pathname = TDPath.getTdrFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.TDR3 ) ) {
          //   pathname = TDPath.getTdr3File( ze.getName() );

          // } else if ( ze.getName().endsWith( TDPath.TRB ) ) {
          //   pathname = TDPath.getTrbFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.TRO ) ) {
          //   pathname = TDPath.getTroFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.TOP ) ) {
          //   pathname = TDPath.getTopFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.XVI ) ) {
          //   pathname = TDPath.getXviFile( ze.getName() );

          } else if ( ze.getName().endsWith( TDPath.TXT ) ) {
            pathname = TDPath.getNoteFile( ze.getName() );

          } else if ( ze.getName().endsWith( ".wav" ) ) { // AUDIOS
            pathname = TDPath.getAudioDir( surveyname );
            TDFile.makeTopoDroidDir( pathname );
            pathname = TDPath.getAudioFile( surveyname, ze.getName() );
          } else if ( ze.getName().endsWith( ".jpg" ) ) { // PHOTOS
            // FIXME need survey dir
            pathname = TDPath.getJpgDir( surveyname );
            TDFile.makeTopoDroidDir( pathname );
            pathname = TDPath.getJpgFile( surveyname, ze.getName() );
          } else if ( ze.getName().equals( "points.zip" ) ) { // POINTS
            if ( uncompressSymbols( zin, TDPath.getSymbolPointDirname(), "p_" ) ) {
              BrushManager.reloadPointLibrary( mApp, mApp.getResources() );
            }
          } else if ( ze.getName().equals( "lines.zip" ) ) { // LINES
            if ( uncompressSymbols( zin, TDPath.getSymbolLineDirname(), "l_" ) ) {
              BrushManager.reloadLineLibrary( mApp.getResources() );
            }
          } else if ( ze.getName().equals( "areas.zip" ) ) { // AREAS
            if ( uncompressSymbols( zin, TDPath.getSymbolAreaDirname(), "a_" ) ) {
              BrushManager.reloadAreaLibrary( mApp.getResources() );
            }
          } else {
            // TDLog.Error("unexpected file type " + ze.getName() );
            // pathname = null; // already null
          }
          if ( pathname != null ) {
            TDPath.checkPath( pathname );
            fout = TDFile.getFileOutputStream( pathname );
            int size = decompressEntry( zin, ze, fout );
            // TDLog.Log( TDLog.LOG_ZIP, "Unzip file \"" + pathname + "\" size " + size );
            Log.v( "DistoX", "Unzip file \"" + pathname + "\" size " + size );
            fout.close();
            if ( size > 0 ) {
              if ( sql ) {
                // TDLog.Log( TDLog.LOG_ZIP, "Zip sqlfile \"" + pathname + "\" DB version " + mApp.mManifestDbVersion );
                Log.v( "DistoX", "Zip sqlfile \"" + pathname + "\" DB version " + mApp.mManifestDbVersion );
                sql_success = ( app_data.loadFromFile( pathname, mApp.mManifestDbVersion ) >= 0 );
                TDFile.deleteFile( pathname );
              }
            } else {
              TDFile.deleteFile( pathname );
            }
          }
        }
      }
      zin.close();
      
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "ERROR File: " + e.toString() );
    } catch ( IOException e ) {
      TDLog.Error( "ERROR IO: " + e.toString() );
    }
    if ( ok_manifest == 0 && ! sql_success ) {
      TDLog.Error( "ERROR SQL" );
      // tell user that there was a problem
      return -5;
    }

    return ok_manifest; // return 0 or 1
  }

  static public int getOkManifest( InputStream fis, String filename, String surveyname )
  {
    Log.v("DistoX", "get OK manifest - file " + filename + " survey " + surveyname );
    int ok_manifest = -2;
    ZipEntry ze;
    try {
      ZipInputStream zin = new ZipInputStream( fis );
      int nr_entry = 0;
      while ( ( ze = zin.getNextEntry() ) != null ) {
        Log.v("DistoX", "get OK manifest: zentry name " + ze.getName() );
        if ( ze.getName().equals( "manifest" ) ) {
          String pathname = TDPath.getManifestFile( );
          Log.v( "DistoX", "OK imanifest: pathname " + pathname + " entry \"" + ze.getName() + "\"");
          FileOutputStream fout = TDFile.getFileOutputStream( pathname );
          int size = decompressEntry( zin, ze, fout );
          // TDLog.Log( TDLog.LOG_ZIP, "Zip imanifest: \"" + ze.getName() + "\" size " + size );
          if ( size > 0 ) {
            ok_manifest = TopoDroidApp.checkManifestFile( pathname, surveyname  ); // this sets surveyname
            // Log.v( "DistoX", "Zip manifest: \"" + ze.getName() + "\" size " + size + " ok " + ok_manifest );
          } else {
            // Log.v( "DistoX", "Zip manifest: \"" + ze.getName() + "\" size " + size );
          }
          TDFile.deleteFile( pathname );
          if ( ok_manifest < 0 ) return ok_manifest;
          // TDLog.Log( TDLog.LOG_ZIP, "un-archive manifest " + ok_manifest );
          Log.v( "DistoX", "un-archive manifest " + ok_manifest + " entry " + nr_entry + " survey " + surveyname );
          break;
        }
        zin.closeEntry();
      }
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "OK manifest ERROR File: " + e.toString() );
    } catch ( IOException e ) {
      TDLog.Error( "OK manifest ERROR IO: " + e.toString() );
    }
    return ok_manifest;
  }

  // un-archive from an imput stream
  // @param app        TopoDroid
  // @param fis        input stream
  // @param surveyname name of the survey (= new folder)
  static public int unArchive( TopoDroidApp mApp, InputStream fis, String surveyname )
  {
    int ok_manifest = 0;
    String pathname;
    ZipEntry ze;
    DataHelper app_data = TopoDroidApp.mData;
    Log.v("DistoX", "unarchive input stream - survey: " + surveyname );
    // mApp.setSurveyFromName( surveyname, -1, true ); // open survey: tell app to update survey name+id
    TDPath.setSurveyPaths( surveyname );

    try {
      // byte buffer[] = new byte[36768];
      // byte[] buffer = new byte[4096];

      // FileInputStream fis = TDFile.getFileInputStream( filename );
      
      ZipInputStream zin = new ZipInputStream( fis );
      int nr_entry = 0;
      while ( ( ze = zin.getNextEntry() ) != null ) {
        ++ nr_entry;
        if ( ze.isDirectory() ) {
          // Log.v( "DistoX", "Zip dir entry " + nr_entry + " \"" + ze.getName() + "\"");
          TDFile.makeTopoDroidDir( TDPath.getDirFile( ze.getName() ) );
        } else if ( ze.getName().equals( "manifest" ) ) {
          // Log.v( "DistoX", "Zip entry " + nr_entry + " \"manifest\": skipping ...");
          // skip
        } else {
          // Log.v( "DistoX", "Zip file entry " + nr_entry + " \"" + ze.getName() + "\"");
          TDLog.Log( TDLog.LOG_ZIP, "Zip file entry " + nr_entry + " \"" + ze.getName() + "\"");
          boolean sql = false;
          pathname = null;
          if ( ze.getName().equals( "survey.sql" ) ) {
            pathname = TDPath.getSqlFile();
            sql = true;
          } else if ( ze.getName().endsWith( TDPath.TDR ) ) {
            pathname = TDPath.getTdrFile( ze.getName() );
          // } else if ( ze.getName().endsWith( TDPath.TDR3 ) ) {
          //   pathname = TDPath.getTdr3File( ze.getName() );
          } else if ( ze.getName().endsWith( TDPath.TXT ) ) {
            pathname = TDPath.getNoteFile( ze.getName() );
          } else if ( ze.getName().endsWith( ".wav" ) ) { // AUDIOS
            pathname = TDPath.getAudioDir( surveyname );
            TDFile.makeTopoDroidDir( pathname );
            pathname = TDPath.getAudioFile( surveyname, ze.getName() );
          } else if ( ze.getName().endsWith( ".jpg" ) ) { // PHOTOS
            // FIXME need survey dir
            pathname = TDPath.getJpgDir( surveyname );
            TDFile.makeTopoDroidDir( pathname );
            pathname = TDPath.getJpgFile( surveyname, ze.getName() );
          } else if ( ze.getName().equals( "points.zip" ) ) { // POINTS
            if ( uncompressSymbols( zin, TDPath.getSymbolPointDirname(), "p_" ) ) {
              BrushManager.reloadPointLibrary( mApp, mApp.getResources() );
            }
          } else if ( ze.getName().equals( "lines.zip" ) ) { // LINES
            if ( uncompressSymbols( zin, TDPath.getSymbolLineDirname(), "l_" ) ) {
              BrushManager.reloadLineLibrary( mApp.getResources() );
            }
          } else if ( ze.getName().equals( "areas.zip" ) ) { // AREAS
            if ( uncompressSymbols( zin, TDPath.getSymbolAreaDirname(), "a_" ) ) {
              BrushManager.reloadAreaLibrary( mApp.getResources() );
            }
          } else {
            // TDLog.Error("unexpected file type " + ze.getName() );
            // pathname = null; // already null
          }
          if ( pathname != null ) {
            TDPath.checkPath( pathname );
            FileOutputStream fout = TDFile.getFileOutputStream( pathname );
            int size = decompressEntry( zin, ze, fout );
            TDLog.Log( TDLog.LOG_ZIP, "Unzip file \"" + pathname + "\" size " + size );
            // Log.v( "DistoX", "Unzip file \"" + pathname + "\" size " + size );
            if ( size <= 0 ) {
              TDFile.deleteFile( pathname );
            } else {
              if ( sql ) {
                TDLog.Log( TDLog.LOG_ZIP, "Zip sqlfile \"" + pathname + "\" DB version " + mApp.mManifestDbVersion );
                if ( app_data.loadFromFile( pathname, mApp.mManifestDbVersion ) < 0 ) ok_manifest = -5;
                TDFile.deleteFile( pathname );
              }
            }
          }
          zin.closeEntry();
        }
      }
      zin.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "ERROR File: " + e.toString() );
    } catch ( IOException e ) {
      TDLog.Error( "ERROR IO: " + e.toString() );
    }
    if ( ok_manifest < 0 ) { // delete survey folder
      // TODO
    }
    TDPath.setSurveyPaths( null );
    Log.v("DistoX", "unarchive stream returns " + ok_manifest );
    return ok_manifest; // return 0 or 1
  }

}

