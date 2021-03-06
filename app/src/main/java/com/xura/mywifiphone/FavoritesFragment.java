package com.xura.mywifiphone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xura.mywifiphone.Utils.JsonDic;

import java.util.List;

public class FavoritesFragment extends Fragment {

    private Contacts contacts;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (contacts == null) {
            String contactDic = "";
            if (savedInstanceState != null) {
                contactDic = savedInstanceState.getString("favorites_contacts_instance");
            }
            if (contactDic != "") {
                contacts = new Contacts(getActivity(), contactDic);
            } else {
                contacts = new Contacts(getActivity());
            }
        }
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.contact_list_recycler, container, false);

        setupRecyclerView(rv);
        return rv;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (contacts != null) {
            String contactDic = contacts.contactDic.toString();
            outState.putString("favorites_contacts_instance", contactDic);
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        int nbRows = 2;
        int fav_size = getActivity().getResources().getDimensionPixelSize(R.dimen.fav_item_height);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        //metrics.heightPixels;
        //metrics.widthPixels;

        if (metrics.widthPixels > 3 * fav_size) {
            nbRows = 3;
        }
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(),nbRows));
        //recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), contacts));

    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private List<String> mValues;
        private Contacts contacts;
        private Activity mActivity;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public String mBoundString;

            public final View mView;
            public final ImageView mImageView;
            public final TextView mTextView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.contact_avatar);
                mTextView = (TextView) view.findViewById(android.R.id.text1);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mTextView.getText();
            }
        }

        public String getValueAt(int position) {
            return mValues.get(position);
        }

        public SimpleStringRecyclerViewAdapter(Context context, Contacts contacsRef) {
            mActivity = (Activity) context;
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            contacts = contacsRef;
            mValues = contacts.readMostContactedNames();

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.favorite_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            String contactName = mValues.get(position);
            holder.mBoundString = contactName;
            holder.mTextView.setText(contactName);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    ContactInfo mContactInfo = new ContactInfo();
                    mContactInfo.setName(holder.mBoundString);

                    JsonDic aContact = contacts.contactDic.getDic(holder.mBoundString);
                    if (aContact.containsKey("fav_color")) {
                        mContactInfo.setColor(aContact.getInt("fav_color"));
                    } else {
                        mContactInfo.setColor(0);
                    }

                    /*
                    Intent intent = new Intent(context, ContactDetailActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putParcelable(ContactDetailActivity.CONTACT_KEY, mContactInfo);
                    intent.putExtras(mBundle);
                    //intent.putExtra(ContactDetailActivity.EXTRA_NAME, holder.mBoundString);
                    //String backdrop = String.valueOf(contacts.getDrawableBackground(holder.mBoundString));
                    //intent.putExtra(ContactDetailActivity.BACKGROUND, backdrop);
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(holder.mView, 0 , 0 ,holder.mView.getWidth(), holder.mView.getHeight());
                    ActivityCompat.startActivity(mActivity, intent, options.toBundle());
                    //context.startActivity(intent);
                    */
                    Intent intent = new Intent(context, ContactDetailActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putParcelable(ContactDetailActivity.CONTACT_KEY, mContactInfo);
                    intent.putExtras(mBundle);

                    String transitionName = mActivity.getResources().getString(R.string.contact_transition);
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(
                                    mActivity,
                                    holder.mView,   // The view which starts the transition
                                    transitionName    // The transitionName of the view we’re transitioning to
                            );
                    ActivityCompat.startActivity(mActivity, intent, options.toBundle());



                }
            });
            LinearLayout fav_layout = (LinearLayout)holder.mView.findViewById(R.id.contact_layout);
            contacts.setFavoriteBackground(fav_layout, contactName);

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }
}
