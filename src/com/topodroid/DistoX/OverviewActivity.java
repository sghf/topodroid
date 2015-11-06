/* @file OverviewActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketch overview activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;

import android.util.TypedValue;

import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.view.ViewGroup;
import android.view.Display;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
// import android.view.ContextMenu;
// import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ZoomControls;
import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.util.FloatMath;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

/**
 */
public class OverviewActivity extends ItemDrawer
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      , OnItemClickListener
                                      , OnZoomListener
                                      , IZoomer
{
  private static int izons[] = { 
                        R.drawable.iz_measure,       // 0
                        R.drawable.iz_mode,          // 1
                        // FIXME_OVER R.drawable.iz_plan,          // 2
                        R.drawable.iz_menu,          // 3
                        R.drawable.iz_measure_on,
                      };
  // FIXME_OVER private static int BTN_PLOT = 2;

  private static int menus[] = {
                        R.string.menu_options,
                        R.string.menu_help
                     };

  private static int help_icons[] = { 
                        R.string.help_measure,
                        R.string.help_refs,
                        // FIXME_OVER R.string.help_toggle_plot,
                      };
  private static int help_menus[] = {
                        R.string.help_prefs,
                        R.string.help_help
                      };
  // FIXME_OVER BitmapDrawable mBMextend;
  // FIXME_OVER BitmapDrawable mBMplan;
  BitmapDrawable mBMselect;
  BitmapDrawable mBMselectOn;

    private TopoDroidApp mApp;
    private DataHelper mData;

    long getSID() { return mApp.mSID; }
    String getSurvey() { return mApp.mySurvey; }

    private static BezierInterpolator mBezierInterpolator = new BezierInterpolator();
    private DrawingSurface  mOverviewSurface;

    private DistoXNum mNum;
    private Path mCrossPath;
    private Path mCirclePath;

    String mName1;  // first name (PLAN)
    String mName2;  // second name (EXTENDED)

    boolean mZoomBtnsCtrlOn = false;
    ZoomButtonsController mZoomBtnsCtrl = null;
    View mZoomView;
    // ZoomControls mZoomCtrl;
    // ZoomButton mZoomOut;
    // ZoomButton mZoomIn;

    private static final float ZOOM_INC = 1.4f;
    private static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    public static final int MODE_MOVE = 2;
    public static final int MODE_ZOOM = 4;  

    private int mTouchMode = MODE_MOVE;
    private int mOnMeasure = 0;

    private float mSaveX;
    private float mSaveY;
    private PointF mOffset  = new PointF( 0f, 0f );
    private PointF mDisplayCenter;
    private float mZoom  = 1.0f;

    private long mSid;  // survey id
    private long mType;  // current plot type
    private String mFrom;
    private PlotInfo mPlot1;

    @Override
    public void onVisibilityChanged(boolean visible)
    {
      if ( mZoomBtnsCtrlOn && mZoomBtnsCtrl != null ) {
        mZoomBtnsCtrl.setVisible( visible || ( TopoDroidSetting.mZoomCtrl > 1 ) );
      }
    }

    @Override
    public void onZoom( boolean zoomin )
    {
      if ( zoomin ) changeZoom( ZOOM_INC );
      else changeZoom( ZOOM_DEC );
    }

    private void changeZoom( float f ) 
    {
      float zoom = mZoom;
      mZoom     *= f;
      // Log.v( TopoDroidApp.TAG, "zoom " + mZoom );
      mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
      mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
      mOverviewSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      // mOverviewSurface.refresh();
      // mZoomCtrl.hide();
      // mZoomBtnsCtrl.setVisible( false );
    }

    // private void resetZoom() 
    // {
    //   int w = mOverviewSurface.width();
    //   int h = mOverviewSurface.height();
    //   mOffset.x = w/4;
    //   mOffset.y = h/4;
    //   mZoom = mApp.mScaleFactor;
    //   // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "zoom one " + mZoom + " off " + mOffset.x + " " + mOffset.y );
    //   if ( mType == PlotInfo.PLOT_PLAN ) {
    //     float zx = w/(mNum.surveyEmax() - mNum.surveyEmin());
    //     float zy = h/(mNum.surveySmax() - mNum.surveySmin());
    //     mZoom = (( zx < zy )? zx : zy)/40;
    //   } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
    //     float zx = w/(mNum.surveyHmax() - mNum.surveyHmin());
    //     float zy = h/(mNum.surveyVmax() - mNum.surveyVmin());
    //     mZoom = (( zx < zy )? zx : zy)/40;
    //   } else {
    //     mZoom = mApp.mScaleFactor;
    //     mOffset.x = 0.0f;
    //     mOffset.y = 0.0f;
    //   }
    //     
    //   // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "zoom one to " + mZoom );
    //     
    //   mOverviewSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    //   // mOverviewSurface.refresh();
    // }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }
    // public void zoomOne() { resetZoom( ); }

    // public void zoomView( )
    // {
    //   // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "zoomView ");
    //   DrawingZoomDialog zoom = new DrawingZoomDialog( mOverviewSurface.getContext(), this );
    //   zoom.show();
    // }


    // splay = false
    // selectable = false
    private void addFixedLine( DistoXDBlock blk, float x1, float y1, float x2, float y2, float xoff, float yoff, boolean splay )
    {
      DrawingPath dpath = null;
      if ( splay ) {
        dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
        if ( blk.mClino > TopoDroidSetting.mVertSplay ) {
          dpath.setPaint( DrawingBrushPaths.fixedSplay4Paint );
        } else if ( blk.mClino < -TopoDroidSetting.mVertSplay ) {
          dpath.setPaint( DrawingBrushPaths.fixedSplay3Paint );
        } else {
          dpath.setPaint( DrawingBrushPaths.fixedSplayPaint );
        }
      } else {
        dpath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk );
        dpath.setPaint( DrawingBrushPaths.fixedShotPaint );
      }
      DrawingUtil.makePath( dpath, x1, y1, x2, y2, xoff, yoff );
      mOverviewSurface.addFixedPath( dpath, splay, false ); // false: non-selectable
    }

    // --------------------------------------------------------------------------------------

    @Override
    protected void setTheTitle()
    {
      // setTitle( res.getString( R.string.title_move ) );
    }

    // private void AlertMissingSymbols()
    // {
    //   new TopoDroidAlertDialog( this, getResources(),
    //                     getResources().getString( R.string.missing_symbols ),
    //     new DialogInterface.OnClickListener() {
    //       @Override
    //       public void onClick( DialogInterface dialog, int btn ) {
    //         mAllSymbols = true;
    //       }
    //     }
    //   );
    // }


  private void computeReferences( int type, float xoff, float yoff, float zoom )
  {
    // Log.v("DistoX", "Overview compute reference. Type " + type );
    // FIXME_OVER
    // mOverviewSurface.clearReferences( type );
    // mOverviewSurface.setManager( type );
    mOverviewSurface.setManager( PlotInfo.PLOT_PLAN ); // MUST use first commandManager

    if ( type == PlotInfo.PLOT_PLAN ) {
      DrawingUtil.addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax(),
                           xoff, yoff, mOverviewSurface );
    } else {
      DrawingUtil.addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax(),
                           xoff, yoff, mOverviewSurface );
    }

    List< NumStation > stations = mNum.getStations();
    List< NumShot > shots = mNum.getShots();
    List< NumSplay > splays = mNum.getSplays();
    // Log.v("DistoX", "Overview stations " + stations.size() + " shots " + shots.size() + " splays " + splays.size() );

    if ( type == PlotInfo.PLOT_PLAN ) {
      for ( NumShot sh : shots ) {
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        addFixedLine( sh.getFirstBlock(), (float)(st1.e), (float)(st1.s), (float)(st2.e), (float)(st2.s),
                      xoff, yoff, false );
      }
      for ( NumSplay sp : splays ) {
        if ( Math.abs( sp.getBlock().mClino ) < TopoDroidSetting.mSplayVertThrs ) {
          NumStation st = sp.from;
          addFixedLine( sp.getBlock(), (float)(st.e), (float)(st.s), (float)(sp.e), (float)(sp.s), 
                        xoff, yoff, true );
        }
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        dst = mOverviewSurface.addDrawingStation( st, DrawingUtil.toSceneX(st.e) - xoff, DrawingUtil.toSceneY(st.s) - yoff, true );
      }
    } else { // if ( type == PlotInfo.PLOT_EXTENDED && 
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          addFixedLine( sh.getFirstBlock(), (float)(st1.h), (float)(st1.v), (float)(st2.h), (float)(st2.v),
                        xoff, yoff, false );
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        addFixedLine( sp.getBlock(), (float)(st.h), (float)(st.v), (float)(sp.h), (float)(sp.v), 
                      xoff, yoff, true );
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        dst = mOverviewSurface.addDrawingStation( st, DrawingUtil.toSceneX(st.h) - xoff, DrawingUtil.toSceneY(st.v) - yoff, true );
      }
    }

    // if ( (! mNum.surveyAttached) && TopoDroidSetting.mCheckAttached ) {
    //   Toast.makeText( this, R.string.survey_not_attached, Toast.LENGTH_SHORT ).show();
    // }
  }
    

    // ------------------------------------------------------------------------------
    // BUTTON BAR
  
    private Button[] mButton1; // primary
    private int mNrButton1 = 2;          // main-primary
    HorizontalListView mListView;
    HorizontalButtonView mButtonView1;
    ListView   mMenu;
    Button     mImage;
    ArrayAdapter< String > mMenuAdapter;
    boolean onMenu;
  
    List<DistoXDBlock> mBlockList = null;
  
    public float zoom() { return mZoom; }


    // this method is a callback to let other objects tell the activity to use zooms or not
    private void switchZoomCtrl( int ctrl )
    {
      // Log.v("DistoX", "DEBUG switchZoomCtrl " + ctrl + " ctrl is " + ((mZoomBtnsCtrl == null )? "null" : "not null") );
      if ( mZoomBtnsCtrl == null ) return;
      mZoomBtnsCtrlOn = (ctrl > 0);
      switch ( ctrl ) {
        case 0:
          mZoomBtnsCtrl.setOnZoomListener( null );
          mZoomBtnsCtrl.setVisible( false );
          mZoomBtnsCtrl.setZoomInEnabled( false );
          mZoomBtnsCtrl.setZoomOutEnabled( false );
          mZoomView.setVisibility( View.GONE );
          break;
        case 1:
          mZoomView.setVisibility( View.VISIBLE );
          mZoomBtnsCtrl.setOnZoomListener( this );
          mZoomBtnsCtrl.setVisible( false );
          mZoomBtnsCtrl.setZoomInEnabled( true );
          mZoomBtnsCtrl.setZoomOutEnabled( true );
          break;
        case 2:
          mZoomView.setVisibility( View.VISIBLE );
          mZoomBtnsCtrl.setOnZoomListener( this );
          mZoomBtnsCtrl.setVisible( true );
          mZoomBtnsCtrl.setZoomInEnabled( true );
          mZoomBtnsCtrl.setZoomOutEnabled( true );
          break;
      }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);

      mCrossPath = new Path();
      mCrossPath.moveTo(10,10);
      mCrossPath.lineTo(-10,-10);
      mCrossPath.moveTo(10,-10);
      mCrossPath.lineTo(-10,10);
      mCirclePath = new Path();
      mCirclePath.addCircle( 0, 0, 15, Path.Direction.CCW );

      // Display display = getWindowManager().getDefaultDisplay();
      // DisplayMetrics dm = new DisplayMetrics();
      // display.getMetrics( dm );
      // int width = dm widthPixels;
      int width = getResources().getDisplayMetrics().widthPixels;

      setContentView(R.layout.overview_activity);
      mApp = (TopoDroidApp)getApplication();
      // mZoom = mApp.mScaleFactor;    // canvas zoom

      mDisplayCenter = new PointF(mApp.mDisplayWidth  / 2, mApp.mDisplayHeight / 2);

      mOverviewSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
      mOverviewSurface.setZoomer( this );
      mOverviewSurface.setOnTouchListener(this);

      mZoomView = (View) findViewById(R.id.zoomView );
      mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );

      switchZoomCtrl( TopoDroidSetting.mZoomCtrl );

      mListView = (HorizontalListView) findViewById(R.id.listview);
      int size = mApp.setListViewHeight( mListView );

      mButton1 = new Button[ mNrButton1 ];
      for ( int k=0; k<mNrButton1; ++k ) {
        mButton1[k] = new Button( this );
        mButton1[k].setPadding(0,0,0,0);
        mButton1[k].setOnClickListener( this );
        BitmapDrawable bm = mApp.setButtonBackground( mButton1[k], size, izons[k] );
        if ( k == 0 ) { // IC_SELECT = 0;
          mBMselect = bm;
        // FIXME_OVER } else if ( k == 2 ) { // IC_PLAN = 2;
        // FIXME_OVER   mBMplan = bm;
        }
      }
      mBMselectOn = mApp.setButtonBackground( null, size, R.drawable.iz_measure_on );
      // FIXME_OVER mBMextend  = mApp.setButtonBackground( null, mButtonSize, izons[IC_EXTEND] ); 

      mButtonView1 = new HorizontalButtonView( mButton1 );
      mListView.setAdapter( mButtonView1.mAdapter );

      DrawingBrushPaths.makePaths( getResources() );
      setTheTitle();

      mData        = mApp.mData; // new DataHelper( this ); 
      Bundle extras = getIntent().getExtras();
      mSid         = extras.getLong( TopoDroidTag.TOPODROID_SURVEY_ID );
      mFrom        = extras.getString( TopoDroidTag.TOPODROID_PLOT_FROM );
      mZoom        = extras.getFloat( TopoDroidTag.TOPODROID_PLOT_ZOOM );
      mType = (int)extras.getLong( TopoDroidTag.TOPODROID_PLOT_TYPE );

      // Log.v("DistoX", "Overview from " + mFrom + " Type " + mType + " Zoom " + mZoom );

      // mBezierInterpolator = new BezierInterpolator( );

      mImage = (Button) findViewById( R.id.handle );
      mImage.setOnClickListener( this );
      mApp.setButtonBackground( mImage, size, R.drawable.iz_menu );
      mMenu = (ListView) findViewById( R.id.menu );
      setMenuAdapter();
      closeMenu();
      mMenu.setOnItemClickListener( this );

      doStart();
      // Log.v("DistoX", "Overview offset " + mOffset.x + " " + mOffset.y );

      mOffset.x   += extras.getFloat( TopoDroidTag.TOPODROID_PLOT_XOFF );
      mOffset.y   += extras.getFloat( TopoDroidTag.TOPODROID_PLOT_YOFF );
      mOverviewSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    }

    @Override
    protected synchronized void onResume()
    {
      super.onResume();
      doResume();
    }

    @Override
    protected synchronized void onPause() 
    { 
      super.onPause();
      // Log.v("DistoX", "Drawing Activity onPause " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      doPause();
    }

    @Override
    protected synchronized void onStart()
    {
      super.onStart();
    }

    @Override
    protected synchronized void onStop()
    {
      super.onStop();
    }

    private void doResume()
    {
      // PlotInfo info = mApp.mData.getPlotInfo( mSid, mName );
      // mOffset.x = info.xoffset;
      // mOffset.y = info.yoffset;
      // mZoom     = info.zoom;
      mOverviewSurface.isDrawing = true;
      switchZoomCtrl( TopoDroidSetting.mZoomCtrl );
    }

    private void doPause()
    {
      switchZoomCtrl( 0 );
      mOverviewSurface.isDrawing = false;
    }

// ----------------------------------------------------------------------------

    private void doStart()
    {
      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "do Start " + mName1 + " " + mName2 );
      // mBlockList = mData.selectAllLegShots( mSid, TopoDroidApp.STATUS_NORMAL );
      mBlockList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      if ( mBlockList.size() == 0 ) {
        Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
        finish();
      } else {
        loadFiles( mType ); 
      }
    }

    boolean mAllSymbols = true;

    private void loadFiles( long type )
    {
      List<PlotInfo> plots = mApp.mData.selectAllPlotsWithType( mSid, TopoDroidApp.STATUS_NORMAL, type );

      // Log.v( "DistoX", "Overview plots " + plots.size() );

      // if ( plots.size() < 1 ) { // N.B. this should never happpen
      //   Toast.makeText( this, R.string.few_plots, Toast.LENGTH_SHORT ).show();
      //   finish();
      //   return;
      // }
      mAllSymbols  = true; // by default there are all the symbols
      SymbolsPalette missingSymbols = new SymbolsPalette(); 

      NumStation mStartStation = null;

      for ( int k=0; k<plots.size(); ++k ) {
        PlotInfo plot = plots.get(k);
        // Log.v( "DistoX", "plot " + plot.name );

        String start = plot.start;
        float xdelta = 0.0f;
        float ydelta = 0.0f;
        if ( k == 0 ) {
          String view  = plot.view;
          mPlot1 = plot;
          // mPid = plot.id;
          mNum = new DistoXNum( mBlockList, start, null, null );
          mStartStation = mNum.getStation( start );
          computeReferences( (int)type, mOffset.x, mOffset.y, mZoom );
          // Log.v( "DistoX", "Overview num stations " + mNum.stationsNr() + " shots " + mNum.shotsNr() );
        } else {
          NumStation st = mNum.getStation( start );
          if ( type == PlotInfo.PLOT_PLAN ) {
            xdelta = st.e - mStartStation.e; // FIXME SCALE FACTORS ???
            ydelta = st.s - mStartStation.s;
          } else {
            xdelta = st.h - mStartStation.h;
            ydelta = st.v - mStartStation.v;
          }
        }
        xdelta *= DrawingUtil.SCALE_FIX;
        ydelta *= DrawingUtil.SCALE_FIX;
        // Log.v( "DistoX", " delta " + xdelta + " " + ydelta );

        // now try to load drawings from therion file
        String fullName = mApp.mySurvey + "-" + plot.name;
        TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "load th2 file " + fullName );
        // Log.v("DistoX", "Overview load file " + fullName );

        String filename = TopoDroidPath.getTh2FileWithExt( fullName );
        // FIXME_OVER 
        // N.B. this loads the drawing on DrawingSurface.mCommandManager1
        boolean all_symbols = mOverviewSurface.loadTherion( filename, xdelta, ydelta, missingSymbols );
        mAllSymbols = mAllSymbols && all_symbols;
      }

      if ( ! mAllSymbols ) {
        String msg = missingSymbols.getMessage( getResources() );
        TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "Missing " + msg );
        Toast.makeText( this, "Missing symbols \n" + msg, Toast.LENGTH_LONG ).show();
        // (new MissingDialog( this, this, msg )).show();
        // finish();
      }

      // // resetZoom();
      // resetReference( mPlot1 );
   }

   // private void saveReference( PlotInfo plot, long pid )
   // {
   //   // Log.v("DistoX", "save ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
   //   plot.xoffset = mOffset.x;
   //   plot.yoffset = mOffset.y;
   //   plot.zoom    = mZoom;
   //   mData.updatePlot( pid, mSid, mOffset.x, mOffset.y, mZoom );
   // }

   // private void resetReference( PlotInfo plot )
   // {
   //   mOffset.x = plot.xoffset; 
   //   mOffset.y = plot.yoffset; 
   //   mZoom     = plot.zoom;    
   //   // Log.v("DistoX", "reset ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
   //   mOverviewSurface.setTransform( mOffset.x, mOffset.y, mZoom );
   //   // mOverviewSurface.refresh();
   // }

    float mSave0X, mSave0Y;
    float mSave1X, mSave1Y;

    
    private void dumpEvent( WrapMotionEvent ev )
    {
      String name[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
      StringBuilder sb = new StringBuilder();
      int action = ev.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      sb.append( "Event action_").append( name[actionCode] );
      if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
        sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
      }
      sb.append( " [" );
      for (int i=0; i<ev.getPointerCount(); ++i ) {
        sb.append( "#" ).append( i );
        sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
        if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
      }
      sb.append( "]" );
      // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, sb.toString() );
    }
    

    float spacing( WrapMotionEvent ev )
    {
      int np = ev.getPointerCount();
      if ( np < 2 ) return 0.0f;
      float x = ev.getX(1) - ev.getX(0);
      float y = ev.getY(1) - ev.getY(0);
      return FloatMath.sqrt(x*x + y*y);
    }

    void saveEventPoint( WrapMotionEvent ev )
    {
      int np = ev.getPointerCount();
      if ( np >= 1 ) {
        mSave0X = ev.getX(0);
        mSave0Y = ev.getY(0);
        if ( np >= 2 ) {
          mSave1X = ev.getX(1);
          mSave1Y = ev.getY(1);
        } else {
          mSave1X = mSave0X;
          mSave1Y = mSave0Y;
        } 
      }
    }

    
    void shiftByEvent( WrapMotionEvent ev )
    {
      float x0 = 0.0f;
      float y0 = 0.0f;
      float x1 = 0.0f;
      float y1 = 0.0f;
      int np = ev.getPointerCount();
      if ( np >= 1 ) {
        x0 = ev.getX(0);
        y0 = ev.getY(0);
        if ( np >= 2 ) {
          x1 = ev.getX(1);
          y1 = ev.getY(1);
        } else {
          x1 = x0;
          y1 = y0;
        } 
      }
      float x_shift = ( x0 - mSave0X + x1 - mSave1X ) / 2;
      float y_shift = ( y0 - mSave0Y + y1 - mSave1Y ) / 2;
      mSave0X = x0;
      mSave0Y = y0;
      mSave1X = x1;
      mSave1Y = y1;
    
      if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        mOverviewSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      }

    }

    private void moveCanvas( float x_shift, float y_shift )
    {
      if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        mOverviewSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        // mOverviewSurface.refresh();
      }
    }


    float oldDist = 0;
    float mStartX = 0;
    float mStartY = 0;

    public boolean onTouch( View view, MotionEvent rawEvent )
    {
      float d0 = TopoDroidSetting.mCloseCutoff + TopoDroidSetting.mCloseness / mZoom;
      checkZoomBtnsCtrl();

      WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
      // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingActivity onTouch() " );
      // dumpEvent( event );

      float x_canvas = event.getX();
      float y_canvas = event.getY();
      // Log.v("DistoX", "touch canvas " + x_canvas + " " + y_canvas ); 

      if ( mZoomBtnsCtrlOn && y_canvas > DrawingUtil.CENTER_Y*2-20 ) {
        mZoomBtnsCtrl.setVisible( true );
        // mZoomCtrl.show( );
      }
      float x_scene = x_canvas/mZoom - mOffset.x;
      float y_scene = y_canvas/mZoom - mOffset.y;
      // Log.v("DistoX", "touch scene " + x_scene + " " + y_scene );

      int action = event.getAction() & MotionEvent.ACTION_MASK;

      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        if ( mOnMeasure == 2 ) mOnMeasure = 1;
        mTouchMode = MODE_ZOOM;
        oldDist = spacing( event );
        saveEventPoint( event );
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        if ( mOnMeasure == 1 ) mOnMeasure = 2;
        mTouchMode = MODE_MOVE;
        /* nothing */

      // ---------------------------------------- DOWN

      } else if (action == MotionEvent.ACTION_DOWN) {
        mSaveX = x_canvas; // FIXME-000
        mSaveY = y_canvas;
        if ( mOnMeasure == 1 ) {
          mStartX = x_canvas/mZoom - mOffset.x;
          mStartY = y_canvas/mZoom - mOffset.y;
          mOnMeasure = 2;
          // add reference point
          DrawingPath path = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH );
          path.setPaint( DrawingBrushPaths.highlightPaint );
          path.makePath( mCirclePath, new Matrix(), mStartX, mStartY );
          mOverviewSurface.setFirstReference( path );
        } else if ( mOnMeasure == 2 ) {
          // FIXME use scene values
          float x = x_canvas/mZoom - mOffset.x;
          float y = y_canvas/mZoom - mOffset.y;
          float dx = (  (x - mStartX) / DrawingUtil.SCALE_FIX ) / TopoDroidSetting.mUnitGrid;
          float dy = ( -(y - mStartY) / DrawingUtil.SCALE_FIX ) / TopoDroidSetting.mUnitGrid;
          // StringWriter sw = new StringWriter();
          // PrintWriter pw = new PrintWriter( sw );
          double a = Math.atan2( dx, dy ) * 180 / Math.PI;
          if ( a < 0 ) a += 360;

          String format;
          if ( mType == PlotInfo.PLOT_PLAN ) {
            format = getResources().getString( R.string.format_measure_plan );
          } else {
            if ( a <= 180 ) {
              a = 90 - a;
            } else {
              a = a - 270;
            } 
            format = getResources().getString( R.string.format_measure_profile );
          }
          a *= TopoDroidSetting.mUnitAngle;

          String msg = String.format( format, FloatMath.sqrt( dx * dx + dy * dy ), dx, dy, a );
          // pw.format("%.2f DX %.2f DY %.2f Bearing %.1f ", 
          // setTitle( sw.getBuffer().toString() );
          setTitle( msg );
          // replace target point
          DrawingPath path = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH );
          path.setPaint( DrawingBrushPaths.fixedBluePaint );
          path.makePath( mCrossPath, new Matrix(), x, y );
          path.mPath.moveTo( mStartX, mStartY );
          path.mPath.lineTo( x, y );
          mOverviewSurface.setSecondReference( path );
        }
      // ---------------------------------------- MOVE

      } else if ( action == MotionEvent.ACTION_MOVE ) {
        if ( mTouchMode == MODE_MOVE) {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          if ( mOnMeasure == 0 ) {
            if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
              mOffset.x += x_shift / mZoom;                // add shift to offset
              mOffset.y += y_shift / mZoom; 
              mOverviewSurface.setTransform( mOffset.x, mOffset.y, mZoom );
            }
            mSaveX = x_canvas; 
            mSaveY = y_canvas;
          }
        } else { // mTouchMode == MODE_ZOOM
          float newDist = spacing( event );
          if ( newDist > 16.0f && oldDist > 16.0f ) {
            float factor = newDist/oldDist;
            if ( factor > 0.05f && factor < 4.0f ) {
              changeZoom( factor );
              oldDist = newDist;
            }
          }
          shiftByEvent( event );
        }

      // ---------------------------------------- UP

      } else if (action == MotionEvent.ACTION_UP) {
        if ( onMenu ) {
          closeMenu();
          return true;
        }

        if ( mTouchMode == MODE_ZOOM ) {
          mTouchMode = MODE_MOVE;
        } else {
          // NOTHING
          // if ( mOnMeasure == 0 ) {
          //   // float x_shift = x_canvas - mSaveX; // compute shift
          //   // float y_shift = y_canvas - mSaveY;
          // } else {
          // }
        }
      }
      return true;
    }


    private Button makeButton( String text )
    {
      Button myTextView = new Button( this );
      myTextView.setHeight( 42 );

      myTextView.setText( text );
      myTextView.setTextColor( 0xffffffff );
      myTextView.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 16 );
      myTextView.setBackgroundColor( 0xff333333 );
      myTextView.setSingleLine( true );
      myTextView.setGravity( 0x03 ); // left
      myTextView.setPadding( 4, 4, 4, 4 );
      // Log.v(TopoDroidApp.TAG, "makeButton " + text );
      return myTextView;
    }

    /* FIXME_OVER
    private void switchPlotType()
    {
      if ( mType == PlotInfo.PLOT_PLAN ) {
        // saveReference( mPlot1, mPid1 );
        // mPid  = mPid2;
        mType = (int)PlotInfo.PLOT_EXTENDED;
        mButton1[ BTN_PLOT ].setBackgroundDrawable( mBMextend );

        mOverviewSurface.setManager( mType );
        resetReference( mPlot2 );
      } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
        // saveReference( mPlot2, mPid2 );
        // mPid  = mPid1;
        // mName = mName1;
        mType = (int)PlotInfo.PLOT_PLAN;
        mButton1[ BTN_PLOT ].setBackgroundDrawable( mBMplan );
        mOverviewSurface.setManager( mType );
        resetReference( mPlot1 );
      }
    }
    */
  
    public void onClick(View view)
    {
      if ( onMenu ) {
        closeMenu();
        return;
      }

      Button b = (Button)view;
      if ( b == mImage ) {
        if ( mMenu.getVisibility() == View.VISIBLE ) {
          mMenu.setVisibility( View.GONE );
          onMenu = false;
        } else {
          mMenu.setVisibility( View.VISIBLE );
          onMenu = true;
        }
        return;
      }
      if ( b == mButton1[0] ) { // measure
        if ( mOnMeasure > 0 ) {
          mOnMeasure = 0;
          mButton1[0].setBackgroundDrawable( mBMselect );
          mOverviewSurface.setFirstReference( null );
          mOverviewSurface.setSecondReference( null );
        } else {
          mOnMeasure = 1;
          mButton1[0].setBackgroundDrawable( mBMselectOn );
        }
      } else if ( b == mButton1[1] ) { // references
        new DrawingModeDialog( this, null, mOverviewSurface ).show();

      // FIXME_OVER } else if ( b == mButton1[2] ) { // toggle plan/extended
      // FIXME_OVER   switchPlotType();
      }
    }


  @Override
  public boolean onSearchRequested()
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "search requested" );
    Intent intent = new Intent( this, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
    startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        return onSearchRequested();
      case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "key down: code " + code );
    }
    return false;
  }


  private void setMenuAdapter()
  {
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // OPTIONS
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 2 ) ).show();
      }
    }
  }


  public void checkZoomBtnsCtrl()
  {
    if ( TopoDroidSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
      mZoomBtnsCtrl.setVisible( true );
    }
  }

}
