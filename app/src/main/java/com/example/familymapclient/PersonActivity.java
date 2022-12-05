package com.example.familymapclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {

    public static final String PERSON_KEY = "ReceivedPersonKey";
    private Person person;
    private DataCache dataCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        dataCache = DataCache.getInstance();
        Intent intent = getIntent();
        person = dataCache.getPerson(intent.getStringExtra(PERSON_KEY));

        TextView firstName = findViewById(R.id.firstName);
        TextView lastName = findViewById(R.id.lastName);
        TextView gender = findViewById(R.id.gender);
        if(person.getGender().equals("f")){
            gender.setText(R.string.female_option);
        } else {
            gender.setText(R.string.male_option);
        }
        firstName.setText(person.getFirstName());
        lastName.setText(person.getLastName());
        LinkedList<Person> relatives = new LinkedList<>(dataCache.getPersonChildren(person.getPersonID()));
        Person spouse = dataCache.getPersonSpouse(person.getPersonID());
        Person father = dataCache.getPersonFather(person.getPersonID());
        Person mother = dataCache.getPersonMother(person.getPersonID());

        if (spouse != null) {
            relatives.add(spouse);
        }
        if(mother != null) {
            relatives.add(mother);
        }
        if(father != null) {
            relatives.add(father);
        }

        ExpandableListView expandableListView = findViewById(R.id.expandable_list_view);
        expandableListView.setAdapter(new ExpandableListAdapter(
                dataCache.getSortedUserLifeEvents(person.getPersonID()),
                relatives
        ));

    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter{

        private static final int PERSON_LIFE_EVENTS_POSITION = 0;
        private static final int RELATIVES_POSITION = 1;

        private final List<Event> personLifeEvents;
        private final List<Person> relatives;

        public ExpandableListAdapter(List<Event> personLifeEvents, List<Person> relatives) {
            this.personLifeEvents = personLifeEvents;
            this.relatives = relatives;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition){
                case PERSON_LIFE_EVENTS_POSITION:
                    return personLifeEvents.size();
                case RELATIVES_POSITION:
                    return relatives.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            switch (groupPosition){
                case PERSON_LIFE_EVENTS_POSITION:
                    return getString(R.string.life_events_title);
                case RELATIVES_POSITION:
                    return getString(R.string.relatives_title);
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            switch (groupPosition){
                case PERSON_LIFE_EVENTS_POSITION:
                    return personLifeEvents.get(childPosition);
                case RELATIVES_POSITION:
                    return relatives.get(childPosition);
                default:
                    throw new IllegalArgumentException("Unrecognized group position");
            }
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.list_item_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition){
                case PERSON_LIFE_EVENTS_POSITION:
                    titleView.setText(R.string.life_events_title);
                    break;
                case RELATIVES_POSITION:
                    titleView.setText(R.string.relatives_title);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            switch (groupPosition){
                case PERSON_LIFE_EVENTS_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                    initializeLifeEventView(itemView, childPosition);
                    break;
                case RELATIVES_POSITION:
                    itemView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
                    initializeRelativeView(itemView, childPosition);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
            return itemView;
        }

        private void initializeLifeEventView(View lifeEventView, final int childPosition){

            Drawable eventIcon = new IconDrawable(lifeEventView.getContext(), FontAwesomeIcons.fa_map_marker)
                    .colorRes(R.color.red).sizeDp(20);

            TextView eventDetails = lifeEventView.findViewById(R.id.top_text);
            eventDetails.setText(dataCache.getEventDetails(personLifeEvents.get(childPosition)));

            TextView associatedPerson = lifeEventView.findViewById(R.id.bottom_text);
            associatedPerson.setText(dataCache.getPersonOfEvent(personLifeEvents.get(childPosition)));

            ImageView icon = lifeEventView.findViewById(R.id.item_icon);
            icon.setImageDrawable(eventIcon);

            lifeEventView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Set the event to be viewed in map fragment
                    dataCache.setCurrentMapEvent(personLifeEvents.get(childPosition));
                    Intent intent = new Intent(PersonActivity.this, EventActivity.class);
                    startActivity(intent);
                }
            });
        }

        private void initializeRelativeView(View lifeEventView, final int childPosition){

            Drawable maleIcon = new IconDrawable(lifeEventView.getContext(), FontAwesomeIcons.fa_male)
                    .colorRes(R.color.male_color).sizeDp(20);
            Drawable femaleIcon = new IconDrawable(lifeEventView.getContext(), FontAwesomeIcons.fa_female)
                    .colorRes(R.color.female_color).sizeDp(20);

            Person relative = relatives.get(childPosition);
            String motherID = person.getMotherID();
            String fatherID = person.getFatherID();
            String spouseID = person.getSpouseID();
            TextView relativeName = lifeEventView.findViewById(R.id.top_text);
            relativeName.setText(relative.getFirstName() + " " + relative.getLastName());

            String relation = "Child";
            if (motherID != null){
                if(motherID.equals(relative.getPersonID())) {
                    relation = "Mother";
                }
            }

            if (fatherID != null){
                if (fatherID.equals(relative.getPersonID())) {
                    relation = "Father";
                }
            }

            if (spouseID != null){
                if (spouseID.equals(relative.getPersonID())){
                    relation = "Spouse";
                }
            }

            TextView relationToPerson = lifeEventView.findViewById(R.id.bottom_text);
            relationToPerson.setText(relation);

            ImageView icon = lifeEventView.findViewById(R.id.item_icon);
            if(relative.getGender().equalsIgnoreCase("f")){
                icon.setImageDrawable(femaleIcon);
            } else {
                icon.setImageDrawable(maleIcon);
            }

            lifeEventView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(PersonActivity.this, PersonActivity.class);
                    intent.putExtra(PersonActivity.PERSON_KEY, relative.getPersonID());
                    startActivity(intent);
                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        @Override
        public void notifyDataSetInvalidated() {
            super.notifyDataSetInvalidated();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return super.areAllItemsEnabled();
        }

        @Override
        public void onGroupCollapsed(int groupPosition) {
            super.onGroupCollapsed(groupPosition);
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            super.onGroupExpanded(groupPosition);
        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            return super.getCombinedChildId(groupId, childId);
        }

        @Override
        public long getCombinedGroupId(long groupId) {
            return super.getCombinedGroupId(groupId);
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }

        @Override
        public int getChildType(int groupPosition, int childPosition) {
            return super.getChildType(groupPosition, childPosition);
        }

        @Override
        public int getChildTypeCount() {
            return super.getChildTypeCount();
        }

        @Override
        public int getGroupType(int groupPosition) {
            return super.getGroupType(groupPosition);
        }

        @Override
        public int getGroupTypeCount() {
            return super.getGroupTypeCount();
        }
    }
}