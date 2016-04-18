package com.huhx0015.xyzreader.activities;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.huhx0015.xyzreader.R;
import com.huhx0015.xyzreader.data.ArticleLoader;
import com.huhx0015.xyzreader.ui.ImageLoaderHelper;

import butterknife.Bind;
import butterknife.ButterKnife;

/** -----------------------------------------------------------------------------------------------
 *  [ArticleDetailActivity] CLASS
 *  DESCRIPTION: ArticleDetailActivity is an activity representing a single Article detail screen,
 *  letting you swipe between articles.
 *  -----------------------------------------------------------------------------------------------
 */
public class ArticleDetailActivityNew extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // ARTICLE VARIABLES
    private String mArticleName;
    public static final String ARG_ITEM_ID = "item_id";

    // CURSOR VARIABLES
    private Cursor mCursor;
    private long mItemId;

    // METABAR VARIABLES
    private int mMutedColor = 0xFF333333;

    // LOGGING VARIABLES
    private static final String LOG_TAG = ArticleDetailActivityNew.class.getSimpleName();

    // VIEW INJECTION VARIABLES
    @Bind(R.id.activity_article_detail_body) AppCompatTextView mBodyView;
    @Bind(R.id.activity_article_detail_byline) AppCompatTextView mBylineView;
    @Bind(R.id.activity_article_detail_title) AppCompatTextView mTitleView;
    @Bind(R.id.activity_article_detail_collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.activity_article_detail_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.activity_article_detail_share_fab) FloatingActionButton mFloatingActionButton;
    @Bind(R.id.activity_article_detail_meta_bar) LinearLayout mMetabar;
    @Bind(R.id.activity_article_detail_photo) ImageView mPhotoView;
    @Bind(R.id.activity_article_detail_scrollview) NestedScrollView mScrollView;
    @Bind(R.id.activity_article_detail_toolbar) Toolbar mToolbar;

    /** ACTIVITY LIFECYCLE METHODS _____________________________________________________________ **/

    // onCreate(): The initial function that is called when the activity is run. onCreate() only runs
    // when the activity is first started.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail_new); // Sets the XML layout for this activity.
        ButterKnife.bind(this); // ButterKnife view injection initialization.

        // FIX: Fixes the status bar for Lollipop devices.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

//        if (savedInstanceState.containsKey(ARG_ITEM_ID)) {
//            mItemId = savedInstanceState.getLong(ARG_ITEM_ID);
//        }

        initMetabar(); // Sets the article text and subtitle.
        initToolbar(); // Sets the attributes for the Toolbar and CollapsingToolbarLayout.
    }

    // onResume(): This function is run when the fragment is resumed from an onPause state.
    @Override
    public void onResume() {
        super.onResume();
    }

    // onPause(): This function runs when the fragment enters into an suspended state.
    @Override
    public void onPause() {
        super.onPause();
    }

    /** LOADER METHODS _________________________________________________________________________ **/

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(this, mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(LOG_TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    /** LAYOUT METHODS _________________________________________________________________________ **/

    // initMetabar(): Sets up the TextViews inside the meta bar.
    private void initMetabar() {
        mTitleView.setText("TEST ARTICLE");
        mBylineView.setText("By: TEST");
    }

    // initToolbar(): Sets up the Toolbar for the object.
    private void initToolbar() {

        // Sets the attributes for the toolbar.
        setSupportActionBar(mToolbar); // Sets the tagToolbar as the primary action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enables the back button on the tagActionBar.
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Sets the attributes for the collapsing toolbar layout.
        mCollapsingToolbarLayout.setTitle("TEST ARTICLE");
        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        mCollapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.meta_bar_color));
        mCollapsingToolbarLayout.setStatusBarScrimColor(getResources().getColor(R.color.meta_bar_color));
    }

    private void bindViews() {

        mBylineView.setMovementMethod(new LinkMovementMethod());
        mBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {

            // Sets the article name, subtitle, and body text.
            mArticleName = mCursor.getString(ArticleLoader.Query.TITLE);
            mTitleView.setText(mArticleName);
            mBylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            mBodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            ImageLoaderHelper.getInstance(this).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {

                                mPhotoView.setImageBitmap(imageContainer.getBitmap());

                                // Sets the color of the metabar and the collapsing toolbar layout.
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mMetabar.setBackgroundColor(mMutedColor);
                                mCollapsingToolbarLayout.setContentScrimColor(mMutedColor);
                                mCollapsingToolbarLayout.setStatusBarScrimColor(mMutedColor);
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mTitleView.setText("N/A");
            mBylineView.setText("N/A" );
            mBodyView.setText("N/A");
        }
    }
}
