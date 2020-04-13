/*
 * Copyright (C) 2018 Timo Engel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.tengel.timescale;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.text.Html;
import java.lang.Runnable;
import android.content.Intent;
import android.net.Uri;


public class TimeScaleMain extends Activity
{
    private float    m_xLastTouch;
    private MenuItem m_clearSearch;

    private class TableUpdaterRunnable implements Runnable
    {
        private Activity m_activity;
        private String   m_nameId;
        public TableUpdaterRunnable(Activity activity, String nameId)
        {
            m_activity = activity;
            m_nameId   = nameId;
        }
        public void run()
        {
            if (m_nameId == null)
            {
                Table.instance(m_activity).update();
            }
            else
            {
                Table.instance(m_activity).scrollTo(m_nameId);
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        LinearLayout rootLayout = (LinearLayout) findViewById(R.id.rootlayout);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction()))
        {
            String nameId = intent.getData().toString();
            rootLayout.post(new TableUpdaterRunnable(this, nameId));
            Table.instance(this).setSelectionActive(true);
        }
        else
        {
            rootLayout.post(new TableUpdaterRunnable(this, null));
            getContentResolver().bulkInsert(
                Uri.parse("content://org.tengel.timescale.search/suggestions"),
                Table.instance(this).getSearchData());
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem itemScale = menu.findItem(R.id.option_scale);
        itemScale.setChecked(Table.instance(this).getTrueScale());
        MenuItem itemFit = menu.findItem(R.id.option_fit);
        itemFit.setChecked(Table.instance(this).getFitScreen());
        m_clearSearch = menu.findItem(R.id.option_clear_search);
        if (!Table.instance(this).getSelectionActive())
        {
            m_clearSearch.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.option_zoom_plus)
        {
            Table.instance(this).addColumn();
        }
        else if (id == R.id.option_zoom_minus)
        {
            Table.instance(this).removeColumn();
        }
        else if (id == R.id.option_scale)
        {
            item.setChecked(Table.instance(this).toggleTrueScale());
        }
        else if (id == R.id.option_fit)
        {
            item.setChecked(Table.instance(this).toggleFitScreen());
        }
        else if (id == R.id.option_help)
        {
            InfoDialog.show(this, "Help/Info",
                            Html.fromHtml(getString(R.string.help_text)));
        }
        else if (id == R.id.option_search)
        {
            onSearchRequested();
        }
        else if (id == R.id.option_clear_search)
        {
            Table.instance(this).setSelectionActive(false);
            m_clearSearch.setVisible(false);
        }
        else if (id == android.R.id.home)
        {
            Table.instance(this).resetView();
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                           "Sorry, not implemented.\n :-(",
                           Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        final int MIN_DISTANCE = 150;
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN)
        {
            m_xLastTouch = event.getX();
        }
        else if (action == MotionEvent.ACTION_UP)
        {
            float deltaX = event.getX() - m_xLastTouch;
            if (Math.abs(deltaX) > MIN_DISTANCE)
            {
                if (deltaX > 0)
                {
                    Table.instance(this).shiftLeft();
                    return true;

                }
                else if (deltaX < 0)
                {
                    Table.instance(this).shiftRight();
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }


    @Override
    public void onBackPressed()
    {
        Table.instance(this).resetView();
    }

}
