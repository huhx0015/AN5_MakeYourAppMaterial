package com.huhx0015.xyzreader.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    // ARTICLE VARIABLES
    private String mArticleName;

    // LOGGING VARIABLES
    private static final String LOG_TAG = ArticleDetailFragment.class.getSimpleName();

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    // VIEW INJECTION VARIABLES
    @Bind(R.id.fragment_article_detail_collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.fragment_article_detail_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.fragment_article_detail_share_fab) FloatingActionButton mFloatingActionButton;
    @Bind(R.id.fragment_article_detail_photo) ImageView mPhotoView;
    @Bind(R.id.fragment_article_detail_scrollview) NestedScrollView mScrollView;
//    @Bind(R.id.fragment_article_detail_photo_container) View mPhotoContainerView;
    @Bind(R.id.fragment_article_detail_body) AppCompatTextView mBodyView;
    @Bind(R.id.fragment_article_detail_byline) AppCompatTextView mBylineView;
    @Bind(R.id.fragment_article_detail_title) AppCompatTextView mTitleView;
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

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        ButterKnife.bind(this, mRootView);

//        mCoordinatorLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
//            @Override
//            public void onInsetsChanged(Rect insets) {
//                mTopInset = insets.top;
//            }
//        });


//        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
//            @Override
//            public void onScrollChanged() {
//                mScrollY = mScrollView.getScrollY();
//                getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
//                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
//                updateStatusBar();
//            }
//        });


        mStatusBarColorDrawable = new ColorDrawable(0);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        updateStatusBar();

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

    // initToolbar(): Sets up the Toolbar for the fragment.
    private void initToolbar() {

        mToolbar.setTitle(mArticleName);
        mCollapsingToolbarLayout.setTitle(mArticleName);

        getActivityCast().setSupportActionBar(mToolbar);
        getActivityCast().getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enables the back button on the tagActionBar.
        getActivityCast().getSupportActionBar().setHomeButtonEnabled(true);
        getActivityCast().getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        //mCoordinatorLayout.setInsetBackground(mStatusBarColorDrawable);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
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

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        mBylineView.setMovementMethod(new LinkMovementMethod());
        mBodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

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

            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                mRootView.findViewById(R.id.fragment_article_detail_meta_bar)
                                        .setBackgroundColor(mMutedColor);
                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
            mTitleView.setText("N/A");
            mBylineView.setText("N/A" );
            mBodyView.setText("N/A");
        }
    }

//    public int getUpButtonFloor() {
//        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
//            return Integer.MAX_VALUE;
//        }
//
//        // account for parallax
//        return mIsCard
//                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
//                : mPhotoView.getHeight() - mScrollY;
//    }
}
