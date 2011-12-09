package dtd.phs.sms.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Helpers {

	public static View inflate(int layout, ViewGroup root, Context context) {
		LayoutInflater i = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return i.inflate(layout, root);
	}

	public static Bundle getContactFromNumber(String number, Context context) {
		ContentResolver cr = context.getContentResolver();
		String[] projection = new String[] {
				Contacts._ID,
				Contacts.DISPLAY_NAME
		};
		Bundle bundle = new Bundle();
		Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor cursor = cr.query(contactUri, projection, null, null, null);
		try {
			if ( cursor.moveToFirst()) {
				for(String key : projection) {
					bundle.putString(key, cursor.getString(cursor.getColumnIndex(key)));
				}
			}
			return bundle;
		} catch (Exception e) {
			return null;
		} finally {
			cursor.close();
		}

	}

	public static String hashMD5(String s) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(s.getBytes(), 0, s.length());
			String result = new BigInteger(1,m.digest()).toString();
			while (result.length() < 32 ) {
				result = "0" + result;
			}
			return result;
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static String generatePassword(String username) {
		return username + "@";
	}

	/**
	 * 
	 * @param rawNumber
	 * @return username - null if the number is invalid
	 */
	public static String generateUsername(String rawNumber) {
		int indexDoubleZero = rawNumber.indexOf("00");
		String username = "";
		if ( indexDoubleZero == 0 ) {
			username = "p"+rawNumber.substring(2);
		} else if (rawNumber.charAt(0)=='+') {
			username = "p" + rawNumber.substring(1);
		} else if (rawNumber.charAt(0) == '0') {
			username = "p"+ getCountryCode() + rawNumber.substring(1);  
		} else {
			return null;
		}
		return username;
	}

	private static String getCountryCode() {
		// TODO method stub
		return "84"; //VN code		
	}

}
