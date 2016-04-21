package com.huhx0015.xyzreader.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.huhx0015.xyzreader.R;
import com.huhx0015.xyzreader.activities.ArticleDetailActivity;
import com.huhx0015.xyzreader.activities.ArticleListActivity;
import com.huhx0015.xyzreader.data.ArticleLoader;
import com.huhx0015.xyzreader.ui.ImageLoaderHelper;
import butterknife.Bind;
import butterknife.ButterKnife;

/** -----------------------------------------------------------------------------------------------
 * [ArticleDetailFragment] CLASS
 * DESCRIPTION: A fragment representing a single Article detail screen. This fragment is either
 * contained in a {@link ArticleListActivity} in two-pane mode (on tablets) or a
 * {@link ArticleDetailActivity} on handsets.
 *  -----------------------------------------------------------------------------------------------
 */
public class ArticleDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // ACTIVITY VARIABLES
    private ArticleDetailActivity mArticleDetailActivity;

    // ARTICLE VARIABLES
    private String mArticleName = "XYZ Reader";

    // CURSOR VARIABLES
    private Cursor mCursor;
    private long mItemId;

    // LOGGING VARIABLES
    private static final String LOG_TAG = ArticleDetailFragment.class.getSimpleName();

    // META BAR VARIABLES
    private ColorDrawable mStatusBarColorDrawable;
    private int mMutedColor = 0xFF333333;
    public static final String ARG_ITEM_ID = "item_id";

    // VIEW VARIABLES
    private int mScrollY;
    private int mStatusBarFullOpacityBottom;
    private int mTopInset;
    private View mRootView;

    // VIEW INJECTION VARIABLES
    @Bind(R.id.fragment_article_detail_body) AppCompatTextView mBodyView;
    @Bind(R.id.fragment_article_detail_byline) AppCompatTextView mBylineView;
    @Bind(R.id.fragment_article_detail_title) AppCompatTextView mTitleView;
    @Bind(R.id.fragment_article_detail_collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.fragment_article_detail_share_fab) FloatingActionButton mFloatingActionButton;
    @Bind(R.id.fragment_article_detail_photo) ImageView mPhotoView;
    @Bind(R.id.fragment_article_detail_meta_bar) LinearLayout mMetaBar;
    @Bind(R.id.fragment_article_detail_toolbar) Toolbar mToolbar;

    /** CONSTRUCTOR METHODS ____________________________________________________________________ **/

    // ArticleDetailFragment(): Mandatory empty constructor for the fragment manager to instantiate
    // the fragment (e.g. upon screen orientation changes).
    public ArticleDetailFragment() {}

    // newInstance(): Creates an instance of this Fragment.
    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    /** FRAGMENT LIFECYCLE METHODS _____________________________________________________________ **/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets a reference to the attached activity.
        mArticleDetailActivity = (ArticleDetailActivity) getActivity();

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        boolean mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

        mStatusBarColorDrawable = new ColorDrawable(0);

        bindViews();
        updateStatusBar();
        initButtons(); // Initializes the buttons in this fragment.
        initToolbar(); // Initializes the Toolbar and CollapsingToolbarLayout.

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    // onDestroyView(): This function runs when the screen is no longer visible and the view is
    // destroyed.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this); // Sets all injected views to null.
    }

    /** ACTIVITY OVERRIDE METHODS ______________________________________________________________ **/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mArticleDetailActivity.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /** LOADER METHODS _________________________________________________________________________ **/

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
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

    /** ACTIVITY METHODS _______________________________________________________________________ **/

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    /** LAYOUT METHODS _________________________________________________________________________ **/

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

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

            // Sets the article name in the Toolbar and CollapsingToolbarLayout.
            mToolbar.setTitle(mArticleName);
            mCollapsingToolbarLayout.setTitle(mArticleName);

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null && !isDetached()) {

                                // Sets the background color of the meta bar, based on the Palette
                                // of the bitmap.
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mMetaBar.setBackgroundColor(mMutedColor);

                                // Sets the new bitmap into the ImageView.
                                if (imageContainer.getBitmap() != null) {
                                    mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                }

                                // Sets the CollapsingToolbarLayout attributes.
                                mCollapsingToolbarLayout.setContentScrimColor(mMutedColor);
                                mCollapsingToolbarLayout.setStatusBarScrimColor(mMutedColor);

                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.e(LOG_TAG, "ERROR: Image failed to load: " + volleyError.getMessage());
                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
        }
    }

    // initButtons(): Initializes the attributes for the buttons in this Fragment.
    private void initButtons() {
        mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.theme_accent)));
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setChooserTitle(getResources().getString(R.string.share_chooser_title))
                        .setText(mArticleName)
                        .getIntent(), getString(R.string.action_share)));
            }
        });
    }

    // initToolbar(): Sets up the Toolbar for the fragment.
    private void initToolbar() {

        getActivityCast().setSupportActionBar(mToolbar);
        getActivityCast().getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enables the back button on the tagActionBar.
        getActivityCast().getSupportActionBar().setHomeButtonEnabled(true);
        getActivityCast().getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Sets the attributes for the collapsing toolbar layout.
        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
    }
}