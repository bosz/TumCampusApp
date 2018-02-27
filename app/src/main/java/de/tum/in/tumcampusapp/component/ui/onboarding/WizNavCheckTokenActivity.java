package de.tum.in.tumcampusapp.component.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Optional;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineConst;
import de.tum.in.tumcampusapp.api.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.component.other.generic.activity.ActivityForLoadingInBackground;
import de.tum.in.tumcampusapp.component.tumui.person.model.Identity;
import de.tum.in.tumcampusapp.component.tumui.person.model.IdentitySet;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.NetUtils;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 *
 */
public class WizNavCheckTokenActivity extends ActivityForLoadingInBackground<Void, Integer> {

    public WizNavCheckTokenActivity() {
        super(R.layout.activity_wiznav_checktoken);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableRefresh();
    }

    /**
     * If back key is pressed start previous activity
     */
    @Override
    public void onBackPressed() {
        finish();
        startActivity(new Intent(this, WizNavStartActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Open next activity on skip
     *
     * @param skip Skip button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickSkip(View skip) {
        finish();
        startActivity(new Intent(this, WizNavExtrasActivity.class));
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * If next is pressed, check if token has been activated
     *
     * @param next Next button handle
     */
    @SuppressWarnings("UnusedParameters")
    public void onClickNext(View next) {
        if (!NetUtils.isConnected(this)) {
            showNoInternetLayout();
            return;
        }
        startLoading();
    }

    /**
     * Check in background if token has been enabled and get identity for enabling chat
     */
    @Override
    protected Integer onLoadInBackground(Void... arg) {
        // Check if token has been enabled
        if (TUMOnlineRequest.checkTokenInactive(this)) {
            if (NetUtils.isConnected(this)) {
                return R.string.token_not_enabled;
            } else {
                return R.string.no_internet_connection;
            }
        } else {
            // Get users full name
            TUMOnlineRequest<IdentitySet> request = new TUMOnlineRequest<>(TUMOnlineConst.Companion.getIDENTITY(), this, true);
            Optional<IdentitySet> id = request.fetch();
            if (!id.isPresent()) {
                return R.string.no_rights_to_access_id;
            }

            Identity identity = id.get()
                                  .getIds()
                                  .get(0);

            // Save the name to preferences
            Utils.INSTANCE.setSetting(this, Const.CHAT_ROOM_DISPLAY_NAME, identity
                    .toString());

            // Save the TUMOnline id to preferences
            Utils.INSTANCE.setSetting(this, Const.TUMO_PIDENT_NR, identity.getObfuscated_ids()
                                                                          .getStudierende()); // Switch to identity.getObfuscated_id() in the future
            Utils.INSTANCE.setSetting(this, Const.TUMO_STUDENT_ID, identity.getObfuscated_ids()
                                                                           .getStudierende());
            Utils.INSTANCE.setSetting(this, Const.TUMO_EXTERNAL_ID, identity.getObfuscated_ids()
                                                                            .getExtern());
            Utils.INSTANCE.setSetting(this, Const.TUMO_EMPLOYEE_ID, identity.getObfuscated_ids()
                                                                            .getBedienstete());

            return null;
        }
    }

    /**
     * If everything worked, start the next activity page
     * otherwise give the user the possibility to retry
     */
    @Override
    protected void onLoadFinished(Integer errorMessageStrResId) {
        if (errorMessageStrResId == null) {
            finish();
            startActivity(new Intent(this, WizNavExtrasActivity.class));
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        } else {
            Utils.INSTANCE.showToast(this, errorMessageStrResId);
            showLoadingEnded();
        }
    }

    /**
     * Adds clickable link to activity
     */
    @Override
    protected void onStart() {
        super.onStart();
        TextView textView = findViewById(R.id.tvBrowse);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
