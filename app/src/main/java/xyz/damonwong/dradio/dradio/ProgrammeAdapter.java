package xyz.damonwong.dradio.dradio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by damon on 9/13/16.
 */
public class ProgrammeAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private List<Programme> programmes;

    public ProgrammeAdapter(Context context, List<Programme> programme){
        myInflater = LayoutInflater.from(context);
        this.programmes = programme;
    }

    @Override
    public int getCount() {
        return programmes.size();
    }

    @Override
    public Object getItem(int arg0) {
        return programmes.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return programmes.indexOf(getItem(position));
    }

    private class ViewHolder {
        TextView txtTitle;
        TextView txtDate;
        public ViewHolder(TextView txtTitle, TextView txtDate){
            this.txtTitle = txtTitle;
            this.txtDate = txtDate;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.main_list, null);
            holder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.main_item_title),
                    (TextView) convertView.findViewById(R.id.main_item_date)
            );
            Programme programme=(Programme) getItem(position);
            holder.txtTitle.setText(programme.getTitle());
            holder.txtDate.setText(programme.getDate());
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        return convertView;
    }


}
