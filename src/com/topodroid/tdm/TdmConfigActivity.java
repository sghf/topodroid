/** @file TdmConfigActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Manager interface activity for a tdconfig file
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDRequest;
import com.topodroid.utils.TDVersion;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.HelpDialog;
import com.topodroid.DistoX.TDandroid;
import com.topodroid.DistoX.TopoDroidApp;
import com.topodroid.DistoX.TopoDroidAlertDialog;
import com.topodroid.DistoX.DataHelper;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.TDPath;
import com.topodroid.DistoX.R;
import com.topodroid.DistoX.ExportDialogTdm;
import com.topodroid.DistoX.IExporter;
import com.topodroid.DistoX.TDandroid;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import android.widget.TextView;
import android.widget.ListView;
import android.app.Dialog;
import android.widget.Button;
import android.widget.ArrayAdapter;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;

import android.content.res.Resources;

import android.util.Log;

public class TdmConfigActivity extends Activity
                              implements OnClickListener
                              , OnItemClickListener
                              , IExporter
{
  int mNrButton1 = 5; 
  private static int[] izons = { 
    R.drawable.iz_add,
    R.drawable.iz_drop,
    R.drawable.iz_view,
    R.drawable.iz_equates,
    R.drawable.iz_3d
  };
  private static final int[] help_icons = {
    R.string.help_add_surveys,
    R.string.help_drop_surveys,
    R.string.help_view_surveys,
    R.string.help_view_equates,
    R.string.help_3d
  };

  boolean onMenu;
  int mNrMenus   = 4;
  private static int[] menus = { 
    R.string.menu_close,
    R.string.menu_export,
    R.string.menu_delete,
    R.string.menu_help
  };
  private static final int[] help_menus = {
    R.string.help_close,
    R.string.help_export_config,
    R.string.help_delete_config,
    R.string.help_help
  };
  private static final int HELP_PAGE = R.string.TdmConfigWindow;

  private TdmInputAdapter mTdmInputAdapter;
  // TdManagerApp mApp;

  static TdmConfig mTdmConfig = null;  // current config file

  private static String[] mExportTypes = { "Therion", "Survex" };

  // ----------------------------------------------------------------------
  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;

  private ListView mList;
  private Button   mImage;
  private ListView mMenu;
  private ArrayAdapter<String> mMenuAdapter;
  private Button[] mButton1;
  private int mButtonSize;

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );

    // mApp = (TdManagerApp) getApplication();

    mTdmConfig = null;
    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      String path = extras.getString( TDRequest.TDCONFIG_PATH );
      if ( path != null ) {
        mTdmConfig = new TdmConfig( path, false ); // false: no save 
        if ( mTdmConfig != null ) {
          mTdmConfig.readTdmConfig();
          setTitle( String.format( getResources().getString(R.string.project),  mTdmConfig.toString() ) );
        } else {
          TDToast.make( R.string.no_file );
        }
      } else {
        // Log.v("DistoX-TdManager", "TdmConfig activity missing TdmConfig path");
        TDToast.make( R.string.no_path );
      }
    }
    if ( mTdmConfig == null ) {
      doFinish( TDRequest.RESULT_TDCONFIG_NONE );
    } else {
      setContentView(R.layout.tdconfig_activity);
      // getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

      mList = (ListView) findViewById(R.id.th_list);
      // mList.setOnItemClickListener( this );
      mList.setDividerHeight( 2 );

      mListView = (MyHorizontalListView) findViewById(R.id.listview);
      resetButtonBar();

      mImage = (Button) findViewById( R.id.handle );
      mImage.setOnClickListener( this );
      TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( (TopoDroidApp)getApplication(), getResources(), R.drawable.iz_menu ) );

      mMenu = (ListView) findViewById( R.id.menu );
      mMenuAdapter = null;
      setMenuAdapter( getResources() );
      closeMenu();
      mMenu.setOnItemClickListener( this );

      updateList();
    }
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    // Log.v("DistoX-MANAGER", "TdmConfig activity on pause");
    if ( mTdmConfig != null ) mTdmConfig.writeTdmConfig( false );
  }

  boolean hasSource( String name ) 
  {
    return mTdmConfig.hasInput( name );
  }

  /** update surveys list
   */
  void updateList()
  {
    if ( mTdmConfig != null ) {
      // Log.v("DistoX-MANAGER", "TdmConfig update list input nr. " + mTdmConfig.getInputsSize() );
      mTdmInputAdapter = new TdmInputAdapter( this, R.layout.row, mTdmConfig.getInputs() );
      mList.setAdapter( mTdmInputAdapter );
      mList.invalidate();
    } else {
      TDToast.make( R.string.no_tdconfig );
    }
  }

  
  // -------------------------------------------------

  private void resetButtonBar()
  {
    if ( mNrButton1 > 0 ) {
      mButtonSize = TopoDroidApp.setListViewHeight( this, mListView );
      // MyButton.resetCache( size );
      // int size = TopoDroidApp.getScaledSize( this );
      // LinearLayout layout = (LinearLayout) findViewById( R.id.list_layout );
      // layout.setMinimumHeight( size + 40 );
      // LayoutParams lp = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
      // lp.setMargins( 10, 10, 10, 10 );
      // lp.width  = size;
      // lp.height = size;

      // FIXME TDMANAGER
      mButton1 = new Button[mNrButton1];

      for (int k=0; k<mNrButton1; ++k ) {
        mButton1[k] = MyButton.getButton( this, this, izons[k] );
        // layout.addView( mButton1[k], lp );
      }

      mButtonView1 = new MyHorizontalButtonView( mButton1 );
      mListView.setAdapter( mButtonView1.mAdapter );
    }
  }

  private void setMenuAdapter( Resources res )
  {
    mMenuAdapter = new ArrayAdapter<String>( this, R.layout.menu );
    for ( int k=0; k<mNrMenus; ++k ) {
      mMenuAdapter.add( res.getString( menus[k] ) );  
    }
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  private void handleMenu( int pos ) 
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) {        // CLOSE
      onBackPressed();
    } else if ( p++ == pos ) {  // EXPORT
      if ( mTdmConfig != null ) {
        new ExportDialogTdm( this, this, mExportTypes, R.string.title_export ).show();
      }
    } else if ( p++ == pos ) { // DELETE
      askDelete();
    } else if ( p++ == pos ) { // HELP
      new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }

  // ------------------------ DISPLAY -----------------------------
  private void startTdmSurveysActivity()
  {
    TdmSurvey mySurvey = new TdmSurvey( "." );

    // Log.v("DistoX-MANAGER", "start Config activity. inputs " + mTdmConfig.getInputsSize() );
    for ( TdmInput input : mTdmConfig.getInputs() ) {
      if ( input.isChecked() ) {
        // DataHelper mAppData = TopoDroidApp.mData;
        input.loadSurveyData ( TopoDroidApp.mData );
        mySurvey.addSurvey( input );
        // Log.v("DistoX-TdManager", "parse file " + input.getSurveyName() );
        // TdParser parser = new TdParser( mAppData, input.getSurveyName(), mySurvey );
      }
    }
    if ( mySurvey.mSurveys.size() == 0 ) {
      TDToast.make( R.string.no_surveys );
      return;
    }
    // list of display surveys
    mTdmConfig.populateViewSurveys( mySurvey.mSurveys );

    // TODO start drawing activity with reduced surveys
    Intent intent = new Intent( this, TdmViewActivity.class );
    startActivity( intent );
  }

  // ------------------------ ADD ------------------------------
  // called by TdmSourcesDialog with a list of sources filenames
  //
  void addSources( List< String > surveynames )
  {
    for ( String name : surveynames ) {
      // Log.v("DistoX-MANAGER", "add source " + name );
      TdmInput input = new TdmInput( name );
      // mTdmConfig.addInput( input );
      mTdmConfig.setSave();
      mTdmInputAdapter.add( input );
    }
    updateList();
  }

  // ------------------------ DELETE ------------------------------
  private void askDelete()
  {
    TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString( R.string.ask_delete_tdconfig ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doDelete();
        }
      }
    );
  }

  void doDelete()
  {
    // if ( ! TdManagerApp.deleteTdmConfigFile( mTdmConfig.getFilepath() ) ) { 
    //   TDToast.make( "delete FAILED" );
    // } else {
      doFinish( TDRequest.RESULT_TDCONFIG_DELETE );
    // }
  }

  void doFinish( int result )
  {
    Intent intent = new Intent();
    if ( mTdmConfig != null ) {
      intent.putExtra( TDRequest.TDCONFIG_PATH, mTdmConfig.getFilepath() );
    } else {
      intent.putExtra( TDRequest.TDCONFIG_PATH, "no_path" );
    }
    setResult( result, intent );
    finish();
  }
  // ---------------------- DROP SURVEYS ----------------------------
  void dropSurveys()
  {
    TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString( R.string.title_drop ), 
      new DialogInterface.OnClickListener() {
	@Override
	public void onClick( DialogInterface dialog, int btn ) {
          ArrayList< TdmInput > inputs = new ArrayList<>();
          final Iterator it = mTdmConfig.getInputsIterator();
          while ( it.hasNext() ) {
            TdmInput input = (TdmInput) it.next();
            if ( ! input.isChecked() ) {
              inputs.add( input );
            } else {
              String survey = input.getSurveyName();
              // Log.v("DistoX-TdManager", "drop survey >" + survey + "<" );
              mTdmConfig.dropEquates( survey );
            }
          }
          mTdmConfig.setInputs( inputs );
          updateList();
	} 
    } );
  }

  // ---------------------- SAVE -------------------------------------

  @Override
  public void onBackPressed()
  {
    // Log.v("DistoX-TdManager", "TdmConfig activity back pressed");
    // if ( mTdmConfig != null ) mTdmConfig.writeTdmConfig( false ); // already done by onPause
    doFinish( TDRequest.RESULT_TDCONFIG_OK );
  }

  @Override
  public void onClick(View view)
  { 
    if ( onMenu ) {
      closeMenu();
      return;
    }
    Button b0 = (Button)view;

    if ( b0 == mImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    }

    int k1 = 0;
    if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // ADD
      (new TdmSourcesDialog(this, this)).show();
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // DROP
      boolean drop = false;
      final Iterator it = mTdmConfig.getInputsIterator();
      while ( it.hasNext() ) {
        TdmInput input = (TdmInput) it.next();
        if ( input.isChecked() ) { drop = true; break; }
      }
      if ( drop ) {
        dropSurveys();
      } else {
        TDToast.make( R.string.no_survey );
      }
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // VIEW
      startTdmSurveysActivity();
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // EQUATES
      (new TdmEquatesDialog( this, mTdmConfig, null )).show();
    } else if ( k1 < mNrButton1 && b0 == mButton1[k1++] ) {  // 3D
      int check = TDVersion.checkCave3DVersion( this );
      if ( check < 0 ) {
        TDToast.makeBad( R.string.no_cave3d );
      } else if ( check > 0 ) {
        TDToast.makeBad( R.string.outdated_cave3d );
      } else {
        try {
          // Log.v("DistoX-TdManager", "Cave3D of " + mTdmConfig.getFilepath() );
          Intent intent = new Intent( "Cave3D.intent.action.Launch" );
          intent.putExtra( "INPUT_THCONFIG", mTdmConfig.getSurveyName() ); // thconfig (project) name, without ".thconfig" extension
          intent.putExtra( "SURVEY_BASE", TDPath.getPathBase() );          // current work directory
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          TDToast.make( R.string.no_cave3d );
        }
      }
    }
  }

  // @implements IExporter
  public void doExport( String type )
  {
    boolean overwrite = true;
    String filepath = null;
    if ( type.equals("Therion") ) {
       filepath = mTdmConfig.exportTherion( overwrite );
    } else if ( type.equals("Survex") ) {
       filepath = mTdmConfig.exportSurvex( overwrite );
    }
    if ( filepath != null ) {
      TDToast.make( String.format( getResources().getString(R.string.exported), filepath ) );
    } else {
      TDToast.make( R.string.export_failed );
    }
  }


  @Override
  public void onItemClick( AdapterView<?> parent, View view, int pos, long id )
  {
    // CharSequence item = ((TextView) view).getText();
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }
  }

}
