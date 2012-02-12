package dtd.phs.sil.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import dtd.phs.sil.EditMessage;
import dtd.phs.sil.R;
import dtd.phs.sil.SendSMSService;
import dtd.phs.sil.SentMessageView;
import dtd.phs.sil.data.DataCenter;
import dtd.phs.sil.entities.SentMessageItem;
import dtd.phs.sil.utils.Helpers;
import dtd.phs.sil.utils.I_SMSListener;
import dtd.phs.sil.utils.Logger;

public class SentMessageDialog extends Dialog{


	private TextView tvTitle;

	private Resources resources;

	//	private TextView tvRemove;

	private TextView tvResend;

	private TextView tvEdit;

	private SentMessageItem message;

	private SentMessageView sentFrame;

	protected Activity hostedActivity;

	protected boolean errorOcc;

	public SentMessageDialog(Activity hostedActivity, SentMessageView sentMessageView) {
		super(hostedActivity);
		this.hostedActivity = hostedActivity;
		init(sentMessageView);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.options_on_click_dialog);
		setCancelable(true);
		initViews();
	}

	private void initViews() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		//		tvRemove = (TextView) findViewById(R.id.textview01);
		//		tvRemove.setText(resources.getString(R.string.Remove));
		//		tvRemove.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View v) {
		//				if ( message != null ) {
		//					DataCenter.removeSentItem(getContext(), message.getId());
		//					sentFrame.onRefresh();
		//					cancel();
		//				}
		//			}
		//		});


		tvResend = (TextView) findViewById(R.id.textview02);
		tvResend.setText(resources.getString(R.string.Resend_now));
		tvResend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toast(R.string.Sending_please_wait);
				if (message != null) {
					errorOcc = false;
					for(String number : message.getPhoneNumbers())
						Helpers.sendMessage(
								getContext(), 
								number, 
								message.getContent(),
								new I_SMSListener() {

									@Override
									public void onSentSuccess() {
										//Nothing
									}

									@Override
									public void onSentFailed(int errorCode) {
										errorOcc = true;
									}

									@Override
									public void onMessageDeliveryFailed() {
										errorOcc = true;
										//TODO: test 2 case : cannot sent And cannot delivered (turn off each phone)
									}

									@Override
									public void onMessageDelivered() {
									}
								});
				}
				cancel();
				Helpers.startAfter(SendSMSService.WAITING_DELIVERY_REPORT_TIME, new RunAfterSendingFinish());
			}
		});

		tvEdit = (TextView) findViewById(R.id.textview03);
		tvEdit.setText(resources.getString(R.string.Edit));
		tvEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( message != null) {
					EditMessage.passedSentMessage = message;
					Intent i = new Intent(getContext(),EditMessage.class);
					hostedActivity.startActivity(i);
					cancel();
				}

			}
		});
	}

	private void init(SentMessageView sentFrame) {
		this.resources = getContext().getResources();
		this.sentFrame = sentFrame;
	}

	public void setTitle(String title) {
		tvTitle.setText(title);
	}

	public void prepare() {
		tvTitle.setText(message.getContact());
	}

	public void setMessage(SentMessageItem message) {
		this.message = message;
	}

	public class RunAfterSendingFinish implements Runnable {

		@Override
		public void run() {
			if ( errorOcc ) {
				Logger.logInfo("Save failed message is progressing ... ");
				DataCenter.saveSentMessage(getContext(), message, false);
			} else {
				Logger.logInfo("Save successful message is progressing ... ");
				DataCenter.saveSentMessage(getContext(), message, true);
			}
			Logger.logInfo("Weird !");
			Helpers.broadcastDatabaseChanged(getContext());
			
		}

	}
	
	private void toast(final int res) {
		tvTitle.post(new  Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getContext(), res, Toast.LENGTH_LONG).show();
			}
		});
		
		
	}
}