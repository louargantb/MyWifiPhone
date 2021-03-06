/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xura.mywifiphone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xura.mywifiphone.Utils.JsonDic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ContactFragment extends Fragment {

    private Contacts contacts;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (contacts == null) {
            String contactDic = "";
            if (savedInstanceState != null) {
                contactDic = savedInstanceState.getString("all_contacts_instance");
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
            outState.putString("all_contacts_instance", contactDic);
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(), contacts));
    }

    private List<String> getRandomSublist(String[] array, int amount) {
        ArrayList<String> list = new ArrayList<>(amount);
        Random random = new Random();
        while (list.size() < amount) {
            list.add(array[random.nextInt(array.length)]);
        }
        return list;
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
            mValues = contacts.readContactsNames();

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contact_item, parent, false);
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
            contacts.setContactThumbnail(holder.mImageView, contactName);

        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }
}

