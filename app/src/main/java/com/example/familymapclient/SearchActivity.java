package com.example.familymapclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import java.util.ArrayList;
import java.util.Locale;

import model.Event;
import model.Person;

public class SearchActivity extends AppCompatActivity {

    private static final int PERSON_ITEM_VIEW_TYPE = 0;
    private static final int EVENT_ITEM_VIEW_TYPE = 1;
    private DataCache dataCache = DataCache.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));

        ArrayList<Person> filteredPeople = new ArrayList<>();
        ArrayList<Event>  filteredEvents = new ArrayList<>();

        SearchView searchView = findViewById(R.id.search_bar);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String search) {
                search = search.toLowerCase();

                dataCache.refineSearch(filteredPeople, filteredEvents, search);

                SearchAdapter adapter = new SearchAdapter(filteredPeople, filteredEvents);
                recyclerView.setAdapter(adapter);
                return false;
            }
        });

    }

    private void refineSearch (ArrayList<Person> filteredPeople, ArrayList<Event> filteredEvents, String search){
        filteredEvents.clear();
        filteredPeople.clear();
        if(!search.equals("")){
            for(Person person : dataCache.getPeople()){
                if((person.getFirstName().toLowerCase() + " " + person.getLastName().toLowerCase()).contains(search)){
                    filteredPeople.add(person);
                }
            }
            for(Event event : dataCache.getFilteredEvents(dataCache.getUserPersonID())) {
                if(dataCache.getEventDetails(event).toLowerCase().contains(search)){
                    filteredEvents.add(event);
                }
            }
        }
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {

        private final ArrayList<Person> people;
        private final ArrayList<Event> events;

        SearchAdapter(ArrayList<Person> people, ArrayList<Event> events){
            this.people = people;
            this.events = events;
        }

        @Override
        public int getItemViewType(int position) {
            return position < people.size() ? PERSON_ITEM_VIEW_TYPE : EVENT_ITEM_VIEW_TYPE;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            view = getLayoutInflater().inflate(R.layout.list_item, parent, false);

            return new SearchViewHolder(view, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            if(position < people.size()){
                holder.bind(people.get(position));
            } else {
                holder.bind(events.get(position - people.size()));
            }
        }

        @Override
        public int getItemCount() {
            return people.size() + events.size();
        }
    }

    private class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView topText;
        private final TextView bottomText;
        private final ImageView icon;

        private final int viewType;
        private Person person;
        private Event event;

        SearchViewHolder(View view, int viewType){
            super(view);
            this.viewType = viewType;

            itemView.setOnClickListener(this);

            topText = itemView.findViewById(R.id.top_text);
            bottomText = itemView.findViewById(R.id.bottom_text);
            icon = itemView.findViewById(R.id.item_icon);

        }

        private void bind(Person person){
            Drawable maleIcon = new IconDrawable(icon.getContext(), FontAwesomeIcons.fa_male)
                    .colorRes(R.color.male_color).sizeDp(20);
            Drawable femaleIcon = new IconDrawable(icon.getContext(), FontAwesomeIcons.fa_female)
                    .colorRes(R.color.female_color).sizeDp(20);
            this.person = person;
            topText.setText(person.getFirstName() + " " + person.getLastName());
            bottomText.setText("");

            if(person.getGender().equalsIgnoreCase("f")){
                icon.setImageDrawable(femaleIcon);
            } else{
                icon.setImageDrawable(maleIcon);
            }
        }

        private void bind(Event event){
            Drawable eventIcon = new IconDrawable(icon.getContext(), FontAwesomeIcons.fa_map_marker)
                    .colorRes(R.color.red).sizeDp(20);

            this.event = event;
            topText.setText(dataCache.getEventDetails(event));
            bottomText.setText(dataCache.getPersonOfEvent(event));
            icon.setImageDrawable(eventIcon);
        }


        @Override
        public void onClick(View view) {
            Intent intent;
            if(viewType == PERSON_ITEM_VIEW_TYPE){
                intent = new Intent(SearchActivity.this, PersonActivity.class);
                intent.putExtra(PersonActivity.PERSON_KEY, person.getPersonID());
            } else {
                dataCache.setCurrentMapEvent(event);
                intent = new Intent(SearchActivity.this, EventActivity.class);
                intent.putExtra(EventActivity.EVENT_KEY, event.getEventID());
            }
            startActivity(intent);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.up_only_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }
}