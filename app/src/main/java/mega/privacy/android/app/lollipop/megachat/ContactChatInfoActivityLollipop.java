package mega.privacy.android.app.lollipop.megachat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.MegaPreferences;
import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.PinActivityLollipop;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;


@SuppressLint("NewApi")
public class ContactChatInfoActivityLollipop extends PinActivityLollipop implements OnClickListener, MegaRequestListenerInterface, OnCheckedChangeListener, OnItemClickListener{

	RelativeLayout imageLayout;

	CollapsingToolbarLayout collapsingToolbar;
	TextView initialLetter;
	ImageView contactPropertiesImage;
	LinearLayout optionsLayout;
	LinearLayout notificationsLayout;
	Switch notificationsSwitch;
	TextView notificationsTitle;

	RelativeLayout messageSoundLayout;
	TextView messageSoundText;
	View dividerMessageSoundLayout;

	RelativeLayout ringtoneLayout;
	TextView ringtoneText;
	View dividerRingtoneLayout;

	RelativeLayout shareContactLayout;
	TextView shareContactText;
	View dividerShareContactLayout;

	RelativeLayout clearChatLayout;

	Toolbar toolbar;
	ActionBar aB;

	MegaUser user;
	String userEmail;
	String fullName;

	private MegaApiAndroid megaApi = null;

	private static int EDIT_TEXT_ID = 1;
	private Handler handler;

	Display display;
	DisplayMetrics outMetrics;
	float density;
	float scaleW;
	float scaleH;

	DatabaseHandler dbH = null;
	MegaPreferences prefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		log("onCreate");
		if (megaApi == null){
			MegaApplication app = (MegaApplication)getApplication();
			megaApi = app.getMegaApi();
		}

		handler = new Handler();

		dbH = DatabaseHandler.getDbHandler(getApplicationContext());

		display = getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;

	    scaleW = Util.getScaleW(outMetrics, density);
	    scaleH = Util.getScaleH(outMetrics, density);

		Bundle extras = getIntent().getExtras();
		if (extras != null){
			userEmail = extras.getString("userEmail");
			if(userEmail==null){
				log("userMail is NULL");
				finish();
				return;
			}
			fullName = extras.getString("userFullName");

			user = megaApi.getContact(userEmail);
			if(user==null){
				log("MegaUser is NULL");
				finish();
				return;
			}

			setContentView(R.layout.activity_chat_contact_properties);
			toolbar = (Toolbar) findViewById(R.id.toolbar);
			setSupportActionBar(toolbar);
			aB = getSupportActionBar();
			imageLayout = (RelativeLayout) findViewById(R.id.chat_contact_properties_image_layout);
			collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
			collapsingToolbar.setTitle(fullName);
			collapsingToolbar.setExpandedTitleMarginBottom(Util.scaleHeightPx(24, outMetrics));
			collapsingToolbar.setExpandedTitleMarginStart(Util.scaleWidthPx(72, outMetrics));
			getSupportActionBar().setDisplayShowTitleEnabled(false);

			collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.white));
			collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(this, R.color.white));

			aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
			aB.setHomeButtonEnabled(true);
			aB.setDisplayHomeAsUpEnabled(true);

			contactPropertiesImage = (ImageView) findViewById(R.id.chat_contact_properties_toolbar_image);
			initialLetter = (TextView) findViewById(R.id.chat_contact_properties_toolbar_initial_letter);

			float scaleText;
			if (scaleH < scaleW){
				scaleText = scaleH;
			}
			else{
				scaleText = scaleW;
			}

			setDefaultAvatar();

			setAvatar();

			//OPTIONS LAYOUT
			optionsLayout = (LinearLayout) findViewById(R.id.chat_contact_properties_options);

			//Notifications Layout

			notificationsLayout = (LinearLayout) findViewById(R.id.chat_contact_properties_notifications_layout);
			notificationsLayout.setVisibility(View.VISIBLE);

			notificationsTitle = (TextView) findViewById(R.id.chat_contact_properties_notifications_text);

			notificationsSwitch = (Switch) findViewById(R.id.chat_contact_properties_switch);
			notificationsSwitch.setOnCheckedChangeListener(this);
			LinearLayout.LayoutParams paramsSwitch = (LinearLayout.LayoutParams) notificationsSwitch.getLayoutParams();
			paramsSwitch.rightMargin = Util.scaleWidthPx(16, outMetrics);
			notificationsSwitch.setLayoutParams(paramsSwitch);

			//Chat message sound Layout

			messageSoundLayout = (RelativeLayout) findViewById(R.id.chat_contact_properties_messages_sound_layout);
			messageSoundLayout.setOnClickListener(this);
			LinearLayout.LayoutParams paramsSound = (LinearLayout.LayoutParams) messageSoundLayout.getLayoutParams();
			paramsSound.leftMargin = Util.scaleWidthPx(72, outMetrics);
			messageSoundLayout.setLayoutParams(paramsSound);

			messageSoundText = (TextView) findViewById(R.id.chat_contact_properties_messages_sound);

			dividerMessageSoundLayout = (View) findViewById(R.id.divider_message_sound_layout);
			LinearLayout.LayoutParams paramsDividerSound = (LinearLayout.LayoutParams) dividerMessageSoundLayout.getLayoutParams();
			paramsDividerSound.leftMargin = Util.scaleWidthPx(72, outMetrics);
			dividerMessageSoundLayout.setLayoutParams(paramsDividerSound);

			//Call ringtone Layout

			ringtoneLayout = (RelativeLayout) findViewById(R.id.chat_contact_properties_ringtone_layout);
			ringtoneLayout.setOnClickListener(this);
			LinearLayout.LayoutParams paramsRingtone = (LinearLayout.LayoutParams) ringtoneLayout.getLayoutParams();
			paramsRingtone.leftMargin = Util.scaleWidthPx(72, outMetrics);
			ringtoneLayout.setLayoutParams(paramsRingtone);

			ringtoneText = (TextView) findViewById(R.id.chat_contact_properties_ringtone);

			dividerRingtoneLayout = (View) findViewById(R.id.divider_ringtone_layout);
			LinearLayout.LayoutParams paramsRingtoneDivider = (LinearLayout.LayoutParams) dividerRingtoneLayout.getLayoutParams();
			paramsRingtoneDivider.leftMargin = Util.scaleWidthPx(72, outMetrics);
			dividerRingtoneLayout.setLayoutParams(paramsRingtoneDivider);

			//Share Contact Layout

			shareContactLayout = (RelativeLayout) findViewById(R.id.chat_contact_properties_share_contact_layout);
			shareContactLayout.setOnClickListener(this);
			LinearLayout.LayoutParams paramsShareContact = (LinearLayout.LayoutParams) shareContactLayout.getLayoutParams();
			paramsShareContact.leftMargin = Util.scaleWidthPx(72, outMetrics);
			shareContactLayout.setLayoutParams(paramsShareContact);

			shareContactText = (TextView) findViewById(R.id.chat_contact_properties_share_contact);
			shareContactText.setText(userEmail);

			dividerShareContactLayout = (View) findViewById(R.id.divider_share_contact_layout);
			LinearLayout.LayoutParams paramsShareContactDivider = (LinearLayout.LayoutParams) dividerShareContactLayout.getLayoutParams();
			paramsShareContactDivider.leftMargin = Util.scaleWidthPx(72, outMetrics);
			dividerShareContactLayout.setLayoutParams(paramsShareContactDivider);

			//Clear chat Layout
			clearChatLayout = (RelativeLayout) findViewById(R.id.chat_contact_properties_clear_layout);
			clearChatLayout.setOnClickListener(this);
			LinearLayout.LayoutParams paramsClearChat = (LinearLayout.LayoutParams) clearChatLayout.getLayoutParams();
			paramsClearChat.leftMargin = Util.scaleWidthPx(72, outMetrics);
			clearChatLayout.setLayoutParams(paramsClearChat);

		}
		else{
			log("Extras is NULL");
		}
	}

	public void setAvatar(){
		log("setAvatar");
		File avatar = null;
		if (getExternalCacheDir() != null){
			avatar = new File(getExternalCacheDir().getAbsolutePath(), userEmail + ".jpg");
		}
		else{
			avatar = new File(getCacheDir().getAbsolutePath(), userEmail + ".jpg");
		}

		if(avatar!=null){
			setProfileAvatar(avatar);
		}
	}

	public void setProfileAvatar(File avatar){
		log("setProfileAvatar");
		Bitmap imBitmap = null;
		if (avatar.exists()){
			if (avatar.length() > 0){
				BitmapFactory.Options bOpts = new BitmapFactory.Options();
				bOpts.inPurgeable = true;
				bOpts.inInputShareable = true;
				imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
				if (imBitmap == null) {
					avatar.delete();
					if (getExternalCacheDir() != null){
						megaApi.getUserAvatar(user, getExternalCacheDir().getAbsolutePath() + "/" + userEmail, this);
					}
					else{
						megaApi.getUserAvatar(user, getCacheDir().getAbsolutePath() + "/" + userEmail, this);
					}
				}
				else{
					contactPropertiesImage.setImageBitmap(imBitmap);
					initialLetter.setVisibility(View.GONE);
				}
			}
		}
	}

	public void setDefaultAvatar(){
		log("setDefaultAvatar");

		Bitmap defaultAvatar = Bitmap.createBitmap(outMetrics.widthPixels,outMetrics.widthPixels, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(defaultAvatar);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(Color.TRANSPARENT);
		c.drawPaint(p);

		String color = megaApi.getUserAvatarColor(user);
		if(color!=null){
			log("The color to set the avatar is "+color);
			imageLayout.setBackgroundColor(Color.parseColor(color));
		}
		else{
			log("Default color to the avatar");
			imageLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.lollipop_primary_color));
		}

		contactPropertiesImage.setImageBitmap(defaultAvatar);

		int avatarTextSize = getAvatarTextSize(density);
		log("DENSITY: " + density + ":::: " + avatarTextSize);

		boolean setInitialByMail = false;

		if (fullName != null){
			if (fullName.trim().length() > 0){
				String firstLetter = fullName.charAt(0) + "";
				firstLetter = firstLetter.toUpperCase(Locale.getDefault());
				initialLetter.setText(firstLetter);
				initialLetter.setTextSize(100);
				initialLetter.setTextColor(Color.WHITE);
				initialLetter.setVisibility(View.VISIBLE);
			}else{
				setInitialByMail=true;
			}
		}
		else{
			setInitialByMail=true;
		}
		if(setInitialByMail){
			if (userEmail != null){
				if (userEmail.length() > 0){
					log("email TEXT: " + userEmail);
					log("email TEXT AT 0: " + userEmail.charAt(0));
					String firstLetter = userEmail.charAt(0) + "";
					firstLetter = firstLetter.toUpperCase(Locale.getDefault());
					initialLetter.setText(firstLetter);
					initialLetter.setTextSize(100);
					initialLetter.setTextColor(Color.WHITE);
					initialLetter.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private int getAvatarTextSize (float density){
		float textSize = 0.0f;

		if (density > 3.0){
			textSize = density * (DisplayMetrics.DENSITY_XXXHIGH / 72.0f);
		}
		else if (density > 2.0){
			textSize = density * (DisplayMetrics.DENSITY_XXHIGH / 72.0f);
		}
		else if (density > 1.5){
			textSize = density * (DisplayMetrics.DENSITY_XHIGH / 72.0f);
		}
		else if (density > 1.0){
			textSize = density * (72.0f / DisplayMetrics.DENSITY_HIGH / 72.0f);
		}
		else if (density > 0.75){
			textSize = density * (72.0f / DisplayMetrics.DENSITY_MEDIUM / 72.0f);
		}
		else{
			textSize = density * (72.0f / DisplayMetrics.DENSITY_LOW / 72.0f);
		}

		return (int)textSize;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.chat_contact_properties_clear_layout:{
				log("Clear chat option");
				break;
			}
			case R.id.chat_contact_properties_share_contact_layout:{
				log("Share contact option");
				break;
			}
//			case R.id.chat_contact_properties_toolbar_back:{
//				finish();
//				break;
//			}
//			case R.id.chat_contact_properties_toolbar_overflow:{
//				overflowMenuLayout.setVisibility(View.VISIBLE);
//				break;
//			}
//			case R.id.chat_contact_properties_toolbar_download:{
//
//				break;
//			}
//			case R.id.chat_contact_properties_toolbar_rubbish_bin:{
//
//				break;
//			}
//			case R.id.file_properties_content_table:{
//				Intent i = new Intent(this, FileContactListActivityLollipop.class);
//				i.putExtra("name", node.getHandle());
//				startActivity(i);
//				finish();
//				break;
//			}
//
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelectedLollipop");
		int id = item.getItemId();
		switch(id) {
			case android.R.id.home: {
				finish();
			}
		}
		return true;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		log("onCheckedChanged");

		if (!isChecked){
			log("isChecked");

			supportInvalidateOptionsMenu();
		}
		else{
			log("NOT Checked");


			supportInvalidateOptionsMenu();
		}
	}

	/*
	 * Display keyboard
	 */
	private void showKeyboardDelayed(final View view) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 50);
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		log("onRequestStart: " + request.getName());
	}

	@SuppressLint("NewApi")
	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,MegaError e) {

		log("onRequestFinish: "+request.getType() + "__" + request.getRequestString());

		if (request.getType() == MegaRequest.TYPE_GET_ATTR_USER){

			log("MegaRequest.TYPE_GET_ATTR_USER");
			if (e.getErrorCode() == MegaError.API_OK){
				File avatar = null;
				if (getExternalCacheDir() != null){
					avatar = new File(getExternalCacheDir().getAbsolutePath(), request.getEmail() + ".jpg");
				}
				else{
					avatar = new File(getCacheDir().getAbsolutePath(), request.getEmail() + ".jpg");
				}
				Bitmap imBitmap = null;
				if (avatar.exists()){
					if (avatar.length() > 0){
						BitmapFactory.Options bOpts = new BitmapFactory.Options();
						bOpts.inPurgeable = true;
						bOpts.inInputShareable = true;
						imBitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
						if (imBitmap == null) {
							avatar.delete();
						}
						else{
							contactPropertiesImage.setImageBitmap(imBitmap);
							initialLetter.setVisibility(View.GONE);
						}
					}
				}
			}
		}
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		log("onRequestTemporaryError: " + request.getName());
	}

	@Override
	protected void onDestroy(){
    	super.onDestroy();
    }

	public static void log(String message) {
		Util.log("ContactChatInfoActivityLollipop", message);
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub

	}


	@Override
	protected void onResume() {
		log("onResume-ContactChatInfoActivityLollipop");
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}
	
	@Override
	public void onBackPressed() {
//		if (overflowMenuLayout != null){
//			if (overflowMenuLayout.getVisibility() == View.VISIBLE){
//				overflowMenuLayout.setVisibility(View.GONE);
//				return;
//			}
//		}
		super.onBackPressed();
	}
}